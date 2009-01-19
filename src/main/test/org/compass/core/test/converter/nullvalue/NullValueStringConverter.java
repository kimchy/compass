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

package org.compass.core.test.converter.nullvalue;

import org.compass.core.converter.basic.DateConverter;
import org.compass.core.mapping.ResourcePropertyMapping;
import org.compass.core.marshall.MarshallingContext;

/**
 * A simple converter that returns "moo" for null values.
 *
 * @author kimchy
 */
public class NullValueStringConverter extends DateConverter {

    protected boolean handleNulls(ResourcePropertyMapping resourcePropertyMapping, MarshallingContext context) {
        return true;
    }

    protected String getNullValue(ResourcePropertyMapping resourcePropertyMapping, MarshallingContext context) {
        return "moo";
    }

    protected boolean isNullValue(String value, ResourcePropertyMapping resourcePropertyMapping, MarshallingContext context) {
        return "moo".equals(value);
    }
}