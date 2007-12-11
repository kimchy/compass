package org.compass.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.compass.core.engine.subindex.SubIndexHash;

/**
 * Configures a {@link SubIndexHash} associated with the given {@link Searchable}
 *
 * @author kimchy
 * @see SubIndexHash
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface SearchableSubIndexHash {

    /**
     * The type of the given {@link SubIndexHash} implementation
     */
    Class<? extends SubIndexHash> value();

    /**
     * Settings associated with the actual {@link SubIndexHash} implementation.
     */
    SearchSetting[] settings() default {};
}
