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
import org.compass.core.mapping.osem.ReferenceMapping;
import org.compass.core.marshall.MarshallingContext;
import org.compass.core.marshall.MarshallingEnvironment;

/**
 * @author kimchy
 */
public class ReferenceMappingConverter implements Converter {

    public boolean marshall(Resource resource, Object root, Mapping mapping, MarshallingContext context)
            throws ConversionException {
        // no need to marshall if it is null
        if (root == null) {
            return false;
        }
        ReferenceMapping referenceMapping = (ReferenceMapping) mapping;

        Object current = context.getAttribute(MarshallingEnvironment.ATTRIBUTE_CURRENT);
        context.setAttribute(MarshallingEnvironment.ATTRIBUTE_PARENT, current);
        boolean stored = context.getMarshallingStrategy().marshallIds(resource, referenceMapping.getRefClassMapping(), root);

        if (referenceMapping.getRefCompMapping() != null) {
            context.setAttribute(MarshallingEnvironment.ATTRIBUTE_PARENT, current);
            stored |= referenceMapping.getRefCompMapping().getConverter().marshall(resource, root, referenceMapping.getRefCompMapping(), context);
        }
        return stored;
    }

    public Object unmarshall(Resource resource, Mapping mapping, MarshallingContext context) throws ConversionException {
        ReferenceMapping referenceMapping = (ReferenceMapping) mapping;
        ClassMapping classMapping = referenceMapping.getRefClassMapping();
        Object[] ids = context.getMarshallingStrategy().unmarshallIds(resource, classMapping);
        if (ids == null) {
            // the reference was not marshalled
            return null;
        }
        Object current = context.getAttribute(MarshallingEnvironment.ATTRIBUTE_CURRENT);
        context.setAttribute(MarshallingEnvironment.ATTRIBUTE_PARENT, current);
        return context.getSession().get(referenceMapping.getRefAlias(), ids, context);
    }
}
