package org.compass.gps.device.jpa.hibernate;

import org.compass.gps.device.jpa.JpaGpsDevice;
import org.compass.gps.device.jpa.entities.DefaultJpaEntitiesLocator;

/**
 * @author kimchy
 */
public class HibernateSimpleDefaultLocatorJpaGpsDeviceTests extends HibernateSimpleJpaGpsDeviceTests {

    @Override
    protected void addDeviceSettings(JpaGpsDevice device) {
        super.addDeviceSettings(device);
        device.setEntitiesLocator(new DefaultJpaEntitiesLocator());
    }

}
