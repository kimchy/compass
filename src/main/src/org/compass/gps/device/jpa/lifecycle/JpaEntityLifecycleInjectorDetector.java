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

package org.compass.gps.device.jpa.lifecycle;

import javax.persistence.EntityManagerFactory;

import org.compass.core.config.CompassSettings;
import org.compass.core.util.ClassUtils;
import org.compass.gps.device.jpa.JpaGpsDeviceException;
import org.compass.gps.device.jpa.extractor.NativeJpaHelper;

/**
 * A {@link JpaEntityLifecycleInjector} detector. Tries to check for the actual implementation of JPA
 * <code>EntityManagerFactory</code>, and based on it, check if the current set of actual JPA
 * implementations is one of compass supported ones (like Hibernate).
 * <p/>
 * Currently support the following JPA implementations: Hibernate, TopLink Essentials (Glassfish Persistence), OpenJPA.
 * <p/>
 * Assumes that the <code>EntityManagerFactory</code> is the native one, since the
 * {@link org.compass.gps.device.jpa.extractor.NativeJpaExtractor} of the
 * {@link org.compass.gps.device.jpa.JpaGpsDevice} was used to extract it.
 *
 * @author kimchy
 */
public abstract class JpaEntityLifecycleInjectorDetector {

    public static JpaEntityLifecycleInjector detectInjector(EntityManagerFactory entityManagerFactory, final CompassSettings settings)
            throws JpaGpsDeviceException {
        String injectorClassName =
                NativeJpaHelper.detectNativeJpa(entityManagerFactory, new NativeJpaHelper.NativeJpaCallback<String>() {

                    public String onHibernate() {
                        try {
                            ClassUtils.forName("org.hibernate.event.AbstractCollectionEvent", settings.getClassLoader());
                            return "org.compass.gps.device.jpa.lifecycle.HibernateJpaEntityCollectionLifecycleInjector";
                        } catch (ClassNotFoundException e) {
                            return "org.compass.gps.device.jpa.lifecycle.HibernateJpaEntityLifecycleInjector";
                        }
                    }

                    public String onTopLinkEssentials() {
                        return "org.compass.gps.device.jpa.lifecycle.TopLinkEssentialsJpaEntityLifecycleInjector";
                    }

                    public String onEclipseLink() {
                        return "org.compass.gps.device.jpa.lifecycle.EclipseLinkJpaEntityLifecycleInjector";
                    }

                    public String onOpenJPA() {
                        return "org.compass.gps.device.jpa.lifecycle.OpenJPAJpaEntityLifecycleInjector";
                    }

                    public String onUnknown() {
                        return null;
                    }
                });

        if (injectorClassName == null) {
            return null;
        }

        try {
            Class injectorClass = ClassUtils.forName(injectorClassName, settings.getClassLoader());
            return (JpaEntityLifecycleInjector) injectorClass.newInstance();
        } catch (Exception e) {
            throw new JpaGpsDeviceException("Failed to create injector class [" + injectorClassName + "]", e);
        }
    }
}
