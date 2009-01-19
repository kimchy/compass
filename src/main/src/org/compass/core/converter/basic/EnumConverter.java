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

import org.compass.core.accessor.AccessorUtils;
import org.compass.core.converter.ConversionException;
import org.compass.core.mapping.ResourcePropertyMapping;
import org.compass.core.mapping.osem.ClassPropertyMetaDataMapping;
import org.compass.core.marshall.MarshallingContext;

/**
 * @author kimchy
 */
public class EnumConverter extends AbstractBasicConverter {

    protected Object doFromString(String str, ResourcePropertyMapping resourcePropertyMapping, MarshallingContext context) throws ConversionException {
        ClassPropertyMetaDataMapping metaDataMapping = (ClassPropertyMetaDataMapping) resourcePropertyMapping;
        // if this is an EnumSet, get the collection parameter
        // TODO we can do better than check it every time, we should store it on the mapping level
        Class<? extends Enum> enumType = AccessorUtils.getCollectionParameter(metaDataMapping.getGetter());
        // if it is not, just use the actual type and assume it is the actual enum
        if (enumType == null) {
            enumType = (Class<? extends Enum>) metaDataMapping.getGetter().getReturnType();
        }
        return Enum.valueOf(enumType, str);
    }

    protected String doToString(Object o, ResourcePropertyMapping resourcePropertyMapping, MarshallingContext context) {
        return ((Enum) o).name();
    }
}
