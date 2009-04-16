/*
 * Copyright 2004-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.compass.gps.device.jpa.lifecycle;

import java.lang.reflect.Field;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.listener.DeleteLifecycleListener;
import javax.jdo.listener.InstanceLifecycleEvent;
import javax.jdo.listener.InstanceLifecycleListener;
import javax.jdo.listener.StoreLifecycleListener;
import javax.persistence.EntityManagerFactory;

import org.compass.gps.device.jpa.AbstractDeviceJpaEntityListener;
import org.compass.gps.device.jpa.JpaGpsDevice;
import org.compass.gps.device.jpa.JpaGpsDeviceException;
import org.datanucleus.jdo.JDOPersistenceManagerFactory;
import org.datanucleus.jpa.EntityManagerFactoryImpl;

/**
 * @author kimchy
 */
public class DatanucleusEntityLifecycleInjector implements JpaEntityLifecycleInjector {

    // we don't implement CreateLifecycleListener since no id is set in this case , and store is called anyhow
    private class DatanucleusEventListener extends AbstractDeviceJpaEntityListener implements DeleteLifecycleListener, StoreLifecycleListener/*, CreateLifecycleListener*/ {

        private final JpaGpsDevice device;

        public DatanucleusEventListener(JpaGpsDevice device) {
            this.device = device;
        }

        @Override
        protected JpaGpsDevice getDevice() {
            return this.device;
        }

        public void postStore(InstanceLifecycleEvent instanceLifecycleEvent) {
            postUpdate(instanceLifecycleEvent.getSource());
        }

        public void postDelete(InstanceLifecycleEvent instanceLifecycleEvent) {
            postRemove(instanceLifecycleEvent.getSource());
        }

        public void postCreate(InstanceLifecycleEvent instanceLifecycleEvent) {
            postPersist(instanceLifecycleEvent.getSource());
        }

        public void preDelete(InstanceLifecycleEvent instanceLifecycleEvent) {
        }

        public void preStore(InstanceLifecycleEvent instanceLifecycleEvent) {

        }
    }

    private InstanceLifecycleListener eventListener;

    public void setEventListener(InstanceLifecycleListener eventListener) {
        this.eventListener = eventListener;
    }

    public void injectLifecycle(EntityManagerFactory entityManagerFactory, JpaGpsDevice device) throws JpaGpsDeviceException {
        PersistenceManagerFactory pmf = extractPMF(entityManagerFactory);
        if (eventListener == null) {
            eventListener = new DatanucleusEventListener(device);
        }
        // TODO This does not seem to work, adding lifecycle listener does not work with JPA, strange...
        pmf.addInstanceLifecycleListener(eventListener, null);
    }

    public void removeLifecycle(EntityManagerFactory entityManagerFactory, JpaGpsDevice device) throws JpaGpsDeviceException {
        PersistenceManagerFactory pmf = extractPMF(entityManagerFactory);
        pmf.removeInstanceLifecycleListener(eventListener);
    }

    public boolean requireRefresh() {
        return true;
    }

    private static JDOPersistenceManagerFactory extractPMF(EntityManagerFactory emf) {
        Class pmfHolderClass = emf.getClass();
        while (!pmfHolderClass.getName().equals(EntityManagerFactoryImpl.class.getName())) {
            pmfHolderClass = pmfHolderClass.getSuperclass();
            if (pmfHolderClass == Object.class) {
                throw new IllegalStateException("Failed to find PMF from [" + emf.getClass() + "], no [" + EntityManagerFactoryImpl.class.getName() + "] found");
            }
        }
        try {
            Field field = pmfHolderClass.getDeclaredField("pmf");
            field.setAccessible(true);
            return (JDOPersistenceManagerFactory) field.get(emf);
        } catch (NoSuchFieldException e) {
            throw new IllegalStateException("Failed to extract PMF from [" + emf.getClass() + "], no field [pmf]");
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Failed to extract PMF from [" + emf.getClass() + "], illegal access");
        }
    }
}
