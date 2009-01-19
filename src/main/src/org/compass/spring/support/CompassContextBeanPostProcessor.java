/*
 * Copyright 2004-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.compass.spring.support;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.compass.core.Compass;
import org.compass.core.CompassContext;
import org.compass.core.CompassSession;
import org.compass.core.spi.InternalCompass;
import org.compass.core.spi.InternalCompassSession;
import org.compass.core.support.session.CompassSessionTransactionalProxy;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessorAdapter;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.ReflectionUtils;

/**
 * BeanPostProcessor that processes {@link org.compass.core.CompassContext}
 * annotation for injection of Compass interfaces. Any such annotated fields
 * or methods in any Spring-managed object will automatically be injected.
 * <p/>
 * Will inject either a {@link Compass} or {@link CompassSession} instances.
 *
 * @author kimchy
 */
public class CompassContextBeanPostProcessor extends InstantiationAwareBeanPostProcessorAdapter
        implements ApplicationContextAware {

    protected final Log logger = LogFactory.getLog(getClass());

    private ApplicationContext applicationContext;

    private Map<Class<?>, List<AnnotatedMember>> classMetadata = new HashMap<Class<?>, List<AnnotatedMember>>();

    private Map<String, Compass> compassesByName;

    private Compass uniqueCompass;

    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * Lazily initialize compass map.
     */
    private synchronized void initMapsIfNecessary() {
        if (this.compassesByName == null) {
            this.compassesByName = new HashMap<String, Compass>();
            // Look for named Compasses
            String[] beanNames = BeanFactoryUtils.beanNamesForTypeIncludingAncestors(applicationContext, Compass.class);
            for (String emfName : beanNames) {
                Compass compass = (Compass) this.applicationContext.getBean(emfName);
                compassesByName.put(((InternalCompass) compass).getName(), compass);
            }

            if (this.compassesByName.isEmpty()) {
                if (beanNames.length == 1) {
                    this.uniqueCompass = (Compass) this.applicationContext.getBean(beanNames[0]);
                }
            } else if (this.compassesByName.size() == 1) {
                this.uniqueCompass = this.compassesByName.values().iterator().next();
            }

            if (this.compassesByName.isEmpty() && this.uniqueCompass == null) {
                logger.warn("No named compass instances defined and not exactly one anonymous one: cannot inject");
            }
        }
    }

    /**
     * Find a Compass with the given name in the current
     * application context
     *
     * @param compassName name of the EntityManagerFactory
     * @return the EntityManagerFactory or throw NoSuchBeanDefinitionException
     * @throws NoSuchBeanDefinitionException if there is no such EntityManagerFactory
     *                                       in the context
     */
    protected Compass findEntityManagerFactoryByName(String compassName)
            throws NoSuchBeanDefinitionException {

        initMapsIfNecessary();
        if (compassName == null || "".equals(compassName)) {
            if (this.uniqueCompass != null) {
                return this.uniqueCompass;
            } else {
                throw new NoSuchBeanDefinitionException(
                        "No Compass name given and factory contains several");
            }
        }
        Compass namedCompass = this.compassesByName.get(compassName);
        if (namedCompass == null) {
            throw new NoSuchBeanDefinitionException("No Compass found for name [" + compassName + "]");
        }
        return namedCompass;
    }

    @Override
    public boolean postProcessAfterInstantiation(Object bean, String beanName) throws BeansException {
        List<AnnotatedMember> metadata = findClassMetadata(bean.getClass());
        for (AnnotatedMember member : metadata) {
            member.inject(bean);
        }
        return true;
    }

    private synchronized List<AnnotatedMember> findClassMetadata(Class<? extends Object> clazz) {
        List<AnnotatedMember> metadata = this.classMetadata.get(clazz);
        if (metadata == null) {
            final List<AnnotatedMember> newMetadata = new LinkedList<AnnotatedMember>();

            ReflectionUtils.doWithFields(clazz, new ReflectionUtils.FieldCallback() {
                public void doWith(Field f) {
                    addIfPresent(newMetadata, f);
                }
            });

            // TODO is it correct to walk up the hierarchy for methods? Otherwise inheritance
            // is implied? CL to resolve
            ReflectionUtils.doWithMethods(clazz, new ReflectionUtils.MethodCallback() {
                public void doWith(Method m) {
                    addIfPresent(newMetadata, m);
                }
            });

            metadata = newMetadata;
            this.classMetadata.put(clazz, metadata);
        }
        return metadata;
    }


    private void addIfPresent(List<AnnotatedMember> metadata, AccessibleObject ao) {
        CompassContext compassContext = ao.getAnnotation(CompassContext.class);
        if (compassContext != null) {
            metadata.add(new AnnotatedMember(compassContext.name(), ao));
        }
    }


    /**
     * Class representing injection information about an annotated field
     * or setter method.
     */
    private class AnnotatedMember {

        private final String name;

        private final AccessibleObject member;

        public AnnotatedMember(String name, AccessibleObject member) {
            this.name = name;
            this.member = member;

            // Validate member type
            Class<?> memberType = getMemberType();
            if (!(Compass.class.isAssignableFrom(memberType) || CompassSession.class.isAssignableFrom(memberType))) {
                throw new IllegalArgumentException("Cannot inject " + member + ": not a supported Compass type");
            }
        }

        public void inject(Object instance) {
            Object value = resolve();
            try {
                if (!this.member.isAccessible()) {
                    this.member.setAccessible(true);
                }
                if (this.member instanceof Field) {
                    ((Field) this.member).set(instance, value);
                } else if (this.member instanceof Method) {
                    ((Method) this.member).invoke(instance, value);
                } else {
                    throw new IllegalArgumentException("Cannot inject unknown AccessibleObject type " + this.member);
                }
            }
            catch (IllegalAccessException ex) {
                throw new IllegalArgumentException("Cannot inject member " + this.member, ex);
            }
            catch (InvocationTargetException ex) {
                // Method threw an exception
                throw new IllegalArgumentException("Attempt to inject setter method " + this.member +
                        " resulted in an exception", ex);
            }
        }

        /**
         * Return the type of the member, whether it's a field or a method.
         */
        public Class<?> getMemberType() {
            if (member instanceof Field) {
                return ((Field) member).getType();
            } else if (member instanceof Method) {
                Method setter = (Method) member;
                if (setter.getParameterTypes().length != 1) {
                    throw new IllegalArgumentException(
                            "Supposed setter " + this.member + " must have 1 argument, not " +
                                    setter.getParameterTypes().length);
                }
                return setter.getParameterTypes()[0];
            } else {
                throw new IllegalArgumentException(
                        "Unknown AccessibleObject type " + this.member.getClass() +
                                "; Can only inject settermethods or fields");
            }
        }

        /**
         * Resolve the object against the application context.
         */
        protected Object resolve() {
            // Resolves to Compass or CompassSession.
            Compass compass = findEntityManagerFactoryByName(this.name);
            if (Compass.class.isAssignableFrom(getMemberType())) {
                if (!getMemberType().isInstance(compass)) {
                    throw new IllegalArgumentException("Cannot inject " + this.member +
                            " with Compass [" + this.name + "]: type mismatch");
                }
                return compass;
            } else {
                // We need to inject aa CompassSession.
                return Proxy.newProxyInstance(
                        CompassContextBeanPostProcessor.class.getClassLoader(),
                        new Class[]{InternalCompassSession.class},
                        new CompassSessionTransactionalProxy(compass));

            }
        }
    }

}
