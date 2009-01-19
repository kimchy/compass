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

import org.compass.core.Property;
import org.compass.core.converter.ConversionException;
import org.compass.core.converter.mapping.ResourcePropertyConverter;
import org.compass.core.mapping.ResourcePropertyMapping;
import org.compass.core.xml.XmlObject;

/**
 * An extension to simple xml value converter that delegates the {@link #toString(org.compass.core.xml.XmlObject, org.compass.core.mapping.ResourcePropertyMapping)}
 * method to a {@link org.compass.core.converter.mapping.ResourcePropertyConverter} which can normalize the string using
 * its {@link org.compass.core.converter.mapping.ResourcePropertyConverter#fromString(String, org.compass.core.mapping.ResourcePropertyMapping)}
 * and then {@link org.compass.core.converter.mapping.ResourcePropertyConverter#toString(Object, org.compass.core.mapping.ResourcePropertyMapping)}.
 *
 * @author kimchy
 */
public class ResourcePropertyValueConverter extends SimpleXmlValueConverter implements ResourcePropertyConverter {

    private ResourcePropertyConverter converter;

    public ResourcePropertyValueConverter(ResourcePropertyConverter converter) {
        this.converter = converter;
    }

    /**
     * Normalizes the {@link org.compass.core.xml.XmlObject#getValue()} using the delegated
     * {@link org.compass.core.converter.mapping.ResourcePropertyConverter#fromString(String, org.compass.core.mapping.ResourcePropertyMapping)},
     * and then using {@link org.compass.core.converter.xsem.ResourcePropertyValueConverter#toString(Object, org.compass.core.mapping.ResourcePropertyMapping)}.
     */
    public String toString(XmlObject xmlObject, ResourcePropertyMapping resourcePropertyMapping) {
        return converter.toString(converter.fromString(xmlObject.getValue(), resourcePropertyMapping), resourcePropertyMapping);
    }

    // ResourcePropertyConverter methods

    public Object fromString(String str, ResourcePropertyMapping resourcePropertyMapping) throws ConversionException {
        return converter.fromString(str, resourcePropertyMapping);
    }

    public String toString(Object o, ResourcePropertyMapping resourcePropertyMapping) throws ConversionException {
        return converter.toString(o, resourcePropertyMapping);
    }

    public boolean canNormalize() {
        return converter.canNormalize();
    }

    public Property.Index suggestIndex() {
        return converter.suggestIndex();
    }

    public Property.TermVector suggestTermVector() {
        return converter.suggestTermVector();
    }

    public Property.Store suggestStore() {
        return converter.suggestStore();
    }

    public Boolean suggestOmitNorms() {
        return converter.suggestOmitNorms();
    }

    public Boolean suggestOmitTf() {
        return converter.suggestOmitTf();
    }
}
