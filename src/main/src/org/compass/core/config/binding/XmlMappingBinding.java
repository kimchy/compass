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

import org.compass.core.Property;
import org.compass.core.config.CommonMetaDataLookup;
import org.compass.core.config.ConfigurationException;
import org.compass.core.config.CompassSettings;
import org.compass.core.converter.MetaDataFormatDelegateConverter;
import org.compass.core.mapping.*;
import org.compass.core.mapping.xsem.XmlObjectMapping;
import org.compass.core.mapping.xsem.XmlIdMapping;
import org.compass.core.mapping.xsem.XmlPropertyMapping;
import org.compass.core.mapping.osem.*;
import org.compass.core.mapping.rsem.RawResourceMapping;
import org.compass.core.mapping.rsem.RawResourcePropertyAnalyzerController;
import org.compass.core.mapping.rsem.RawResourcePropertyIdMapping;
import org.compass.core.mapping.rsem.RawResourcePropertyMapping;
import org.compass.core.metadata.Alias;
import org.compass.core.metadata.CompassMetaData;
import org.compass.core.util.ClassUtils;
import org.compass.core.util.DTDEntityResolver;
import org.compass.core.util.config.ConfigurationHelper;
import org.xml.sax.EntityResolver;

/**
 * @author kimchy
 */
public class XmlMappingBinding extends AbstractXmlMappingBinding {

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

        String subIndex = xmlObjectConf.getAttribute("sub-index", xmlObjectMapping.getAlias());
        xmlObjectMapping.setSubIndex(subIndex);

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
    }

    private void bindXmlProperty(ConfigurationHelper xmlPropConf, XmlPropertyMapping xmlPropertyMapping) {
        String name = xmlPropConf.getAttribute("name", null);
        if (name != null) {
            name = valueLookup.lookupMetaDataName(name);
        }
        xmlPropertyMapping.setBoost(getBoost(xmlPropConf));
        xmlPropertyMapping.setName(name);
        xmlPropertyMapping.setPath(name);
        bindConverter(xmlPropConf, xmlPropertyMapping);
        String storeType = xmlPropConf.getAttribute("store", "yes");
        xmlPropertyMapping.setStore(Property.Store.fromString(storeType));
        String indexType = xmlPropConf.getAttribute("index", "tokenized");
        xmlPropertyMapping.setIndex(Property.Index.fromString(indexType));
        String termVectorType = xmlPropConf.getAttribute("term-vector", "no");
        xmlPropertyMapping.setTermVector(Property.TermVector.fromString(termVectorType));
        String reverseType = xmlPropConf.getAttribute("reverse", "no");
        xmlPropertyMapping.setReverse(ResourcePropertyMapping.ReverseType.fromString(reverseType));
        xmlPropertyMapping.setInternal(false);
        xmlPropertyMapping.setAnalyzer(xmlPropConf.getAttribute("analyzer", null));
        boolean excludeFromAll = xmlPropConf.getAttributeAsBoolean("exclude-from-all", false);
        xmlPropertyMapping.setExcludeFromAll(excludeFromAll);
        boolean override = xmlPropConf.getAttributeAsBoolean("override", true);
        xmlPropertyMapping.setOverrideByName(override);

        xmlPropertyMapping.setXPath(xmlPropConf.getAttribute("xpath"));

        bindConverter(xmlPropConf, xmlPropertyMapping);
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

        String subIndex = resourceConf.getAttribute("sub-index", rawResourceMapping.getAlias());
        rawResourceMapping.setSubIndex(subIndex);

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
    }

    private void bindResourceProperty(ConfigurationHelper resourcePropConf, RawResourcePropertyMapping propertyMapping) {
        String name = valueLookup.lookupMetaDataName(resourcePropConf.getAttribute("name"));
        propertyMapping.setBoost(getBoost(resourcePropConf));
        propertyMapping.setName(name);
        propertyMapping.setPath(name);
        bindConverter(resourcePropConf, propertyMapping);
        String storeType = resourcePropConf.getAttribute("store", "yes");
        propertyMapping.setStore(Property.Store.fromString(storeType));
        String indexType = resourcePropConf.getAttribute("index", "tokenized");
        propertyMapping.setIndex(Property.Index.fromString(indexType));
        String termVectorType = resourcePropConf.getAttribute("term-vector", "no");
        propertyMapping.setTermVector(Property.TermVector.fromString(termVectorType));
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

        String subIndex = classConf.getAttribute("sub-index", classMapping.getAlias());
        classMapping.setSubIndex(subIndex);

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
    }

    private void bindReference(ConfigurationHelper referenceConf, AliasMapping aliasMapping,
                               ReferenceMapping referenceMapping) {
        String name = referenceConf.getAttribute("name");
        referenceMapping.setName(name);

        String refAlias = referenceConf.getAttribute("ref-alias", null);
        referenceMapping.setRefAlias(valueLookup.lookupAliasName(refAlias));

        String refCompAlias = referenceConf.getAttribute("ref-comp-alias", null);
        if (refCompAlias != null) {
            referenceMapping.setRefCompAlias(valueLookup.lookupAliasName(refCompAlias));
        }

        bindConverter(referenceConf, referenceMapping);

        referenceMapping.setAccessor(referenceConf.getAttribute("accessor", null));
        if (aliasMapping instanceof ClassMapping) {
            referenceMapping.setObjClass(((ClassMapping) aliasMapping).getClazz());
        }
        referenceMapping.setPropertyName(name);
    }

    private void bindComponent(ConfigurationHelper componentConf, AliasMapping aliasMapping,
                               ComponentMapping compMapping) {
        String name = componentConf.getAttribute("name");
        compMapping.setName(name);
        String refAlias = componentConf.getAttribute("ref-alias", null);
        compMapping.setRefAlias(valueLookup.lookupAliasName(refAlias));

        int maxDepth = componentConf.getAttributeAsInteger("max-depth", 5);
        compMapping.setMaxDepth(maxDepth);

        bindConverter(componentConf, compMapping);

        compMapping.setAccessor(componentConf.getAttribute("accessor", null));
        if (aliasMapping instanceof ClassMapping) {
            compMapping.setObjClass(((ClassMapping) aliasMapping).getClazz());
        }
        compMapping.setPropertyName(name);

        boolean override = componentConf.getAttributeAsBoolean("override", true);
        compMapping.setOverrideByName(override);
    }

    private void bindParent(ConfigurationHelper parentConf, AliasMapping aliasMapping, ParentMapping parentMapping) {
        String name = parentConf.getAttribute("name");
        parentMapping.setName(name);
        bindConverter(parentConf, parentMapping);

        parentMapping.setAccessor(parentConf.getAttribute("accessor", null));
        if (aliasMapping instanceof ClassMapping) {
            parentMapping.setObjClass(((ClassMapping) aliasMapping).getClazz());
        }
        parentMapping.setPropertyName(name);
    }

    private void bindClassProperty(ConfigurationHelper classPropertyConf, AliasMapping aliasMapping,
                                   ClassPropertyMapping classPropertyMapping) {
        String name = classPropertyConf.getAttribute("name");
        classPropertyMapping.setName(name);

        String sClass = classPropertyConf.getAttribute("class", null);
        classPropertyMapping.setClassName(sClass);

        classPropertyMapping.setBoost(getBoost(classPropertyConf));

        classPropertyMapping.setColClassName(classPropertyConf.getAttribute("col-class", null));

        classPropertyMapping.setAccessor(classPropertyConf.getAttribute("accessor", null));
        if (aliasMapping instanceof ClassMapping) {
            classPropertyMapping.setObjClass(((ClassMapping) aliasMapping).getClazz());
        }
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

        boolean override = classPropertyConf.getAttributeAsBoolean("override", true);
        classPropertyMapping.setOverrideByName(override);

        bindConverter(classPropertyConf, classPropertyMapping);

        ConfigurationHelper[] metadatas = classPropertyConf.getChildren("meta-data");
        for (int i = 0; i < metadatas.length; i++) {
            ClassPropertyMetaDataMapping mdMapping = new ClassPropertyMetaDataMapping();
            bindMetaData(metadatas[i], classPropertyMapping, mdMapping);
            classPropertyMapping.addMapping(mdMapping);
        }
    }

    private void bindConstant(ConfigurationHelper constantConf, AliasMapping classMapping,
                              ConstantMetaDataMapping constantMapping) {
        ConfigurationHelper metadataConf = constantConf.getChild("meta-data");
        String metaDataValue = metadataConf.getValue().trim();
        constantMapping.setName(valueLookup.lookupMetaDataName(metaDataValue));
        constantMapping.setBoost(getBoost(metadataConf, 1.0f));
        String storeType = metadataConf.getAttribute("store", "yes");
        constantMapping.setStore(Property.Store.fromString(storeType));
        String indexType = metadataConf.getAttribute("index", "tokenized");
        constantMapping.setIndex(Property.Index.fromString(indexType));
        String termVectorType = metadataConf.getAttribute("term-vector", "no");
        constantMapping.setTermVector(Property.TermVector.fromString(termVectorType));

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

    private void bindMetaData(ConfigurationHelper metadataConf, ClassPropertyMapping classPropertyMapping,
                              ClassPropertyMetaDataMapping mdMapping) {
        String name = valueLookup.lookupMetaDataName(metadataConf.getValue().trim());
        mdMapping.setBoost(getBoost(metadataConf, classPropertyMapping.getBoost()));
        mdMapping.setName(name);

        mdMapping.setAccessor(classPropertyMapping.getAccessor());
        mdMapping.setObjClass(classPropertyMapping.getObjClass());
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
            ArrayList extendedMappings = new ArrayList();
            StringTokenizer st = new StringTokenizer(extendsAliases, ",");
            while (st.hasMoreTokens()) {
                String extendedAlias = st.nextToken().trim();
                Alias alias = valueLookup.lookupAlias(extendedAlias);
                if (alias == null) {
                    extendedMappings.add(extendedAlias);
                } else {
                    extendedMappings.add(alias.getName());
                }
            }
            mapping.setExtendedMappings((String[]) extendedMappings.toArray(new String[extendedMappings.size()]));
        }
    }

    private void bindConverter(ConfigurationHelper conf, Mapping mapping) {
        String converterName = conf.getAttribute("converter", null);
        mapping.setConverterName(converterName);
    }

    private static float getBoost(ConfigurationHelper conf) {
        return getBoost(conf, 1.0f);
    }

    private static float getBoost(ConfigurationHelper conf, float defaultBoost) {
        return conf.getAttributeAsFloat("boost", defaultBoost);
    }

}
