package org.compass.core.config;

import org.compass.core.mapping.CompassMapping;

/**
 * An interface allowing for custom components that wish to be aware of CompassMapping
 * to be injected with it.
 *
 * @author kimchy
 */
public interface CompassMappingAware {

    /**
     * Injects the component with the compass mappings.
     */
    void setCompassMapping(CompassMapping mapping);
}
