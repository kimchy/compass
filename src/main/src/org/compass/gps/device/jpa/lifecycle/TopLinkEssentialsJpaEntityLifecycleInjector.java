package org.compass.gps.device.jpa.lifecycle;

import java.util.Map;
import java.util.Vector;
import javax.persistence.EntityManagerFactory;

import oracle.toplink.essentials.descriptors.ClassDescriptor;
import oracle.toplink.essentials.descriptors.DescriptorEvent;
import oracle.toplink.essentials.descriptors.DescriptorEventListener;
import oracle.toplink.essentials.ejb.cmp3.EntityManager;
import oracle.toplink.essentials.internal.ejb.cmp3.base.EntityManagerFactoryImpl;
import oracle.toplink.essentials.sessions.Session;
import org.compass.core.mapping.CascadeMapping;
import org.compass.gps.device.jpa.AbstractDeviceJpaEntityListener;
import org.compass.gps.device.jpa.JpaGpsDevice;
import org.compass.gps.device.jpa.JpaGpsDeviceException;
import org.compass.gps.spi.CompassGpsInterfaceDevice;

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

    private DescriptorEventListener eventListener;

    public void setEventListener(DescriptorEventListener eventListener) {
        this.eventListener = eventListener;
    }

    public void injectLifecycle(EntityManagerFactory entityManagerFactory, JpaGpsDevice device) throws JpaGpsDeviceException {

        CompassGpsInterfaceDevice gps = (CompassGpsInterfaceDevice) device.getGps();

        if (eventListener == null) {
            eventListener = new TopLinkEssentialsEventListener(device);
        }

        EntityManager entityManager = (EntityManager) entityManagerFactory.createEntityManager();
        Session session = entityManager.getServerSession();
        entityManager.close();

        Map descriptors = session.getDescriptors();
        for (Object o : descriptors.values()) {
            ClassDescriptor classDescriptor = (ClassDescriptor) o;
            Class mappedClass = classDescriptor.getJavaClass();
            if (gps.hasMappingForEntityForMirror(mappedClass, CascadeMapping.Cascade.ALL)) {
                classDescriptor.getDescriptorEventManager().addListener(eventListener);
            }
        }
    }

    public void removeLifecycle(EntityManagerFactory entityManagerFactory, JpaGpsDevice device) throws JpaGpsDeviceException {
        CompassGpsInterfaceDevice gps = (CompassGpsInterfaceDevice) device.getGps();

        EntityManagerFactoryImpl emf = (EntityManagerFactoryImpl) entityManagerFactory;
        Session session = emf.getServerSession();
        Map descriptors = session.getDescriptors();
        for (Object o : descriptors.values()) {
            ClassDescriptor classDescriptor = (ClassDescriptor) o;
            Class mappedClass = classDescriptor.getJavaClass();
            if (gps.hasMappingForEntityForMirror(mappedClass, CascadeMapping.Cascade.ALL)) {
                classDescriptor.getDescriptorEventManager().removeListener(eventListener);
            }
        }
    }
}
