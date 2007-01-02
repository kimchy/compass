package org.compass.gps.device.jpa.lifecycle;

import javax.persistence.EntityManagerFactory;

import org.apache.openjpa.event.DeleteListener;
import org.apache.openjpa.event.LifecycleEvent;
import org.apache.openjpa.event.PersistListener;
import org.apache.openjpa.event.StoreListener;
import org.apache.openjpa.persistence.OpenJPAEntityManagerFactory;
import org.apache.openjpa.persistence.OpenJPAPersistence;
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

    public void injectLifecycle(EntityManagerFactory entityManagerFactory, JpaGpsDevice device) throws JpaGpsDeviceException {

        CompassGpsInterfaceDevice gps = (CompassGpsInterfaceDevice) device.getGps();

        OpenJPAEntityManagerFactory emf = OpenJPAPersistence.cast(entityManagerFactory);

        OpenJPAEventListener eventListener = new OpenJPAEventListener(device);
        emf.addLifecycleListener(eventListener, null);
        // TODO once we manage to understand how to get the class meta data properly
//        ClassMetaData[] classMetaDatas = emf.getConfiguration().getMetaDataRepositoryInstance().getMetaDatas();
//        for (ClassMetaData classMetaData : classMetaDatas) {
//            Class mappedClass = classMetaData.getDescribedType();
//            if (gps.hasMappingForEntityForMirror(mappedClass, CascadeMapping.Cascade.ALL)) {
//                emf.addLifecycleListener(eventListener, mappedClass);
//            }
//        }
    }
}
