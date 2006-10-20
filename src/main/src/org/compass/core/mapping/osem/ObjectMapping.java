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

package org.compass.core.mapping.osem;

import org.compass.core.accessor.Getter;
import org.compass.core.accessor.Setter;

/**
 * 
 * @author kimchy
 *
 */
public interface ObjectMapping extends OsemMapping {

    Class getObjClass();

    void setObjClass(Class objClass);

    String getAccessor();

    void setAccessor(String accessor);

    String getPropertyName();

    void setPropertyName(String propertyName);

    /**
     * Returns which alias (or if not present, the FQN of the class name)
     * this object property is defined.
     */
    String getDefinedInAlias();
    
    void setDefinedInAlias(String alias);

    Getter getGetter();

    void setGetter(Getter getter);

    Setter getSetter();

    void setSetter(Setter setter);

    boolean canBeCollectionWrapped();
}
