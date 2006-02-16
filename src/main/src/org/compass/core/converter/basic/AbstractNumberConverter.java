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

package org.compass.core.converter.basic;

import java.text.DecimalFormat;
import java.text.Format;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Locale;

import org.compass.core.converter.ConversionException;
import org.compass.core.converter.ParameterConverter;
import org.compass.core.mapping.Mapping;
import org.compass.core.mapping.ResourcePropertyMapping;

/**
 * @author kimchy
 */
public abstract class AbstractNumberConverter extends AbstractBasicConverter implements ParameterConverter {

    private static class NumberFormatter implements ThreadSafeFormat.FormatterFactory {

        private String format;

        private Locale locale;

        public NumberFormatter(String format) {
            this(format, null);
        }

        public NumberFormatter(String format, Locale locale) {
            this.format = format;
            this.locale = locale;
        }

        public Format create() {
            // TODO find how to support locale
            if (locale != null) {
                return new DecimalFormat(format);
            }
            return new DecimalFormat(format);
        }

    }

    private HashMap formats = new HashMap();

    public void addParameter(Mapping mapping) {
        if (mapping.getConverterParam() != null) {
            formats.put(mapping.getConverterParam(), new ThreadSafeFormat(4, 20,
                    new AbstractNumberConverter.NumberFormatter(mapping.getConverterParam())));
        }
    }

    protected abstract Object defaultFromString(String str, ResourcePropertyMapping resourcePropertyMapping);

    protected abstract Object fromNumber(Number number);

    protected String defaultToString(Object o, ResourcePropertyMapping resourcePropertyMapping) {
        return super.toString(o, resourcePropertyMapping);
    }

    public Object fromString(String str, ResourcePropertyMapping resourcePropertyMapping) {
        try {
            String converterParam = resourcePropertyMapping.getConverterParam();
            if (converterParam == null) {
                return defaultFromString(str, resourcePropertyMapping);
            } else {
                return fromNumber((Number) getFormat(converterParam).parse(str));
            }
        } catch (ParseException e) {
            throw new ConversionException("Failed to parse date [" + str + "]", e);
        }
    }

    public String toString(Object o, ResourcePropertyMapping resourcePropertyMapping) {
        if (resourcePropertyMapping != null && resourcePropertyMapping.getConverterParam() != null) {
            return getFormat(resourcePropertyMapping.getConverterParam()).format(o);
        } else {
            return defaultToString(o, resourcePropertyMapping);
        }
    }

    private ThreadSafeFormat getFormat(String converterParam) throws ConversionException {
        ThreadSafeFormat format = (ThreadSafeFormat) formats.get(converterParam);
        if (format == null) {
            synchronized (formats) {
                format = new ThreadSafeFormat(4, 20, new AbstractNumberConverter.NumberFormatter(converterParam));
                formats.put(converterParam, format);
            }
        }
        return format;
    }

}
