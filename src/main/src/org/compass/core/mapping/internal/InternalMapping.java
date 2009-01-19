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

package org.compass.core.mapping.internal;

import org.compass.core.converter.Converter;
import org.compass.core.engine.naming.PropertyPath;
import org.compass.core.mapping.Mapping;

/**
 * An extension to the {@link Mapping} interface allowing to making it mutable.
 *
 * @author kimchy
 */
public interface InternalMapping extends Mapping {

    /**
     * Sets the name of the mapping. Acts as the "logical" name of the mapping (think
     * Java Bean Property name).
     */
    void setName(String name);
    
    /**
     * Sets the path of the mapping. The path is the value under which it will
     * be saved in the Search Engine.
     */
    void setPath(PropertyPath path);

    /**
     * Sets the conveter associated with the mapping. The converter is responsible for
     * marshalling and unmarshalling the Mapping from and to the Search Engine.
     */
    void setConverter(Converter converter);

    /**
     * Sets the converter name associated with the Mapping. The conveter name
     * can be the actual class name of the converter, or a lookup name that has a
     * converter associated with it.
     */
    void setConverterName(String name);
}
