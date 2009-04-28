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

package org.compass.core.test.engine;

import org.compass.core.Property;
import org.compass.core.config.CompassEnvironment;
import org.compass.core.engine.naming.PropertyPath;
import org.compass.core.engine.subindex.ConstantSubIndexHash;
import org.compass.core.engine.subindex.SubIndexHash;
import org.compass.core.mapping.AliasMapping;
import org.compass.core.mapping.AllMapping;
import org.compass.core.mapping.BoostPropertyMapping;
import org.compass.core.mapping.Cascade;
import org.compass.core.mapping.CascadeMapping;
import org.compass.core.mapping.Mapping;
import org.compass.core.mapping.ResourceAnalyzerController;
import org.compass.core.mapping.ResourceIdMappingProvider;
import org.compass.core.mapping.ResourcePropertyMapping;
import org.compass.core.mapping.SpellCheck;
import org.compass.core.mapping.internal.DefaultAllMapping;
import org.compass.core.mapping.internal.InternalAllMapping;
import org.compass.core.mapping.internal.InternalResourceMapping;
import org.compass.core.mapping.support.AbstractMultipleMapping;
import org.compass.core.util.config.ConfigurationHelper;

/**
 * @author kimchy
 */
public class MockResourceMapping extends AbstractMultipleMapping implements InternalResourceMapping, AliasMapping {

    public String[] getExtendingAliases() {
        return extendingAliases;
    }

    public void setExtendingAliases(String[] extendingAliases) {
        this.extendingAliases = extendingAliases;
    }

    private class MockResourceIdMapping implements ResourceIdMappingProvider {
        private ResourcePropertyMapping[] ids = new ResourcePropertyMapping[0];

        public void addId(ResourcePropertyMapping id) {
            ResourcePropertyMapping[] result = new ResourcePropertyMapping[ids.length + 1];
            int i = 0;
            for (i = 0; i < ids.length; i++)
                result[i] = ids[i];
            result[i] = id;
            ids = result;
        }

        public Mapping[] getIdMappings() {
            return ids;
        }

        public ResourcePropertyMapping[] getResourceIdMappings() {
            return ids;
        }
    }

    private String alias;

    private String[] extendedAliases = new String[0];

    private String[] extendingAliases = new String[0];

    private float boost = 1.0f;

    private SubIndexHash subIndexHash;

    private AllMapping allMapping = new DefaultAllMapping();

    private MockResourceIdMapping idMapping = new MockResourceIdMapping();

    public MockResourceMapping(String alias) {
        this.alias = alias;
        ((InternalAllMapping) allMapping).setExcludeAlias(false);
        ((InternalAllMapping) allMapping).setOmitNorms(false);
        ((InternalAllMapping) allMapping).setOmitTf(false);
        ((InternalAllMapping) allMapping).setSupported(true);
        ((InternalAllMapping) allMapping).setProperty(CompassEnvironment.All.DEFAULT_NAME);
        ((InternalAllMapping) allMapping).setTermVector(Property.TermVector.NO);
        ((InternalAllMapping) allMapping).setIncludePropertiesWithNoMappings(true);
    }

    public AliasMapping shallowCopy() {
        return new MockResourceMapping(alias);
    }

    public void addId(ResourcePropertyMapping id) {
        idMapping.addId(id);
    }

    public Mapping[] getIdMappings() {
        return idMapping.getIdMappings();
    }

    public ResourcePropertyMapping[] getResourceIdMappings() {
        return idMapping.getResourceIdMappings();
    }

    public SubIndexHash getSubIndexHash() {
        if (this.subIndexHash != null) {
            return this.subIndexHash;
        }
        return new ConstantSubIndexHash(getAlias());
    }

    public boolean isIncludePropertiesWithNoMappingsInAll() {
        return true;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public float getBoost() {
        return boost;
    }

    public void setBoost(float boost) {
        this.boost = boost;
    }

    public Mapping copy() {
        return null;
    }

    public boolean isRoot() {
        return true;
    }

    public String getAllProperty() {
        return null;
    }

    public Boolean isAllSupported() {
        return true;
    }

    public String[] getExtendedAliases() {
        return extendedAliases;
    }

    public void setExtendedAliases(String[] extendedMappings) {
        this.extendedAliases = extendedMappings;
    }

    public String[] getRecursiveExtendedAliases() {
        return new String[0];
    }

    public void setRecursiveExtendedAliases(String[] recursiveExtendedAliases) {
    }

    public void setSubIndexHash(SubIndexHash subIndexHash) {
        this.subIndexHash = subIndexHash;
    }

    public Property.TermVector getAllTermVector() {
        return Property.TermVector.NO;
    }

    public ConfigurationHelper getConfiguration() {
        return null;
    }

    public ResourcePropertyMapping[] getResourcePropertyMappings() {
        return new ResourcePropertyMapping[0];
    }

    public String getAnalyzer() {
        return null;
    }

    public boolean hasSpecificAnalyzerPerResourceProperty() {
        return false;
    }

    public ResourcePropertyMapping getResourcePropertyMapping(String propertyName) {
        return null;
    }

    public ResourcePropertyMapping getResourcePropertyMappingByPath(PropertyPath path) {
        return null;
    }

    public ResourcePropertyMapping[] getResourcePropertyMappings(String propertyName) {
        return new ResourcePropertyMapping[0];
    }

    public String getAllAnalyzer() {
        return null;
    }

    public ResourceAnalyzerController getAnalyzerController() {
        return null;
    }

    public BoostPropertyMapping getBoostPropertyMapping() {
        return null;
    }

    public String[] getResourcePropertyNames() {
        return new String[0];
    }

    public ResourcePropertyMapping getResourcePropertyMappingByDotPath(String path) {
        return null;
    }

    public CascadeMapping[] getCascadeMappings() {
        return null;
    }


    public boolean operationAllowed(Cascade cascade) {
        return isRoot();
    }

    public boolean isAllOmitNorms() {
        return false;
    }

    public boolean isExcludeAliasFromAll() {
        return true;
    }

    public void setUIDPath(String uid) {
        throw new IllegalStateException("Should not be called, just for testing");
    }

    public String getUIDPath() {
        return "$uid";
    }

    public void setAnalyzer(String analyzer) {
    }

    public AllMapping getAllMapping() {
        return allMapping;
    }

    public void setAllMapping(AllMapping allMapping) {
        this.allMapping = allMapping;
    }

    public void setSpellCheck(SpellCheck spellCheck) {
    }

    public SpellCheck getSpellCheck() {
        return SpellCheck.NA;
    }
}
