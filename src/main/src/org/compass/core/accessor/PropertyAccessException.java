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

package org.compass.core.accessor;

import org.compass.core.CompassException;
import org.compass.core.util.StringUtils;

/**
 * @author kimchy
 */
public class PropertyAccessException extends CompassException {

    private static final long serialVersionUID = 3257002142513772083L;

    private final Class persistentClass;

    private final String propertyName;

    private final boolean wasSetter;

    public PropertyAccessException(Throwable root, String s, boolean wasSetter, Class persistentClass,
                                   String propertyName) {
        super(s, root);
        this.persistentClass = persistentClass;
        this.wasSetter = wasSetter;
        this.propertyName = propertyName;
    }

    public Class getPersistentClass() {
        return persistentClass;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public String getMessage() {
        return "[" + super.getMessage() + "]" + (wasSetter ? " setter of " : " getter of ")
                + StringUtils.qualify(persistentClass.getName(), propertyName);
    }
}
