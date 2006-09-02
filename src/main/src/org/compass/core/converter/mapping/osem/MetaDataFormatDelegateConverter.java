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

package org.compass.core.converter.mapping.osem;

import org.compass.core.Resource;
import org.compass.core.converter.ConversionException;
import org.compass.core.converter.Converter;
import org.compass.core.converter.DelegateConverter;
import org.compass.core.converter.basic.FormatConverter;
import org.compass.core.mapping.Mapping;
import org.compass.core.marshall.MarshallingContext;

/**
 * A metadata based format converter, that creates its own intenral format converter based on the format
 * that it is initalized with.
 *
 * @author kimchy
 */
public class MetaDataFormatDelegateConverter implements DelegateConverter {

    private FormatConverter delegatedConverter;

    private String format;

    public MetaDataFormatDelegateConverter(String format) {
        this.format = format;
    }

    /**
     * Sets a delegated format converter. Will not use the actual passed converter,
     * but will use {@link org.compass.core.converter.basic.FormatConverter#copy()} to create a new format converter.
     */
    public void setDelegatedConverter(Converter delegatedConverter) {
        if (!(delegatedConverter instanceof FormatConverter)) {
            throw new IllegalArgumentException("Meta data format attribute can only work on format converters. The " +
                    "converter [" + delegatedConverter.getClass().getName() + "] is not one.");
        }
        this.delegatedConverter = ((FormatConverter) delegatedConverter).copy();
        this.delegatedConverter.setFormat(format);
    }

    public boolean marshall(Resource resource, Object root, Mapping mapping, MarshallingContext context) throws ConversionException {
        return delegatedConverter.marshall(resource, root, mapping, context);
    }

    public Object unmarshall(Resource resource, Mapping mapping, MarshallingContext context) throws ConversionException {
        return delegatedConverter.unmarshall(resource, mapping, context);
    }
}
