package org.compass.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines a collection of {@link SearchConverter}s.
 * Set on package definition (<code>package-info.java</code>).
 *
 * @author kimchy
 */
@Target({ElementType.PACKAGE})
@Retention(RetentionPolicy.RUNTIME)
public @interface SearchConverters {

    /**
     * A collection of search converters.
     */
    SearchConverter[] value();
}
