package org.compass.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies a searchable reference on property or field of the {@link Searchable} class.
 * <p/>
 * A searchable reference is a class field/property that reference another class, and the
 * relationship need to be stored by Compass so it can be traversed when getting the class
 * from the index.
 * <p/>
 * Compass will end up saving only the ids of the referenced clas in the search engine index.
 * <p/>
 * The searchalbe component can annotate a {@link java.util.Collection} type field/property,
 * supporting either {@link java.util.List} or {@link java.util.Set}. The searchable component
 * will try and automatically identify the element type using generics, but if the collection
 * is not defined with generics, {@link #refAlias()} should be used to reference the component
 * searchable class mapping definitions.
 * <p/>
 * The searchable compoent can annotate an array as well, with the array element type used for
 * refernced searchable class mapping definitions.
 * <p/>
 * The refence mapping can have a "shadow" component mapping associated with it, if specifing
 * the {@link #refComponentAlias()}.
 *
 * @author kimchy
 */
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface SearchableReference {

    /**
     * The reference alias that points to the searchable class (either defined using
     * annotations or xml). Not required since most of the times it can be automatically
     * detected.
     */
    String refAlias() default "";

    /**
     * Specifies a reference to a searchable component that will be used
     * to embed some of the referenced class searchable content into the
     * field/property searchable class.
     */
    String refComponentAlias() default "";

    /**
     * The conveter lookup name that will convert the {@link org.compass.core.mapping.osem.ReferenceMapping}.
     * Defaults to compass own intenral {@link org.compass.core.converter.mapping.osem.ReferenceMappingConverter}.
     */
    String converter() default "";
}
