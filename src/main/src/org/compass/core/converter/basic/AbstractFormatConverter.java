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

import java.util.Locale;

import org.compass.core.CompassException;
import org.compass.core.config.CompassConfigurable;
import org.compass.core.config.CompassEnvironment;
import org.compass.core.config.CompassSettings;

/**
 * A base class that can handle {@link ThreadSafeFormat} and provide formatting support.
 * The format is read from a configuration setting {@link CompassEnvironment.Converter.Format#FORMAT}.
 * Uses a pool of formatters for better performance, using {@link CompassEnvironment.Converter.Format#MIN_POOL_SIZE},
 * and {@link CompassEnvironment.Converter.Format#MAX_POOL_SIZE} as configuration settings for the pool size.
 * <p/>
 * If specific locale is required for the formatted, the {@link CompassEnvironment.Converter.Format#LOCALE} can
 * be used to specify the required locale.
 * <p/>
 * Allows to specify the default format if none is provided by overriding {@link #doGetDefaultFormat()}.
 *
 * @author kimchy
 */
public abstract class AbstractFormatConverter extends AbstractBasicConverter implements CompassConfigurable, FormatConverter {

    protected ThreadSafeFormat formatter;

    protected boolean hasFormatter = true;

    public void configure(CompassSettings settings) throws CompassException {
        String format = settings.getSetting(CompassEnvironment.Converter.Format.FORMAT);
        if (format == null) {
            format = doGetDefaultFormat();
        }
        if (format == null) {
            hasFormatter = false;
            return;
        }
        String localeSetting = settings.getSetting(CompassEnvironment.Converter.Format.LOCALE);
        Locale locale;
        if (localeSetting != null) {
            locale = new Locale(localeSetting);
        } else {
            locale = Locale.getDefault();
        }

        ThreadSafeFormat.FormatterFactory formatterFactory = doCreateFormatterFactory();
        formatterFactory.configure(format, locale);

        int minPoolSize = settings.getSettingAsInt(CompassEnvironment.Converter.Format.MIN_POOL_SIZE, 4);
        int maxPoolSize = settings.getSettingAsInt(CompassEnvironment.Converter.Format.MIN_POOL_SIZE, 20);

        formatter = new ThreadSafeFormat(minPoolSize, maxPoolSize, formatterFactory);
    }

    public void setFormat(String format) {
        ThreadSafeFormat.FormatterFactory formatterFactory = doCreateFormatterFactory();
        formatterFactory.configure(format, Locale.getDefault());

        formatter = new ThreadSafeFormat(4, 20, formatterFactory);
    }

    public FormatConverter copy() {
        try {
            return (FormatConverter) getClass().newInstance();
        } catch (Exception e) {
            throw new CompassException("Should not happen", e);
        }
    }

    protected abstract ThreadSafeFormat.FormatterFactory doCreateFormatterFactory();

    protected String doGetDefaultFormat() {
        return null;
    }
}
