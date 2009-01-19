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
 * Allows to define cascading annotation which will result in certain operations done on the object
 * that holds the property to be cascaded to its referenced objects.
 *
 * @author kimchy
 */
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface SearchableCascading {

    /**
     * The conveter lookup name that will convert the {@link org.compass.core.mapping.osem.PlainCascadeMapping}.
     * Defaults to compass own intenral {@link org.compass.core.converter.mapping.osem.PlainCascadeMappingConverter}.
     */
    public abstract String converter() default "";

    /**
     * The property accessor that will be fetch and write the property value.
     *
     * <p>It is automatically set based on where the annotation is used, but can be
     * explicitly set. Compass also supports custom property accessors, registered
     * under a custom name, which can then be used here as well.
     */
    public abstract String accessor() default "";

    /**
     * The operations that will cascade to the target association. Defaults all operations
     * being cascaded.
     */
    public abstract Cascade[] cascade() default Cascade.ALL;
}