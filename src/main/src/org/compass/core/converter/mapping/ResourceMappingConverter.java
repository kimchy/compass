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

package org.compass.core.converter.mapping;

import org.compass.core.Resource;
import org.compass.core.converter.ConversionException;
import org.compass.core.converter.Converter;
import org.compass.core.mapping.ResourceMapping;
import org.compass.core.marshall.MarshallingContext;

/**
 * A converter associated with a {@link ResourceMapping} implementation. Allows to marhsall/unmarshall
 * just ids as well.
 *
 * @author kimchy
 */
public interface ResourceMappingConverter extends Converter {

    /**
     * Marshalls teh given <code>Object</code> id into the given resource. Returns <code>true</code>
     * if anything was was stored in the resource.
     */
    boolean marshallIds(Resource idResource, Object id, ResourceMapping resourceMapping, MarshallingContext context)
            throws ConversionException;

    /**
     * Unamrshalls the given id into its id properties values.
     */
    Object[] unmarshallIds(Object id, ResourceMapping resourceMapping, MarshallingContext context)
            throws ConversionException;
}
