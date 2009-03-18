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

import java.util.Map;
import java.util.Vector;
import javax.persistence.EntityManagerFactory;

import org.compass.core.mapping.Cascade;
import org.compass.gps.device.jpa.AbstractDeviceJpaEntityListener;
import org.compass.gps.device.jpa.JpaGpsDevice;
import org.compass.gps.device.jpa.JpaGpsDeviceException;
import org.compass.gps.spi.CompassGpsInterfaceDevice;
import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.descriptors.DescriptorEvent;
import org.eclipse.persistence.descriptors.DescriptorEventAdapter;
import org.eclipse.persistence.descriptors.DescriptorEventListener;
import org.eclipse.persistence.jpa.JpaEntityManager;
import org.eclipse.persistence.sessions.Session;

/**
 * Injects lifecycle listeners directly into Eclipse Persistence for mirroring operations.
 *
 * @author kimchy
 */
public class EclipseLinkJpaEntityLifecycleInjector implements JpaEntityLifecycleInjector {

    private class TopLinkEssentialsEventListener extends AbstractDeviceJpaEntityListener implements DescriptorEventListener {

        private JpaGpsDevice device;

        public TopLinkEssentialsEventListener(JpaGpsDevice device) {
            this.device = device;
        }

        @Override
        protected JpaGpsDevice getDevice() {
            return this.device;
        }

        public void postUpdate(DescriptorEvent event) {
            postUpdate(event.getObject());
        }

        public void postDelete(DescriptorEvent event) {
            postRemove(event.getObject());
        }

        public void postInsert(DescriptorEvent event) {
            postPersist(event.getObject());
        }

        // things we don't use

        public void aboutToDelete(DescriptorEvent event) {
        }

        public void aboutToInsert(DescriptorEvent event) {
        }

        public void aboutToUpdate(DescriptorEvent event) {
        }

        public boolean isOverriddenEvent(DescriptorEvent event, Vector eventManagers) {
            return false;
        }

        public void postBuild(DescriptorEvent event) {
        }

        public void postClone(DescriptorEvent event) {
        }

        public void postMerge(DescriptorEvent event) {
        }

        public void postRefresh(DescriptorEvent event) {
        }

        public void postWrite(DescriptorEvent event) {
        }

        public void preDelete(DescriptorEvent event) {
        }

        public void preInsert(DescriptorEvent event) {
        }

        public void prePersist(DescriptorEvent event) {
        }

        public void preRemove(DescriptorEvent event) {
        }

        public void preUpdate(DescriptorEvent event) {
        }

        public void preUpdateWithChanges(DescriptorEvent event) {
        }

        public void preWrite(DescriptorEvent event) {
        }

    }

    private DescriptorEventListener eventListener;

    private DescriptorEventListener dummyEventListener = new DescriptorEventAdapter();

    public void setEventListener(DescriptorEventListener eventListener) {
        this.eventListener = eventListener;
    }

    public boolean requireRefresh() {
        return true;
    }

    public void injectLifecycle(EntityManagerFactory entityManagerFactory, JpaGpsDevice device) throws JpaGpsDeviceException {

        CompassGpsInterfaceDevice gps = (CompassGpsInterfaceDevice) device.getGps();

        if (eventListener == null) {
            eventListener = new TopLinkEssentialsEventListener(device);
        }

        JpaEntityManager entityManager = (JpaEntityManager) entityManagerFactory.createEntityManager();
        Session session = entityManager.getServerSession();
        entityManager.close();

        Map descriptors = session.getDescriptors();
        for (Object o : descriptors.values()) {
            ClassDescriptor classDescriptor = (ClassDescriptor) o;
            Class mappedClass = classDescriptor.getJavaClass();

            // if we have a parent class that is mapped in JPA and in Compass, then don't add an event listner
            // since we will add it to the parent descriptor and it will notify this class as well
            if (classDescriptor.isChildDescriptor()) {
                Class parentClass = classDescriptor.getInheritancePolicy().getParentDescriptor().getJavaClass();
                if (gps.hasMappingForEntityForMirror(parentClass, Cascade.ALL)) {
                    // we need to inject a dummy listener, just so it will kick in the listener notifications
                    classDescriptor.getDescriptorEventManager().addListener(dummyEventListener);
                    continue;
                }
            }

            if (gps.hasMappingForEntityForMirror(mappedClass, Cascade.ALL)) {
                classDescriptor.getDescriptorEventManager().addListener(eventListener);
            }
        }
    }

    public void removeLifecycle(EntityManagerFactory entityManagerFactory, JpaGpsDevice device) throws JpaGpsDeviceException {
        CompassGpsInterfaceDevice gps = (CompassGpsInterfaceDevice) device.getGps();

        JpaEntityManager entityManager = (JpaEntityManager) entityManagerFactory.createEntityManager();
        Session session = entityManager.getServerSession();
        entityManager.close();

        Map descriptors = session.getDescriptors();
        for (Object o : descriptors.values()) {
            ClassDescriptor classDescriptor = (ClassDescriptor) o;
            Class mappedClass = classDescriptor.getJavaClass();

            // if we have a parent class that is mapped in JPA and in Compass, then don't add an event listner
            // since we will add it to the parent descriptor and it will notify this class as well
            if (classDescriptor.isChildDescriptor()) {
                Class parentClass = classDescriptor.getInheritancePolicy().getParentDescriptor().getJavaClass();
                if (gps.hasMappingForEntityForMirror(parentClass, Cascade.ALL)) {
                    classDescriptor.getDescriptorEventManager().removeListener(dummyEventListener);
                    continue;
                }
            }
            
            if (gps.hasMappingForEntityForMirror(mappedClass, Cascade.ALL)) {
                classDescriptor.getDescriptorEventManager().removeListener(eventListener);
            }
        }
    }
}