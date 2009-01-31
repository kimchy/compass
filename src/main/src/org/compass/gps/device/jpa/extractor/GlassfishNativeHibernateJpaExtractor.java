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

package org.compass.gps.device.jpa.extractor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.compass.core.CompassException;
import org.compass.gps.device.jpa.JpaGpsDeviceException;

/**
 * A JPA native extractor that extracts the actual implementation of JPA EntityManagerFactory/EntityManager
 * when running within Glassfish.
 *
 * @author kimchy
 */
public class GlassfishNativeHibernateJpaExtractor implements NativeJpaExtractor {

    private static final String EMF_WRAPPER_CLASS = "com.sun.enterprise.util.EntityManagerFactoryWrapper";

    private static final String EM_WRAPPER_CLASS = "com.sun.enterprise.util.EntityManagerWrapper";

    private Class emfWrapperClass;

    private Class emWrapperClass;

    private Method emfGetDelegateMethod;

    private Method emGetDelegateMethod;

    public GlassfishNativeHibernateJpaExtractor() {
        try {
            emfWrapperClass = GlassfishNativeHibernateJpaExtractor.class.getClassLoader().loadClass(EMF_WRAPPER_CLASS);
            emfGetDelegateMethod = emfWrapperClass.getDeclaredMethod("getDelegate");
            emfGetDelegateMethod.setAccessible(true);
        } catch (ClassNotFoundException e) {
            throw new CompassException("Failed to find Glassfish EMF wrapper class [" + EMF_WRAPPER_CLASS + "]");
        } catch (NoSuchMethodException e) {
            throw new CompassException("Failed to find Glassfish getDelegate method within wrapper class [" + EMF_WRAPPER_CLASS + "]");
        }
        try {
            emWrapperClass = GlassfishNativeHibernateJpaExtractor.class.getClassLoader().loadClass(EM_WRAPPER_CLASS);
            emGetDelegateMethod = emWrapperClass.getDeclaredMethod("getDelegate");
            emGetDelegateMethod.setAccessible(true);
        } catch (ClassNotFoundException e) {
            throw new CompassException("Failed to find Glassfish EM wrapper class [" + EM_WRAPPER_CLASS + "]");
        } catch (NoSuchMethodException e) {
            throw new CompassException("Failed to find Glassfish getDelegate method within wrapper class [" + EM_WRAPPER_CLASS + "]");
        }
    }

    public EntityManagerFactory extractNative(EntityManagerFactory entityManagerFactory) throws JpaGpsDeviceException {
        if (emfWrapperClass == null) {
            return entityManagerFactory;
        }
        if (emfWrapperClass.isAssignableFrom(entityManagerFactory.getClass())) {
            try {
                return (EntityManagerFactory) emfGetDelegateMethod.invoke(entityManagerFactory);
            } catch (InvocationTargetException e) {
                throw new JpaGpsDeviceException("Failed to invoke getDelegate method on [" + entityManagerFactory + "]", e.getTargetException());
            } catch (Exception e) {
                throw new JpaGpsDeviceException("Failed to invoke getDelegate method on [" + entityManagerFactory + "]", e);
            }
        }
        return entityManagerFactory;
    }

    public EntityManager extractNative(EntityManager entityManager) throws JpaGpsDeviceException {
        if (emWrapperClass == null) {
            return entityManager;
        }
        if (emWrapperClass.isAssignableFrom(entityManager.getClass())) {
            try {
                return (EntityManager) emGetDelegateMethod.invoke(entityManager);
            } catch (InvocationTargetException e) {
                throw new JpaGpsDeviceException("Failed to invoke getDelegate method on [" + entityManager + "]", e.getTargetException());
            } catch (Exception e) {
                throw new JpaGpsDeviceException("Failed to invoke getDelegate method on [" + entityManager + "]", e);
            }
        }
        return entityManager;
    }
}
