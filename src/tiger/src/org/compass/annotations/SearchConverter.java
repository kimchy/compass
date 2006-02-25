package org.compass.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.compass.core.converter.Converter;

/**
 * Configure {@link Converter} to be used within Compass.
 * Set on package definition (<code>package-info.java</code>).
 * <p/>
 * The {@link Converter} is registed under a lookup name ({@link #name()}), which can then
 * be reference in the different mapping definitions.
 *
 * @author kimchy
 */
@Target(ElementType.PACKAGE)
@Retention(RetentionPolicy.RUNTIME)
public @interface SearchConverter {

    /**
     * The name the {@link Converter} will be registered under.
     */
    String name();

    /**
     * The {@link Converter} implementation.
     */
    Class<? extends Converter> type();

    /**
     * Settings for the {@link Converter} implemenation. If set,
     * the {@link Converter} should implement the {@link org.compass.core.config.CompassConfigurable}
     * interface.
     */
    SearchSetting[] settings() default {};
}
