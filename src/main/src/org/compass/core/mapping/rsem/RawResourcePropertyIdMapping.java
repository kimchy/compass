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

package org.compass.core.mapping.rsem;

import org.compass.core.Property.Index;
import org.compass.core.Property.Store;
import org.compass.core.mapping.Mapping;
import org.compass.core.mapping.ResourceIdMappingProvider;
import org.compass.core.mapping.ResourcePropertyMapping;

/**
 * @author kimchy
 */
public class RawResourcePropertyIdMapping extends RawResourcePropertyMapping implements ResourceIdMappingProvider {

    public RawResourcePropertyIdMapping() {
        setOmitNorms(true);
        setOmitTf(true);
    }

    public ResourcePropertyMapping[] getIdMappings() {
        return getResourceIdMappings();
    }

    public ResourcePropertyMapping[] getResourceIdMappings() {
        return new ResourcePropertyMapping[] { this };
    }

    public Index getIndex() {
        return Index.NOT_ANALYZED;
    }
    
    public Store getStore() {
        return Store.YES;
    }

    public Mapping copy() {
        RawResourcePropertyIdMapping copy = new RawResourcePropertyIdMapping();
        copy(copy);
        return copy;
    }
}
