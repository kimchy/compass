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

package org.compass.core.converter.basic;

import java.util.Locale;

import org.compass.core.CompassException;
import org.compass.core.Property;
import org.compass.core.config.CompassConfigurable;
import org.compass.core.config.CompassEnvironment;
import org.compass.core.config.CompassSettings;
import org.compass.core.converter.basic.format.Formatter;
import org.compass.core.converter.basic.format.FormatterFactory;
import org.compass.core.converter.basic.format.ThreadSafeFormat;
import org.compass.core.util.StringUtils;

/**
 * A base class that can handle {@link ThreadSafeFormat} and provide formatting support.
 * The format is read from a configuration setting {@link CompassEnvironment.Converter.Format#FORMAT}.
 * Uses a pool of formatters for better performance, using {@link CompassEnvironment.Converter.Format#MIN_POOL_SIZE},
 * and {@link CompassEnvironment.Converter.Format#MAX_POOL_SIZE} as configuration settings for the pool size.
 *
 * <p>If specific locale is required for the formatted, the {@link CompassEnvironment.Converter.Format#LOCALE} can
 * be used to specify the required locale.
 *
 * <p>Allows to specify the default format if none is provided by overriding {@link #doGetDefaultFormat()}.
 *
 * @author kimchy
 */
public abstract class AbstractFormatConverter<T> extends AbstractBasicConverter<T> implements CompassConfigurable, FormatConverter<T> {

    protected Formatter[] formatters;

    protected boolean hasFormatter = true;

    protected Locale locale;

    public void configure(CompassSettings settings) throws CompassException {
        String format = settings.getSetting(CompassEnvironment.Converter.Format.FORMAT);
        if (format == null) {
            format = doGetDefaultFormat();
        }
        String localeSetting = settings.getSetting(CompassEnvironment.Converter.Format.LOCALE);
        if (localeSetting != null) {
            locale = new Locale(localeSetting);
        } else {
            locale = Locale.getDefault();
        }
        if (format == null) {
            hasFormatter = false;
            return;
        }
        createFormatters(format, settings);
    }

    public void setFormat(String format) {
        createFormatters(format, null);
    }

    public FormatConverter copy() {
        try {
            AbstractFormatConverter copy = getClass().newInstance();
            copy.locale = locale;
            return copy;
        } catch (Exception e) {
            throw new CompassException("Should not happen", e);
        }
    }

    protected abstract FormatterFactory doCreateFormatterFactory();

    protected String doGetDefaultFormat() {
        return null;
    }

    private void createFormatters(String format, CompassSettings settings) {
        String[] formatStrings = StringUtils.delimitedListToStringArray(format, "||");
        formatters = new Formatter[formatStrings.length];
        for (int i = 0; i < formatters.length; i++) {
            String currentFromat = formatStrings[i];
            FormatterFactory formatterFactory = doCreateFormatterFactory();
            formatterFactory.configure(currentFromat, locale);

            formatters[i] = formatterFactory.create();
            if (!formatters[i].isThreadSafe()) {
                int minPoolSize = 4;
                int maxPoolSize = 20;
                if (settings != null) {
                    minPoolSize = settings.getSettingAsInt(CompassEnvironment.Converter.Format.MIN_POOL_SIZE, minPoolSize);
                    maxPoolSize = settings.getSettingAsInt(CompassEnvironment.Converter.Format.MAX_POOL_SIZE, maxPoolSize);
                }
                formatters[i] = new ThreadSafeFormat(minPoolSize, maxPoolSize, formatterFactory);
            }
        }
    }

    /**
     * Format based converters should can be used (and should) when using query parser notation.
     * Returns <code>true</code>.
     */
    public boolean canNormalize() {
        return true;
    }

    /**
     * Formattable types should usually be {@link org.compass.core.Property.Index#NOT_ANALYZED}.
     */
    public Property.Index suggestIndex() {
        return Property.Index.NOT_ANALYZED;
    }
}
