package org.compass.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A general search setting applied to different search annotations.
 *
 * @author kimchy
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface SearchSetting {

    /**
     * The name of the setting.
     */
    String name();

    /**
     * The value of the setting.
     */
    String value();
}
