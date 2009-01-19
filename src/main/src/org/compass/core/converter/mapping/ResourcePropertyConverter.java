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

import org.compass.core.Property;
import org.compass.core.converter.ConversionException;
import org.compass.core.converter.Converter;
import org.compass.core.mapping.ResourcePropertyMapping;

/**
 * A specialized converter that can convert to and from strings as well. Handles {@link ResourcePropertyMapping}.
 *
 * @author kimchy
 */
public interface ResourcePropertyConverter<T> extends Converter<T> {

    /**
     * Converts from a String and into it's Object representation.
     *
     * @param str                     The string to convert from
     * @param resourcePropertyMapping The resource property mapping
     * @return Theh object converterd from the String
     * @throws org.compass.core.converter.ConversionException
     *
     */
    T fromString(String str, ResourcePropertyMapping resourcePropertyMapping) throws ConversionException;

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
    String toString(T o, ResourcePropertyMapping resourcePropertyMapping) throws ConversionException;

    /**
     * Returns <code>true</code> if this converter should be used to convert query parser related
     * values. Conversion is done by calling {@link #fromString(String, org.compass.core.mapping.ResourcePropertyMapping)}
     * and then {@link #toString(Object, org.compass.core.mapping.ResourcePropertyMapping)}.
     */
    boolean canNormalize();

    /**
     * The converter can suggest what type of index will be used in case no index is configured.
     * Can return <code>null</code> and will let global Compass defaults to be used.
     */
    Property.Index suggestIndex();

    /**
     * The converter can suggest if term vectors should be saved for this type in case no explicit one is configured.
     * Can return <code>null</code> and will let global Compass defaults to be used.
     */
    Property.TermVector suggestTermVector();

    /**
     * The converter can suggest the store type for this type in case no explicit one is configured.
     * Can return <code>null</code> and will let global Compass defaults to be used.
     */
    Property.Store suggestStore();

    /**
     * The converter can suggest if norms should be saved for this type in case no explicit one is configured.
     * Can return <code>null</code> and will let global Compass defaults to be used.
     */
    Boolean suggestOmitNorms();

    /**
     * The converter can suggest if tf should be saved for this type in case no explicit one is configured.
     * Can return <code>null</code> and will let global Compass defaults to be used.
     */
    Boolean suggestOmitTf();
}
