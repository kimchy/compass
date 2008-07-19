/*
 * Copyright 2004-2006 the original author or authors.
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

package org.compass.gps.device.jpa.support;

import java.util.HashSet;
import java.util.Set;
import javax.persistence.EntityManagerFactory;

import org.compass.core.config.CompassSettings;
import org.compass.core.util.ClassUtils;
import org.compass.gps.device.jpa.JpaGpsDeviceException;
import org.compass.gps.device.jpa.NativeJpaExtractor;

/**
 * @author kimchy
 */
public abstract class NativeJpaHelper {

    public static interface NativeJpaCallback<T> {

        T onHibernate();

        T onTopLinkEssentials();

        T onOpenJPA();

        T onEclipseLink();

        T onUnknown();
    }

    public static <T> T detectNativeJpa(EntityManagerFactory emf, CompassSettings settings, NativeJpaCallback<T> callback) throws JpaGpsDeviceException {
        EntityManagerFactory nativeEmf = extractNativeJpa(emf, settings.getClassLoader());

        Set interfaces = ClassUtils.getAllInterfacesAsSet(nativeEmf);
        Set<String> interfacesAsStrings = new HashSet<String>();
        for (Object anInterface : interfaces) {
            interfacesAsStrings.add(((Class) anInterface).getName());
        }
        interfacesAsStrings.add(nativeEmf.getClass().getName());

        T retVal;
        if (interfacesAsStrings.contains("org.hibernate.ejb.HibernateEntityManagerFactory")) {
            retVal = callback.onHibernate();
        } else
        if (interfacesAsStrings.contains("oracle.toplink.essentials.internal.ejb.cmp3.EntityManagerFactoryImpl")) {
            retVal = callback.onTopLinkEssentials();
        } else if (interfacesAsStrings.contains("org.eclipse.persistence.internal.jpa.EntityManagerFactoryImpl")) {
            retVal = callback.onEclipseLink();
        } else if (interfacesAsStrings.contains("org.apache.openjpa.persistence.OpenJPAEntityManagerFactory")) {
            retVal = callback.onOpenJPA();
        } else {
            retVal = callback.onUnknown();
        }
        return retVal;
    }

    public static EntityManagerFactory extractNativeJpa(EntityManagerFactory emf) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) {
            classLoader = NativeJpaHelper.class.getClassLoader();
        }
        return extractNativeJpa(emf, classLoader);
    }

    public static EntityManagerFactory extractNativeJpa(EntityManagerFactory emf, CompassSettings settings) {
        return extractNativeJpa(emf, settings.getClassLoader());
    }

    public static EntityManagerFactory extractNativeJpa(EntityManagerFactory emf, ClassLoader classLoader) {
        Set interfaces = ClassUtils.getAllInterfacesAsSet(emf);
        Set<String> interfacesAsStrings = new HashSet<String>();
        for (Object anInterface : interfaces) {
            interfacesAsStrings.add(((Class) anInterface).getName());
        }
        interfacesAsStrings.add(emf.getClass().getName());

        NativeJpaExtractor extractor = null;
        if (interfacesAsStrings.contains("org.springframework.orm.jpa.EntityManagerFactoryInfo")) {
            try {
                extractor = (NativeJpaExtractor)
                        ClassUtils.forName("org.compass.spring.device.jpa.SpringNativeJpaExtractor", classLoader).newInstance();
            } catch (Exception e) {
                throw new JpaGpsDeviceException("Failed to load/create spring native extractor", e);
            }
        } else if (interfacesAsStrings.contains("org.jboss.ejb3.entity.InjectedEntityManagerFactory")) {
            try {
                extractor = (NativeJpaExtractor)
                        ClassUtils.forName("org.compass.jboss.device.jpa.JBossNativeHibernateJpaExtractor", classLoader).newInstance();
            } catch (Exception e) {
                throw new JpaGpsDeviceException("Failed to load/create JBoss native extractor", e);
            }
        }
        // possible else if ...

        EntityManagerFactory nativeEmf = emf;
        if (extractor != null) {
            nativeEmf = extractor.extractNative(emf);
            // recursivly call in order to find
            nativeEmf = extractNativeJpa(nativeEmf, classLoader);
        }
        return nativeEmf;
    }
}
