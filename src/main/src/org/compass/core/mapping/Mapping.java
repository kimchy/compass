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

package org.compass.core.mapping;

import org.compass.core.converter.Converter;
import org.compass.core.engine.naming.PropertyPath;

/**
 * A general interface for all things Mapping in compass. Has a name and a path, where the
 * name is usually the "logical name" of the mapping, and the path is the actual name which
 * it will be saved under in the search engine.
 *
 * <p>Also provides general support for converters attached to the mappings, which can have
 * parameters associated with them.
 *
 * @author kimchy
 */
public interface Mapping {

    /**
     * The name of the mapping. Acts as the "logical" name of the mapping (think
     * Java Bean Property name).
     */
    String getName();

    /**
     * Returns the path of the mapping. The path is the value under which it will
     * be saved in the Search Engine.
     */
    PropertyPath getPath();

    /**
     * Returns the conveter associated with the mapping. The converter is responsible for
     * marshalling and unmarshalling the Mapping from and to the Search Engine.
     */
    Converter getConverter();

    /**
     * Returns the converter name associated with the Mapping. The conveter name
     * can be the actual class name of the converter, or a lookup name that has a
     * converter associated with it.
     */
    String getConverterName();

    /**
     * Returns <code>true</code> if the Mapping controlls the fact that if it has no value, it's
     * parent might be a candidate for being nullable.
     */
    boolean controlsObjectNullability();

    /**
     * Copies over the mapping definition into a newly instanciated Mapping object.
     */
    Mapping copy();

}
