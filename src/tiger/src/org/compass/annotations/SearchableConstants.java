package org.compass.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines a collection of {@link SearchableConstant} associated with a
 * {@link Searchable} class.
 *
 * @author kimchy
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface SearchableConstants {

    /**
     * Collection of {@link SearchableConstant} associated with a {@link Searchable} class.
     */
    SearchableConstant[] value();
}
