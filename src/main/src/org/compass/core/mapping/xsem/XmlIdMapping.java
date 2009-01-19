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

package org.compass.core.mapping.xsem;

import org.compass.core.Property;
import org.compass.core.mapping.Mapping;
import org.compass.core.mapping.ResourceIdMappingProvider;
import org.compass.core.mapping.ResourcePropertyMapping;

/**
 * @author kimchy
 */
public class XmlIdMapping extends XmlPropertyMapping implements ResourceIdMappingProvider {

    public XmlIdMapping() {
        setOmitNorms(true);
        setOmitTf(true);
    }

    public Mapping[] getIdMappings() {
        return getResourceIdMappings();
    }

    public ResourcePropertyMapping[] getResourceIdMappings() {
        return new ResourcePropertyMapping[]{this};
    }

    public Property.Index getIndex() {
        return Property.Index.NOT_ANALYZED;
    }

    public Property.Store getStore() {
        return Property.Store.YES;
    }

    public boolean isOverrideByName() {
        return true;
    }

    public Mapping copy() {
        XmlIdMapping copy = new XmlIdMapping();
        copy(copy);
        return copy;
    }

}
