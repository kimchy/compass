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

package org.compass.gps.device.jpa.entities;

import javax.persistence.EntityManagerFactory;

import org.compass.core.config.CompassSettings;
import org.compass.core.util.ClassUtils;
import org.compass.gps.device.jpa.JpaGpsDeviceException;
import org.compass.gps.device.jpa.extractor.NativeJpaHelper;

/**
 * A {@link JpaEntitiesLocator} detector. Tries to check for the actual implementation of JPA
 * <code>EntityManagerFactory</code>, and based on it, check if the current set of actual JPA
 * implementations is one of compass supported ones (like Hibernate).
 * <p/>
 * If no implementation is found for the native <code>EntityManagerFactory</code> implementation,
 * uses the {@link DefaultJpaEntitiesLocator}.
 * <p/>
 * Currently support the following JPA implementations: Hibernate, TopLink Essentials (Glassfish Persistence), OpenJPA.
 * <p/>
 * Assumes that the <code>EntityManagerFactory</code> is the native one, since the
 * {@link org.compass.gps.device.jpa.extractor.NativeJpaExtractor} of the
 * {@link org.compass.gps.device.jpa.JpaGpsDevice} was used to extract it.
 *
 * @author kimchy
 */
public abstract class JpaEntitiesLocatorDetector {

    public static JpaEntitiesLocator detectLocator(EntityManagerFactory entityManagerFactory, CompassSettings settings) {

        String locatorClassName =
                NativeJpaHelper.detectNativeJpa(entityManagerFactory, new NativeJpaHelper.NativeJpaCallback<String>() {

                    public String onHibernate() {
                        return "org.compass.gps.device.jpa.entities.HibernateJpaEntitiesLocator";
                    }

                    public String onTopLinkEssentials() {
                        return "org.compass.gps.device.jpa.entities.TopLinkEssentialsJpaEntitiesLocator";
                    }

                    public String onEclipseLink() {
                        return "org.compass.gps.device.jpa.entities.EclipseLinkJpaEntitiesLocator";
                    }

                    public String onOpenJPA() {
                        return "org.compass.gps.device.jpa.entities.OpenJPAJpaEntitiesLocator";
                    }

                    public String onUnknown() {
                        return DefaultJpaEntitiesLocator.class.getName();
                    }
                });

        try {
            Class locatorClass = ClassUtils.forName(locatorClassName, settings.getClassLoader());
            return (JpaEntitiesLocator) locatorClass.newInstance();
        } catch (Exception e) {
            throw new JpaGpsDeviceException("Failed to create locator class [" + locatorClassName + "]", e);
        }

    }
}
