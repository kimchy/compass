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

package org.compass.core.converter;

import org.compass.core.Resource;
import org.compass.core.mapping.Mapping;
import org.compass.core.marshall.MarshallingContext;

/**
 * A converter is responsible for performing conversion between the actual
 * object and the resource. Must be thread safe.
 *
 * @author kimchy
 */
public interface Converter<T> {

    /**
     * Marshall the given <code>Object</code> to the given <code>Resource</code>. Will use
     * the mapping definition as to how to marshall the object.
     * <p/>
     * Returns <code>true</code> if data was saved in the index, and it can
     * be read as well (i.e. stored).
     *
     * @param resource The resource to marhsall the object to
     * @param root     The Object to marshall to the resource
     * @param mapping  The mapping definition of how to marshall the Object to the resoruce
     * @param context  The context for the current marhslling process
     * @return <code>true</code> if data was saved in the the index that can be read.
     * @throws ConversionException
     */
    boolean marshall(Resource resource, T root, Mapping mapping, MarshallingContext context)
            throws ConversionException;

    /**
     * Unmarshall the given <code>Resource</code> to the appropiate <code>Object</code>.
     *
     * @param resource The resource to unmarshall into an Object
     * @param mapping  The mapping definition of how to unmarshall the Resource into an Object
     * @param context  The context for the current marshalling process
     * @return The object unmarshalled
     * @throws ConversionException
     */
    T unmarshall(Resource resource, Mapping mapping, MarshallingContext context) throws ConversionException;
}
