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

package org.compass.core.mapping.support;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.compass.core.engine.naming.PropertyPath;
import org.compass.core.engine.subindex.SubIndexHash;
import org.compass.core.mapping.AliasMapping;
import org.compass.core.mapping.AllMapping;
import org.compass.core.mapping.BoostPropertyMapping;
import org.compass.core.mapping.Cascade;
import org.compass.core.mapping.CascadeMapping;
import org.compass.core.mapping.Mapping;
import org.compass.core.mapping.MappingException;
import org.compass.core.mapping.ResourceAnalyzerController;
import org.compass.core.mapping.ResourceIdMappingProvider;
import org.compass.core.mapping.ResourcePropertyMapping;
import org.compass.core.mapping.SpellCheck;
import org.compass.core.mapping.internal.DefaultAllMapping;
import org.compass.core.mapping.internal.InternalResourceMapping;
import org.compass.core.mapping.internal.PostProcessingMapping;

/**
 * @author kimchy
 */
public abstract class AbstractResourceMapping extends AbstractMultipleMapping implements InternalResourceMapping,
        AliasMapping, PostProcessingMapping {

    private String alias;

    private SubIndexHash subIndexHash;

    private String[] extendedAliases = new String[0];

    private String[] recursiveExtendedAliases = new String[0];

    private String[] extendingAliases = new String[0];

    private String analyzer;

    private float boost = 1.0f;

    private boolean isRoot = true;

    private AllMapping allMapping = new DefaultAllMapping();

    private SpellCheck spellCheck = SpellCheck.NA;

    private String uidProperty;

    private Mapping[] idMappings;

    private ResourcePropertyMapping[] idPropertyMappings;

    private CascadeMapping[] cascades;

    private Map<String, ResourcePropertyMapping[]> resourcePropertyMappingsByNameMap;

    private Map<PropertyPath, ResourcePropertyMapping> resourcePropertyMappingsByPathMap;

    private String[] resourcePropertyNames;

    private boolean hasSpecificAnalyzerPerResourceProperty;

    private ResourceAnalyzerController analyzerController;

    private BoostPropertyMapping boostPropertyMapping;

    /**
     * Gets the idMappings of the resource.
     */
    public Mapping[] getIdMappings() {
        if (idMappings == null) {
            buildResourceIds();
        }
        return idMappings;
    }

    public AllMapping getAllMapping() {
        return allMapping;
    }

    public void setAllMapping(AllMapping allMapping) {
        this.allMapping = allMapping;
    }

    public SpellCheck getSpellCheck() {
        return spellCheck;
    }

    public void setSpellCheck(SpellCheck spellCheck) {
        this.spellCheck = spellCheck;
    }

    public ResourcePropertyMapping[] getResourceIdMappings() {
        if (idPropertyMappings == null) {
            buildResourceIds();
        }
        return idPropertyMappings;
    }

    protected void copy(AbstractResourceMapping resourceMapping) {
        super.copy(resourceMapping);
        shallowCopy(resourceMapping);
    }

    public void shallowCopy(AbstractResourceMapping resourceMapping) {
        super.shallowCopy(resourceMapping);
        resourceMapping.setAlias(getAlias());
        resourceMapping.setSubIndexHash(getSubIndexHash());
        resourceMapping.setExtendedAliases(getExtendedAliases());
        resourceMapping.setRecursiveExtendedAliases(getRecursiveExtendedAliases());
        resourceMapping.setExtendingAliases(getExtendingAliases());
        resourceMapping.setRoot(isRoot());
        resourceMapping.setBoost(getBoost());
        resourceMapping.setAnalyzer(getAnalyzer());
        resourceMapping.setUIDPath(getUIDPath());
        resourceMapping.setAllMapping(getAllMapping().copy());
        resourceMapping.setSpellCheck(getSpellCheck());
        if (boostPropertyMapping != null) {
            resourceMapping.boostPropertyMapping = (BoostPropertyMapping) boostPropertyMapping.copy();
        }
        if (analyzerController != null) {
            resourceMapping.analyzerController = (ResourceAnalyzerController) analyzerController.copy();
        }
    }

    public void postProcess() throws MappingException {
        doPostProcess();
        buildResourcePropertyMap();
        buildAnalyzerSpecificFlag();
        buildResourceIds();
    }

    protected abstract void doPostProcess() throws MappingException;

    private void buildResourceIds() {
        ArrayList<Mapping> resourceIds = new ArrayList<Mapping>();
        ArrayList<ResourcePropertyMapping> resourceIdPropertyMappings = new ArrayList<ResourcePropertyMapping>();
        for (Iterator it = mappingsIt(); it.hasNext();) {
            Mapping mapping = (Mapping) it.next();
            if (mapping instanceof ResourceIdMappingProvider) {
                Mapping[] tempIds = ((ResourceIdMappingProvider) mapping).getIdMappings();
                for (Mapping tempId : tempIds) {
                    if (tempId != null) {
                        resourceIds.add(tempId);
                    }
                }
                ResourcePropertyMapping[] tempPropertyIds = ((ResourceIdMappingProvider) mapping).getResourceIdMappings();
                for (ResourcePropertyMapping tempPropertyId : tempPropertyIds) {
                    if (tempPropertyId !=  null) {
                        resourceIdPropertyMappings.add(tempPropertyId);
                    }
                }
            }
        }
        idMappings = resourceIds.toArray(new Mapping[resourceIds.size()]);
        idPropertyMappings = resourceIdPropertyMappings.toArray(new ResourcePropertyMapping[resourceIdPropertyMappings.size()]);
    }

    private void buildResourcePropertyMap() {
        resourcePropertyMappingsByPathMap = new HashMap<PropertyPath, ResourcePropertyMapping>();
        HashMap<String, ArrayList<ResourcePropertyMapping>> tempMap = new HashMap<String, ArrayList<ResourcePropertyMapping>>();

        ResourcePropertyMapping[] resourcePropertyMappings = getResourcePropertyMappings();
        for (ResourcePropertyMapping resourcePropertyMapping : resourcePropertyMappings) {
            resourcePropertyMappingsByPathMap.put(resourcePropertyMapping.getPath(), resourcePropertyMapping);

            ArrayList<ResourcePropertyMapping> propertyList = tempMap.get(resourcePropertyMapping.getName());
            if (propertyList == null) {
                propertyList = new ArrayList<ResourcePropertyMapping>();
                tempMap.put(resourcePropertyMapping.getName(), propertyList);
            }
            propertyList.add(resourcePropertyMapping);
        }
        resourcePropertyNames = new String[tempMap.size()];
        int i = 0;
        resourcePropertyMappingsByNameMap = new HashMap<String, ResourcePropertyMapping[]>();
        for (Map.Entry<String, ArrayList<ResourcePropertyMapping>> entry : tempMap.entrySet()) {
            String propertyName = entry.getKey();
            resourcePropertyNames[i++] = propertyName;
            ArrayList<ResourcePropertyMapping> propertyList = entry.getValue();
            resourcePropertyMappingsByNameMap.put(propertyName,
                    propertyList.toArray(new ResourcePropertyMapping[propertyList.size()]));
        }
    }

    private void buildAnalyzerSpecificFlag() {
        for (String propertyName : resourcePropertyMappingsByNameMap.keySet()) {
            ResourcePropertyMapping[] mappings = resourcePropertyMappingsByNameMap.get(propertyName);
            // the system will validate in the mappings if there are different
            // analyzers (or null) set to the same property mapping, here we can
            // just use the first one
            if (mappings[0].getAnalyzer() != null) {
                // no need to say we have a specific analyzer if it is the same as the one set on the resource
                if (analyzer == null || !analyzer.equals(mappings[0].getAnalyzer())) {
                    hasSpecificAnalyzerPerResourceProperty = true;
                }
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
        if (mapping instanceof BoostPropertyMapping) {
            boostPropertyMapping = (BoostPropertyMapping) mapping;
        }
        return super.addMapping(mapping);
    }

    public ResourcePropertyMapping[] getResourcePropertyMappings(String propertyName) {
        return resourcePropertyMappingsByNameMap.get(propertyName);
    }

    public ResourcePropertyMapping getResourcePropertyMapping(String propertyName) {
        ResourcePropertyMapping[] retVal = getResourcePropertyMappings(propertyName);
        if (retVal == null) {
            return null;
        }
        return retVal[0];
    }

    public ResourcePropertyMapping getResourcePropertyMappingByPath(PropertyPath path) {
        return resourcePropertyMappingsByPathMap.get(path);
    }

    public CascadeMapping[] getCascadeMappings() {
        return cascades;
    }

    public boolean operationAllowed(Cascade cascade) {
        if (isRoot()) {
            return true;
        }
        if (cascades == null || cascades.length == 0) {
            return false;
        }
        for (CascadeMapping cascade1 : cascades) {
            if (cascade1.shouldCascade(cascade)) {
                return true;
            }
        }
        return false;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String[] getExtendedAliases() {
        return extendedAliases;
    }

    public void setExtendedAliases(String[] extendedMappings) {
        this.extendedAliases = extendedMappings;
    }

    public String[] getRecursiveExtendedAliases() {
        return recursiveExtendedAliases;
    }

    public void setRecursiveExtendedAliases(String[] recursiveExtendedAliases) {
        this.recursiveExtendedAliases = recursiveExtendedAliases;
    }

    public void setUIDPath(String uid) {
        this.uidProperty = uid;
    }

    public String getUIDPath() {
        return this.uidProperty;
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

    public SubIndexHash getSubIndexHash() {
        return subIndexHash;
    }

    public void setSubIndexHash(SubIndexHash subIndexHash) {
        this.subIndexHash = subIndexHash;
    }

    public boolean hasSpecificAnalyzerPerResourceProperty() {
        return hasSpecificAnalyzerPerResourceProperty;
    }

    public ResourceAnalyzerController getAnalyzerController() {
        return analyzerController;
    }

    public void setAnalyzerController(ResourceAnalyzerController analyzerController) {
        this.analyzerController = analyzerController;
    }

    public BoostPropertyMapping getBoostPropertyMapping() {
        return boostPropertyMapping;
    }

    public void setBoostPropertyMapping(BoostPropertyMapping boostPropertyMapping) {
        this.boostPropertyMapping = boostPropertyMapping;
    }

    public String[] getResourcePropertyNames() {
        return resourcePropertyNames;
    }

    public String[] getExtendingAliases() {
        return extendingAliases;
    }

    public void setExtendingAliases(String[] extendingAliases) {
        this.extendingAliases = extendingAliases;
    }

    public void setCascades(CascadeMapping[] cascades) {
        this.cascades = cascades;
    }
}
