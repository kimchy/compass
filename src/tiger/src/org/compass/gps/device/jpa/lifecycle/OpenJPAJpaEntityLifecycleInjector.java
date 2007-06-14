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

    private OpenJPAEventListener eventListener;

    public void setUseSpecificClassEvents(boolean useSpecificClassEvents) {
        this.useSpecificClassEvents = useSpecificClassEvents;
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public void injectLifecycle(EntityManagerFactory entityManagerFactory, JpaGpsDevice device) throws JpaGpsDeviceException {

        CompassGpsInterfaceDevice gps = (CompassGpsInterfaceDevice) device.getGps();

        OpenJPAEntityManagerFactory emf = OpenJPAPersistence.cast(entityManagerFactory);

        eventListener = new OpenJPAEventListener(device);

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
