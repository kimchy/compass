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

package org.compass.gps.device.jpa.lifecycle;

import java.util.Collection;
import javax.persistence.EntityManagerFactory;

import org.apache.openjpa.event.DeleteListener;
import org.apache.openjpa.event.LifecycleEvent;
import org.apache.openjpa.event.PersistListener;
import org.apache.openjpa.event.StoreListener;
import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.persistence.OpenJPAEntityManagerFactory;
import org.apache.openjpa.persistence.OpenJPAPersistence;
import org.compass.core.mapping.CascadeMapping;
import org.compass.gps.device.jpa.AbstractDeviceJpaEntityListener;
import org.compass.gps.device.jpa.JpaGpsDevice;
import org.compass.gps.device.jpa.JpaGpsDeviceException;
import org.compass.gps.spi.CompassGpsInterfaceDevice;

/**
 * Injects lifecycle listeners directly into OpenJPA for mirroring operations.
 *
 * @author kimchy
 */
public class OpenJPAJpaEntityLifecycleInjector implements JpaEntityLifecycleInjector {

    private class OpenJPAEventListener extends AbstractDeviceJpaEntityListener implements DeleteListener, PersistListener, StoreListener {

        private JpaGpsDevice device;

        public OpenJPAEventListener(JpaGpsDevice device) {
            this.device = device;
        }

        @Override
        protected JpaGpsDevice getDevice() {
            return this.device;
        }

        public void beforeDelete(LifecycleEvent lifecycleEvent) {
        }

        public void afterDelete(LifecycleEvent lifecycleEvent) {
            postRemove(lifecycleEvent.getSource());
        }

        public void beforePersist(LifecycleEvent lifecycleEvent) {
        }

        public void afterPersist(LifecycleEvent lifecycleEvent) {
            postPersist(lifecycleEvent.getSource());
        }

        public void beforeStore(LifecycleEvent lifecycleEvent) {
        }

        public void afterStore(LifecycleEvent lifecycleEvent) {
            postUpdate(lifecycleEvent.getSource());
        }
    }

    private boolean useSpecificClassEvents = true;

    private ClassLoader classLoader;

    private Object eventListener;

    public void setUseSpecificClassEvents(boolean useSpecificClassEvents) {
        this.useSpecificClassEvents = useSpecificClassEvents;
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public void setEventListener(Object eventListener) {
        this.eventListener = eventListener;
    }

    public void injectLifecycle(EntityManagerFactory entityManagerFactory, JpaGpsDevice device) throws JpaGpsDeviceException {

        CompassGpsInterfaceDevice gps = (CompassGpsInterfaceDevice) device.getGps();

        OpenJPAEntityManagerFactory emf = OpenJPAPersistence.cast(entityManagerFactory);

        if (eventListener == null) {
            eventListener = new OpenJPAEventListener(device);
        }

        if (useSpecificClassEvents) {
            Collection<Class> classes = emf.getConfiguration().getMetaDataRepositoryInstance().loadPersistentTypes(true, classLoader);
            for (Class clazz : classes) {
                ClassMetaData classMetaData = emf.getConfiguration().getMetaDataRepositoryInstance().getMetaData(clazz, classLoader, true);
                Class mappedClass = classMetaData.getDescribedType();
                if (gps.hasMappingForEntityForMirror(mappedClass, CascadeMapping.Cascade.ALL)) {
                    emf.addLifecycleListener(eventListener, mappedClass);
                }
            }
        } else {
            emf.addLifecycleListener(eventListener, null);
        }
    }

    public void removeLifecycle(EntityManagerFactory entityManagerFactory, JpaGpsDevice device) throws JpaGpsDeviceException {
        OpenJPAEntityManagerFactory emf = OpenJPAPersistence.cast(entityManagerFactory);
        eventListener = new OpenJPAEventListener(device);
        emf.removeLifecycleListener(eventListener);
    }
}
