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

package org.compass.core.test.compositeid;

import org.compass.core.converter.ConversionException;
import org.compass.core.converter.basic.AbstractBasicConverter;
import org.compass.core.mapping.ResourcePropertyMapping;
import org.compass.core.marshall.MarshallingContext;

/**
 * @author kimchy
 */
public class CompositeIdConverter extends AbstractBasicConverter {

    protected Object doFromString(String str, ResourcePropertyMapping resourcePropertyMapping, MarshallingContext context) throws ConversionException {
        CompositeId id = new CompositeId();
        id.value1 = str.substring(0, str.indexOf("/"));
        id.value2 = str.substring(id.value1.length() + 1, str.length());
        return id;
    }

    protected String doToString(Object o, ResourcePropertyMapping resourcePropertyMapping, MarshallingContext context) {
        CompositeId id = (CompositeId) o;
        return id.value1 + "/" + id.value2;
    }
}
