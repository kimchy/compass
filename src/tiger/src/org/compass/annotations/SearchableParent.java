package org.compass.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies a parent reference for {@link SearchableComponent}.
 * <p/>
 * If a {@link SearchableComponent} has a reference it it's parent,
 * the annotation will make sure that this reference will be initalized
 * when the {@link Searchable} class is loaded from the index.
 *
 * @author kimchy
 */
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface SearchableParent {

    /**
     * The conveter lookup name that will convert the {@link org.compass.core.mapping.osem.ParentMapping}.
     * Defaults to compass own intenral {@link org.compass.core.converter.mapping.osem.ParentMappingConverter}.
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
