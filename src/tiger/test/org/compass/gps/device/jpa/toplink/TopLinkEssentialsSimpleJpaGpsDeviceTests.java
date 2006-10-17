package org.compass.gps.device.jpa.toplink;

import java.util.HashMap;
import javax.persistence.EntityManagerFactory;

import oracle.toplink.essentials.PersistenceProvider;
import org.compass.gps.device.jpa.AbstractSimpleJpaGpsDeviceTests;
import org.compass.gps.device.jpa.JpaGpsDevice;

/**
 * @author kimchy
 */
public class TopLinkEssentialsSimpleJpaGpsDeviceTests extends AbstractSimpleJpaGpsDeviceTests {

    @Override
    protected void addDeviceSettings(JpaGpsDevice device) {
        device.setInjectEntityLifecycleListener(true);
    }

    protected EntityManagerFactory doSetUpEntityManagerFactory() {
        return new PersistenceProvider().createEntityManagerFactory("toplink", new HashMap());
    }
}
