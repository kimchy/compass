package org.compass.gps.device.jpa.lifecycle;

import java.util.Map;
import java.util.Vector;
import javax.persistence.EntityManagerFactory;

import oracle.toplink.essentials.descriptors.ClassDescriptor;
import oracle.toplink.essentials.descriptors.DescriptorEvent;
import oracle.toplink.essentials.descriptors.DescriptorEventListener;
import oracle.toplink.essentials.internal.ejb.cmp3.base.EntityManagerFactoryImpl;
import oracle.toplink.essentials.sessions.Session;
import org.compass.gps.spi.CompassGpsInterfaceDevice;
import org.compass.gps.device.jpa.AbstractDeviceJpaEntityListener;
import org.compass.gps.device.jpa.JpaGpsDevice;
import org.compass.gps.device.jpa.JpaGpsDeviceException;

/**
 * Injects lifecycle listeners directly into TopLink Essentials for mirroring operations.
 *
 * @author kimchy
 */
public class TopLinkEssentialsJpaEntityLifecycleInjector implements JpaEntityLifecycleInjector {

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

    public void injectLifecycle(EntityManagerFactory entityManagerFactory, JpaGpsDevice device) throws JpaGpsDeviceException {

        CompassGpsInterfaceDevice gps = (CompassGpsInterfaceDevice) device.getGps();

        EntityManagerFactoryImpl emf = (EntityManagerFactoryImpl) entityManagerFactory;
        Session session = emf.getServerSession();
        Map descriptors = session.getDescriptors();
        TopLinkEssentialsEventListener eventListener = new TopLinkEssentialsEventListener(device);
        for (Object o : descriptors.values()) {
            ClassDescriptor classDescriptor = (ClassDescriptor) o;
            Class mappedClass = classDescriptor.getJavaClass();
            if (gps.hasMappingForEntityForMirror(mappedClass)) {
                classDescriptor.getDescriptorEventManager().addListener(eventListener);
            }
        }
    }
}
