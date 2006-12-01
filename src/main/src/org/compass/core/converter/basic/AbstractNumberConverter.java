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
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

import org.compass.core.config.CompassConfigurable;
import org.compass.core.converter.ConversionException;
import org.compass.core.mapping.ResourcePropertyMapping;

/**
 * A base class for number based converters. Allows for formatting (if specified) and for default
 * behaviour handling if none is provided.
 *
 * @author kimchy
 */
public abstract class AbstractNumberConverter extends AbstractFormatConverter implements CompassConfigurable {

    private static class NumberFormatter implements ThreadSafeFormat.FormatterFactory {

        private String format;

        private Locale locale;

        public void configure(String format, Locale locale) {
            this.format = format;
            this.locale = locale;
        }

        public java.text.Format create() {
            NumberFormat numberFormat;
            if (locale != null) {
                numberFormat = NumberFormat.getInstance(locale);
            } else {
                numberFormat = NumberFormat.getInstance();
            }
            ((DecimalFormat) numberFormat).applyPattern(format);
            return numberFormat;
        }

    }

    protected ThreadSafeFormat.FormatterFactory doCreateFormatterFactory() {
        return new AbstractNumberConverter.NumberFormatter();
    }

    protected abstract Object defaultFromString(String str, ResourcePropertyMapping resourcePropertyMapping);

    protected abstract Object fromNumber(Number number);

    protected String defaultToString(Object o, ResourcePropertyMapping resourcePropertyMapping) {
        return super.toString(o, resourcePropertyMapping);
    }

    public Object fromString(String str, ResourcePropertyMapping resourcePropertyMapping) {
        try {
            if (hasFormatter) {
                return fromNumber((Number) formatter.parse(str));
            } else {
                return defaultFromString(str, resourcePropertyMapping);
            }
        } catch (ParseException e) {
            throw new ConversionException("Failed to parse number [" + str + "]", e);
        }
    }

    public String toString(Object o, ResourcePropertyMapping resourcePropertyMapping) {
        if (hasFormatter) {
            return formatter.format(o);
        } else {
            return defaultToString(o, resourcePropertyMapping);
        }
    }
}
