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

import org.compass.core.Property;
import org.compass.core.converter.mapping.ResourcePropertyConverter;
import org.compass.core.mapping.ExcludeFromAll;
import org.compass.core.mapping.ReverseType;
import org.compass.core.mapping.SpellCheck;
import org.compass.core.mapping.internal.InternalResourcePropertyMapping;

/**
 * @author kimchy
 */
public abstract class AbstractResourcePropertyMapping extends AbstractMapping implements InternalResourcePropertyMapping {

    private String originalName;

    private String rootAlias;

    private String analyzer;

    private float boost = 1.0f;

    private Property.Store store;

    private Property.Index index;

    private Property.TermVector termVector;

    private boolean isInternal = false;

    private ExcludeFromAll excludeFromAll = ExcludeFromAll.NO;

    private SpellCheck spellCheck = SpellCheck.NA;

    private Boolean omitNorms = false;

    private Boolean omitTf = false;

    private ReverseType reverse = ReverseType.NO;

    private String nullValue = "";

    protected void copy(AbstractResourcePropertyMapping copy) {
        super.copy(copy);
        copy.originalName = originalName;
        copy.setBoost(getBoost());
        copy.setName(getName());
        copy.setStore(getStore());
        copy.setIndex(getIndex());
        copy.setPath(getPath());
        copy.setInternal(isInternal());
        copy.setExcludeFromAll(getExcludeFromAll());
        copy.setTermVector(getTermVector());
        copy.setAnalyzer(getAnalyzer());
        copy.setReverse(getReverse());
        copy.setOmitNorms(isOmitNorms());
        copy.setOmitTf(isOmitTf());
        copy.setRootAlias(getRootAlias());
        copy.setNullValue(getNullValue());
        copy.setSpellCheck(getSpellCheck());
    }

    @Override
    public void setName(String name) {
        if (name != null && originalName == null) {
            if (!name.equals(getName())) {
                originalName = getName();
            }
        }
        super.setName(name);
    }

    public String getOriginalName() {
        if (originalName != null) {
            return this.originalName;
        }
        return getName();
    }

    public String getRootAlias() {
        return rootAlias;
    }

    public void setRootAlias(String rootAlias) {
        this.rootAlias = rootAlias;
    }

    public float getBoost() {
        return this.boost;
    }

    public void setBoost(float boost) {
        this.boost = boost;
    }

    public Property.Store getStore() {
        return store;
    }

    public void setStore(Property.Store store) {
        this.store = store;
    }

    public Property.Index getIndex() {
        return index;
    }

    public void setIndex(Property.Index index) {
        this.index = index;
    }

    public boolean isInternal() {
        return isInternal;
    }

    public void setInternal(boolean isInternal) {
        this.isInternal = isInternal;
    }

    public ExcludeFromAll getExcludeFromAll() {
        return excludeFromAll;
    }

    public void setExcludeFromAll(ExcludeFromAll excludeFromAll) {
        this.excludeFromAll = excludeFromAll;
    }

    public Property.TermVector getTermVector() {
        return termVector;
    }

    public void setTermVector(Property.TermVector termVector) {
        this.termVector = termVector;
    }

    public String getAnalyzer() {
        return analyzer;
    }

    public void setAnalyzer(String analyzer) {
        this.analyzer = analyzer;
    }

    public ReverseType getReverse() {
        return reverse;
    }

    public void setReverse(ReverseType reverse) {
        this.reverse = reverse;
    }

    public Boolean isOmitNorms() {
        return omitNorms;
    }

    public void setOmitNorms(Boolean omitNorms) {
        this.omitNorms = omitNorms;
    }

    public String getNullValue() {
        return nullValue;
    }

    public Boolean isOmitTf() {
        return omitTf;
    }

    public void setOmitTf(Boolean omitTf) {
        this.omitTf = omitTf;
    }

    public void setNullValue(String nullValue) {
        if (nullValue == null) {
            this.nullValue = "";
        } else {
            this.nullValue = nullValue;
        }
    }

    public boolean hasNullValue() {
        return nullValue.length() > 0;
    }

    public SpellCheck getSpellCheck() {
        return spellCheck;
    }

    public void setSpellCheck(SpellCheck spellCheck) {
        this.spellCheck = spellCheck;
    }

    public ResourcePropertyConverter getResourcePropertyConverter() {
        if (getConverter() instanceof ResourcePropertyConverter) {
            return (ResourcePropertyConverter) getConverter();
        }
        return null;
    }
}
