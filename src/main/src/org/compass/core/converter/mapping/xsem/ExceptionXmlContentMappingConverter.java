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

package org.compass.core.converter.mapping.xsem;

import org.compass.core.converter.Converter;
import org.compass.core.converter.ConversionException;
import org.compass.core.Resource;
import org.compass.core.marshall.MarshallingContext;
import org.compass.core.mapping.Mapping;

/**
 * The default xml content mapping converter, will raise a runtime exception once used, since
 * a specific one (dom4j, ...) should be specified at configuration time or in the mapping file.
 *
 * @author kimchy
 */
public class ExceptionXmlContentMappingConverter implements Converter {

    public boolean marshall(Resource resource, Object root, Mapping mapping, MarshallingContext context) throws ConversionException {
        throw new ConversionException("Trying to use xml-content mapping for alias [" + resource.getAlias() +
                "] without setting a specific converter either in the configuration file, or the mapping definition");
    }

    public Object unmarshall(Resource resource, Mapping mapping, MarshallingContext context) throws ConversionException {
        throw new ConversionException("Trying to use xml-content mapping for alias [" + resource.getAlias() +
                "] without setting a specific converter either in the configuration file, or the mapping definition");
    }
}
