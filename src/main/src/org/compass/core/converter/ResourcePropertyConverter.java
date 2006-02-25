/*
 * Copyright 2004-2006 the original author or authors.
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

import org.compass.core.mapping.ResourcePropertyMapping;

/**
 * A specialized converter that can convert to and from strings as well.
 *
 * @author kimchy
 */
public interface ResourcePropertyConverter extends Converter {

    /**
     * Converts from a String and into it's Object representation.
     *
     * @param str The string to convert from
     * @param resourcePropertyMapping The resource property mapping
     * @return Theh object converterd from the String
     * @throws ConversionException
     */
    Object fromString(String str, ResourcePropertyMapping resourcePropertyMapping) throws ConversionException;

    /**
     * Converts the Object into a String.
     *
     * Note that toString must be able to handle a <code>null</code> resourcePropertyMapping.
     *
     * @param o The Object to convert from
     * @param resourcePropertyMapping The resource proeprty mapping
     * @return The String converted from the Object
     * @throws ConversionException
     */
    String toString(Object o, ResourcePropertyMapping resourcePropertyMapping) throws ConversionException;

}
