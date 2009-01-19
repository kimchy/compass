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

import org.compass.core.converter.Converter;

/**
 * Configure {@link Converter} to be used within Compass.
 * Set on package definition (<code>package-info.java</code>).
 *
 * <p>The {@link Converter} is registed under a lookup name ({@link #name()}), which can then
 * be reference in the different mapping definitions.
 *
 * @author kimchy
 */
@Target({ElementType.PACKAGE, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface SearchConverter {

    /**
     * The name the {@link Converter} will be registered under.
     */
    String name();

    /**
     * The {@link Converter} implementation.
     */
    Class<? extends Converter> type();

    /**
     * Optional, the actual java type this converter will be used for. If used, this converter will be
     * applies to all the types of this class.
     */
    Class registerClass() default Object.class;

    /**
     * Settings for the {@link Converter} implemenation. If set,
     * the {@link Converter} should implement the {@link org.compass.core.config.CompassConfigurable}
     * interface.
     */
    SearchSetting[] settings() default {};
}
