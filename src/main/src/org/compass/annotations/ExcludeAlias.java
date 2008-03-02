package org.compass.annotations;

/**
 * Controls if the all property will exclude the alias from it
 *
 * @author kimchy
 */
public enum ExcludeAlias {

    /**
     * Will use Compass globabl setting for all property.
     *
     * @see org.compass.core.config.CompassEnvironment.All#EXCLUDE_ALIAS
     */
    NA,

    /**
     * Alias will be excluded for this mapping. Regardless of the globabl
     * setting.
     */
    TRUE,

    /**
     * All will be included for this mapping. Regardless of the globabl
     * setting.
     */
    FALSE
}