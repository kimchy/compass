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

import java.util.Iterator;

import org.compass.core.mapping.AliasMapping;
import org.compass.core.mapping.InvalidMappingException;
import org.compass.core.mapping.Mapping;
import org.compass.core.mapping.MappingException;
import org.compass.core.mapping.OverrideByNameMapping;
import org.compass.core.mapping.ResourcePropertyMapping;
import org.compass.core.mapping.support.AbstractResourceMapping;
import org.compass.core.xml.XmlXPathExpression;

/**
 * @author kimchy
 */
public class XmlObjectMapping extends AbstractResourceMapping implements XPathEnabledMapping {

    private ResourcePropertyMapping[] resourcePropertyMappings;

    private String xpath;
    
    private XmlXPathExpression xpathExpression;

    private XmlContentMapping xmlContentMapping;

    public Mapping copy() {
        XmlObjectMapping copy = new XmlObjectMapping();
        copy.setXPath(getXPath());
        copy(copy);
        return copy;
    }

    public AliasMapping shallowCopy() {
        XmlObjectMapping copy = new XmlObjectMapping();
        copy.setXPath(getXPath());
        shallowCopy(copy);
        return copy;
    }

    public int addMapping(Mapping mapping) {
        // no duplicate mapping names are allowed
        if (mapping instanceof ResourcePropertyMapping) {
            ResourcePropertyMapping resourcePropertyMapping = (ResourcePropertyMapping) mapping;
            if (mappingsByNameMap.get(resourcePropertyMapping.getName()) != null) {
                if (!(resourcePropertyMapping instanceof OverrideByNameMapping) ||
                        !((OverrideByNameMapping) resourcePropertyMapping).isOverrideByName()) {
                    throw new InvalidMappingException("Two resource property mappings are mapped to property path ["
                            + resourcePropertyMapping.getPath().getPath() + "], it is not allowed");
                }
            }
        }
        if (mapping instanceof XmlContentMapping) {
            xmlContentMapping = (XmlContentMapping) mapping;
        }
        return super.addMapping(mapping);
    }

    protected void doPostProcess() throws MappingException {
        resourcePropertyMappings = new ResourcePropertyMapping[mappingsSize()];
        int i = 0;
        for (Iterator it = mappingsIt(); it.hasNext();) {
            resourcePropertyMappings[i++] = (ResourcePropertyMapping) it.next();
        }
    }

    public ResourcePropertyMapping getResourcePropertyMappingByDotPath(String path) {
        return (ResourcePropertyMapping) mappingsByNameMap.get(path);
    }

    public ResourcePropertyMapping[] getResourcePropertyMappings() {
        return resourcePropertyMappings;
    }

    public String getXPath() {
        return xpath;
    }

    public void setXPath(String xpath) {
        this.xpath = xpath;
    }

    public XmlXPathExpression getXPathExpression() {
        return xpathExpression;
    }

    public void setXPathExpression(XmlXPathExpression xpathExpression) {
        this.xpathExpression = xpathExpression;
    }

    /**
     * Returns the xml content mapping (might be <code>null</code>).
     */
    public XmlContentMapping getXmlContentMapping() {
        return xmlContentMapping;
    }
}
