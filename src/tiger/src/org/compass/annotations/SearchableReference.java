package org.compass.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author kimchy
 */
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface SearchableReference {

    /**
     * The reference alias that points to the searchable class. Not required
     * since most of the times it can be automatically detected.
     */
    String refAlias() default "";

    String refComponentAlias() default "";

    String converter() default "";
}
