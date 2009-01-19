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

package org.compass.core.converter;

import org.compass.core.config.CompassConfigurable;
import org.compass.core.config.CompassSettings;

/**
 * Provides the ability to lookup a converter based on it's type. The type is
 * the actual <code>Class</code> that should be converted by the returned
 * <code>Converter</code>.
 *
 * @author kimchy
 * @see Converter
 */
public interface ConverterLookup extends CompassConfigurable {

    /**
     * Looks up a converter based on the <code>Class</code> type.
     *
     * @param type The class to look the converter for
     * @return The converter that match for the given class type.
     */
    Converter lookupConverter(Class type);

    /**
     * Looks up a converter based on the converter name (or class name). The converter
     * should have been registered with the converter lookup using the
     * {@link #registerConverter(String, Converter)}.
     *
     * @param name The lookup name of the converter
     * @return The converter that match for the given name.
     */
    Converter lookupConverter(String name);

    /**
     * Registers a {@link Converter} under a converter name.
     *
     * @param converterName The converter name to be registered against
     * @param converter     The converter to use
     */
    void registerConverter(String converterName, Converter converter);

    /**
     * Registers a {@link Converter} under the converter name. Also associates
     * the converter with the given type.
     *
     * @param converterName The converter name to be registered against
     * @param converter     The converter to use
     * @param registerType  The type to associate the converter with
     */
    void registerConverter(String converterName, Converter converter, Class registerType);

    /**
     * Return the settings that this converter was created with.
     */
    CompassSettings getSettings();
}
