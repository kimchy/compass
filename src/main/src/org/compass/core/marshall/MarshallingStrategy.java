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

package org.compass.core.marshall;

import org.compass.core.Resource;
import org.compass.core.mapping.osem.ClassMapping;

/**
 * @author kimchy
 */
public interface MarshallingStrategy {

    Object unmarshall(Resource resource);

    Object unmarshall(String alias, Resource resource);

    Object unmarshall(Resource resource, MarshallingContext context);

    Object unmarshall(String alias, Resource resource, MarshallingContext context);

    Resource marshallIds(String alias, Object id);

    Resource marshallIds(Class clazz, Object id);

    Resource marshallIds(ClassMapping classMapping, Object id);

    void marshallIds(Object root, Object id);

    void marshallIds(Resource idResource, ClassMapping classMapping, Object id);

    Object[] unmarshallIds(Resource resource, ClassMapping classMapping);

    Object[] unmarshallIds(String alias, Object id);

    Object[] unmarshallIds(Class clazz, Object id);

    Object[] unmarshallIds(ClassMapping classMapping, Object id);

    Resource marshall(String alias, Object root);

    Resource marshall(Object root);

}
