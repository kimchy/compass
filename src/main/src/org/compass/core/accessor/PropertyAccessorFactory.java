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

package org.compass.core.accessor;

import java.util.Map;

import org.compass.core.mapping.MappingException;
import org.compass.core.util.ClassUtils;

/**
 * A factory that creates a PropertyAccessor.
 * <p>
 * Initial version taken from hibernate.
 * </p>
 * 
 * @author kimchy
 */
public final class PropertyAccessorFactory {

    private static final PropertyAccessor BASIC_PROPERTY_ACCESSOR = new BasicPropertyAccessor();

    private static final PropertyAccessor DIRECT_PROPERTY_ACCESSOR = new DirectPropertyAccessor();

    public static PropertyAccessor getPropertyAccessor(Class optionalClass, String type) throws MappingException {
        if (type == null)
            type = optionalClass == null || optionalClass == Map.class ? "map" : "property";
        return getPropertyAccessor(type);
    }

    public static PropertyAccessor getPropertyAccessor(String type) throws MappingException {

        if (type == null || "property".equals(type))
            return BASIC_PROPERTY_ACCESSOR;
        if ("field".equals(type))
            return DIRECT_PROPERTY_ACCESSOR;

        Class accessorClass;
        try {
            accessorClass = ClassUtils.forName(type);
        } catch (ClassNotFoundException cnfe) {
            throw new MappingException("could not find PropertyAccessor class: " + type, cnfe);
        }
        try {
            return (PropertyAccessor) accessorClass.newInstance();
        } catch (Exception e) {
            throw new MappingException("could not instantiate PropertyAccessor class: " + type, e);
        }

    }

    private PropertyAccessorFactory() {
    }
}
