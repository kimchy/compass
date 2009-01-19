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

package org.compass.core.mapping.xsem.builder;

import org.compass.core.Property;
import org.compass.core.converter.Converter;
import org.compass.core.converter.mapping.ResourcePropertyConverter;
import org.compass.core.converter.mapping.support.FormatDelegateConverter;
import org.compass.core.engine.naming.StaticPropertyPath;
import org.compass.core.mapping.ExcludeFromAll;
import org.compass.core.mapping.SpellCheck;
import org.compass.core.mapping.xsem.XmlPropertyMapping;

/**
 * A builder allowing to constrcut XML property mapping definition.
 *
 * @author kimchy
 * @see XSEM#property(String)
 */
public class XmlPropertyMappingBuilder {

    final XmlPropertyMapping mapping;

    /**
     * Constructs a new xml property mapping builder for the specified xpath expression.
     *
     * <p>Note, the xpath expression used to identify the xml-property. Can return no xml objects, one xml object, or
     * many xml objects.
     */
    public XmlPropertyMappingBuilder(String xpath) {
        this.mapping = new XmlPropertyMapping();
        mapping.setXPath(xpath);
    }

    /**
     * The name of the resource property that will be stored in the index. Defaults to the element name.
     */
    public XmlPropertyMappingBuilder indexName(String indexName) {
        mapping.setName(indexName);
        mapping.setPath(new StaticPropertyPath(indexName));
        return this;
    }

    /**
     * Specifies whether and how a property will be stored. Deftauls to
     * {@link org.compass.core.Property.Store#YES}.
     */
    public XmlPropertyMappingBuilder store(Property.Store store) {
        mapping.setStore(store);
        return this;
    }

    /**
     * Specifies whether and how a property should be indexed. Defaults to
     * {@link org.compass.core.Property.Index#ANALYZED}.
     */
    public XmlPropertyMappingBuilder index(Property.Index index) {
        mapping.setIndex(index);
        return this;
    }

    /**
     * Specifies whether and how a property should have term vectors. Defaults to
     * {@link org.compass.core.Property.TermVector#NO}.
     */
    public XmlPropertyMappingBuilder termVector(Property.TermVector termVector) {
        mapping.setTermVector(termVector);
        return this;
    }

    /**
     * If set, omit normalization factors associated with this indexed field.
     * This effectively disables indexing boosts and length normalization for this field. Defaults
     * to <code>false</code>.
     */
    public XmlPropertyMappingBuilder omitNorms(boolean omitNorms) {
        mapping.setOmitNorms(omitNorms);
        return this;
    }

    /**
     * If set, omit tf from postings of this indexed property. Defaults to <code>false</code>.
     */
    public XmlPropertyMappingBuilder omitTf(boolean omitTf) {
        mapping.setOmitTf(omitTf);
        return this;
    }

    /**
     * Sets the boost value for the property mapping. Defaults to <code>1.0f</code>.
     */
    public XmlPropertyMappingBuilder boost(float boost) {
        mapping.setBoost(boost);
        return this;
    }

    /**
     * Sets the format that will be used for formattable capable converters (such as numbers and dates).
     */
    public XmlPropertyMappingBuilder format(String format) {
        mapping.setValueConverter(new FormatDelegateConverter(format));
        mapping.setFormat(format);
        return this;
    }

    /**
     * Sets the lookup converter name (registered with Compass) that will be used to convert the XML
     * property. Defaults to {@link org.compass.core.converter.mapping.xsem.XmlPropertyMappingConverter}.
     */
    public XmlPropertyMappingBuilder mappingConverter(String converterName) {
        mapping.setConverterName(converterName);
        return this;
    }

    /**
     * Sets the actual converter that will be used to convert the XML
     * property. Defaults to {@link org.compass.core.converter.mapping.xsem.XmlPropertyMappingConverter}.
     */
    public XmlPropertyMappingBuilder mappingConverter(Converter converter) {
        mapping.setConverter(converter);
        return this;
    }

    /**
     * Sets the lookup converter name (registered with Compass) that will be used to convert the actual
     * value of the XML property. Detaults to {@link org.compass.core.converter.xsem.SimpleXmlValueConverter}.
     */
    public XmlPropertyMappingBuilder valueConverter(String converterName) {
        mapping.setValueConverterName(converterName);
        return this;
    }

    /**
     * Sets the actual converter that will be used to convert the actual
     * value of the XML property. Detaults to {@link org.compass.core.converter.xsem.SimpleXmlValueConverter}.
     */
    public XmlPropertyMappingBuilder valueConverter(Converter converter) {
        mapping.setValueConverter(converter);
        return this;
    }

    /**
     * Sets the actual converter that will be used to convert the actual
     * value of the XML property. Detaults to {@link org.compass.core.converter.xsem.SimpleXmlValueConverter}.
     */
    public XmlPropertyMappingBuilder valueConverter(ResourcePropertyConverter converter) {
        mapping.setValueConverter(converter);
        return this;
    }

    /**
     * Sets the analyzer logical name that will be used to analyzer the property value. The name
     * is a lookup name for an Analyzer that is registered with Compass.
     */
    public XmlPropertyMappingBuilder analyzer(String analyzer) {
        mapping.setAnalyzer(analyzer);
        return this;
    }

    /**
     * Controls if the property will be excluded from all or not.
     */
    public XmlPropertyMappingBuilder excludeFromAll(ExcludeFromAll excludeFromAll) {
        mapping.setExcludeFromAll(excludeFromAll);
        return this;
    }

    /**
     * Sets the null value of the property. If the property value is null, will store in the
     * index the provided value.
     */
    public XmlPropertyMappingBuilder nullValue(String nullValue) {
        mapping.setNullValue(nullValue);
        return this;
    }

    /**
     * Sets the spell check specific setting for the mapping.
     */
    public XmlPropertyMappingBuilder spellCheck(SpellCheck spellCheck) {
        mapping.setSpellCheck(spellCheck);
        return this;
    }
}