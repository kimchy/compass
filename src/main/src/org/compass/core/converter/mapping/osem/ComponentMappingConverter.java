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

package org.compass.core.converter.mapping.osem;

import java.util.ArrayList;
import java.util.List;

import org.compass.core.Resource;
import org.compass.core.converter.ConversionException;
import org.compass.core.mapping.osem.ClassMapping;
import org.compass.core.mapping.osem.ComponentMapping;
import org.compass.core.mapping.osem.RefAliasObjectMapping;
import org.compass.core.marshall.MarshallingContext;
import org.compass.core.marshall.MarshallingEnvironment;

/**
 * @author kimchy
 */
public class ComponentMappingConverter extends AbstractRefAliasMappingConverter {

    protected boolean doMarshall(Resource resource, Object root, RefAliasObjectMapping refAliasMapping,
                                 ClassMapping refMapping, MarshallingContext context) throws ConversionException {
        ComponentMapping componentMapping = (ComponentMapping) refAliasMapping;
        List<String> prefixes = null;
        if (componentMapping.getPrefix() != null) {
            prefixes = (List<String>) context.getAttribute(MarshallingEnvironment.ATTRIBUTE_PREFIXES);
            if (prefixes == null) {
                prefixes = new ArrayList<String>();
                context.setAttribute(MarshallingEnvironment.ATTRIBUTE_PREFIXES, prefixes);
            }
            prefixes.add(componentMapping.getPrefix());
        }
        boolean retVal = refMapping.getConverter().marshall(resource, root, refMapping, context);
        if (componentMapping.getPrefix() != null) {
            prefixes.remove(prefixes.size() - 1);
        }
        return retVal;
    }

    protected Object doUnmarshall(Resource resource, RefAliasObjectMapping refAliasMapping,
                                  ClassMapping refMapping, MarshallingContext context) throws ConversionException {
        ComponentMapping componentMapping = (ComponentMapping) refAliasMapping;
        List<String> prefixes = null;
        if (componentMapping.getPrefix() != null) {
            prefixes = (List<String>) context.getAttribute(MarshallingEnvironment.ATTRIBUTE_PREFIXES);
            if (prefixes == null) {
                prefixes = new ArrayList<String>();
                context.setAttribute(MarshallingEnvironment.ATTRIBUTE_PREFIXES, prefixes);
            }
            prefixes.add(componentMapping.getPrefix());
        }
        Object retVal = refMapping.getConverter().unmarshall(resource, refMapping, context);
        if (componentMapping.getPrefix() != null) {
            prefixes.remove(prefixes.size() - 1);
        }
        return retVal;
    }
}
