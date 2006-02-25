package org.compass.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines a collection of {@link SearchableMetaData} associated with
 * a {@link Searchable} class field/property.
 * <p/>
 * Only applies to a field/property that is annotated with {@link SearchableId}
 * or {@link SearchableProperty}.
 *
 * @author kimchy
 */
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface SearchableMetaDatas {

    /**
     * Colleciton of meta-data that will be associated with a {@link Searchable} class field/property.
     */
    SearchableMetaData[] value();
}
