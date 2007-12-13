package org.compass.annotations;

/**
 * Controls if the all property will be enabled or not.
 *
 * @author kimchy
 */
public enum EnableAll {

    /**
     * Will use Compass globabl setting for all property.
     *
     * @see org.compass.core.config.CompassEnvironment.All#ENABLED
     */
    NA,

    /**
     * All will be enabled for this mapping. Regardless of the globabl
     * setting.
     */
    TRUE,

    /**
     * All will be disabled for this mapping. Regardless of the globabl
     * setting.
     */
    FALSE
}
