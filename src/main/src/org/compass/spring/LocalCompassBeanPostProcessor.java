package org.compass.spring;

import org.compass.core.config.CompassConfiguration;
import org.compass.core.config.ConfigurationException;

/**
 * Optional {@link org.compass.spring.LocalCompassBean} post processor
 * allowing to add additional configuration settings / mappings.
 *
 * @author kimchy
 */
public interface LocalCompassBeanPostProcessor {

    /**
     * Process the {@link org.compass.core.config.CompassConfiguration} allowing
     * to add additional configuration settings / mappings before the
     * {@link org.compass.spring.LocalCompassBean} creates the {@link org.compass.core.Compass}
     * instance.
     *
     * @param config The Compass configuration that will be used to creat the Compass instance
     * @throws ConfigurationException
     */
    void process(CompassConfiguration config) throws ConfigurationException;
}
