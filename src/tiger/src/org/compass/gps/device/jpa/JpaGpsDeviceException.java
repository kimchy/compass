package org.compass.gps.device.jpa;

import org.compass.gps.CompassGpsException;

/**
 * A Jpa specific Gps device exception.
 *
 * @author kimchy
 */
public class JpaGpsDeviceException extends CompassGpsException {

    private static final long serialVersionUID = 4051326747222029623L;

    public JpaGpsDeviceException(String string, Throwable root) {
        super(string, root);
    }

    public JpaGpsDeviceException(String s) {
        super(s);
    }
}
