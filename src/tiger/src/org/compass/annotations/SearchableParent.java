package org.compass.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies a parent reference for cyclic components.
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
}
