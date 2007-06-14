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

package org.compass.core.config.binding;

import java.util.ArrayList;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.compass.core.Property;
import org.compass.core.config.CommonMetaDataLookup;
import org.compass.core.config.CompassConfigurable;
import org.compass.core.config.CompassSettings;
import org.compass.core.config.ConfigurationException;
import org.compass.core.converter.mapping.osem.MetaDataFormatDelegateConverter;
import org.compass.core.engine.naming.StaticPropertyPath;
import org.compass.core.engine.subindex.ConstantSubIndexHash;
import org.compass.core.engine.subindex.SubIndexHash;
import org.compass.core.mapping.AbstractResourceMapping;
import org.compass.core.mapping.AliasMapping;
import org.compass.core.mapping.CascadeMapping;
import org.compass.core.mapping.CompassMapping;
import org.compass.core.mapping.ContractMapping;
import org.compass.core.mapping.Mapping;
import org.compass.core.mapping.MappingException;
import org.compass.core.mapping.ResourcePropertyMapping;
import org.compass.core.mapping.osem.AbstractRefAliasMapping;
import org.compass.core.mapping.osem.ClassBoostPropertyMapping;
import org.compass.core.mapping.osem.ClassIdPropertyMapping;
import org.compass.core.mapping.osem.ClassMapping;
import org.compass.core.mapping.osem.ClassPropertyAnalyzerController;
import org.compass.core.mapping.osem.ClassPropertyMapping;
import org.compass.core.mapping.osem.ClassPropertyMetaDataMapping;
import org.compass.core.mapping.osem.ComponentMapping;
import org.compass.core.mapping.osem.ConstantMetaDataMapping;
import org.compass.core.mapping.osem.DynamicMetaDataMapping;
import org.compass.core.mapping.osem.ParentMapping;
import org.compass.core.mapping.osem.ReferenceMapping;
import org.compass.core.mapping.rsem.RawBoostPropertyMapping;
import org.compass.core.mapping.rsem.RawResourceMapping;
import org.compass.core.mapping.rsem.RawResourcePropertyAnalyzerController;
import org.compass.core.mapping.rsem.RawResourcePropertyIdMapping;
import org.compass.core.mapping.rsem.RawResourcePropertyMapping;
import org.compass.core.mapping.xsem.XmlBoostPropertyMapping;
import org.compass.core.mapping.xsem.XmlContentMapping;
import org.compass.core.mapping.xsem.XmlIdMapping;
import org.compass.core.mapping.xsem.XmlObjectMapping;
import org.compass.core.mapping.xsem.XmlPropertyAnalyzerController;
import org.compass.core.mapping.xsem.XmlPropertyMapping;
import org.compass.core.metadata.Alias;
import org.compass.core.metadata.CompassMetaData;
import org.compass.core.util.ClassUtils;
import org.compass.core.util.DTDEntityResolver;
import org.compass.core.util.StringUtils;
import org.compass.core.util.config.ConfigurationHelper;
import org.xml.sax.EntityResolver;

/**
 * @author kimchy
 */
public class XmlMappingBinding extends AbstractXmlMappingBinding {

    public static final Log log = LogFactory.getLog(XmlMappingBinding.class);

    private CommonMetaDataLookup valueLookup;

    public void setUpBinding(CompassMapping mapping, CompassMetaData metaData, CompassSettings settings) {
        super.setUpBinding(mapping, metaData, settings);
        this.valueLookup = new CommonMetaDataLookup(metaData);
    }

    protected EntityResolver doGetEntityResolver() {
        return new DTDEntityResolver();
    }

    protected String getSuffix() {
        return ".cpm.xml";
    }

    protected boolean doAddConfiguration(ConfigurationHelper doc) throws ConfigurationException, MappingException {
        if (!doc.getName().equals("compass-core-mapping")) {
            return false;
        }

        String defaultPackage = doc.getAttribute("package", null);
        if (defaultPackage != null) {
            defaultPackage = defaultPackage + ".";
        } else {
            defaultPackage = "";
        }
        ConfigurationHelper[] contractArr = doc.getChildren("contract");
        for (int i = 0; i < contractArr.length; i++) {
            ContractMapping contractMapping = new ContractMapping();
            bindContract(contractArr[i], contractMapping);
            mapping.addMapping(contractMapping);
        }
        ConfigurationHelper[] resourceContractArr = doc.getChildren("resource-contract");
        for (int i = 0; i < resourceContractArr.length; i++) {
            ContractMapping contractMapping = new ContractMapping();
            bindResourceContract(resourceContractArr[i], contractMapping);
            mapping.addMapping(contractMapping);
        }
        ConfigurationHelper[] classArr = doc.getChildren("class");
        for (int i = 0; i < classArr.length; i++) {
            ConfigurationHelper classConf = classArr[i];
            String alias = classConf.getAttribute("alias");
            boolean newClassMapping = false;
            ClassMapping classMapping;
            AliasMapping aliasMapping = mapping.getAliasMapping(alias);
            if (aliasMapping != null) {
                if (!(aliasMapping instanceof ClassMapping)) {
                    throw new MappingException("Defined searchable annotation on a class with alias [" + alias + "] but it" +
                            " not of type class mapping");
                }
                classMapping = (ClassMapping) aliasMapping;
            } else {
                classMapping = new ClassMapping();
                newClassMapping = true;
            }
            bindClass(classArr[i], classMapping, defaultPackage);
            if (newClassMapping) {
                mapping.addMapping(classMapping);
            }
        }
        ConfigurationHelper[] resourceArr = doc.getChildren("resource");
        for (int i = 0; i < resourceArr.length; i++) {
            RawResourceMapping rawResourceMapping = new RawResourceMapping();
            bindResource(resourceArr[i], rawResourceMapping);
            mapping.addMapping(rawResourceMapping);
        }
        ConfigurationHelper[] xmlObjectArr = doc.getChildren("xml-object");
        for (int i = 0; i < xmlObjectArr.length; i++) {
            XmlObjectMapping xmlObjectMapping = new XmlObjectMapping();
            bindXmlObject(xmlObjectArr[i], xmlObjectMapping);
            mapping.addMapping(xmlObjectMapping);
        }

        return true;
    }

    private void bindXmlObject(ConfigurationHelper xmlObjectConf, XmlObjectMapping xmlObjectMapping)
            throws ConfigurationException {
        String aliasValue = xmlObjectConf.getAttribute("alias");
        Alias alias = valueLookup.lookupAlias(aliasValue);
        if (alias == null) {
            xmlObjectMapping.setAlias(aliasValue);
        } else {
            xmlObjectMapping.setAlias(alias.getName());
        }

        bindSubIndexHash(xmlObjectConf, xmlObjectMapping);

        bindExtends(xmlObjectConf, xmlObjectMapping);

        String analyzer = xmlObjectConf.getAttribute("analyzer", null);
        xmlObjectMapping.setAnalyzer(analyzer);

        String sAllSupported = xmlObjectConf.getAttribute("all", "true");
        boolean allSupported = sAllSupported.equalsIgnoreCase("true");
        xmlObjectMapping.setAllSupported(allSupported);

        String termVectorType = xmlObjectConf.getAttribute("all-term-vector", null);
        if (termVectorType == null) {
            xmlObjectMapping.setAllTermVector(null);
        } else {
            xmlObjectMapping.setAllTermVector(Property.TermVector.fromString(termVectorType));
        }

        xmlObjectMapping.setAllOmitNorms(xmlObjectConf.getAttributeAsBoolean("all-omit-norms", false));

        String allAnalyzer = xmlObjectConf.getAttribute("all-analyzer", null);
        xmlObjectMapping.setAllAnalyzer(allAnalyzer);

        if (xmlObjectMapping.isAllSupported()) {
            String allProperty = xmlObjectConf.getAttribute("all-metadata", null);
            xmlObjectMapping.setAllProperty(allProperty);
        }
        xmlObjectMapping.setRoot(true);
        xmlObjectMapping.setBoost(getBoost(xmlObjectConf));

        xmlObjectMapping.setXPath(xmlObjectConf.getAttribute("xpath", null));

        bindConverter(xmlObjectConf, xmlObjectMapping);

        bindXmlObjectChildren(xmlObjectConf, xmlObjectMapping);
    }

    private void bindXmlObjectChildren(ConfigurationHelper resourceConf, AliasMapping resourceMapping) {
        ConfigurationHelper[] ids = resourceConf.getChildren("xml-id");
        for (int i = 0; i < ids.length; i++) {
            XmlIdMapping xmlIdMapping = new XmlIdMapping();
            bindXmlProperty(ids[i], xmlIdMapping);
            resourceMapping.addMapping(xmlIdMapping);
        }

        ConfigurationHelper[] properties = resourceConf.getChildren("xml-property");
        for (int i = 0; i < properties.length; i++) {
            XmlPropertyMapping xmlPropertyMapping = new XmlPropertyMapping();
            bindXmlProperty(properties[i], xmlPropertyMapping);
            resourceMapping.addMapping(xmlPropertyMapping);
        }

        ConfigurationHelper xmlContentConf = resourceConf.getChild("xml-content", false);
        if (xmlContentConf != null) {
            XmlContentMapping xmlContentMapping = new XmlContentMapping();
            bindXmlContent(xmlContentConf, xmlContentMapping);
            resourceMapping.addMapping(xmlContentMapping);
        }

        ConfigurationHelper analyzerConf = resourceConf.getChild("xml-analyzer", false);
        if (analyzerConf != null) {
            XmlPropertyAnalyzerController analyzerController = new XmlPropertyAnalyzerController();
            bindXmlProperty(analyzerConf, analyzerController);
            analyzerController.setNullAnalyzer(analyzerConf.getAttribute("null-analyzer", null));
            resourceMapping.addMapping(analyzerController);
        }

        ConfigurationHelper boostConf = resourceConf.getChild("xml-boost", false);
        if (boostConf != null) {
            XmlBoostPropertyMapping boostPropertyMapping = new XmlBoostPropertyMapping();
            bindXmlProperty(boostConf, boostPropertyMapping);
            String defaultBoost = boostConf.getAttribute("default", null);
            if (defaultBoost != null) {
                boostPropertyMapping.setDefaultBoost(Float.parseFloat(defaultBoost));
            }
            resourceMapping.addMapping(boostPropertyMapping);
        }
    }

    private void bindXmlContent(ConfigurationHelper xmlContentConf, XmlContentMapping xmlContentMapping) {

        String name = xmlContentConf.getAttribute("name", null);
        if (name != null) {
            name = valueLookup.lookupMetaDataName(name);
        }
        xmlContentMapping.setName(name);
        xmlContentMapping.setPath(new StaticPropertyPath(name));
        bindConverter(xmlContentConf, xmlContentMapping);
        String storeType = xmlContentConf.getAttribute("store", "yes");
        xmlContentMapping.setStore(Property.Store.fromString(storeType));
        xmlContentMapping.setInternal(true);
    }

    private void bindXmlProperty(ConfigurationHelper xmlPropConf, XmlPropertyMapping xmlPropertyMapping) {
        String name = xmlPropConf.getAttribute("name", null);
        if (name != null) {
            name = valueLookup.lookupMetaDataName(name);
        }
        xmlPropertyMapping.setBoost(getBoost(xmlPropConf));
        xmlPropertyMapping.setName(name);
        xmlPropertyMapping.setPath((name == null ? null : new StaticPropertyPath(name)));
        bindConverter(xmlPropConf, xmlPropertyMapping);
        String storeType = xmlPropConf.getAttribute("store", "yes");
        xmlPropertyMapping.setStore(Property.Store.fromString(storeType));
        String indexType = xmlPropConf.getAttribute("index", "tokenized");
        xmlPropertyMapping.setIndex(Property.Index.fromString(indexType));
        String termVectorType = xmlPropConf.getAttribute("term-vector", "no");
        xmlPropertyMapping.setTermVector(Property.TermVector.fromString(termVectorType));
        boolean omitNorms = xmlPropConf.getAttributeAsBoolean("omit-norms", false);
        xmlPropertyMapping.setOmitNorms(omitNorms);
        String reverseType = xmlPropConf.getAttribute("reverse", "no");
        xmlPropertyMapping.setReverse(ResourcePropertyMapping.ReverseType.fromString(reverseType));
        xmlPropertyMapping.setInternal(false);
        xmlPropertyMapping.setAnalyzer(xmlPropConf.getAttribute("analyzer", null));
        boolean excludeFromAll = xmlPropConf.getAttributeAsBoolean("exclude-from-all", false);
        xmlPropertyMapping.setExcludeFromAll(excludeFromAll);
        boolean override = xmlPropConf.getAttributeAsBoolean("override", true);
        xmlPropertyMapping.setOverrideByName(override);

        xmlPropertyMapping.setXPath(xmlPropConf.getAttribute("xpath"));

        xmlPropertyMapping.setValueConverterName(xmlPropConf.getAttribute("value-converter", null));
    }

    private void bindResourceContract(ConfigurationHelper contractConf, ContractMapping contractMapping)
            throws ConfigurationException {
        String aliasValue = contractConf.getAttribute("alias");
        Alias alias = valueLookup.lookupAlias(aliasValue);
        if (alias == null) {
            contractMapping.setAlias(aliasValue);
        } else {
            contractMapping.setAlias(alias.getName());
        }
        bindExtends(contractConf, contractMapping);

        bindResourceMappingChildren(contractConf, contractMapping);
    }

    private void bindResource(ConfigurationHelper resourceConf, RawResourceMapping rawResourceMapping)
            throws ConfigurationException {
        String aliasValue = resourceConf.getAttribute("alias");
        Alias alias = valueLookup.lookupAlias(aliasValue);
        if (alias == null) {
            rawResourceMapping.setAlias(aliasValue);
        } else {
            rawResourceMapping.setAlias(alias.getName());
        }

        bindSubIndexHash(resourceConf, rawResourceMapping);

        bindExtends(resourceConf, rawResourceMapping);

        String analyzer = resourceConf.getAttribute("analyzer", null);
        rawResourceMapping.setAnalyzer(analyzer);

        String sAllSupported = resourceConf.getAttribute("all", "true");
        boolean allSupported = sAllSupported.equalsIgnoreCase("true");
        rawResourceMapping.setAllSupported(allSupported);

        String termVectorType = resourceConf.getAttribute("all-term-vector", null);
        if (termVectorType == null) {
            rawResourceMapping.setAllTermVector(null);
        } else {
            rawResourceMapping.setAllTermVector(Property.TermVector.fromString(termVectorType));
        }

        rawResourceMapping.setAllOmitNorms(resourceConf.getAttributeAsBoolean("all-omit-norms", false));


        String allAnalyzer = resourceConf.getAttribute("all-analyzer", null);
        rawResourceMapping.setAllAnalyzer(allAnalyzer);

        if (rawResourceMapping.isAllSupported()) {
            String allProperty = resourceConf.getAttribute("all-metadata", null);
            rawResourceMapping.setAllProperty(allProperty);
        }
        rawResourceMapping.setRoot(true);
        rawResourceMapping.setBoost(getBoost(resourceConf));

        bindResourceMappingChildren(resourceConf, rawResourceMapping);
    }

    private void bindResourceMappingChildren(ConfigurationHelper resourceConf, AliasMapping resourceMapping) {
        ConfigurationHelper[] ids = resourceConf.getChildren("resource-id");
        for (int i = 0; i < ids.length; i++) {
            RawResourcePropertyIdMapping rawIdPropertyMapping = new RawResourcePropertyIdMapping();
            bindResourceProperty(ids[i], rawIdPropertyMapping);
            resourceMapping.addMapping(rawIdPropertyMapping);
        }

        ConfigurationHelper[] properties = resourceConf.getChildren("resource-property");
        for (int i = 0; i < properties.length; i++) {
            RawResourcePropertyMapping rawPropertyMapping = new RawResourcePropertyMapping();
            bindResourceProperty(properties[i], rawPropertyMapping);
            resourceMapping.addMapping(rawPropertyMapping);
        }

        ConfigurationHelper analyzerConf = resourceConf.getChild("resource-analyzer", false);
        if (analyzerConf != null) {
            RawResourcePropertyAnalyzerController analyzerController = new RawResourcePropertyAnalyzerController();
            bindResourceProperty(analyzerConf, analyzerController);
            analyzerController.setNullAnalyzer(analyzerConf.getAttribute("null-analyzer", null));
            resourceMapping.addMapping(analyzerController);
        }

        ConfigurationHelper boostConf = resourceConf.getChild("resource-boost", false);
        if (boostConf != null) {
            RawBoostPropertyMapping boostPropertyMapping = new RawBoostPropertyMapping();
            bindResourceProperty(boostConf, boostPropertyMapping);
            String defaultBoost = boostConf.getAttribute("default", null);
            if (defaultBoost != null) {
                boostPropertyMapping.setDefaultBoost(Float.parseFloat(defaultBoost));
            }
            resourceMapping.addMapping(boostPropertyMapping);
        }
    }

    private void bindResourceProperty(ConfigurationHelper resourcePropConf, RawResourcePropertyMapping propertyMapping) {
        String name = valueLookup.lookupMetaDataName(resourcePropConf.getAttribute("name"));
        propertyMapping.setBoost(getBoost(resourcePropConf));
        propertyMapping.setName(name);
        propertyMapping.setPath(new StaticPropertyPath(name));
        bindConverter(resourcePropConf, propertyMapping);
        String storeType = resourcePropConf.getAttribute("store", "yes");
        propertyMapping.setStore(Property.Store.fromString(storeType));
        String indexType = resourcePropConf.getAttribute("index", "tokenized");
        propertyMapping.setIndex(Property.Index.fromString(indexType));
        String termVectorType = resourcePropConf.getAttribute("term-vector", "no");
        propertyMapping.setTermVector(Property.TermVector.fromString(termVectorType));
        boolean omitNorms = resourcePropConf.getAttributeAsBoolean("omit-norms", false);
        propertyMapping.setOmitNorms(omitNorms);
        String reverseType = resourcePropConf.getAttribute("reverse", "no");
        propertyMapping.setReverse(ResourcePropertyMapping.ReverseType.fromString(reverseType));
        propertyMapping.setInternal(false);
        propertyMapping.setAnalyzer(resourcePropConf.getAttribute("analyzer", null));
        boolean excludeFromAll = resourcePropConf.getAttributeAsBoolean("exclude-from-all", false);
        propertyMapping.setExcludeFromAll(excludeFromAll);
        boolean override = resourcePropConf.getAttributeAsBoolean("override", true);
        propertyMapping.setOverrideByName(override);
    }

    private void bindContract(ConfigurationHelper contractConf, ContractMapping contractMapping)
            throws ConfigurationException {
        String aliasValue = contractConf.getAttribute("alias");
        Alias alias = valueLookup.lookupAlias(aliasValue);
        if (alias == null) {
            contractMapping.setAlias(aliasValue);
        } else {
            contractMapping.setAlias(alias.getName());
        }
        bindExtends(contractConf, contractMapping);

        bindClassMappingChildren(contractConf, contractMapping);
    }

    private void bindClass(ConfigurationHelper classConf, ClassMapping classMapping, String defaultPackage)
            throws ConfigurationException {
        String className = classConf.getAttribute("name");
        classMapping.setName(defaultPackage + className);
        try {
            Class clazz = ClassUtils.forName(classMapping.getName());
            classMapping.setClazz(clazz);
        } catch (ClassNotFoundException e) {
            throw new ConfigurationException("Failed to find class [" + defaultPackage + classMapping.getName() + "]");
        }

        String aliasValue = classConf.getAttribute("alias");
        Alias alias = valueLookup.lookupAlias(aliasValue);
        if (alias == null) {
            classMapping.setAlias(aliasValue);
        } else {
            classMapping.setAlias(alias.getName());
        }

        bindExtends(classConf, classMapping);

        bindSubIndexHash(classConf, classMapping);

        String analyzer = classConf.getAttribute("analyzer", null);
        classMapping.setAnalyzer(analyzer);

        boolean allSupported = classConf.getAttributeAsBoolean("all", true);
        classMapping.setAllSupported(allSupported);

        if (classMapping.isAllSupported()) {
            String allProperty = classConf.getAttribute("all-metadata", null);
            classMapping.setAllProperty(allProperty);
        }

        String termVectorType = classConf.getAttribute("all-term-vector", null);
        if (termVectorType == null) {
            classMapping.setAllTermVector(null);
        } else {
            classMapping.setAllTermVector(Property.TermVector.fromString(termVectorType));
        }

        classMapping.setAllOmitNorms(classConf.getAttributeAsBoolean("all-omit-norms", false));


        String allAnalyzer = classConf.getAttribute("all-analyzer", null);
        classMapping.setAllAnalyzer(allAnalyzer);

        boolean poly = classConf.getAttributeAsBoolean("poly", false);
        classMapping.setPoly(poly);

        String polyClassName = classConf.getAttribute("poly-class", null);
        if (polyClassName != null) {
            try {
                classMapping.setPolyClass(ClassUtils.forName(polyClassName));
            } catch (ClassNotFoundException e) {
                throw new ConfigurationException("Failed to load polyClass [" + polyClassName + "]", e);
            }
        }

        boolean root = classConf.getAttributeAsBoolean("root", true);
        classMapping.setRoot(root);
        classMapping.setBoost(getBoost(classConf));

        // don't set support unmarshall unless it is set, since it might be globally set
        String supportUnmarshall = classConf.getAttribute("support-unmarshall", null);
        if (supportUnmarshall != null) {
            if (supportUnmarshall.equalsIgnoreCase("true")) {
                classMapping.setSupportUnmarshall(true);
            } else {
                classMapping.setSupportUnmarshall(false);
            }
        }

        bindConverter(classConf, classMapping);

        bindClassMappingChildren(classConf, classMapping);
    }

    private void bindClassMappingChildren(ConfigurationHelper classConf, AliasMapping classMapping) {
        ConfigurationHelper[] ids = classConf.getChildren("id");
        for (int i = 0; i < ids.length; i++) {
            ClassIdPropertyMapping idMapping = new ClassIdPropertyMapping();
            bindClassProperty(ids[i], classMapping, idMapping);
            classMapping.addMapping(idMapping);
        }
        ConfigurationHelper[] properties = classConf.getChildren("property");
        for (int i = 0; i < properties.length; i++) {
            ClassPropertyMapping classPropertyMapping = new ClassPropertyMapping();
            bindClassProperty(properties[i], classMapping, classPropertyMapping);
            classMapping.addMapping(classPropertyMapping);
        }
        ConfigurationHelper[] components = classConf.getChildren("component");
        for (int i = 0; i < components.length; i++) {
            ComponentMapping compMapping = new ComponentMapping();
            bindComponent(components[i], classMapping, compMapping);
            classMapping.addMapping(compMapping);
        }
        ConfigurationHelper[] references = classConf.getChildren("reference");
        for (int i = 0; i < references.length; i++) {
            ReferenceMapping referenceMapping = new ReferenceMapping();
            bindReference(references[i], classMapping, referenceMapping);
            classMapping.addMapping(referenceMapping);
        }
        ConfigurationHelper[] constants = classConf.getChildren("constant");
        for (int i = 0; i < constants.length; i++) {
            ConstantMetaDataMapping constantMapping = new ConstantMetaDataMapping();
            bindConstant(constants[i], classMapping, constantMapping);
            classMapping.addMapping(constantMapping);
        }
        ConfigurationHelper parentConf = classConf.getChild("parent", false);
        if (parentConf != null) {
            ParentMapping parentMapping = new ParentMapping();
            bindParent(parentConf, classMapping, parentMapping);
            classMapping.addMapping(parentMapping);
        }

        ConfigurationHelper analyzerConf = classConf.getChild("analyzer", false);
        if (analyzerConf != null) {
            ClassPropertyAnalyzerController analyzerController = new ClassPropertyAnalyzerController();
            bindClassProperty(analyzerConf, classMapping, analyzerController);
            analyzerController.setNullAnalyzer(analyzerConf.getAttribute("null-analyzer", null));
            classMapping.addMapping(analyzerController);
        }

        ConfigurationHelper boostConf = classConf.getChild("boost", false);
        if (boostConf != null) {
            ClassBoostPropertyMapping boostPropertyMapping = new ClassBoostPropertyMapping();
            bindClassProperty(boostConf, classMapping, boostPropertyMapping);
            String defaultBoost = boostConf.getAttribute("default", null);
            if (defaultBoost != null) {
                boostPropertyMapping.setDefaultBoost(Float.parseFloat(defaultBoost));
            }
            classMapping.addMapping(boostPropertyMapping);
        }

        ConfigurationHelper[] dynamicConfs = classConf.getChildren("dynamic-meta-data");
        for (int i = 0; i < dynamicConfs.length; i++) {
            DynamicMetaDataMapping dynamicMetaDataMapping = new DynamicMetaDataMapping();
            bindDynamicMetaData(dynamicConfs[i], classMapping, dynamicMetaDataMapping);
            classMapping.addMapping(dynamicMetaDataMapping);
        }
    }

    private void bindDynamicMetaData(ConfigurationHelper dynamicConf, AliasMapping aliasMapping,
                                     DynamicMetaDataMapping dynamicMetaDataMapping) {
        String name = valueLookup.lookupMetaDataName(dynamicConf.getAttribute("name"));
        dynamicMetaDataMapping.setBoost(getBoost(dynamicConf));
        dynamicMetaDataMapping.setName(name);
        dynamicMetaDataMapping.setPath(new StaticPropertyPath(name));

        dynamicMetaDataMapping.setExpression(dynamicConf.getValue().trim());

        dynamicMetaDataMapping.setFormat(dynamicConf.getAttribute("format", null));
        String type = dynamicConf.getAttribute("type", null);
        if (type != null) {
            try {
                dynamicMetaDataMapping.setType(ClassUtils.forName(type));
            } catch (ClassNotFoundException e) {
                throw new MappingException("Failed to find class [" + type + "]", e);
            }
        }

        bindConverter(dynamicConf, dynamicMetaDataMapping);

        boolean override = dynamicConf.getAttributeAsBoolean("override", true);
        dynamicMetaDataMapping.setOverrideByName(override);

        String storeType = dynamicConf.getAttribute("store", "yes");
        dynamicMetaDataMapping.setStore(Property.Store.fromString(storeType));
        String indexType = dynamicConf.getAttribute("index", "tokenized");
        dynamicMetaDataMapping.setIndex(Property.Index.fromString(indexType));
        String termVectorType = dynamicConf.getAttribute("term-vector", "no");
        dynamicMetaDataMapping.setTermVector(Property.TermVector.fromString(termVectorType));
        boolean omitNorms = dynamicConf.getAttributeAsBoolean("omit-norms", false);
        dynamicMetaDataMapping.setOmitNorms(omitNorms);
        String reverseType = dynamicConf.getAttribute("reverse", "no");
        dynamicMetaDataMapping.setReverse(ResourcePropertyMapping.ReverseType.fromString(reverseType));
        dynamicMetaDataMapping.setInternal(false);
        dynamicMetaDataMapping.setAnalyzer(dynamicConf.getAttribute("analyzer", null));
        dynamicMetaDataMapping.setExcludeFromAll(dynamicConf.getAttributeAsBoolean("exclude-from-all", false));
    }

    private void bindReference(ConfigurationHelper referenceConf, AliasMapping aliasMapping,
                               ReferenceMapping referenceMapping) {
        String name = referenceConf.getAttribute("name");
        referenceMapping.setName(name);

        String refAlias = referenceConf.getAttribute("ref-alias", null);
        referenceMapping.setRefAliases(getAliases(refAlias));
        referenceMapping.setDefinedInAlias(aliasMapping.getAlias());

        String refCompAlias = referenceConf.getAttribute("ref-comp-alias", null);
        if (refCompAlias != null) {
            referenceMapping.setRefCompAlias(valueLookup.lookupAliasName(refCompAlias));
        }

        bindConverter(referenceConf, referenceMapping);

        referenceMapping.setAccessor(referenceConf.getAttribute("accessor", null));
        referenceMapping.setPropertyName(name);

        bindCascade(referenceConf, referenceMapping);
    }

    private void bindComponent(ConfigurationHelper componentConf, AliasMapping aliasMapping,
                               ComponentMapping compMapping) {
        String name = componentConf.getAttribute("name");
        compMapping.setName(name);
        String refAlias = componentConf.getAttribute("ref-alias", null);
        compMapping.setRefAliases(getAliases(refAlias));
        compMapping.setDefinedInAlias(aliasMapping.getAlias());

        int maxDepth = componentConf.getAttributeAsInteger("max-depth", 1);
        compMapping.setMaxDepth(maxDepth);

        bindConverter(componentConf, compMapping);

        compMapping.setAccessor(componentConf.getAttribute("accessor", null));
        compMapping.setPropertyName(name);

        boolean override = componentConf.getAttributeAsBoolean("override", true);
        compMapping.setOverrideByName(override);

        bindCascade(componentConf, compMapping);
    }

    private void bindCascade(ConfigurationHelper refConf, AbstractRefAliasMapping refAliasMapping) {
        String commaSeparatedCascades = refConf.getAttribute("cascade", null);
        if (commaSeparatedCascades == null) {
            return;
        }
        ArrayList cascades = new ArrayList();
        StringTokenizer st = new StringTokenizer(commaSeparatedCascades, ",");
        while (st.hasMoreTokens()) {
            String cascade = st.nextToken().trim();
            cascades.add(CascadeMapping.Cascade.fromString(cascade));
        }
        if (cascades.size() > 0) {
            refAliasMapping.setCascades((CascadeMapping.Cascade[]) cascades.toArray(new CascadeMapping.Cascade[cascades.size()]));
        }
    }

    private void bindParent(ConfigurationHelper parentConf, AliasMapping aliasMapping, ParentMapping parentMapping) {
        String name = parentConf.getAttribute("name");
        parentMapping.setName(name);
        bindConverter(parentConf, parentMapping);

        parentMapping.setAccessor(parentConf.getAttribute("accessor", null));
        parentMapping.setPropertyName(name);
        parentMapping.setDefinedInAlias(aliasMapping.getAlias());
    }

    private void bindClassProperty(ConfigurationHelper classPropertyConf, AliasMapping aliasMapping,
                                   ClassPropertyMapping classPropertyMapping) {
        String name = classPropertyConf.getAttribute("name");
        classPropertyMapping.setName(name);

        String sClass = classPropertyConf.getAttribute("class", null);
        classPropertyMapping.setClassName(sClass);
        classPropertyMapping.setDefinedInAlias(aliasMapping.getAlias());

        classPropertyMapping.setBoost(getBoost(classPropertyConf));

        classPropertyMapping.setColClassName(classPropertyConf.getAttribute("col-class", null));

        classPropertyMapping.setAccessor(classPropertyConf.getAttribute("accessor", null));
        classPropertyMapping.setPropertyName(name);

        classPropertyMapping.setAnalyzer(classPropertyConf.getAttribute("analyzer", null));

        boolean excludeFromAll = classPropertyConf.getAttributeAsBoolean("exclude-from-all", false);
        classPropertyMapping.setExcludeFromAll(excludeFromAll);

        String managedId = classPropertyConf.getAttribute("managed-id", "auto");
        classPropertyMapping.setManagedId(ClassPropertyMapping.ManagedId.fromString(managedId));

        String managedIdIndex = classPropertyConf.getAttribute("managed-id-index", null);
        if (managedIdIndex != null) {
            classPropertyMapping.setManagedIdIndex(Property.Index.fromString(managedIdIndex));
        }
        classPropertyMapping.setManagedIdConverterName(classPropertyConf.getAttribute("managed-id-converter", null));

        boolean override = classPropertyConf.getAttributeAsBoolean("override", true);
        classPropertyMapping.setOverrideByName(override);

        bindConverter(classPropertyConf, classPropertyMapping);

        ConfigurationHelper[] metadatas = classPropertyConf.getChildren("meta-data");
        for (int i = 0; i < metadatas.length; i++) {
            ClassPropertyMetaDataMapping mdMapping = new ClassPropertyMetaDataMapping();
            bindMetaData(metadatas[i], aliasMapping, classPropertyMapping, mdMapping);
            classPropertyMapping.addMapping(mdMapping);
        }
    }

    private void bindConstant(ConfigurationHelper constantConf, AliasMapping classMapping,
                              ConstantMetaDataMapping constantMapping) {
        ConfigurationHelper metadataConf = constantConf.getChild("meta-data");
        if (!StringUtils.hasText(metadataConf.getValue())) {
            throw new MappingException("Alias mapping [" + classMapping.getAlias() + "] has a constant mapping with an empty meta-data value");
        }
        String metaDataValue = metadataConf.getValue().trim();
        constantMapping.setName(valueLookup.lookupMetaDataName(metaDataValue));
        constantMapping.setBoost(getBoost(metadataConf, 1.0f));
        String storeType = metadataConf.getAttribute("store", "yes");
        constantMapping.setStore(Property.Store.fromString(storeType));
        String indexType = metadataConf.getAttribute("index", "tokenized");
        constantMapping.setIndex(Property.Index.fromString(indexType));
        String termVectorType = metadataConf.getAttribute("term-vector", "no");
        constantMapping.setTermVector(Property.TermVector.fromString(termVectorType));
        boolean omitNorms = metadataConf.getAttributeAsBoolean("omit-norms", false);
        constantMapping.setOmitNorms(omitNorms);

        constantMapping.setAnalyzer(constantConf.getAttribute("analyzer", null));

        boolean excludeFromAll = constantConf.getAttributeAsBoolean("exclude-from-all", false);
        constantMapping.setExcludeFromAll(excludeFromAll);

        boolean override = constantConf.getAttributeAsBoolean("override", true);
        constantMapping.setOverrideByName(override);

        ConfigurationHelper[] values = constantConf.getChildren("meta-data-value");
        for (int i = 0; i < values.length; i++) {
            String metaDataValueValue = values[i].getValue().trim();
            constantMapping.addMetaDataValue(valueLookup.lookupMetaDataValue(metaDataValueValue));
        }
    }

    private void bindMetaData(ConfigurationHelper metadataConf, AliasMapping aliasMapping,
                              ClassPropertyMapping classPropertyMapping, ClassPropertyMetaDataMapping mdMapping) {
        if (!StringUtils.hasText(metadataConf.getValue())) {
            throw new MappingException("Alias mapping [" + aliasMapping.getAlias() + "] and property [" +
                    classPropertyMapping.getName() + "] has a meta-data mapping with no value");
        }
        String name = valueLookup.lookupMetaDataName(metadataConf.getValue().trim());
        mdMapping.setBoost(getBoost(metadataConf, classPropertyMapping.getBoost()));
        mdMapping.setName(name);
        mdMapping.setPath(new StaticPropertyPath(name));

        mdMapping.setAccessor(classPropertyMapping.getAccessor());
        mdMapping.setPropertyName(classPropertyMapping.getPropertyName());

        bindConverter(metadataConf, mdMapping);
        String format = metadataConf.getAttribute("format", null);
        if (mdMapping.getConverter() == null) {
            if (format == null) {
                format = valueLookup.lookupMetaDataFormat(metadataConf.getValue().trim());
            }
            if (format != null) {
                mdMapping.setConverter(new MetaDataFormatDelegateConverter(format));
            }
        } else {
            // just validate that both are not set, since it makes no sense
            if (format != null) {
                throw new ConfigurationException("Both converter and format are set for property [" +
                        classPropertyMapping.getName() + "], you should choose one or the other (since converter will" +
                        "not use the format defined)");
            }
        }

        String storeType = metadataConf.getAttribute("store", "yes");
        mdMapping.setStore(Property.Store.fromString(storeType));
        String indexType = metadataConf.getAttribute("index", "tokenized");
        mdMapping.setIndex(Property.Index.fromString(indexType));
        String termVectorType = metadataConf.getAttribute("term-vector", "no");
        mdMapping.setTermVector(Property.TermVector.fromString(termVectorType));
        boolean omitNorms = metadataConf.getAttributeAsBoolean("omit-norms", false);
        mdMapping.setOmitNorms(omitNorms);
        String reverseType = metadataConf.getAttribute("reverse", "no");
        mdMapping.setReverse(ResourcePropertyMapping.ReverseType.fromString(reverseType));
        mdMapping.setInternal(false);
        mdMapping.setAnalyzer(metadataConf.getAttribute("analyzer", classPropertyMapping.getAnalyzer()));
        boolean excludeFromAll = metadataConf.getAttributeAsBoolean("exclude-from-all", classPropertyMapping
                .isExcludeFromAll());
        mdMapping.setExcludeFromAll(excludeFromAll);
    }

    private void bindExtends(ConfigurationHelper conf, AliasMapping mapping) throws ConfigurationException {
        String extendsAliases = conf.getAttribute("extends", null);
        if (extendsAliases != null) {
            mapping.setExtendedAliases(getAliases(extendsAliases));
        }
    }

    /**
     * Returns a string array of aliases from a comma separated string
     */
    private String[] getAliases(String commaSeparatedAliases) {
        if (commaSeparatedAliases == null) {
            return null;
        }
        ArrayList aliases = new ArrayList();
        StringTokenizer st = new StringTokenizer(commaSeparatedAliases, ",");
        while (st.hasMoreTokens()) {
            String extendedAlias = st.nextToken().trim();
            Alias alias = valueLookup.lookupAlias(extendedAlias);
            if (alias == null) {
                aliases.add(extendedAlias);
            } else {
                aliases.add(alias.getName());
            }
        }
        return (String[]) aliases.toArray(new String[aliases.size()]);
    }

    private void bindConverter(ConfigurationHelper conf, Mapping mapping) {
        String converterName = conf.getAttribute("converter", null);
        mapping.setConverterName(converterName);
    }

    private void bindSubIndexHash(ConfigurationHelper conf, AbstractResourceMapping resourceMapping) {
        ConfigurationHelper subIndexHashConf = conf.getChild("sub-index-hash", false);
        if (subIndexHashConf == null) {
            String subIndex = conf.getAttribute("sub-index", resourceMapping.getAlias());
            resourceMapping.setSubIndexHash(new ConstantSubIndexHash(subIndex));
            if (log.isTraceEnabled()) {
                log.trace("Alias [" + resourceMapping.getAlias() + "] is mapped to sub index hash [" + resourceMapping.getSubIndexHash() + "]");
            }
            return;
        }

        String type = subIndexHashConf.getAttribute("type", null);
        SubIndexHash subIndexHash;
        try {
            subIndexHash = (SubIndexHash) ClassUtils.forName(type).newInstance();
        } catch (Exception e) {
            throw new ConfigurationException("Failed to create sub index hash of type [" + type + "]", e);
        }
        CompassSettings settings = new CompassSettings();
        ConfigurationHelper[] settingsConf = subIndexHashConf.getChildren("setting");
        if (subIndexHash instanceof CompassConfigurable) {
            for (int i = 0; i < settingsConf.length; i++) {
                settings.setSetting(settingsConf[i].getAttribute("name"), settingsConf[i].getAttribute("value"));
            }
            ((CompassConfigurable) subIndexHash).configure(settings);
        } else {
            if (settingsConf.length < 0) {
                throw new ConfigurationException("Sub index hash [" + subIndexHash + "] does not implement " +
                        "CompassConfigurable, but settings have been set for it");
            }
        }
        resourceMapping.setSubIndexHash(subIndexHash);
        if (log.isTraceEnabled()) {
            log.trace("Alias [" + resourceMapping.getAlias() + "] is mapped to sub index hash [" + resourceMapping.getSubIndexHash() + "]");
        }
    }

    private static float getBoost(ConfigurationHelper conf) {
        return getBoost(conf, 1.0f);
    }

    private static float getBoost(ConfigurationHelper conf, float defaultBoost) {
        return conf.getAttributeAsFloat("boost", defaultBoost);
    }

}
