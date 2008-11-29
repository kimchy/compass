package org.compass.core.load.simple;

import org.compass.core.Compass;
import org.compass.core.config.CompassConfiguration;

/**
 * @author kimchy
 */
public class Processor {

    public static void main(String[] args) throws Exception {
        CompassConfiguration conf = new CompassConfiguration();
        conf.configure("/org/compass/core/load/simple/compass.processor.cfg.xml");
        conf.addClass(A.class);
        Compass compass = conf.buildCompass();
        Thread.sleep(10000000);
    }
}
