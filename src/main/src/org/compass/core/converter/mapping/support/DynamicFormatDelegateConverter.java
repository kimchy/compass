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

package org.compass.core.converter.mapping.support;

import org.compass.core.converter.ConversionException;
import org.compass.core.converter.ConverterLookup;
import org.compass.core.mapping.ResourcePropertyMapping;

/**
 * @author kimchy
 */
public class DynamicFormatDelegateConverter extends FormatDelegateConverter {

    private final ConverterLookup converterLookup;

    public DynamicFormatDelegateConverter(String format, ConverterLookup converterLookup) {
        super(format);
        this.converterLookup = converterLookup;
    }

    @Override
    public String toString(Object o, ResourcePropertyMapping resourcePropertyMapping) throws ConversionException {
        if (getDelegatedConverter() == null) {
            setDelegatedConverter(converterLookup.lookupConverter(o.getClass()));
        }
        return super.toString(o, resourcePropertyMapping);
    }
}
