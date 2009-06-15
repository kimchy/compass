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

package org.compass.core.marshall;

import org.compass.core.converter.mapping.osem.ClassMappingConverter;

/**
 * @author kimchy
 */
public class MarshallingEnvironment {

    public static final String PROPERTY_CLASS = "class";

    public static final String PROPERTY_COLLECTION_TYPE = "colType";

    public static final String PROPERTY_COLLECTION_SIZE = "colSize";

    public static final String PROPERTY_ENUM_NAME = "name";
    

    public static final String ATTRIBUTE_PARENT = "parent";

    public static final String ATTRIBUTE_CURRENT = "current";

    public static final String ATTRIBUTE_UNMARSHALLED = "unmarshalled";

    public static final String ATTRIBUTE_ROOT_CLASS_MAPPING = ClassMappingConverter.ROOT_CLASS_MAPPING_KEY;

    /**
     * Returns a <code>List of strings</code> of all the current prefixes that have been accumalted during the
     * marshalling process. Returns <code>null</code> if there are none.
     *
     * <p>Note, the use of list is done for perfomrance considerations, it should probably not be changed by
     * custom converters.
     */
    public static final String ATTRIBUTE_PREFIXES = "prefixes";
}
