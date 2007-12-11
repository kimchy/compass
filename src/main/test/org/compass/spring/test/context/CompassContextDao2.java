package org.compass.spring.test.context;

import org.compass.core.Compass;
import org.compass.core.CompassSession;

/**
 * @author kimchy
 */
public class CompassContextDao2 {

    Compass compass;

    CompassSession session;

    public void setCompassSession(CompassSession session) {
        this.session = session;
    }

    public void setCompass(Compass compass) {
        this.compass = compass;
    }
}
