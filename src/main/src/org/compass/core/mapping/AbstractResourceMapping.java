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

package org.compass.core.mapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.compass.core.Property;
import org.compass.core.engine.naming.PropertyPath;
import org.compass.core.engine.subindex.SubIndexHash;

/**
 * @author kimchy
 */
public abstract class AbstractResourceMapping extends AbstractMultipleMapping implements ResourceMapping,
        AliasMapping, PostProcessingMapping {

    private String alias;

    private SubIndexHash subIndexHash;

    private String[] extendedMappings;

    private String analyzer;

    private float boost;

    private boolean isRoot = true;

    private boolean isAllSupported;

    private String allProperty;

    private Property.TermVector allTermVector = Property.TermVector.NO;

    private String allAnalyzer;

    private ResourcePropertyMapping[] ids = new ResourcePropertyMapping[0];

    private Map resourcePropertyMappingsByNameMap;

    private Map resourcePropertyMappingsByPathMap;

    private String[] resourcePropertyNames;

    private boolean hasSpecificAnalyzerPerResourceProperty;

    private ResourceAnalyzerController analyzerController;

    public ResourcePropertyMapping[] getIdMappings() {
        return ids;
    }

    protected void copy(AbstractResourceMapping resourceMapping) {
        super.copy(resourceMapping);
        shallowCopy(resourceMapping);
    }

    public void shallowCopy(AbstractResourceMapping resourceMapping) {
        super.shallowCopy(resourceMapping);
        resourceMapping.setAlias(getAlias());
        resourceMapping.setSubIndexHash(getSubIndexHash());
        resourceMapping.setExtendedMappings(getExtendedMappings());
        resourceMapping.setAllProperty(getAllProperty());
        resourceMapping.setAllSupported(isAllSupported());
        resourceMapping.setRoot(isRoot());
        resourceMapping.setBoost(getBoost());
        resourceMapping.setAllTermVector(getAllTermVector());
        resourceMapping.setAnalyzer(getAnalyzer());
        resourceMapping.setAllAnalyzer(getAllAnalyzer());
    }

    public void postProcess() throws MappingException {
        doPostProcess();
        buildResourcePropertyMap();
        buildAnalyzerSpecificFlag();
        buildResourceIds();
    }

    protected abstract void doPostProcess() throws MappingException;
    
    private void buildResourceIds() {
        ArrayList resourceIds = new ArrayList();
        for (Iterator it = mappingsIt(); it.hasNext();) {
            Mapping mapping = (Mapping) it.next();
            if (mapping instanceof ResourceIdMappingProvider) {
                ResourcePropertyMapping[] tempIds = ((ResourceIdMappingProvider) mapping).getIdMappings();
                for (int i = 0; i < tempIds.length; i++) {
                    resourceIds.add(tempIds[i]);
                }
            }
        }
        ids = (ResourcePropertyMapping[]) resourceIds.toArray(new ResourcePropertyMapping[resourceIds.size()]);
    }

    private void buildResourcePropertyMap() {
        resourcePropertyMappingsByPathMap = new HashMap();
        HashMap tempMap = new HashMap();
        ResourcePropertyMapping[] resourcePropertyMappings = getResourcePropertyMappings();
        for (int i = 0; i < resourcePropertyMappings.length; i++) {
            ResourcePropertyMapping resourcePropertyMapping = resourcePropertyMappings[i];

            resourcePropertyMappingsByPathMap.put(resourcePropertyMapping.getPath(), resourcePropertyMapping);

            ArrayList propertyList = (ArrayList) tempMap.get(resourcePropertyMapping.getName());
            if (propertyList == null) {
                propertyList = new ArrayList();
                tempMap.put(resourcePropertyMapping.getName(), propertyList);
            }
            propertyList.add(resourcePropertyMapping);
        }
        resourcePropertyNames = new String[tempMap.size()];
        int i = 0;
        resourcePropertyMappingsByNameMap = new HashMap();
        for (Iterator it = tempMap.entrySet().iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry) it.next();
            String propertyName = (String) entry.getKey();
            resourcePropertyNames[i++] = propertyName;
            ArrayList propertyList = (ArrayList) entry.getValue();
            resourcePropertyMappingsByNameMap.put(propertyName,
                    propertyList.toArray(new ResourcePropertyMapping[propertyList.size()]));
        }
    }

    private void buildAnalyzerSpecificFlag() {
        for (Iterator it = resourcePropertyMappingsByNameMap.keySet().iterator(); it.hasNext();) {
            String propertyName = (String) it.next();
            ResourcePropertyMapping[] mappings = (ResourcePropertyMapping[]) resourcePropertyMappingsByNameMap
                    .get(propertyName);
            // the system will validate in the mappings if there are different
            // analyzers (or null) set to the same property mapping, here we can
            // just use the first one
            if (mappings[0].getAnalyzer() != null) {
                hasSpecificAnalyzerPerResourceProperty = true;
            }
        }
    }

    /**
     * No duplicate names are allowed when added an id (applies the property
     * names)
     */
    public int addMapping(Mapping mapping) {
        if (mapping instanceof ResourceAnalyzerController) {
            analyzerController = (ResourceAnalyzerController) mapping;
        }
        return super.addMapping(mapping);
    }

    public ResourcePropertyMapping[] getResourcePropertyMappings(String propertyName) {
        return (ResourcePropertyMapping[]) resourcePropertyMappingsByNameMap.get(propertyName);
    }

    public ResourcePropertyMapping getResourcePropertyMapping(String propertyName) {
        ResourcePropertyMapping[] retVal = getResourcePropertyMappings(propertyName);
        if (retVal == null) {
            return null;
        }
        return retVal[0];
    }

    public ResourcePropertyMapping getResourcePropertyMappingByPath(PropertyPath path) {
        return (ResourcePropertyMapping) resourcePropertyMappingsByPathMap.get(path);
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String[] getExtendedMappings() {
        return extendedMappings;
    }

    public void setExtendedMappings(String[] extendedMappings) {
        this.extendedMappings = extendedMappings;
    }

    public float getBoost() {
        return boost;
    }

    public void setBoost(float boost) {
        this.boost = boost;
    }

    public String getAnalyzer() {
        return analyzer;
    }

    public void setAnalyzer(String analyzer) {
        this.analyzer = analyzer;
    }

    public boolean isRoot() {
        return isRoot;
    }

    public void setRoot(boolean isRoot) {
        this.isRoot = isRoot;
    }

    public String getAllProperty() {
        return allProperty;
    }

    public void setAllProperty(String allProperty) {
        this.allProperty = allProperty;
    }

    public boolean isAllSupported() {
        return isAllSupported;
    }

    public void setAllSupported(boolean isAllSupported) {
        this.isAllSupported = isAllSupported;
    }

    public SubIndexHash getSubIndexHash() {
        return subIndexHash;
    }

    public void setSubIndexHash(SubIndexHash subIndexHash) {
        this.subIndexHash = subIndexHash;
    }

    public Property.TermVector getAllTermVector() {
        return allTermVector;
    }

    public void setAllTermVector(Property.TermVector allTermVector) {
        this.allTermVector = allTermVector;
    }

    public boolean hasSpecificAnalyzerPerResourceProperty() {
        return hasSpecificAnalyzerPerResourceProperty;
    }

    public String getAllAnalyzer() {
        return allAnalyzer;
    }

    public void setAllAnalyzer(String allAnalyzer) {
        this.allAnalyzer = allAnalyzer;
    }

    public ResourceAnalyzerController getAnalyzerController() {
        return analyzerController;
    }

    public void setAnalyzerController(ResourceAnalyzerController analyzerController) {
        this.analyzerController = analyzerController;
    }

    public String[] getResourcePropertyNames() {
        return resourcePropertyNames;
    }

}
