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

import java.util.Map;

import org.compass.core.Property;
import org.compass.core.Resource;
import org.compass.core.ResourceFactory;
import org.compass.core.converter.ConversionException;
import org.compass.core.converter.mapping.osem.collection.LazyReferenceEntry;
import org.compass.core.mapping.Mapping;
import org.compass.core.mapping.osem.ClassMapping;
import org.compass.core.mapping.osem.RefAliasObjectMapping;
import org.compass.core.mapping.osem.ReferenceMapping;
import org.compass.core.marshall.MarshallingContext;
import org.compass.core.marshall.MarshallingEnvironment;

/**
 * @author kimchy
 */
public class ReferenceMappingConverter extends AbstractRefAliasMappingConverter {

    private static final Object DISABLE_UID_MARK = new Object();

    protected boolean doMarshall(Resource resource, Object root, RefAliasObjectMapping hasRefAliasMapping,
                                 ClassMapping refMapping, MarshallingContext context) throws ConversionException {
        Object current = context.getAttribute(MarshallingEnvironment.ATTRIBUTE_CURRENT);

        ReferenceMapping referenceMapping = (ReferenceMapping) hasRefAliasMapping;

        // handle null values when needed
        if (root == null) {
            if (!refMapping.isSupportUnmarshall()) {
                return false;
            }
            if (!context.handleNulls()) {
                return false;
            }
            Mapping[] ids = refMapping.getResourceIdMappings();
            boolean store = false;
            for (Mapping id : ids) {
                store |= id.getConverter().marshall(resource, context.getResourceFactory().getNullValue(), id, context);
            }
            return store;
        }

        // only add specilized properties for un-marshalling when it is supported
        if (refMapping.isSupportUnmarshall()) {
            ResourceFactory resourceFactory = context.getResourceFactory();
            if (refMapping.isPoly() && refMapping.getPolyClass() == null) {
                // if the class is defined as poly, persist the class name as well
                String className = root.getClass().getName();
                Property p = resourceFactory.createProperty(refMapping.getClassPath().getPath(), className, Property.Store.YES,
                        Property.Index.NOT_ANALYZED);
                p.setOmitNorms(true);
                p.setOmitTf(true);
                resource.addProperty(p);
            }
        }
        context.setAttribute(ClassMappingConverter.DISABLE_UID_MARSHALLING, DISABLE_UID_MARK);
        boolean stored = context.getMarshallingStrategy().marshallIds(resource, refMapping, root, context);
        context.removeAttribute(ClassMappingConverter.DISABLE_UID_MARSHALLING);

        if (referenceMapping.getRefCompMapping() != null) {
            context.setAttribute(MarshallingEnvironment.ATTRIBUTE_PARENT, current);
            stored |= referenceMapping.getRefCompMapping().getConverter().marshall(resource, root, referenceMapping.getRefCompMapping(), context);
        }
        return stored;
    }

    protected Object doUnmarshall(Resource resource, RefAliasObjectMapping hasRefAliasMapping,
                                  ClassMapping refMapping, MarshallingContext context) throws ConversionException {
        Object[] ids = context.getMarshallingStrategy().unmarshallIds(refMapping, resource, context);
        if (ids == null) {
            // the reference was not marshalled
            return null;
        }
        ReferenceMapping referenceMapping = (ReferenceMapping) hasRefAliasMapping;
        if (referenceMapping.isLazy()) {
            return new LazyReferenceEntry(refMapping.getAlias(), ids);
        } else {
            Map<Object, Object> attributes = context.removeAttributes();
            Object retVal = context.getSession().get(refMapping.getAlias(), ids, context);
            context.restoreAttributes(attributes);
            return retVal;
        }
    }


    protected boolean rollbackClassNameOnPoly() {
        return false;
    }
}
