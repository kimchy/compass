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

package org.compass.core.converter.mapping.osem;

import org.compass.core.Resource;
import org.compass.core.converter.ConversionException;
import org.compass.core.converter.Converter;
import org.compass.core.mapping.Mapping;
import org.compass.core.mapping.osem.ClassMapping;
import org.compass.core.mapping.osem.HasRefAliasMapping;
import org.compass.core.marshall.MarshallingContext;
import org.compass.core.marshall.MarshallingEnvironment;

/**
 * @author kimchy
 */
public abstract class AbstractRefAliasMappingConverter implements Converter {

    public boolean marshall(Resource resource, Object root, Mapping mapping, MarshallingContext context) throws ConversionException {
        // no need to marshall if it is null
        if (root == null) {
            return false;
        }
        HasRefAliasMapping hasRefAliasMapping = (HasRefAliasMapping) mapping;
        ClassMapping[] classMappings = hasRefAliasMapping.getRefClassMappings();
        ClassMapping classMapping;
        if (classMappings.length == 1) {
            classMapping = classMappings[0];
        } else {
            classMapping = context.getCompassMapping().getClassMappingByClass(root.getClass());
            if (classMapping == null) {
                throw new ConversionException("No class mapping found when marshalling root alias [" 
                        + resource.getAlias() + "] and class [" + root.getClass() + "]");
            }
            classMapping = hasRefAliasMapping.getRefClassMapping(classMapping.getAlias());
            if (classMapping == null) {
                throw new ConversionException("Mapping for root alias [" + resource.getAlias() + 
                        "] with one of its mappings with multiple ref-alias [" + hasRefAliasMapping.getRefAliases() 
                        + "] did not match [" + classMapping.getAlias() + "]");
            }
        }
        Object current = context.getAttribute(MarshallingEnvironment.ATTRIBUTE_CURRENT);
        context.setAttribute(MarshallingEnvironment.ATTRIBUTE_PARENT, current);
        return doMarshall(resource, root, hasRefAliasMapping, classMapping, context);
    }
    
    protected abstract boolean doMarshall(Resource resource, Object root, HasRefAliasMapping hasRefAliasMapping, 
            ClassMapping refMapping, MarshallingContext context) throws ConversionException;
    
    public Object unmarshall(Resource resource, Mapping mapping, MarshallingContext context) throws ConversionException {
        HasRefAliasMapping hasRefAliasMapping = (HasRefAliasMapping) mapping;
        ClassMapping[] classMappings = hasRefAliasMapping.getRefClassMappings();
        ClassMapping classMapping;
        if (classMappings.length == 1) {
            classMapping = classMappings[0];
        } else {
            throw new UnsupportedOperationException("Not supported yet");
        }
        Object current = context.getAttribute(MarshallingEnvironment.ATTRIBUTE_CURRENT);
        context.setAttribute(MarshallingEnvironment.ATTRIBUTE_PARENT, current);
        return doUnmarshall(resource, hasRefAliasMapping, classMapping, context);
    }
    
    protected abstract Object doUnmarshall(Resource resource, HasRefAliasMapping hasRefAliasMapping, 
            ClassMapping refMapping, MarshallingContext context) throws ConversionException;
}
