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

package org.compass.core.mapping.osem.builder;

import org.compass.core.Property;
import org.compass.core.mapping.ExcludeFromAll;
import org.compass.core.mapping.SpellCheck;
import org.compass.core.mapping.osem.ConstantMetaDataMapping;

/**
 * A constant meta-data that can be defined on a {@link SearchableMappingBuilder} class.
 *
 * <p>A constant meta-data is a predefined name and value pair that will be
 * saved in the search engine index.
 *
 * @author kimchy
 * @see OSEM#constant(String)
 * @see SearchableMappingBuilder#add(SearchableConstantMappingBuilder)
 */
public class SearchableConstantMappingBuilder {

    final ConstantMetaDataMapping mapping;

    /**
     * Constructs a new constant metda data with the specified name.
     */
    public SearchableConstantMappingBuilder(String name) {
        mapping = new ConstantMetaDataMapping();
        mapping.setName(name);
    }

    /**
     * A list of values that the meta-data will have.
     */
    public SearchableConstantMappingBuilder values(String... values) {
        for (String value : values) {
            mapping.addMetaDataValue(value);
        }
        return this;
    }

    /**
     * Specifies whether and how a property will be stored. Deftauls to
     * {@link org.compass.core.Property.Store#YES}.
     */
    public SearchableConstantMappingBuilder store(Property.Store store) {
        mapping.setStore(store);
        return this;
    }

    /**
     * Specifies whether and how a property should be indexed. Defaults to
     * {@link org.compass.core.Property.Index#ANALYZED}.
     */
    public SearchableConstantMappingBuilder index(Property.Index index) {
        mapping.setIndex(index);
        return this;
    }

    /**
     * Specifies whether and how a property should have term vectors. Defaults to
     * {@link org.compass.core.Property.TermVector#NO}.
     */
    public SearchableConstantMappingBuilder termVector(Property.TermVector termVector) {
        mapping.setTermVector(termVector);
        return this;
    }

    /**
     * If set, omit normalization factors associated with this indexed field.
     * This effectively disables indexing boosts and length normalization for this field. Defaults
     * to <code>false</code>.
     */
    public SearchableConstantMappingBuilder omitNorms(boolean omitNorms) {
        mapping.setOmitNorms(omitNorms);
        return this;
    }

    /**
     * If set, omit tf from postings of this indexed property. Defaults to <code>false</code>.
     */
    public SearchableConstantMappingBuilder omitTf(boolean omitTf) {
        mapping.setOmitTf(omitTf);
        return this;
    }

    /**
     * Sets the boost value for the property mapping. Defaults to <code>1.0f</code>.
     */
    public SearchableConstantMappingBuilder boost(float boost) {
        mapping.setBoost(boost);
        return this;
    }

    /**
     * Controls if the property will be excluded from all or not.
     */
    public SearchableConstantMappingBuilder excludeFromAll(ExcludeFromAll excludeFromAll) {
        mapping.setExcludeFromAll(excludeFromAll);
        return this;
    }

    /**
     * Sets if this mapping will override another mapping with the same name. Defaults to
     * <code>true</code>.
     */
    public SearchableConstantMappingBuilder overrideByName(boolean override) {
        mapping.setOverrideByName(override);
        return this;
    }

    /**
     * Sets the analyzer logical name that will be used to analyzer the property value. The name
     * is a lookup name for an Analyzer that is registered with Compass.
     */
    public SearchableConstantMappingBuilder analyzer(String analyzer) {
        mapping.setAnalyzer(analyzer);
        return this;
    }

    /**
     * Sets the null value of the property. If the property value is null, will store in the
     * index the provided value.
     */
    public SearchableConstantMappingBuilder nullValue(String nullValue) {
        mapping.setNullValue(nullValue);
        return this;
    }

    /**
     * Sets the spell check specific setting for the mapping.
     */
    public SearchableConstantMappingBuilder spellCheck(SpellCheck spellCheck) {
        mapping.setSpellCheck(spellCheck);
        return this;
    }
}
