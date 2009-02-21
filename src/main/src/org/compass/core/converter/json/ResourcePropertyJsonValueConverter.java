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

package org.compass.core.converter.json;

import org.compass.core.Property;
import org.compass.core.converter.ConversionException;
import org.compass.core.converter.mapping.ContextResourcePropertyConverter;
import org.compass.core.converter.mapping.ResourcePropertyConverter;
import org.compass.core.mapping.ResourcePropertyMapping;
import org.compass.core.marshall.MarshallingContext;

/**
 * Extends the {@link org.compass.core.converter.json.SimpleJsonValueConverter} by delegating all specific
 * value marshalling (to/from) to a {@link org.compass.core.converter.mapping.ResourcePropertyConverter}.
 *
 * <p>The important part here is the fact that {@link #marshall(org.compass.core.Resource, Object, org.compass.core.mapping.Mapping, org.compass.core.marshall.MarshallingContext)}
 * is still implemented by {@link org.compass.core.converter.json.SimpleJsonValueConverter}, so dynamic nature, full path,
 * and null value are correctly handled.
 *
 * @author kimchy
 */
public class ResourcePropertyJsonValueConverter extends SimpleJsonValueConverter implements ContextResourcePropertyConverter {

    private ResourcePropertyConverter converter;

    public ResourcePropertyJsonValueConverter(ResourcePropertyConverter converter) {
        this.converter = converter;
    }

    public String toString(Object value, ResourcePropertyMapping resourcePropertyMapping, MarshallingContext context) {
        return converter.toString(value, resourcePropertyMapping);
    }

    public Object fromString(String str, ResourcePropertyMapping resourcePropertyMapping) throws ConversionException {
        return converter.fromString(str, resourcePropertyMapping);
    }

    public Object fromString(String str, ResourcePropertyMapping resourcePropertyMapping, MarshallingContext context) throws ConversionException {
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