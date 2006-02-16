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

import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;

import org.compass.core.converter.ConversionException;
import org.compass.core.converter.ParameterConverter;
import org.compass.core.mapping.Mapping;
import org.compass.core.mapping.ResourcePropertyMapping;

/**
 * @author kimchy
 */
public class DateConverter extends AbstractBasicConverter implements ParameterConverter {

    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd-HH-mm-ss-S-a";

    private static class DateFormatter implements ThreadSafeFormat.FormatterFactory {

        private String format;

        private Locale locale;

        public DateFormatter(String format) {
            this(format, null);
        }

        public DateFormatter(String format, Locale locale) {
            this.format = format;
            this.locale = locale;
        }

        public Format create() {
            if (locale != null) {
                return new SimpleDateFormat(format, locale);
            }
            return new SimpleDateFormat(format);
        }
    }

    private final ThreadSafeFormat defaultFormat;

    private final HashMap formats = new HashMap();

    public DateConverter() {
        this.defaultFormat = new ThreadSafeFormat(4, 20, new DateConverter.DateFormatter(DEFAULT_DATE_FORMAT));
    }

    public void addParameter(Mapping mapping) {
        if (mapping.getConverterParam() != null) {
            formats.put(mapping.getConverterParam(), new ThreadSafeFormat(4, 20, new DateConverter.DateFormatter(
                    mapping.getConverterParam())));
        }
    }

    public Object fromString(String str, ResourcePropertyMapping resourcePropertyMapping) throws ConversionException {
        try {
            String converterParam = resourcePropertyMapping.getConverterParam();
            if (converterParam == null) {
                return defaultFormat.parse(str);
            } else {
                return getFormat(converterParam).parse(str);
            }
        } catch (ParseException e) {
            throw new ConversionException("Failed to parse date [" + str + "]", e);
        }
    }

    public String toString(Object o, ResourcePropertyMapping resourcePropertyMapping) throws ConversionException {
        if (resourcePropertyMapping != null && resourcePropertyMapping.getConverterParam() != null) {
            return getFormat(resourcePropertyMapping.getConverterParam()).format(o);
        } else {
            return defaultFormat.format(o);
        }
    }

    private ThreadSafeFormat getFormat(String converterParam) throws ConversionException {
        ThreadSafeFormat format = (ThreadSafeFormat) formats.get(converterParam);
        if (format == null) {
            synchronized (formats) {
                format = new ThreadSafeFormat(4, 20, new DateConverter.DateFormatter(converterParam));
                formats.put(converterParam, format);
            }
        }
        return format;
    }
}
