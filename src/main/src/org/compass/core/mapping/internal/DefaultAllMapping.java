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

package org.compass.core.mapping.internal;

import org.compass.core.Property;
import org.compass.core.mapping.AllMapping;
import org.compass.core.mapping.SpellCheckType;

/**
 * A set of settings configuring the all mapping.
 *
 * @author kimchy
 */
public class DefaultAllMapping implements InternalAllMapping {

    private Boolean supported;

    private Boolean excludeAlias;

    private String property;

    private Property.TermVector termVector;

    private Boolean omitNorms;

    private SpellCheckType spellCheck;

    private boolean includePropertiesWithNoMappings = true;

    public AllMapping copy() {
        DefaultAllMapping allMapping = new DefaultAllMapping();
        allMapping.setExcludeAlias(isExcludeAlias());
        allMapping.setIncludePropertiesWithNoMappings(isIncludePropertiesWithNoMappings());
        allMapping.setOmitNorms(isOmitNorms());
        allMapping.setProperty(getProperty());
        allMapping.setSupported(isSupported());
        allMapping.setTermVector(getTermVector());
        allMapping.setSpellCheck(getSpellCheck());
        return allMapping;
    }

    public Boolean isSupported() {
        return supported;
    }

    public void setSupported(Boolean supported) {
        this.supported = supported;
    }

    public Boolean isExcludeAlias() {
        return excludeAlias;
    }

    public void setExcludeAlias(Boolean excludeAlias) {
        this.excludeAlias = excludeAlias;
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public Property.TermVector getTermVector() {
        return termVector;
    }

    public void setTermVector(Property.TermVector termVector) {
        this.termVector = termVector;
    }

    public Boolean isOmitNorms() {
        return omitNorms;
    }

    public void setOmitNorms(Boolean omitNorms) {
        this.omitNorms = omitNorms;
    }

    public boolean isIncludePropertiesWithNoMappings() {
        return includePropertiesWithNoMappings;
    }

    public void setIncludePropertiesWithNoMappings(boolean includePropertiesWithNoMappings) {
        this.includePropertiesWithNoMappings = includePropertiesWithNoMappings;
    }

    public SpellCheckType getSpellCheck() {
        return spellCheck;
    }

    public void setSpellCheck(SpellCheckType spellCheck) {
        this.spellCheck = spellCheck;
    }
}
