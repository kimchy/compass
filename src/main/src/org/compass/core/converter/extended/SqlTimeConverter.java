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

package org.compass.core.converter.extended;

import java.sql.Time;
import java.util.Date;

import org.compass.core.converter.ConversionException;
import org.compass.core.converter.basic.DateConverter;
import org.compass.core.mapping.ResourcePropertyMapping;
import org.compass.core.marshall.MarshallingContext;

/**
 * @author kimchy
 */
public class SqlTimeConverter extends DateConverter {

    /**
     * Sql Time has no default format, it uses the {@link java.sql.Time#toString()}.
     */
    protected String doGetDefaultFormat() {
        return null;
    }

    protected String doToString(Object o, ResourcePropertyMapping resourcePropertyMapping, MarshallingContext context) {
        if (hasFormatter) {
            return super.doToString(o, resourcePropertyMapping, context);
        }
        return o.toString();
    }

    protected Object doFromString(String str, ResourcePropertyMapping resourcePropertyMapping, MarshallingContext context) throws ConversionException {
        if (hasFormatter) {
            Date date = (Date) super.doFromString(str, resourcePropertyMapping, context);
            return new Time(date.getTime());
        }
        return Time.valueOf(str);

    }
}
