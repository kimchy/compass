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

package org.compass.core.mapping.json;

import org.compass.core.mapping.AliasMapping;
import org.compass.core.mapping.Mapping;
import org.compass.core.mapping.MappingException;
import org.compass.core.mapping.ResourcePropertyMapping;
import org.compass.core.mapping.support.AbstractResourceMapping;

/**
 * @author kimchy
 */
//TODO support recursive mappings using object (or array)
//TODO support appending names based on deep level names
public class JsonObjectMapping extends AbstractResourceMapping {

    public Mapping copy() {
        JsonObjectMapping copy = new JsonObjectMapping();
        super.copy(copy);
        return copy;
    }

    public AliasMapping shallowCopy() {
        JsonObjectMapping copy = new JsonObjectMapping();
        super.shallowCopy(copy);
        return copy;
    }

    protected void doPostProcess() throws MappingException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public ResourcePropertyMapping[] getResourcePropertyMappings() {
        return new ResourcePropertyMapping[0];  //To change body of implemented methods use File | Settings | File Templates.
    }

    public ResourcePropertyMapping getResourcePropertyMappingByDotPath(String path) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
