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

package org.compass.core.mapping.osem;

import org.compass.core.mapping.Mapping;
import org.compass.core.mapping.osem.internal.InternalLazyMapping;

/**
 * @author kimchy
 */
public class CollectionMapping extends AbstractCollectionMapping implements InternalLazyMapping {

    private Boolean lazy = false;

    public Mapping copy() {
        CollectionMapping copy = new CollectionMapping();
        super.copy(copy);
        copy.setLazy(isLazy());
        return copy;
    }

    public Boolean isLazy() {
        return lazy;
    }

    public void setLazy(Boolean lazy) {
        this.lazy = lazy;
    }
}
