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
import org.compass.core.converter.mapping.support.FormatDelegateConverter;
import org.compass.core.engine.naming.StaticPropertyPath;
import org.compass.core.engine.subindex.ConstantSubIndexHash;
import org.compass.core.engine.subindex.SubIndexHash;
import org.compass.core.mapping.AliasMapping;
import org.compass.core.mapping.CascadeMapping;
import org.compass.core.mapping.ContractMapping;
import org.compass.core.mapping.Mapping;
import org.compass.core.mapping.MappingException;
import org.compass.core.mapping.ResourcePropertyMapping;
import org.compass.core.mapping.SpellCheckType;
import org.compass.core.mapping.internal.DefaultAllMapping;
import org.compass.core.mapping.internal.InternalCompassMapping;
import org.compass.core.mapping.internal.InternalResourceMapping;
import org.compass.core.mapping.internal.InternalResourcePropertyMapping;
import org.compass.core.mapping.json.JsonArrayMapping;
import org.compass.core.mapping.json.JsonContentMapping;
import org.compass.core.mapping.json.JsonIdMapping;
import org.compass.core.mapping.json.JsonPropertyMapping;
import org.compass.core.mapping.json.JsonRootObjectMapping;
import org.compass.core.mapping.json.PlainJsonObjectMapping;
import org.compass.core.mapping.osem.ClassBoostPropertyMapping;
import org.compass.core.mapping.osem.ClassIdPropertyMapping;
import org.compass.core.mapping.osem.ClassMapping;
import org.compass.core.mapping.osem.ClassPropertyAnalyzerController;
import org.compass.core.mapping.osem.ClassPropertyMapping;
import org.compass.core.mapping.osem.ClassPropertyMetaDataMapping;
import org.compass.core.mapping.osem.ComponentMapping;
import org.compass.core.mapping.osem.ConstantMetaDataMapping;
import org.compass.core.mapping.osem.DynamicMetaDataMapping;
import org.compass.core.mapping.osem.IdComponentMapping;
import org.compass.core.mapping.osem.ParentMapping;
import org.compass.core.mapping.osem.PlainCascadeMapping;
import org.compass.core.mapping.osem.ReferenceMapping;
import org.compass.core.mapping.rsem.RawBoostPropertyMapping;
import org.compass.core.mapping.rsem.RawResourceMapping;
import org.compass.core.mapping.rsem.RawResourcePropertyAnalyzerController;
import org.compass.core.mapping.rsem.RawResourcePropertyIdMapping;
import org.compass.core.mapping.rsem.RawResourcePropertyMapping;
import org.compass.core.mapping.support.AbstractResourceMapping;
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

    public void setUpBinding(InternalCompassMapping mapping, CompassMetaData metaData, CompassSettings settings) {
        super.setUpBinding(mapping, metaData, settings);
        this.valueLookup = new CommonMetaDataLookup(metaData);
    }

    protected EntityResolver doGetEntityResolver() {
        return new DTDEntityResolver();
    }

    public String[] getSuffixes() {
        return new String[]{".cpm.xml"};
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
        for (ConfigurationHelper aContractArr : contractArr) {
            ContractMapping contractMapping = new ContractMapping();
            bindContract(aContractArr, contractMapping);
            mapping.addMapping(contractMapping);
        }
        ConfigurationHelper[] resourceContractArr = doc.getChildren("resource-contract");
        for (ConfigurationHelper aResourceContractArr : resourceContractArr) {
            ContractMapping contractMapping = new ContractMapping();
            bindResourceContract(aResourceContractArr, contractMapping);
            mapping.addMapping(contractMapping);
        }
        ConfigurationHelper[] classArr = doc.getChildren("class");
        for (ConfigurationHelper classConf : classArr) {
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
            bindClass(classConf, classMapping, defaultPackage);
            if (newClassMapping) {
                mapping.addMapping(classMapping);
            }
        }
        for (ConfigurationHelper conf : doc.getChildren("resource")) {
            RawResourceMapping rawResourceMapping = new RawResourceMapping();
            bindResource(conf, rawResourceMapping);
            mapping.addMapping(rawResourceMapping);
        }
        for (ConfigurationHelper conf : doc.getChildren("xml-object")) {
            XmlObjectMapping xmlObjectMapping = new XmlObjectMapping();
            bindXmlObject(conf, xmlObjectMapping);
            mapping.addMapping(xmlObjectMapping);
        }
        for (ConfigurationHelper conf : doc.getChildren("root-json-object")) {
            JsonRootObjectMapping jsonRootObjectMapping = new JsonRootObjectMapping();
            bindRootJsonObject(conf, jsonRootObjectMapping);
            mapping.addMapping(jsonRootObjectMapping);
        }

        return true;
    }

    private void bindRootJsonObject(ConfigurationHelper jsonObjectConf, JsonRootObjectMapping jsonRootObjectMapping)
            throws ConfigurationException {
        String aliasValue = jsonObjectConf.getAttribute("alias");
        Alias alias = valueLookup.lookupAlias(aliasValue);
        if (alias == null) {
            jsonRootObjectMapping.setAlias(aliasValue);
        } else {
            jsonRootObjectMapping.setAlias(alias.getName());
        }

        bindSubIndexHash(jsonObjectConf, jsonRootObjectMapping);

        bindExtends(jsonObjectConf, jsonRootObjectMapping);

        bindAll(jsonObjectConf, jsonRootObjectMapping);
        bindSpellCheck(jsonObjectConf, jsonRootObjectMapping);

        String analyzer = jsonObjectConf.getAttribute("analyzer", null);
        jsonRootObjectMapping.setAnalyzer(analyzer);

        jsonRootObjectMapping.setRoot(true);
        jsonRootObjectMapping.setBoost(getBoost(jsonObjectConf));

        bindConverter(jsonObjectConf, jsonRootObjectMapping);

        for (ConfigurationHelper id : jsonObjectConf.getChildren("json-id")) {
            JsonIdMapping jsonIdMapping = new JsonIdMapping();
            bindJsonProperty(id, jsonIdMapping, jsonRootObjectMapping);
            jsonRootObjectMapping.addMapping(jsonIdMapping);
        }

        for (ConfigurationHelper prop : jsonObjectConf.getChildren("json-property")) {
            JsonPropertyMapping jsonPropertyMapping = new JsonPropertyMapping();
            bindJsonProperty(prop, jsonPropertyMapping, jsonRootObjectMapping);
            jsonRootObjectMapping.addMapping(jsonPropertyMapping);
        }

        ConfigurationHelper jsonContentConf = jsonObjectConf.getChild("json-content", false);
        if (jsonContentConf != null) {
            JsonContentMapping jsonContentMapping = new JsonContentMapping();
            bindJsonContent(jsonContentConf, jsonContentMapping);
            jsonRootObjectMapping.addMapping(jsonContentMapping);
        }

        for (ConfigurationHelper obj : jsonObjectConf.getChildren("json-object")) {
            PlainJsonObjectMapping jsonObjectMapping = new PlainJsonObjectMapping();
            bindJsonObject(obj, jsonObjectMapping, jsonRootObjectMapping);
            jsonRootObjectMapping.addMapping(jsonObjectMapping);
        }

        for (ConfigurationHelper arr : jsonObjectConf.getChildren("json-array")) {
            JsonArrayMapping jsonArrayMapping = new JsonArrayMapping();
            bindJsonArray(arr, jsonArrayMapping, jsonRootObjectMapping);
            jsonRootObjectMapping.addMapping(jsonArrayMapping);
        }
    }

    private void bindJsonArray(ConfigurationHelper jsonArrayConf, JsonArrayMapping jsonArrayMapping,
                               JsonRootObjectMapping jsonRootObjectMapping) {
        String name = jsonArrayConf.getAttribute("name", null);
        if (name != null) {
            name = valueLookup.lookupMetaDataName(name);
        }
        jsonArrayMapping.setName(name);
        jsonArrayMapping.setPath(new StaticPropertyPath(name));
        bindConverter(jsonArrayConf, jsonArrayMapping);

        ConfigurationHelper conf = jsonArrayConf.getChild("json-property", false);
        if (conf != null) {
            JsonPropertyMapping jsonPropertyMapping = new JsonPropertyMapping();
            bindJsonProperty(conf, jsonPropertyMapping, jsonRootObjectMapping);
            if (jsonPropertyMapping.getName() == null) {
                jsonPropertyMapping.setName(jsonArrayMapping.getName());
                jsonPropertyMapping.setPath(new StaticPropertyPath(jsonArrayMapping.getName()));
            }
            jsonArrayMapping.setElementMapping(jsonPropertyMapping);
        }

        conf = jsonArrayConf.getChild("json-object", false);
        if (conf != null) {
            PlainJsonObjectMapping jsonObjectMapping = new PlainJsonObjectMapping();
            bindJsonObject(conf, jsonObjectMapping, jsonRootObjectMapping);
            if (jsonObjectMapping.getName() == null) {
                jsonObjectMapping.setName(jsonArrayMapping.getName());
                jsonObjectMapping.setPath(new StaticPropertyPath(jsonArrayMapping.getName()));
            }
            jsonArrayMapping.setElementMapping(jsonObjectMapping);
        }

        conf = jsonArrayConf.getChild("json-array", false);
        if (conf != null) {
            JsonArrayMapping intenralJsonArrayMapping = new JsonArrayMapping();
            bindJsonArray(conf, intenralJsonArrayMapping, jsonRootObjectMapping);
            if (intenralJsonArrayMapping.getName() == null) {
                intenralJsonArrayMapping.setName(jsonArrayMapping.getName());
                intenralJsonArrayMapping.setPath(new StaticPropertyPath(jsonArrayMapping.getName()));
            }
            jsonArrayMapping.setElementMapping(intenralJsonArrayMapping);
        }
    }

    private void bindJsonObject(ConfigurationHelper jsonObjectConf, PlainJsonObjectMapping jsonObjectMapping,
                                JsonRootObjectMapping jsonRootObjectMapping) {
        String name = jsonObjectConf.getAttribute("name", null);
        if (name != null) {
            name = valueLookup.lookupMetaDataName(name);
        }
        jsonObjectMapping.setName(name);
        jsonObjectMapping.setPath(new StaticPropertyPath(name));
        bindConverter(jsonObjectConf, jsonObjectMapping);

        for (ConfigurationHelper prop : jsonObjectConf.getChildren("json-property")) {
            JsonPropertyMapping jsonPropertyMapping = new JsonPropertyMapping();
            bindJsonProperty(prop, jsonPropertyMapping, jsonRootObjectMapping);
            jsonObjectMapping.addMapping(jsonPropertyMapping);
        }

        for (ConfigurationHelper obj : jsonObjectConf.getChildren("json-object")) {
            PlainJsonObjectMapping intenralJsonObjectMapping = new PlainJsonObjectMapping();
            bindJsonObject(obj, intenralJsonObjectMapping, jsonRootObjectMapping);
            jsonObjectMapping.addMapping(jsonObjectMapping);
        }

        for (ConfigurationHelper arr : jsonObjectConf.getChildren("json-array")) {
            JsonArrayMapping jsonArrayMapping = new JsonArrayMapping();
            bindJsonArray(arr, jsonArrayMapping, jsonRootObjectMapping);
            jsonObjectMapping.addMapping(jsonArrayMapping);
        }
    }

    private void bindJsonContent(ConfigurationHelper jsonContentConf, JsonContentMapping jsonContentMapping) {
        String name = jsonContentConf.getAttribute("name", null);
        if (name != null) {
            name = valueLookup.lookupMetaDataName(name);
        }
        jsonContentMapping.setName(name);
        jsonContentMapping.setPath(new StaticPropertyPath(name));
        bindConverter(jsonContentConf, jsonContentMapping);
        String storeType = jsonContentConf.getAttribute("store", null);
        jsonContentMapping.setStore(Property.Store.fromString(storeType));
        jsonContentMapping.setInternal(true);
    }


    private void bindJsonProperty(ConfigurationHelper jsonPropConf, JsonPropertyMapping jsonPropertyMapping,
                                  AliasMapping aliasMapping) {
        String name = jsonPropConf.getAttribute("name", null);
        if (name != null) {
            name = valueLookup.lookupMetaDataName(name);
        }
        jsonPropertyMapping.setBoost(getBoost(jsonPropConf));
        jsonPropertyMapping.setName(name);
        jsonPropertyMapping.setPath((name == null ? null : new StaticPropertyPath(name)));
        bindConverter(jsonPropConf, jsonPropertyMapping);

        String format = jsonPropConf.getAttribute("format", null);
        if (format != null) {
            jsonPropertyMapping.setValueConverter(new FormatDelegateConverter(format));
        }


        bindResourcePropertyMapping(jsonPropConf, jsonPropertyMapping, aliasMapping);


        boolean override = jsonPropConf.getAttributeAsBoolean("override", true);
        jsonPropertyMapping.setOverrideByName(override);

        jsonPropertyMapping.setValueConverterName(jsonPropConf.getAttribute("value-converter", null));

        bindSpellCheck(jsonPropConf, jsonPropertyMapping);
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

        bindAll(xmlObjectConf, xmlObjectMapping);
        bindSpellCheck(xmlObjectConf, xmlObjectMapping);

        String analyzer = xmlObjectConf.getAttribute("analyzer", null);
        xmlObjectMapping.setAnalyzer(analyzer);

        xmlObjectMapping.setRoot(true);
        xmlObjectMapping.setBoost(getBoost(xmlObjectConf));

        xmlObjectMapping.setXPath(xmlObjectConf.getAttribute("xpath", null));

        bindConverter(xmlObjectConf, xmlObjectMapping);

        bindXmlObjectChildren(xmlObjectConf, xmlObjectMapping);
    }

    private void bindXmlObjectChildren(ConfigurationHelper resourceConf, AliasMapping resourceMapping) {
        ConfigurationHelper[] ids = resourceConf.getChildren("xml-id");
        for (ConfigurationHelper id : ids) {
            XmlIdMapping xmlIdMapping = new XmlIdMapping();
            bindXmlProperty(id, xmlIdMapping, resourceMapping);
            resourceMapping.addMapping(xmlIdMapping);
        }

        ConfigurationHelper[] properties = resourceConf.getChildren("xml-property");
        for (ConfigurationHelper property : properties) {
            XmlPropertyMapping xmlPropertyMapping = new XmlPropertyMapping();
            bindXmlProperty(property, xmlPropertyMapping, resourceMapping);
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
            bindXmlProperty(analyzerConf, analyzerController, resourceMapping);
            analyzerController.setNullAnalyzer(analyzerConf.getAttribute("null-analyzer", null));
            resourceMapping.addMapping(analyzerController);
        }

        ConfigurationHelper boostConf = resourceConf.getChild("xml-boost", false);
        if (boostConf != null) {
            XmlBoostPropertyMapping boostPropertyMapping = new XmlBoostPropertyMapping();
            bindXmlProperty(boostConf, boostPropertyMapping, resourceMapping);
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
        String storeType = xmlContentConf.getAttribute("store", null);
        xmlContentMapping.setStore(Property.Store.fromString(storeType));
        xmlContentMapping.setInternal(true);
    }

    private void bindXmlProperty(ConfigurationHelper xmlPropConf, XmlPropertyMapping xmlPropertyMapping,
                                 AliasMapping aliasMapping) {
        String name = xmlPropConf.getAttribute("name", null);
        if (name != null) {
            name = valueLookup.lookupMetaDataName(name);
        }
        xmlPropertyMapping.setBoost(getBoost(xmlPropConf));
        xmlPropertyMapping.setName(name);
        xmlPropertyMapping.setPath((name == null ? null : new StaticPropertyPath(name)));
        bindConverter(xmlPropConf, xmlPropertyMapping);

        String format = xmlPropConf.getAttribute("format", null);
        if (format != null) {
            xmlPropertyMapping.setValueConverter(new FormatDelegateConverter(format));
        }


        bindResourcePropertyMapping(xmlPropConf, xmlPropertyMapping, aliasMapping);


        boolean override = xmlPropConf.getAttributeAsBoolean("override", true);
        xmlPropertyMapping.setOverrideByName(override);

        xmlPropertyMapping.setXPath(xmlPropConf.getAttribute("xpath"));

        xmlPropertyMapping.setValueConverterName(xmlPropConf.getAttribute("value-converter", null));

        bindSpellCheck(xmlPropConf, xmlPropertyMapping);
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

        bindAll(resourceConf, rawResourceMapping);
        bindSpellCheck(resourceConf, rawResourceMapping);

        rawResourceMapping.setRoot(true);
        rawResourceMapping.setBoost(getBoost(resourceConf));

        bindResourceMappingChildren(resourceConf, rawResourceMapping);
    }

    private void bindResourceMappingChildren(ConfigurationHelper resourceConf, AliasMapping resourceMapping) {
        ConfigurationHelper[] ids = resourceConf.getChildren("resource-id");
        for (ConfigurationHelper id : ids) {
            RawResourcePropertyIdMapping rawIdPropertyMapping = new RawResourcePropertyIdMapping();
            bindResourceProperty(id, rawIdPropertyMapping, resourceMapping);
            resourceMapping.addMapping(rawIdPropertyMapping);
        }

        ConfigurationHelper[] properties = resourceConf.getChildren("resource-property");
        for (ConfigurationHelper property : properties) {
            RawResourcePropertyMapping rawPropertyMapping = new RawResourcePropertyMapping();
            bindResourceProperty(property, rawPropertyMapping, resourceMapping);
            resourceMapping.addMapping(rawPropertyMapping);
        }

        ConfigurationHelper analyzerConf = resourceConf.getChild("resource-analyzer", false);
        if (analyzerConf != null) {
            RawResourcePropertyAnalyzerController analyzerController = new RawResourcePropertyAnalyzerController();
            bindResourceProperty(analyzerConf, analyzerController, resourceMapping);
            analyzerController.setNullAnalyzer(analyzerConf.getAttribute("null-analyzer", null));
            resourceMapping.addMapping(analyzerController);
        }

        ConfigurationHelper boostConf = resourceConf.getChild("resource-boost", false);
        if (boostConf != null) {
            RawBoostPropertyMapping boostPropertyMapping = new RawBoostPropertyMapping();
            bindResourceProperty(boostConf, boostPropertyMapping, resourceMapping);
            String defaultBoost = boostConf.getAttribute("default", null);
            if (defaultBoost != null) {
                boostPropertyMapping.setDefaultBoost(Float.parseFloat(defaultBoost));
            }
            resourceMapping.addMapping(boostPropertyMapping);
        }
    }

    private void bindResourceProperty(ConfigurationHelper resourcePropConf, RawResourcePropertyMapping propertyMapping,
                                      AliasMapping aliasMapping) {
        String name = valueLookup.lookupMetaDataName(resourcePropConf.getAttribute("name"));
        propertyMapping.setBoost(getBoost(resourcePropConf));
        propertyMapping.setName(name);
        propertyMapping.setPath(new StaticPropertyPath(name));
        bindConverter(resourcePropConf, propertyMapping);

        String format = resourcePropConf.getAttribute("format", null);
        if (format != null) {
            propertyMapping.setConverter(new FormatDelegateConverter(format));
        }

        boolean override = resourcePropConf.getAttributeAsBoolean("override", true);
        propertyMapping.setOverrideByName(override);

        bindResourcePropertyMapping(resourcePropConf, propertyMapping, aliasMapping);

        bindSpellCheck(resourcePropConf, propertyMapping);
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
            Class clazz = ClassUtils.forName(classMapping.getName(), settings.getClassLoader());
            classMapping.setClazz(clazz);
        } catch (ClassNotFoundException e) {
            throw new ConfigurationException("Failed to find class [" + classMapping.getName() + "] and class loader [" + settings.getClassLoader() + "]");
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

        bindAll(classConf, classMapping);
        bindSpellCheck(classConf, classMapping);

        boolean poly = classConf.getAttributeAsBoolean("poly", false);
        classMapping.setPoly(poly);

        String managedId = classConf.getAttribute("managed-id", null);
        if (managedId != null) {
            classMapping.setManagedId(ClassPropertyMapping.ManagedId.fromString(managedId));
        }

        String polyClassName = classConf.getAttribute("poly-class", null);
        if (polyClassName != null) {
            try {
                classMapping.setPolyClass(ClassUtils.forName(polyClassName, settings.getClassLoader()));
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
        for (ConfigurationHelper id : ids) {
            ClassIdPropertyMapping idMapping = new ClassIdPropertyMapping();
            bindClassProperty(id, classMapping, idMapping);
            classMapping.addMapping(idMapping);
        }
        ConfigurationHelper[] idComponents = classConf.getChildren("id-component");
        for (ConfigurationHelper idComponent : idComponents) {
            IdComponentMapping idMapping = new IdComponentMapping();
            bindComponent(idComponent, classMapping, idMapping);
            classMapping.addMapping(idMapping);
        }
        ConfigurationHelper[] properties = classConf.getChildren("property");
        for (ConfigurationHelper property : properties) {
            ClassPropertyMapping classPropertyMapping = new ClassPropertyMapping();
            bindClassProperty(property, classMapping, classPropertyMapping);
            classMapping.addMapping(classPropertyMapping);
        }
        ConfigurationHelper[] components = classConf.getChildren("component");
        for (ConfigurationHelper component : components) {
            ComponentMapping compMapping = new ComponentMapping();
            bindComponent(component, classMapping, compMapping);
            classMapping.addMapping(compMapping);
        }
        ConfigurationHelper[] references = classConf.getChildren("reference");
        for (ConfigurationHelper reference : references) {
            ReferenceMapping referenceMapping = new ReferenceMapping();
            bindReference(reference, classMapping, referenceMapping);
            classMapping.addMapping(referenceMapping);
        }
        ConfigurationHelper[] constants = classConf.getChildren("constant");
        for (ConfigurationHelper constant : constants) {
            ConstantMetaDataMapping constantMapping = new ConstantMetaDataMapping();
            bindConstant(constant, classMapping, constantMapping);
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
        for (ConfigurationHelper dynamicConf : dynamicConfs) {
            DynamicMetaDataMapping dynamicMetaDataMapping = new DynamicMetaDataMapping();
            bindDynamicMetaData(dynamicConf, classMapping, dynamicMetaDataMapping);
            classMapping.addMapping(dynamicMetaDataMapping);
        }

        ConfigurationHelper[] cascadeConfs = classConf.getChildren("cascade");
        for (ConfigurationHelper cascadeConf : cascadeConfs) {
            PlainCascadeMapping cascadeMapping = new PlainCascadeMapping();
            bindPlainCascading(cascadeConf, cascadeMapping);
            classMapping.addMapping(cascadeMapping);
        }
    }

    private void bindPlainCascading(ConfigurationHelper conf, PlainCascadeMapping cascadeMapping) {
        String name = conf.getAttribute("name");
        cascadeMapping.setName(name);

        cascadeMapping.setAccessor(conf.getAttribute("accessor", null));
        cascadeMapping.setPropertyName(name);

        bindConverter(conf, cascadeMapping);
        bindCascade(conf, cascadeMapping, "all");
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
                dynamicMetaDataMapping.setType(ClassUtils.forName(type, settings.getClassLoader()));
            } catch (ClassNotFoundException e) {
                throw new MappingException("Failed to find class [" + type + "]", e);
            }
        }

        bindConverter(dynamicConf, dynamicMetaDataMapping);
        bindSpellCheck(dynamicConf, dynamicMetaDataMapping);
        bindResourcePropertyMapping(dynamicConf, dynamicMetaDataMapping, aliasMapping);

        boolean override = dynamicConf.getAttributeAsBoolean("override", true);
        dynamicMetaDataMapping.setOverrideByName(override);
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

        bindCascade(referenceConf, referenceMapping, null);
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

        bindCascade(componentConf, compMapping, null);
    }

    private void bindCascade(ConfigurationHelper refConf, CascadeMapping cascadeMapping, String defaultValue) {
        String commaSeparatedCascades = refConf.getAttribute("cascade", defaultValue);
        if (commaSeparatedCascades == null) {
            return;
        }
        if ("none".equals(commaSeparatedCascades)) {
            return;
        }
        ArrayList<CascadeMapping.Cascade> cascades = new ArrayList<CascadeMapping.Cascade>();
        StringTokenizer st = new StringTokenizer(commaSeparatedCascades, ",");
        while (st.hasMoreTokens()) {
            String cascade = st.nextToken().trim();
            cascades.add(CascadeMapping.Cascade.fromString(cascade));
        }
        if (cascades.size() > 0) {
            cascadeMapping.setCascades(cascades.toArray(new CascadeMapping.Cascade[cascades.size()]));
        }
    }

    private void bindParent(ConfigurationHelper parentConf, AliasMapping aliasMapping, ParentMapping parentMapping) {
        String name = parentConf.getAttribute("name");
        parentMapping.setName(name);
        bindConverter(parentConf, parentMapping);

        parentMapping.setAccessor(parentConf.getAttribute("accessor", null));
        parentMapping.setPropertyName(name);
        parentMapping.setDefinedInAlias(aliasMapping.getAlias());

        bindCascade(parentConf, parentMapping, null);
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

        classPropertyMapping.setAnalyzer(classPropertyConf.getAttribute("analyzer", aliasMapping.getAnalyzer()));

        String excludeFromAll = classPropertyConf.getAttribute("exclude-from-all", "no");
        classPropertyMapping.setExcludeFromAll(ResourcePropertyMapping.ExcludeFromAllType.fromString(excludeFromAll));

        String managedId = classPropertyConf.getAttribute("managed-id", null);
        if (managedId != null) {
            classPropertyMapping.setManagedId(ClassPropertyMapping.ManagedId.fromString(managedId));
        }

        String managedIdIndex = classPropertyConf.getAttribute("managed-id-index", null);
        if (managedIdIndex != null) {
            classPropertyMapping.setManagedIdIndex(Property.Index.fromString(managedIdIndex));
        }
        classPropertyMapping.setManagedIdConverterName(classPropertyConf.getAttribute("managed-id-converter", null));

        boolean override = classPropertyConf.getAttributeAsBoolean("override", true);
        classPropertyMapping.setOverrideByName(override);

        bindConverter(classPropertyConf, classPropertyMapping);

        ConfigurationHelper[] metadatas = classPropertyConf.getChildren("meta-data");
        for (ConfigurationHelper metadata : metadatas) {
            ClassPropertyMetaDataMapping mdMapping = new ClassPropertyMetaDataMapping();
            bindMetaData(metadata, aliasMapping, classPropertyMapping, mdMapping);
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

        String excludeFromAll = constantConf.getAttribute("exclude-from-all", "no");
        constantMapping.setExcludeFromAll(ResourcePropertyMapping.ExcludeFromAllType.fromString(excludeFromAll));

        bindResourcePropertyMapping(metadataConf, constantMapping, 1.0f, constantMapping.getExcludeFromAll(),
                classMapping.getAnalyzer());

        bindSpellCheck(constantConf, constantMapping);

        boolean override = constantConf.getAttributeAsBoolean("override", true);
        constantMapping.setOverrideByName(override);

        ConfigurationHelper[] values = constantConf.getChildren("meta-data-value");
        for (ConfigurationHelper value : values) {
            String metaDataValueValue = value.getValue().trim();
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
                mdMapping.setConverter(new FormatDelegateConverter(format));
            }
        } else {
            // just validate that both are not set, since it makes no sense
            if (format != null) {
                throw new ConfigurationException("Both converter and format are set for property [" +
                        classPropertyMapping.getName() + "], you should choose one or the other (since converter will" +
                        "not use the format defined)");
            }
        }

        bindResourcePropertyMapping(metadataConf, mdMapping, classPropertyMapping.getBoost(),
                classPropertyMapping.getExcludeFromAll(), classPropertyMapping.getAnalyzer());
        bindSpellCheck(metadataConf, mdMapping);
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
        ArrayList<String> aliases = new ArrayList<String>();
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
        return aliases.toArray(new String[aliases.size()]);
    }

    private void bindConverter(ConfigurationHelper conf, Mapping mapping) {
        String converterName = conf.getAttribute("converter", null);
        mapping.setConverterName(converterName);
    }

    private void bindAll(ConfigurationHelper conf, AbstractResourceMapping resourceMapping) {
        ConfigurationHelper allConf = conf.getChild("all", false);
        DefaultAllMapping allMapping = new DefaultAllMapping();
        if (allConf != null) {
            String sAllSupported = allConf.getAttribute("enable", null);
            if (sAllSupported != null) {
                allMapping.setSupported(sAllSupported.equalsIgnoreCase("true"));
            }
            String termVectorType = allConf.getAttribute("term-vector", null);
            if (termVectorType != null) {
                allMapping.setTermVector(Property.TermVector.fromString(termVectorType));
            }
            String sOmitNorms = allConf.getAttribute("omit-norms", null);
            if (sOmitNorms != null) {
                allMapping.setOmitNorms(sOmitNorms.equalsIgnoreCase("true"));
            }
            String sExcludeAlias = allConf.getAttribute("exclude-alias", null);
            if (sExcludeAlias != null) {
                allMapping.setExcludeAlias(sExcludeAlias.equalsIgnoreCase("true"));
            }
            allMapping.setProperty(allConf.getAttribute("name", null));
            allMapping.setSpellCheck(SpellCheckType.fromString(allConf.getAttribute("spell-check", "na")));
        }
        resourceMapping.setAllMapping(allMapping);
    }

    private void bindSpellCheck(ConfigurationHelper conf, InternalResourcePropertyMapping mapping) {
        mapping.setSpellCheck(SpellCheckType.fromString(conf.getAttribute("spell-check", "na")));
    }

    private void bindSpellCheck(ConfigurationHelper conf, InternalResourceMapping mapping) {
        mapping.setSpellCheck(SpellCheckType.fromString(conf.getAttribute("spell-check", "na")));
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
            subIndexHash = (SubIndexHash) ClassUtils.forName(type, settings.getClassLoader()).newInstance();
        } catch (Exception e) {
            throw new ConfigurationException("Failed to create sub index hash of type [" + type + "]", e);
        }
        CompassSettings settings = this.settings.copy().clear();
        ConfigurationHelper[] settingsConf = subIndexHashConf.getChildren("setting");
        if (subIndexHash instanceof CompassConfigurable) {
            for (ConfigurationHelper aSettingsConf : settingsConf) {
                settings.setSetting(aSettingsConf.getAttribute("name"), aSettingsConf.getAttribute("value"));
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

    private void bindResourcePropertyMapping(ConfigurationHelper conf, InternalResourcePropertyMapping mapping,
                                             AliasMapping aliasMapping) {
        bindResourcePropertyMapping(conf, mapping, 1.0f, ResourcePropertyMapping.ExcludeFromAllType.NO,
                aliasMapping.getAnalyzer());
    }

    private void bindResourcePropertyMapping(ConfigurationHelper conf, InternalResourcePropertyMapping mapping,
                                             float defaultBoost,
                                             ResourcePropertyMapping.ExcludeFromAllType excludeFromAllType,
                                             String analyzer) {
        mapping.setBoost(getBoost(conf, defaultBoost));
        String storeType = conf.getAttribute("store", null);
        mapping.setStore(Property.Store.fromString(storeType));
        String indexType = conf.getAttribute("index", null);
        mapping.setIndex(Property.Index.fromString(indexType));
        String termVectorType = conf.getAttribute("term-vector", null);
        mapping.setTermVector(Property.TermVector.fromString(termVectorType));

        String omitNorms = conf.getAttribute("omit-norms", null);
        if (omitNorms != null) {
            mapping.setOmitNorms(Boolean.valueOf(omitNorms));
        }

        String reverseType = conf.getAttribute("reverse", "no");
        mapping.setReverse(ResourcePropertyMapping.ReverseType.fromString(reverseType));
        mapping.setAnalyzer(conf.getAttribute("analyzer", analyzer));
        mapping.setNullValue(conf.getAttribute("null-value", null));
        String excludeFromAll = conf.getAttribute("exclude-from-all", ResourcePropertyMapping.ExcludeFromAllType.toString(excludeFromAllType));
        mapping.setExcludeFromAll(ResourcePropertyMapping.ExcludeFromAllType.fromString(excludeFromAll));

        mapping.setInternal(false);
    }

    private static float getBoost(ConfigurationHelper conf) {
        return getBoost(conf, 1.0f);
    }

    private static float getBoost(ConfigurationHelper conf, float defaultBoost) {
        return conf.getAttributeAsFloat("boost", defaultBoost);
    }

}
