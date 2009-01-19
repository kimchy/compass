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

package org.compass.core.converter.mapping;

import org.compass.core.converter.ConversionException;
import org.compass.core.mapping.ResourcePropertyMapping;
import org.compass.core.marshall.MarshallingContext;

/**
 * @author kimchy
 */
public interface ContextResourcePropertyConverter<T> extends ResourcePropertyConverter<T> {

    /**
     * Converts from a String and into it's Object representation.
     *
     * @param str                     The string to convert from
     * @param resourcePropertyMapping The resource property mapping
     * @return Theh object converterd from the String
     * @throws org.compass.core.converter.ConversionException
     *
     */
    T fromString(String str, ResourcePropertyMapping resourcePropertyMapping, MarshallingContext context) throws ConversionException;

    /**
     * Converts the Object into a String.
     *
     * <p>Note that toString must be able to handle a <code>null</code> resourcePropertyMapping.
     *
     * @param o                       The Object to convert from
     * @param resourcePropertyMapping The resource proeprty mapping
     * @return The String converted from the Object
     * @throws ConversionException
     */
    String toString(T o, ResourcePropertyMapping resourcePropertyMapping, MarshallingContext context) throws ConversionException;

}
