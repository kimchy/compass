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

package org.compass.core.converter.xsem;

import java.io.Reader;

import org.compass.core.converter.ConversionException;
import org.compass.core.xml.AliasedXmlObject;
import org.compass.core.xml.XmlObject;

/**
 * Converts an {@link XmlObject} to and from an xml string.
 *
 * @author kimchy
 */
public interface XmlContentConverter {

    /**
     * Converts an {@link org.compass.core.xml.XmlObject} into an xml string.
     *
     * @param xmlObject The xml object to convert to an xml string
     * @return An xml string representation of the xml object
     * @throws org.compass.core.converter.ConversionException
     *          Failed to convert the xml object to an xml string
     */
    String toXml(XmlObject xmlObject) throws ConversionException;

    /**
     * Converts an xml string into an {@link org.compass.core.xml.AliasedXmlObject}.
     *
     * @param alias The alias the aliases xml object is associated with
     * @param xml   The xml string that will be converted into an aliases xml object
     * @return The aliases xml object that is the restult of the xml parsed
     * @throws ConversionException
     */
    AliasedXmlObject fromXml(String alias, Reader xml) throws ConversionException;

}
