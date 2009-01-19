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

    /**
     * The operations that will cascade to the target association. Defaults to no operations
     * being cascaded.
     */
    Cascade[] cascade() default {};
}
