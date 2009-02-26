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

package org.compass.core.config.binding;

import java.util.ArrayList;
import java.util.StringTokenizer;

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
import org.compass.core.mapping.Cascade;
import org.compass.core.mapping.ExcludeFromAll;
import org.compass.core.mapping.MappingException;
import org.compass.core.mapping.ReverseType;
import org.compass.core.mapping.SpellCheck;
import org.compass.core.mapping.internal.DefaultAllMapping;
import org.compass.core.mapping.internal.DefaultContractMapping;
import org.compass.core.mapping.internal.InternalAliasMapping;
import org.compass.core.mapping.internal.InternalCascadeMapping;
import org.compass.core.mapping.internal.InternalCompassMapping;
import org.compass.core.mapping.internal.InternalContractMapping;
import org.compass.core.mapping.internal.InternalMapping;
import org.compass.core.mapping.internal.InternalResourceMapping;
import org.compass.core.mapping.internal.InternalResourcePropertyMapping;
import org.compass.core.mapping.json.JsonArrayMapping;
import org.compass.core.mapping.json.JsonBoostPropertyMapping;
import org.compass.core.mapping.json.JsonContentMapping;
import org.compass.core.mapping.json.JsonIdMapping;
import org.compass.core.mapping.json.JsonPropertyAnalyzerController;
import org.compass.core.mapping.json.JsonPropertyMapping;
import org.compass.core.mapping.json.Naming;
import org.compass.core.mapping.json.PlainJsonObjectMapping;
import org.compass.core.mapping.json.RootJsonObjectMapping;
import org.compass.core.mapping.osem.*;
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
import org.compass.core.util.StringUtils;
import org.compass.core.util.config.ConfigurationHelper;

/**
 * Binds a {@link org.compass.core.util.config.ConfigurationHelper} content into Compass mappings.
 *
 * @author kimchy
 */
public abstract class PlainMappingBinding extends AbstractConfigurationHelperMappingBinding {

    private CommonMetaDataLookup valueLookup;

    public void setUpBinding(InternalCompassMapping mapping, CompassMetaData metaData, CompassSettings settings) {
        super.setUpBinding(mapping, metaData, settings);
        this.valueLookup = new CommonMetaDataLookup(metaData);
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
        for (ConfigurationHelper contractConf : doc.getChildren("contract")) {
            DefaultContractMapping contractMapping = new DefaultContractMapping();
            bindContract(contractConf, contractMapping);
            mapping.addMapping(contractMapping);
        }
        for (ConfigurationHelper resourceContractConf : doc.getChildren("resource-contract")) {
            DefaultContractMapping contractMapping = new DefaultContractMapping();
            bindResourceContract(resourceContractConf, contractMapping);
            mapping.addMapping(contractMapping);
        }
        for (ConfigurationHelper jsonContractConf : doc.getChildren("json-contract")) {
            DefaultContractMapping contractMapping = new DefaultContractMapping();
            bindJsonContract(jsonContractConf, contractMapping);
            mapping.addMapping(contractMapping);
        }
        for (ConfigurationHelper xmlContractConf : doc.getChildren("xml-contract")) {
            DefaultContractMapping contractMapping = new DefaultContractMapping();
            bindXmlContract(xmlContractConf, contractMapping);
            mapping.addMapping(contractMapping);
        }
        for (ConfigurationHelper classConf : doc.getChildren("class")) {
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
        for (ConfigurationHelper conf : doc.getChildren("xml-object", "xml")) {
            XmlObjectMapping xmlObjectMapping = new XmlObjectMapping();
            bindXmlObject(conf, xmlObjectMapping);
            mapping.addMapping(xmlObjectMapping);
        }
        for (ConfigurationHelper conf : doc.getChildren("root-json-object", "json-object", "json")) {
            RootJsonObjectMapping rootJsonObjectMapping = new RootJsonObjectMapping();
            bindJsonRootObject(conf, rootJsonObjectMapping);
            mapping.addMapping(rootJsonObjectMapping);
        }

        return true;
    }


    private void bindJsonContract(ConfigurationHelper contractConf, InternalContractMapping contractMapping)
            throws ConfigurationException {
        String aliasValue = contractConf.getAttribute("alias");
        Alias alias = valueLookup.lookupAlias(aliasValue);
        if (alias == null) {
            contractMapping.setAlias(aliasValue);
        } else {
            contractMapping.setAlias(alias.getName());
        }
        bindExtends(contractConf, contractMapping);

        bindJsonChildren(contractConf, contractMapping);
    }

    private void bindJsonRootObject(ConfigurationHelper jsonObjectConf, RootJsonObjectMapping rootJsonObjectMapping)
            throws ConfigurationException {
        String aliasValue = jsonObjectConf.getAttribute("alias");
        Alias alias = valueLookup.lookupAlias(aliasValue);
        if (alias == null) {
            rootJsonObjectMapping.setAlias(aliasValue);
        } else {
            rootJsonObjectMapping.setAlias(alias.getName());
        }

        bindSubIndexHash(jsonObjectConf, rootJsonObjectMapping);

        bindExtends(jsonObjectConf, rootJsonObjectMapping);

        bindAll(jsonObjectConf, rootJsonObjectMapping);
        bindSpellCheck(jsonObjectConf, rootJsonObjectMapping);

        String analyzer = jsonObjectConf.getAttribute("analyzer", null);
        rootJsonObjectMapping.setAnalyzer(analyzer);

        rootJsonObjectMapping.setRoot(true);
        rootJsonObjectMapping.setBoost(getBoost(jsonObjectConf));

        bindConverter(jsonObjectConf, rootJsonObjectMapping);

        rootJsonObjectMapping.setDynamic(jsonObjectConf.getAttributeAsBoolean("dynamic", false));
        rootJsonObjectMapping.setDynamicNaming(Naming.fromString(jsonObjectConf.getAttribute("dynamic-naming-type", Naming.PLAIN.toString())));

        bindJsonChildren(jsonObjectConf, rootJsonObjectMapping);
    }

    private void bindJsonChildren(ConfigurationHelper jsonObjectConf, InternalAliasMapping rootJsonObjectMapping) {
        for (ConfigurationHelper id : jsonObjectConf.getChildren("json-id", "id")) {
            JsonIdMapping jsonIdMapping = new JsonIdMapping();
            bindJsonProperty(id, jsonIdMapping, rootJsonObjectMapping);
            rootJsonObjectMapping.addMapping(jsonIdMapping);
        }

        for (ConfigurationHelper prop : jsonObjectConf.getChildren("json-property", "property")) {
            JsonPropertyMapping jsonPropertyMapping = new JsonPropertyMapping();
            bindJsonProperty(prop, jsonPropertyMapping, rootJsonObjectMapping);
            rootJsonObjectMapping.addMapping(jsonPropertyMapping);
        }

        ConfigurationHelper jsonContentConf = jsonObjectConf.getChild("json-content", false);
        if (jsonContentConf == null) {
            jsonContentConf = jsonObjectConf.getChild("content", false);
        }
        if (jsonContentConf != null) {
            JsonContentMapping jsonContentMapping = new JsonContentMapping();
            bindJsonContent(jsonContentConf, jsonContentMapping);
            rootJsonObjectMapping.addMapping(jsonContentMapping);
        }

        for (ConfigurationHelper obj : jsonObjectConf.getChildren("json-object", "object")) {
            PlainJsonObjectMapping jsonObjectMapping = new PlainJsonObjectMapping();
            bindJsonPlainObject(obj, jsonObjectMapping, rootJsonObjectMapping);
            rootJsonObjectMapping.addMapping(jsonObjectMapping);
        }

        for (ConfigurationHelper arr : jsonObjectConf.getChildren("json-array", "array")) {
            JsonArrayMapping jsonArrayMapping = new JsonArrayMapping();
            bindJsonArray(arr, jsonArrayMapping, rootJsonObjectMapping);
            rootJsonObjectMapping.addMapping(jsonArrayMapping);
        }

        ConfigurationHelper analyzerConf = jsonObjectConf.getChild("json-analyzer", false);
        if (analyzerConf == null) {
            analyzerConf = jsonObjectConf.getChild("analyzer", false);
        }
        if (analyzerConf != null) {
            JsonPropertyAnalyzerController analyzerController = new JsonPropertyAnalyzerController();
            bindJsonProperty(analyzerConf, analyzerController, rootJsonObjectMapping);
            analyzerController.setNullAnalyzer(analyzerConf.getAttribute("null-analyzer", null));
            rootJsonObjectMapping.addMapping(analyzerController);
        }

        ConfigurationHelper boostConf = jsonObjectConf.getChild("json-boost", false);
        if (boostConf == null) {
            boostConf = jsonObjectConf.getChild("boost", false);
        }
        if (boostConf != null) {
            JsonBoostPropertyMapping boostPropertyMapping = new JsonBoostPropertyMapping();
            bindJsonProperty(boostConf, boostPropertyMapping, rootJsonObjectMapping);
            String defaultBoost = boostConf.getAttribute("default", null);
            if (defaultBoost != null) {
                boostPropertyMapping.setDefaultBoost(Float.parseFloat(defaultBoost));
            }
            rootJsonObjectMapping.addMapping(boostPropertyMapping);
        }
    }

    private void bindJsonArray(ConfigurationHelper jsonArrayConf, JsonArrayMapping jsonArrayMapping,
                               InternalAliasMapping rootJsonObjectMapping) {
        String name = jsonArrayConf.getAttribute("name", null);
        if (name != null) {
            name = valueLookup.lookupMetaDataName(name);
        }
        jsonArrayMapping.setName(name);

        String indexName = jsonArrayConf.getAttribute("index-name", name);
        jsonArrayMapping.setPath((indexName == null ? null : new StaticPropertyPath(indexName)));

        jsonArrayMapping.setDynamic(jsonArrayConf.getAttributeAsBoolean("dynamic", false));
        jsonArrayMapping.setDynamicNaming(Naming.fromString(jsonArrayConf.getAttribute("dynamic-naming-type", Naming.PLAIN.toString())));

        bindConverter(jsonArrayConf, jsonArrayMapping);

        ConfigurationHelper conf = jsonArrayConf.getChild("json-property", false);
        if (conf != null) {
            JsonPropertyMapping jsonPropertyMapping = new JsonPropertyMapping();
            bindJsonProperty(conf, jsonPropertyMapping, rootJsonObjectMapping);
            if (jsonPropertyMapping.getName() == null) {
                jsonPropertyMapping.setName(jsonArrayMapping.getName());
            }
            if (jsonPropertyMapping.getPath() == null) {
                jsonPropertyMapping.setPath(jsonArrayMapping.getPath());
            }
            jsonArrayMapping.setElementMapping(jsonPropertyMapping);
        }

        conf = jsonArrayConf.getChild("json-object", false);
        if (conf != null) {
            PlainJsonObjectMapping jsonObjectMapping = new PlainJsonObjectMapping();
            bindJsonPlainObject(conf, jsonObjectMapping, rootJsonObjectMapping);
            if (jsonObjectMapping.getName() == null) {
                jsonObjectMapping.setName(jsonArrayMapping.getName());
            }
            if (jsonObjectMapping.getPath() == null) {
                jsonObjectMapping.setPath(jsonArrayMapping.getPath());
            }
            jsonArrayMapping.setElementMapping(jsonObjectMapping);
        }

        conf = jsonArrayConf.getChild("json-array", false);
        if (conf != null) {
            JsonArrayMapping intenralJsonArrayMapping = new JsonArrayMapping();
            bindJsonArray(conf, intenralJsonArrayMapping, rootJsonObjectMapping);
            if (intenralJsonArrayMapping.getName() == null) {
                intenralJsonArrayMapping.setName(jsonArrayMapping.getName());
            }
            if (intenralJsonArrayMapping.getPath() == null) {
                intenralJsonArrayMapping.setPath(jsonArrayMapping.getPath());
            }
            jsonArrayMapping.setElementMapping(intenralJsonArrayMapping);
        }
    }

    private void bindJsonPlainObject(ConfigurationHelper jsonObjectConf, PlainJsonObjectMapping jsonObjectMapping,
                                     InternalAliasMapping rootJsonObjectMapping) {
        String name = jsonObjectConf.getAttribute("name", null);
        if (name != null) {
            name = valueLookup.lookupMetaDataName(name);
        }
        jsonObjectMapping.setName(name);
        if (name != null) {
            jsonObjectMapping.setPath(new StaticPropertyPath(name));
        }
        bindConverter(jsonObjectConf, jsonObjectMapping);

        jsonObjectMapping.setDynamic(jsonObjectConf.getAttributeAsBoolean("dynamic", false));
        jsonObjectMapping.setDynamicNaming(Naming.fromString(jsonObjectConf.getAttribute("dynamic-naming-type", Naming.PLAIN.toString())));

        for (ConfigurationHelper prop : jsonObjectConf.getChildren("json-property")) {
            JsonPropertyMapping jsonPropertyMapping = new JsonPropertyMapping();
            bindJsonProperty(prop, jsonPropertyMapping, rootJsonObjectMapping);
            jsonObjectMapping.addMapping(jsonPropertyMapping);
        }

        for (ConfigurationHelper obj : jsonObjectConf.getChildren("json-object")) {
            PlainJsonObjectMapping intenralJsonObjectMapping = new PlainJsonObjectMapping();
            bindJsonPlainObject(obj, intenralJsonObjectMapping, rootJsonObjectMapping);
            jsonObjectMapping.addMapping(jsonObjectMapping);
        }

        for (ConfigurationHelper arr : jsonObjectConf.getChildren("json-array")) {
            JsonArrayMapping jsonArrayMapping = new JsonArrayMapping();
            bindJsonArray(arr, jsonArrayMapping, rootJsonObjectMapping);
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
        if (storeType != null) {
            jsonContentMapping.setStore(Property.Store.fromString(storeType));
        }
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

        String indexName = jsonPropConf.getAttribute("index-name", name);
        jsonPropertyMapping.setPath((indexName == null ? null : new StaticPropertyPath(indexName)));

        bindConverter(jsonPropConf, jsonPropertyMapping);

        String format = jsonPropConf.getAttribute("format", null);
        if (format != null) {
            jsonPropertyMapping.setFormat(format);
            jsonPropertyMapping.setValueConverter(new FormatDelegateConverter(format));
        }

        String namingType = jsonPropConf.getAttribute("naming-type", Naming.PLAIN.toString());
        jsonPropertyMapping.setNamingType(Naming.fromString(namingType));


        bindResourcePropertyMapping(jsonPropConf, jsonPropertyMapping, aliasMapping);


        boolean override = jsonPropConf.getAttributeAsBoolean("override", false);
        jsonPropertyMapping.setOverrideByName(override);

        jsonPropertyMapping.setValueConverterName(jsonPropConf.getAttribute("value-converter", null));

        bindSpellCheck(jsonPropConf, jsonPropertyMapping);
    }

    private void bindXmlContract(ConfigurationHelper contractConf, InternalContractMapping contractMapping)
            throws ConfigurationException {
        String aliasValue = contractConf.getAttribute("alias");
        Alias alias = valueLookup.lookupAlias(aliasValue);
        if (alias == null) {
            contractMapping.setAlias(aliasValue);
        } else {
            contractMapping.setAlias(alias.getName());
        }
        bindExtends(contractConf, contractMapping);

        bindXmlObjectChildren(contractConf, contractMapping);
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

    private void bindXmlObjectChildren(ConfigurationHelper resourceConf, InternalAliasMapping resourceMapping) {
        ConfigurationHelper[] ids = resourceConf.getChildren("xml-id", "id");
        for (ConfigurationHelper id : ids) {
            XmlIdMapping xmlIdMapping = new XmlIdMapping();
            bindXmlProperty(id, xmlIdMapping, resourceMapping);
            resourceMapping.addMapping(xmlIdMapping);
        }

        ConfigurationHelper[] properties = resourceConf.getChildren("xml-property", "property");
        for (ConfigurationHelper property : properties) {
            XmlPropertyMapping xmlPropertyMapping = new XmlPropertyMapping();
            bindXmlProperty(property, xmlPropertyMapping, resourceMapping);
            resourceMapping.addMapping(xmlPropertyMapping);
        }

        ConfigurationHelper xmlContentConf = resourceConf.getChild("xml-content", false);
        if (xmlContentConf == null) {
            xmlContentConf = resourceConf.getChild("content", false);
        }
        if (xmlContentConf != null) {
            XmlContentMapping xmlContentMapping = new XmlContentMapping();
            bindXmlContent(xmlContentConf, xmlContentMapping);
            resourceMapping.addMapping(xmlContentMapping);
        }

        ConfigurationHelper analyzerConf = resourceConf.getChild("xml-analyzer", false);
        if (analyzerConf == null) {
            analyzerConf = resourceConf.getChild("analyzer", false);
        }
        if (analyzerConf != null) {
            XmlPropertyAnalyzerController analyzerController = new XmlPropertyAnalyzerController();
            bindXmlProperty(analyzerConf, analyzerController, resourceMapping);
            analyzerController.setNullAnalyzer(analyzerConf.getAttribute("null-analyzer", null));
            resourceMapping.addMapping(analyzerController);
        }

        ConfigurationHelper boostConf = resourceConf.getChild("xml-boost", false);
        if (boostConf == null) {
            boostConf = resourceConf.getChild("boost", false);
        }
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

    private void bindResourceContract(ConfigurationHelper contractConf, InternalContractMapping contractMapping)
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

    private void bindResourceMappingChildren(ConfigurationHelper resourceConf, InternalAliasMapping resourceMapping) {
        ConfigurationHelper[] ids = resourceConf.getChildren("resource-id", "id");
        for (ConfigurationHelper id : ids) {
            RawResourcePropertyIdMapping rawIdPropertyMapping = new RawResourcePropertyIdMapping();
            bindResourceProperty(id, rawIdPropertyMapping, resourceMapping);
            resourceMapping.addMapping(rawIdPropertyMapping);
        }

        ConfigurationHelper[] properties = resourceConf.getChildren("resource-property", "property");
        for (ConfigurationHelper property : properties) {
            RawResourcePropertyMapping rawPropertyMapping = new RawResourcePropertyMapping();
            bindResourceProperty(property, rawPropertyMapping, resourceMapping);
            resourceMapping.addMapping(rawPropertyMapping);
        }

        ConfigurationHelper analyzerConf = resourceConf.getChild("resource-analyzer", false);
        if (analyzerConf == null) {
            analyzerConf = resourceConf.getChild("analyzer", false);
        }
        if (analyzerConf != null) {
            RawResourcePropertyAnalyzerController analyzerController = new RawResourcePropertyAnalyzerController();
            bindResourceProperty(analyzerConf, analyzerController, resourceMapping);
            analyzerController.setNullAnalyzer(analyzerConf.getAttribute("null-analyzer", null));
            resourceMapping.addMapping(analyzerController);
        }

        ConfigurationHelper boostConf = resourceConf.getChild("resource-boost", false);
        if (boostConf == null) {
            boostConf = resourceConf.getChild("boost", false);
        }
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

    private void bindContract(ConfigurationHelper contractConf, InternalContractMapping contractMapping)
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
            classMapping.setManagedId(ManagedId.fromString(managedId));
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
        String filterDuplicates = classConf.getAttribute("filter-duplicates", null);
        if (filterDuplicates != null) {
            classMapping.setFilterDuplicates(filterDuplicates.equalsIgnoreCase("true"));
        }

        bindConverter(classConf, classMapping);

        bindClassMappingChildren(classConf, classMapping);
    }

    private void bindClassMappingChildren(ConfigurationHelper classConf, InternalAliasMapping classMapping) {
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

        ConfigurationHelper[] dynamicPropertyConfs = classConf.getChildren("dynamic-property");
        for (ConfigurationHelper dynamicConf : dynamicPropertyConfs) {
            ClassDynamicPropertyMapping dynamicPropertyMapping = new ClassDynamicPropertyMapping();
            bindClassDynamicProperty(dynamicConf, classMapping, dynamicPropertyMapping);
            classMapping.addMapping(dynamicPropertyMapping);
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
            bindPlainCascading(cascadeConf, classMapping, cascadeMapping);
            classMapping.addMapping(cascadeMapping);
        }
    }

    private void bindPlainCascading(ConfigurationHelper conf, AliasMapping aliasMapping, PlainCascadeMapping cascadeMapping) {
        String name = conf.getAttribute("name");
        cascadeMapping.setName(name);

        cascadeMapping.setAccessor(conf.getAttribute("accessor", null));
        cascadeMapping.setPropertyName(name);
        cascadeMapping.setDefinedInAlias(aliasMapping.getAlias());

        bindConverter(conf, cascadeMapping);
        bindCascade(conf, cascadeMapping, "all");
    }

    private void bindDynamicMetaData(ConfigurationHelper dynamicConf, AliasMapping aliasMapping,
                                     DynamicMetaDataMapping dynamicMetaDataMapping) {
        String name = valueLookup.lookupMetaDataName(dynamicConf.getAttribute("name"));
        dynamicMetaDataMapping.setBoost(getBoost(dynamicConf));
        dynamicMetaDataMapping.setName(name);
        dynamicMetaDataMapping.setPath(new StaticPropertyPath(name));

        dynamicMetaDataMapping.setExpression(dynamicConf.getAttributeOrValue("expression").trim());

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

        String sLazy = referenceConf.getAttribute("lazy", null);
        if (sLazy != null) {
            referenceMapping.setLazy(sLazy.equalsIgnoreCase("true"));
        }

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

        compMapping.setPrefix(componentConf.getAttribute("prefix", null));

        bindConverter(componentConf, compMapping);

        compMapping.setAccessor(componentConf.getAttribute("accessor", null));
        compMapping.setPropertyName(name);

        boolean override = componentConf.getAttributeAsBoolean("override", true);
        compMapping.setOverrideByName(override);

        bindCascade(componentConf, compMapping, null);
    }

    private void bindCascade(ConfigurationHelper refConf, InternalCascadeMapping cascadeMapping, String defaultValue) {
        String commaSeparatedCascades = refConf.getAttribute("cascade", defaultValue);
        if (commaSeparatedCascades == null) {
            return;
        }
        if ("none".equals(commaSeparatedCascades)) {
            return;
        }
        ArrayList<Cascade> cascades = new ArrayList<Cascade>();
        StringTokenizer st = new StringTokenizer(commaSeparatedCascades, ",");
        while (st.hasMoreTokens()) {
            String cascade = st.nextToken().trim();
            cascades.add(Cascade.fromString(cascade));
        }
        if (cascades.size() > 0) {
            cascadeMapping.setCascades(cascades.toArray(new Cascade[cascades.size()]));
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

        classPropertyMapping.setAccessor(classPropertyConf.getAttribute("accessor", null));
        classPropertyMapping.setPropertyName(name);

        classPropertyMapping.setAnalyzer(classPropertyConf.getAttribute("analyzer", aliasMapping.getAnalyzer()));

        String excludeFromAll = classPropertyConf.getAttribute("exclude-from-all", "no");
        classPropertyMapping.setExcludeFromAll(ExcludeFromAll.fromString(excludeFromAll));

        String managedId = classPropertyConf.getAttribute("managed-id", null);
        if (managedId != null) {
            classPropertyMapping.setManagedId(ManagedId.fromString(managedId));
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

    private void bindClassDynamicProperty(ConfigurationHelper dynamicPropertyConf, AliasMapping classMapping,
                                          ClassDynamicPropertyMapping dynamicPropertyMapping) {
        String name = dynamicPropertyConf.getAttribute("name");
        dynamicPropertyMapping.setName(name);

        dynamicPropertyMapping.setDefinedInAlias(classMapping.getAlias());

        dynamicPropertyMapping.setOverrideByName(dynamicPropertyConf.getAttributeAsBoolean("override", true));

        dynamicPropertyMapping.setAccessor(dynamicPropertyConf.getAttribute("accessor", null));
        dynamicPropertyMapping.setPropertyName(name);

        dynamicPropertyMapping.setNamePrefix(dynamicPropertyConf.getAttribute("name-prefix", null));

        dynamicPropertyMapping.setNameProperty(dynamicPropertyConf.getAttribute("name-property", null));
        dynamicPropertyMapping.setValueProperty(dynamicPropertyConf.getAttribute("value-property", null));

        dynamicPropertyMapping.setNameConverterName(dynamicPropertyConf.getAttribute("name-converter", null));
        dynamicPropertyMapping.setValueConverterName(dynamicPropertyConf.getAttribute("value-converter", null));

        String nameFormat = dynamicPropertyConf.getAttribute("name-format", null);
        if (nameFormat != null) {
            dynamicPropertyMapping.setNameFormat(nameFormat);
        }
        String valueFormat = dynamicPropertyConf.getAttribute("value-format", null);
        if (valueFormat != null) {
            dynamicPropertyMapping.setValueFormat(valueFormat);
        }

        bindConverter(dynamicPropertyConf, dynamicPropertyMapping);
        bindResourcePropertyMapping(dynamicPropertyConf, dynamicPropertyMapping.getResourcePropertyMapping(), classMapping);
    }

    private void bindConstant(ConfigurationHelper constantConf, AliasMapping classMapping,
                              ConstantMetaDataMapping constantMapping) {
        ConfigurationHelper metadataConf = constantConf.getChild("meta-data");
        if (!StringUtils.hasText(metadataConf.getAttributeOrValue("name"))) {
            throw new MappingException("Alias mapping [" + classMapping.getAlias() + "] has a constant mapping with an empty meta-data value");
        }
        String metaDataValue = metadataConf.getAttributeOrValue("name").trim();
        constantMapping.setName(valueLookup.lookupMetaDataName(metaDataValue));

        String excludeFromAll = constantConf.getAttribute("exclude-from-all", "no");
        constantMapping.setExcludeFromAll(ExcludeFromAll.fromString(excludeFromAll));

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
        String value = metadataConf.getAttributeOrValue("name").trim();
        if (!StringUtils.hasText(value)) {
            throw new MappingException("Alias mapping [" + aliasMapping.getAlias() + "] and property [" +
                    classPropertyMapping.getName() + "] has a meta-data mapping with no value");
        }
        String name = valueLookup.lookupMetaDataName(value);
        mdMapping.setName(name);
        mdMapping.setPath(new StaticPropertyPath(name));

        mdMapping.setAccessor(classPropertyMapping.getAccessor());
        mdMapping.setPropertyName(classPropertyMapping.getPropertyName());

        bindConverter(metadataConf, mdMapping);
        String format = metadataConf.getAttribute("format", null);
        if (mdMapping.getConverter() == null) {
            if (format == null) {
                format = valueLookup.lookupMetaDataFormat(value);
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

    private void bindExtends(ConfigurationHelper conf, InternalAliasMapping mapping) throws ConfigurationException {
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

    private void bindConverter(ConfigurationHelper conf, InternalMapping mapping) {
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
            String sOmitTf = allConf.getAttribute("omit-tf", null);
            if (sOmitTf != null) {
                allMapping.setOmitTf(sOmitTf.equalsIgnoreCase("true"));
            }
            String sExcludeAlias = allConf.getAttribute("exclude-alias", null);
            if (sExcludeAlias != null) {
                allMapping.setExcludeAlias(sExcludeAlias.equalsIgnoreCase("true"));
            }
            String sIncludeUnmappedProperties = allConf.getAttribute("include-unmapped-properties", null);
            if (sIncludeUnmappedProperties != null) {
                allMapping.setIncludePropertiesWithNoMappings(sIncludeUnmappedProperties.equalsIgnoreCase("true"));
            }
            allMapping.setProperty(allConf.getAttribute("name", null));
            allMapping.setSpellCheck(SpellCheck.fromString(allConf.getAttribute("spell-check", "na")));
        }
        resourceMapping.setAllMapping(allMapping);
    }

    private void bindSpellCheck(ConfigurationHelper conf, InternalResourcePropertyMapping mapping) {
        mapping.setSpellCheck(SpellCheck.fromString(conf.getAttribute("spell-check", "na")));
    }

    private void bindSpellCheck(ConfigurationHelper conf, InternalResourceMapping mapping) {
        mapping.setSpellCheck(SpellCheck.fromString(conf.getAttribute("spell-check", "na")));
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
        bindResourcePropertyMapping(conf, mapping, 1.0f, ExcludeFromAll.NO,
                aliasMapping.getAnalyzer());
    }

    private void bindResourcePropertyMapping(ConfigurationHelper conf, InternalResourcePropertyMapping mapping,
                                             float defaultBoost,
                                             ExcludeFromAll excludeFromAllType,
                                             String analyzer) {
        mapping.setBoost(getBoost(conf, defaultBoost));
        String storeType = conf.getAttribute("store", null);
        if (storeType != null) {
            mapping.setStore(Property.Store.fromString(storeType));
        }
        String indexType = conf.getAttribute("index", null);
        if (indexType != null) {
            mapping.setIndex(Property.Index.fromString(indexType));
        }
        String termVectorType = conf.getAttribute("term-vector", null);
        if (termVectorType != null) {
            mapping.setTermVector(Property.TermVector.fromString(termVectorType));
        }

        String omitNorms = conf.getAttribute("omit-norms", null);
        if (omitNorms != null) {
            mapping.setOmitNorms(Boolean.valueOf(omitNorms));
        }

        String omitTf = conf.getAttribute("omit-tf", null);
        if (omitTf != null) {
            mapping.setOmitTf(Boolean.valueOf(omitTf));
        }

        String reverseType = conf.getAttribute("reverse", "no");
        mapping.setReverse(ReverseType.fromString(reverseType));
        mapping.setAnalyzer(conf.getAttribute("analyzer", analyzer));
        mapping.setNullValue(conf.getAttribute("null-value", null));
        String excludeFromAll = conf.getAttribute("exclude-from-all", ExcludeFromAll.toString(excludeFromAllType));
        mapping.setExcludeFromAll(ExcludeFromAll.fromString(excludeFromAll));

        mapping.setInternal(false);
    }

    private static float getBoost(ConfigurationHelper conf) {
        return getBoost(conf, 1.0f);
    }

    private static float getBoost(ConfigurationHelper conf, float defaultBoost) {
        return conf.getAttributeAsFloat("boost", defaultBoost);
    }

}
