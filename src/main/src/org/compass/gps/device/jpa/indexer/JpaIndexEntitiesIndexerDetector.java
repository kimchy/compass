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

package org.compass.gps.device.jpa.indexer;

import javax.persistence.EntityManagerFactory;

import org.compass.core.config.CompassSettings;
import org.compass.core.util.ClassUtils;
import org.compass.gps.device.jpa.JpaGpsDeviceException;
import org.compass.gps.device.jpa.extractor.NativeJpaHelper;

/**
 * @author kimchy
 */
public abstract class JpaIndexEntitiesIndexerDetector {

    public static JpaIndexEntitiesIndexer detectEntitiesIndexer(EntityManagerFactory entityManagerFactory, CompassSettings settings) {

        String locatorClassName =
                NativeJpaHelper.detectNativeJpa(entityManagerFactory, new NativeJpaHelper.NativeJpaCallback<String>() {

                    public String onHibernate() {
                        return "org.compass.gps.device.jpa.indexer.HibernateJpaIndexEntitiesIndexer";
                    }

                    public String onTopLinkEssentials() {
                        return DefaultJpaIndexEntitiesIndexer.class.getName();
                    }

                    public String onEclipseLink() {
                        return DefaultJpaIndexEntitiesIndexer.class.getName();
                    }

                    public String onOpenJPA() {
                        return "org.compass.gps.device.jpa.indexer.OpenJPAJpaIndexEntitiesIndexer";
                    }

                    public String onUnknown() {
                        return DefaultJpaIndexEntitiesIndexer.class.getName();
                    }
                });

        try {
            Class locatorClass = ClassUtils.forName(locatorClassName, settings.getClassLoader());
            return (JpaIndexEntitiesIndexer) locatorClass.newInstance();
        } catch (Exception e) {
            throw new JpaGpsDeviceException("Failed to create entities indexer class [" + locatorClassName + "]", e);
        }

    }
}