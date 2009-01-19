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
 * Specifies a class as being "convertable" by Compass.
 *
 * <p>Mostly used to convert classes into a String value that
 * will be stored in the index. Applies to {@link Searchable} class
 * {@link SearchableId} and {@link SearchableProperty} annotations.
 *
 * Requires a converter that implements the {@link Converter} interface. Usually
 * <p>will extend the {@link org.compass.core.converter.basic.AbstractBasicConverter}
 * (since for other cases, the {@link SearchableComponent} can be used).
 *
 * <p>Alloes for additional settings to be passes to the converter using {@link #settings()}.
 * For the converter to be injected with the specified settings, it need to implement the
 * {@link org.compass.core.config.CompassConfigurable} interface.
 *
 * @author kimchy
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface SearchableClassConverter {

    /**
     * The converter that will be used to convert the class.
     *
     * <p>Usually will be a converter that converts the class into a String
     * stored in the search engine index (since for other cases, the {@link SearchableComponent}
     * can be used). Compass comes with a handy class for such converters:
     * {@link org.compass.core.converter.basic.AbstractBasicConverter}.
     */
    Class<? extends Converter> value();

    /**
     * Additional settings to inject to the converter. If set, the converter must implement
     * the {@link org.compass.core.config.CompassConfigurable} for them to be injected.
     */
    SearchSetting[] settings() default {};
}
