package org.compass.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies a {@link Searchable} class field/property that dynamically
 * controls the anlayzer that will be used to analyze the class content.
 * <p/>
 * The value of the field/property will be used as analyzer lookup name
 * of the different analyzers defined within Compass.
 *
 * @author kimchy
 */
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface SearchableAnalyzerProperty {

    /**
     * The lookup name for the analyzer in case the value of
     * the field/proeprty is <code>null</code>.
     */
    String nullAnalyzer() default "";

    /**
     * The converter lookup name that will be used to convert the
     * field/property into a String analyzer lookup name.
     */
    String converter() default "";

    /**
     * The property accessor that will be fetch and write the property value.
     * <p/>
     * It is automatically set based on where the annotation is used, but can be
     * explicitly set. Compass also supports custom property accessors, registered
     * under a custom name, which can then be used here as well.
     */
    String accessor() default "";
}
