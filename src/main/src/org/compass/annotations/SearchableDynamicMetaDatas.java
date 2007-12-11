package org.compass.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines a collection of {@link org.compass.annotations.SearchableDynamicMetaData} associated with
 * a {@link org.compass.annotations.Searchable} class.
 *
 * @author kimchy
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface SearchableDynamicMetaDatas {

    /**
     * Colleciton of dynamci meta-data that will be associated with a {@link org.compass.annotations.Searchable} class.
     */
    SearchableDynamicMetaData[] value();
}
