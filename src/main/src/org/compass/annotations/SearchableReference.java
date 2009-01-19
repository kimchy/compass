/*
 * Copyright 2004-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.compass.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies a searchable reference on property or field of the {@link Searchable} class.
 *
 * <p>A searchable reference is a class field/property that reference another class, and the
 * relationship need to be stored by Compass so it can be traversed when getting the class
 * from the index.
 *
 * <p>Compass will end up saving only the ids of the referenced class in the search engine index.
 *
 * <p>The searchalbe reference can annotate a {@link java.util.Collection} type field/property,
 * supporting either {@link java.util.List} or {@link java.util.Set}. The searchable refrence
 * will try and automatically identify the element type using generics, but if the collection
 * is not defined with generics, {@link #refAlias()} should be used to reference the referenced
 * searchable class mapping definitions.
 *
 * <p>The searchable compoent can annotate an array as well, with the array element type used for
 * refernced searchable class mapping definitions.
 *
 * <p>The refence mapping can have a "shadow" component mapping associated with it, if specifing
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
     * The operations that will cascade to the target association. Defaults to no operations
     * being cascaded.
     */
    Cascade[] cascade() default {};

    /**
     * The conveter lookup name that will convert the {@link org.compass.core.mapping.osem.ReferenceMapping}.
     * Defaults to compass own intenral {@link org.compass.core.converter.mapping.osem.ReferenceMappingConverter}.
     */
    String converter() default "";

    /**
     * The property accessor that will be fetch and write the property value.
     *
     * <p>It is automatically set based on where the annotation is used, but can be
     * explicitly set. Compass also supports custom property accessors, registered
     * under a custom name, which can then be used here as well.
     */
    String accessor() default "";

    /**
     * Should this refernce mapping (only in case of collection) will be lazy or not.
     */
    Lazy lazy() default Lazy.NA;
}
