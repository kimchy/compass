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

package org.compass.core.mapping.xsem.builder;

import org.compass.core.Property;
import org.compass.core.converter.Converter;
import org.compass.core.engine.naming.StaticPropertyPath;
import org.compass.core.mapping.xsem.XmlContentMapping;

/**
 * A builder allowing to constrcut xml content mapping definition allowing to
 * store the actual XML content withn the index to and be able to rebuild the
 * {@link org.compass.core.xml.XmlObject} back from the index (or get the actual
 * xml string).
 *
 * @author kimchy
 * @see XSEM#content(String)
 */
public class XmlContentMappingBuilder {

    final XmlContentMapping mapping;

    /**
     * Constructs a new XML content mapping with the given name. The XML
     * string will be stored as a property within the Resource in the index
     * under the name.
     */
    public XmlContentMappingBuilder(String name) {
        this.mapping = new XmlContentMapping();
        mapping.setName(name);
        mapping.setPath(new StaticPropertyPath(name));
        mapping.setInternal(true);
    }

    /**
     * Specifies whether and how a property will be stored. Deftauls to
     * {@link org.compass.core.Property.Store#YES}. Note, {@link org.compass.core.Property.Store#NO}
     * is not valid here.
     */
    public XmlContentMappingBuilder store(Property.Store store) {
        if (store == Property.Store.NO) {
            throw new IllegalArgumentException("Content must be stored");
        }
        mapping.setStore(store);
        return this;
    }

    /**
     * Sets the lookup converter name (registered with Compass) that will be used to convert the XML
     * content. Defaults to {@link org.compass.core.converter.mapping.xsem.XmlContentMappingConverter}.
     */
    public XmlContentMappingBuilder converter(String converterName) {
        mapping.setConverterName(converterName);
        return this;
    }

    /**
     * Sets the actual converter that will be used to convert the JSON
     * content. Defaults to {@link org.compass.core.converter.mapping.json.JsonContentMappingConverter}.
     */
    public XmlContentMappingBuilder converter(Converter converter) {
        mapping.setConverter(converter);
        return this;
    }
}