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

import org.compass.core.converter.ConversionException;
import org.compass.core.converter.mapping.ContextResourcePropertyConverter;
import org.compass.core.mapping.ResourcePropertyMapping;
import org.compass.core.marshall.MarshallingContext;

/**
 * Same as {@link org.compass.core.converter.json.ResourcePropertyJsonValueConverter} except that
 * can actually handle context conversion since it accepts a {@link org.compass.core.converter.mapping.ContextResourcePropertyConverter}.
 *
 * @author kimchy
 */
public class ContextResourcePropertyJsonValueConverter extends ResourcePropertyJsonValueConverter {

    private final ContextResourcePropertyConverter converter;

    public ContextResourcePropertyJsonValueConverter(ContextResourcePropertyConverter converter) {
        super(converter);
        this.converter = converter;
    }

    @Override
    public String toString(Object value, ResourcePropertyMapping resourcePropertyMapping, MarshallingContext context) {
        return converter.toString(value, resourcePropertyMapping, context);
    }

    @Override
    public Object fromString(String str, ResourcePropertyMapping resourcePropertyMapping, MarshallingContext context) throws ConversionException {
        return converter.fromString(str, resourcePropertyMapping, context);
    }
}
