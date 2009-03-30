/*
 * Copyright 2004-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.compass.core.converter.extended;

import java.text.ParseException;
import java.util.Locale;
import java.util.TimeZone;

import org.compass.core.converter.ConversionException;
import org.compass.core.converter.basic.AbstractFormatConverter;
import org.compass.core.converter.basic.DateMathParser;
import org.compass.core.converter.basic.format.Formatter;
import org.compass.core.converter.basic.format.FormatterFactory;
import org.compass.core.mapping.ResourcePropertyMapping;
import org.compass.core.marshall.MarshallingContext;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.ISODateTimeFormat;

/**
 * A converter for Joda {@link DateTime}. The default format is the ISO format.
 *
 * @author kimchy
 */
public class DataTimeConverter extends AbstractFormatConverter {

    public static final String DEFAULT_NOW_PREFIX = "now";

    public static final String ISO = "iso";

    private class DataTimeFormatterFactory implements FormatterFactory {

        private String format;

        private Locale locale;

        public void configure(String format, Locale locale) {
            this.format = format;
            this.locale = locale;
        }

        public Formatter create() {
            if (ISO.equalsIgnoreCase(format)) {
                return new DateTimeFormatter(ISODateTimeFormat.dateTime());
            }
            org.joda.time.format.DateTimeFormatter formatter = DateTimeFormat.forPattern(format);
            formatter = formatter.withLocale(locale);
            return new DateTimeFormatter(formatter);
        }
    }

    private class DateTimeFormatter implements Formatter {

        private final org.joda.time.format.DateTimeFormatter formatter;

        private DateTimeFormatter(org.joda.time.format.DateTimeFormatter formatter) {
            this.formatter = formatter;
        }

        public String format(Object obj) {
            return formatter.print((DateTime) obj);
        }

        public Object parse(String str) throws ParseException {
            return formatter.parseDateTime(str);
        }

        public boolean isThreadSafe() {
            return true;
        }
    }

    protected FormatterFactory doCreateFormatterFactory() {
        return new DataTimeFormatterFactory();
    }

    @Override
    protected String doGetDefaultFormat() {
        return ISO;
    }

    protected Object doFromString(String str, ResourcePropertyMapping resourcePropertyMapping, MarshallingContext context) throws ConversionException {
        try {
            if (str.toLowerCase().startsWith(DEFAULT_NOW_PREFIX)) {
                // TODO have timezone pluggable
                DateMathParser p = new DateMathParser(TimeZone.getDefault(), locale);
                return p.parseMath(str.substring(DEFAULT_NOW_PREFIX.length()));
            }
            for (int i = 0; i < formatters.length; i++) {
                try {
                    return formatters[i].parse(str);
                } catch (Exception e) {
                    // do nothing, continue to the next one
                }
            }
            throw new ConversionException("Failed to parse date [" + str + "]");
        } catch (Exception e) {
            throw new ConversionException("Failed to parse date [" + str + "]", e);
        }
    }

    @Override
    protected String doToString(Object o, ResourcePropertyMapping resourcePropertyMapping, MarshallingContext context) {
        return formatters[0].format(o);
    }
}
