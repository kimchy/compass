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
 * Specifies a searchable id component on property or field of the {@link org.compass.annotations.Searchable} class.
 *
 * <p>A searchable id component is a class field/property that reference another class, which
 * content need to be embedded into the content of its {@link org.compass.annotations.Searchable} class and
 * represents one of its ids.
 *
 * <p>The referenced class must have searchable definitions, defined either using annotations
 * or other means (like xml).
 *
 * <p>Compass will try to automatically identify the searchable class mapping definitions that
 * map to the component class. If the mappings can not be automatically identified, the
 * {@link #refAlias()} should be used to reference the alias that has the searchable class
 * mapping definition.
 *
 * <p>The searchable component can annotate a {@link java.util.Collection} type field/property,
 * supporting either {@link java.util.List} or {@link java.util.Set}. The searchable component
 * will try and automatically identify the element type using generics, but if the collection
 * is not defined with generics, {@link #refAlias()} should be used to reference the component
 * searchable class mapping definitions.
 *
 * <p>The searchable component can annotate an array as well, with the array element type used for
 * referenced searchable class mapping definitions.
 *
 * @author kimchy
 * @see org.compass.annotations.Searchable
 * @see org.compass.annotations.SearchableReference
 */
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface SearchableIdComponent {

    /**
     * The reference alias that points to the searchable class (either defined using
     * annotations or xml). Not required since most of the times it can be automatically
     * detected.
     */
    public abstract String refAlias() default "";

    /**
     * Should the component definitions override an already existing component definitions
     * for the same field/property.
     */
    public abstract boolean override() default true;

    /**
     * The operations that will cascade to the target association. Defaults to no operations
     * being cascaded.
     */
    public abstract Cascade[] cascade() default {};

    /**
     * The depth of cyclic component references allowed.
     */
    public abstract int maxDepth() default 1;

    /**
     * An optional prefix that will be appended to all the component referenced class mappings.
     */
    String prefix() default "";
    
    /**
     * The conveter lookup name that will convert the {@link org.compass.core.mapping.osem.ComponentMapping}.
     * Defaults to compass own intenral {@link org.compass.core.converter.mapping.osem.ComponentMappingConverter}.
     */
    public abstract String converter() default "";

    /**
     * The property accessor that will be fetch and write the property value.
     * <p/>
     * It is automatically set based on where the annotation is used, but can be
     * explicitly set. Compass also supports custom property accessors, registered
     * under a custom name, which can then be used here as well.
     */
    public abstract String accessor() default "";
}