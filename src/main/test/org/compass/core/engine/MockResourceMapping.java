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

package org.compass.core.engine;

import org.compass.core.Property;
import org.compass.core.engine.naming.PropertyPath;
import org.compass.core.engine.subindex.ConstantSubIndexHash;
import org.compass.core.engine.subindex.SubIndexHash;
import org.compass.core.mapping.AbstractMultipleMapping;
import org.compass.core.mapping.AliasMapping;
import org.compass.core.mapping.Mapping;
import org.compass.core.mapping.ResourceAnalyzerController;
import org.compass.core.mapping.ResourceIdMappingProvider;
import org.compass.core.mapping.ResourceMapping;
import org.compass.core.mapping.ResourcePropertyMapping;
import org.compass.core.util.config.ConfigurationHelper;

/**
 * @author kimchy
 */
public class MockResourceMapping extends AbstractMultipleMapping implements ResourceMapping, AliasMapping {

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

        public ResourcePropertyMapping[] getIdMappings() {
            return ids;
        }
    }

    private String alias;

    private String[] extendedMappings;

    private float boost = 1.0f;

    private SubIndexHash subIndexHash;

    private MockResourceIdMapping idMapping = new MockResourceIdMapping();

    public MockResourceMapping(String alias) {
        this.alias = alias;
    }

    public AliasMapping shallowCopy() {
        return new MockResourceMapping(alias);
    }

    public void addId(ResourcePropertyMapping id) {
        idMapping.addId(id);
    }

    public ResourcePropertyMapping[] getIdMappings() {
        return idMapping.getIdMappings();
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

    public boolean isAllSupported() {
        return true;
    }

    public String[] getExtendedMappings() {
        return extendedMappings;
    }

    public void setExtendedMappings(String[] extendedMappings) {
        this.extendedMappings = extendedMappings;
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

    public String[] getResourcePropertyNames() {
        return new String[0];
    }

    public ResourcePropertyMapping getResourcePropertyMappingByDotPath(String path) {
        return null;
    }

    public void setConfiguration(ConfigurationHelper configuration) {
    }
}
