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

package org.compass.core.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.compass.annotations.config.binding.AnnotationsMappingBinding;
import org.compass.annotations.config.binding.OverrideAnnotationsWithJsonCpmMappingBinding;
import org.compass.annotations.config.binding.OverrideAnnotationsWithXmlCpmMappingBinding;
import org.compass.core.Compass;
import org.compass.core.CompassException;
import org.compass.core.config.binding.JsonMetaDataBinding;
import org.compass.core.config.binding.JsonPlainMappingBinding;
import org.compass.core.config.binding.XmlMetaDataBinding;
import org.compass.core.config.binding.XmlPlainMappingBinding;
import org.compass.core.config.binding.scanner.Filter;
import org.compass.core.config.binding.scanner.ScanItem;
import org.compass.core.config.binding.scanner.Scanner;
import org.compass.core.config.binding.scanner.ScannerFactoy;
import org.compass.core.config.builder.ConfigurationBuilder;
import org.compass.core.config.builder.SmartConfigurationBuilder;
import org.compass.core.config.process.MappingProcessor;
import org.compass.core.converter.Converter;
import org.compass.core.converter.ConverterLookup;
import org.compass.core.converter.DefaultConverterLookup;
import org.compass.core.engine.naming.DefaultPropertyNamingStrategyFactory;
import org.compass.core.engine.naming.PropertyNamingStrategy;
import org.compass.core.engine.naming.PropertyNamingStrategyFactory;
import org.compass.core.executor.DefaultExecutorManager;
import org.compass.core.impl.DefaultCompass;
import org.compass.core.impl.RefreshableCompass;
import org.compass.core.mapping.CompassMapping;
import org.compass.core.mapping.ContractMapping;
import org.compass.core.mapping.ContractMappingProvider;
import org.compass.core.mapping.MappingException;
import org.compass.core.mapping.ResourceMapping;
import org.compass.core.mapping.ResourceMappingProvider;
import org.compass.core.mapping.internal.DefaultCompassMapping;
import org.compass.core.mapping.internal.InternalCompassMapping;
import org.compass.core.metadata.CompassMetaData;
import org.compass.core.metadata.impl.DefaultCompassMetaData;
import org.compass.core.util.ClassUtils;
import org.compass.core.util.matcher.AntPathMatcher;
import org.compass.core.util.matcher.PathMatcher;

/**
 * Used to configure <code>Compass</code> instances.
 * <p/>
 * An instance of it allows the application to specify settings and mapping
 * files to be used when creating <code>Compass</code>.
 * </p>
 * <p/>
 * There are several options to configure a <code>Compass</code> instance,
 * programmatically using the <code>CompassConfiguration</code> class, using
 * the xml configuration file (compass.cfg.xml), or a json configuration file, or a combination of all.
 * </p>
 * <p/>
 * Usually the application will create a single
 * <code>CompassConfiguration</code>, use it to configure and than build a
 * <code>Compass</code> instance, and than instantiate
 * <code>CompassSession</code>s in threads servicing client requests.
 * </p>
 * <p/>
 * The <code>CompassConfiguration</code> is meant only as an
 * initialization-time object. <code>Compass</code> is immutable and do not
 * affect the <code>CompassConfiguration</code> that created it.
 * </p>
 *
 * @author kimchy
 * @see org.compass.core.Compass
 */
public class CompassConfiguration {

    protected static final Log log = LogFactory.getLog(CompassConfiguration.class);

    private CompassMetaData metaData;

    private InternalCompassMapping mapping;

    private CompassSettings settings;

    private ClassLoader classLoader;

    protected CompassMappingBinding mappingBinding;

    protected ConfigurationBuilder configurationBuilder = new SmartConfigurationBuilder();

    private HashMap<String, ConverterHolder> temporaryConvertersByName = new HashMap<String, ConverterHolder>();

    public CompassConfiguration() {
        mapping = new DefaultCompassMapping();
        metaData = new DefaultCompassMetaData();

        settings = new CompassSettings();
    }

    private CompassMappingBinding getMappingBinding() {
        if (mappingBinding == null) {
            mappingBinding = new CompassMappingBinding();
            addMappingBindings(mappingBinding);
            mappingBinding.setUpBinding(mapping, metaData, settings);
        }
        return mappingBinding;
    }

    protected void addMappingBindings(CompassMappingBinding mappingBinding) {
        mappingBinding.addMappingBinding(new XmlMetaDataBinding());
        mappingBinding.addMappingBinding(new JsonMetaDataBinding());
        mappingBinding.addMappingBinding(new XmlPlainMappingBinding());
        mappingBinding.addMappingBinding(new JsonPlainMappingBinding());
        mappingBinding.addMappingBinding(new AnnotationsMappingBinding());
        mappingBinding.addMappingBinding(new OverrideAnnotationsWithXmlCpmMappingBinding());
        mappingBinding.addMappingBinding(new OverrideAnnotationsWithJsonCpmMappingBinding());
    }

    /**
     * Sets the class loader that will be used to load classes and resources.
     */
    public CompassConfiguration setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
        this.settings.setClassLoader(classLoader);
        return this;
    }

    /**
     * Returns the class loader that will be used to load classes and resources. If directly
     * set, will return it. If not, will return the therad local context class loader.
     */
    public ClassLoader getClassLoader() {
        if (this.classLoader == null) {
            return Thread.currentThread().getContextClassLoader();
        }
        return this.classLoader;
    }

    /**
     * Returns the current set of settings associated with the configuration.
     *
     * @return The settings used by the configuration
     */
    public CompassSettings getSettings() {
        return settings;
    }

    /**
     * Sets a specific setting in the compass configuration settings.
     *
     * @param setting The setting name
     * @param value   The setting value
     * @return <code>CompassConfiguration</code> for method chaining
     */
    public CompassConfiguration setSetting(String setting, Object value) {
        settings.setObjectSetting(setting, value);
        return this;
    }

    /**
     * Sets the connection for the compass instance.
     *
     * @param connection The connection for compass to use
     * @return <code>CompassConfiguration</code> for method chaining
     */
    public CompassConfiguration setConnection(String connection) {
        settings.setSetting(CompassEnvironment.CONNECTION, connection);
        return this;
    }

    /**
     * Registers a {@link Converter} under the given name. The name can then be used in the mapping
     * definitions as a logical name to the converter.
     *
     * @param converterName the converter name the converter will be registered under
     * @param converter     The converter to use
     * @return The configuration
     */
    public CompassConfiguration registerConverter(String converterName, Converter converter) {
        this.temporaryConvertersByName.put(converterName, new ConverterHolder(converter));
        return this;
    }

    /**
     * Regsiters a {@link Converter} under the given name. This converter will apply to all the given
     * types that match the given type.
     *
     * @param converterName The name of the converter
     * @param type          The type to register the converter for
     * @param converter     The converter
     * @return The configuration
     */
    public CompassConfiguration registerConverter(String converterName, Class type, Converter converter) {
        this.temporaryConvertersByName.put(converterName, new ConverterHolder(type, converter));
        return this;
    }


    /**
     * Build compass with the configurations set. Creates a copy of all the
     * current settings and mappings, configures a {@link Compass} instance and
     * starts it.
     * <p/>
     * Note that the <code>CompassConfiguration</code> class can be used to
     * create more Compass objects after the method has been called.
     * </p>
     *
     * @return the Compass
     */
    public Compass buildCompass() throws CompassException {

        CompassSettings copySettings = settings.copy();

        copySettings.setClassLoader(getClassLoader());

        // add any mappings set in the properties
        for (Iterator it = settings.keySet().iterator(); it.hasNext();) {
            String setting = (String) it.next();
            if (setting.startsWith(CompassEnvironment.Mapping.MAPPING_PREFIX)) {
                String mapping = settings.getSetting(setting);
                if (mapping.endsWith("cpm.xml") || mapping.endsWith("cmd.xml")) {
                    addResource(mapping);
                } else {
                    try {
                        addClass(ClassUtils.forName(mapping, copySettings.getClassLoader()));
                    } catch (ClassNotFoundException e) {
                        throw new CompassException("Failed to find class [" + mapping + "]");
                    }
                }
                settings.removeSetting(setting);
            }
        }
        // add any scans
        Map<String, CompassSettings> scanGroups = settings.getSettingGroups(CompassEnvironment.Mapping.SCAN_MAPPING_PREFIX);
        for (Map.Entry<String, CompassSettings> entry : scanGroups.entrySet()) {
            String packageSetting = entry.getValue().getSetting(CompassEnvironment.Mapping.SCAN_MAPPING_PACKAGE);
            if (packageSetting == null) {
                throw new ConfigurationException("[" + CompassEnvironment.Mapping.SCAN_MAPPING_PACKAGE + "] must be set when scanning for [" + entry.getKey() + "] scan");
            }
            addScan(packageSetting, entry.getValue().getSetting(CompassEnvironment.Mapping.SCAN_MAPPING_PATTERN));
        }

        ConverterLookup converterLookup = new DefaultConverterLookup();
        registerExtraConverters(converterLookup);
        converterLookup.configure(copySettings);
        for (String converterName : temporaryConvertersByName.keySet()) {
            ConverterHolder converterHolder = temporaryConvertersByName.get(converterName);
            if (converterHolder.type == null) {
                converterLookup.registerConverter(converterName, converterHolder.converter);
            } else {
                converterLookup.registerConverter(converterName, converterHolder.converter, converterHolder.type);
            }
        }

        CompassMapping copyCompassMapping = mapping.copy(converterLookup);

        PropertyNamingStrategyFactory propertyNamingStrategyFactory = new DefaultPropertyNamingStrategyFactory();
        PropertyNamingStrategy propertyNamingStrategy = propertyNamingStrategyFactory.createNamingStrategy(copySettings);

        MappingProcessor mappingProcessor = new CompassMappingProcessor();
        mappingProcessor.process(copyCompassMapping, propertyNamingStrategy, converterLookup, copySettings);

        CompassMetaData copyMetaData = metaData.copy();

        DefaultExecutorManager executorManager = new DefaultExecutorManager();
        executorManager.configure(settings);

        return new RefreshableCompass(this,
                new DefaultCompass(copyCompassMapping, converterLookup, copyMetaData, propertyNamingStrategy, executorManager, copySettings));
    }

    protected void registerExtraConverters(ConverterLookup converterLookup) {

    }

    /**
     * Use the mappings and properties specified in an application resource with
     * the path <code>/compass.cfg.xml</code>.
     *
     * @return <code>CompassConfiguration</code> for method chaining
     */
    public CompassConfiguration configure() throws ConfigurationException {
        configure("/compass.cfg.xml");
        return this;
    }

    /**
     * Use the mappings and properties specified in the given application
     * resource.
     *
     * @param resource The compass configuration resource path
     * @return <code>CompassConfiguration</code> for method chaining
     */
    public CompassConfiguration configure(String resource) throws ConfigurationException {
        log.info("Configuring from resource [" + resource + "]");
        configurationBuilder.configure(resource, this);
        return this;
    }

    /**
     * Use the mappings and properties specified in the given document.
     *
     * @param url URL from which you wish to load the configuration
     * @return A configuration configured via the file
     * @throws ConfigurationException
     */
    public CompassConfiguration configure(URL url) throws ConfigurationException {
        log.info("Configuring from url [" + url.toExternalForm() + "]");
        configurationBuilder.configure(url, this);
        return this;
    }

    /**
     * Use the mappings and properties specified in the given application file.
     *
     * @param configFile <code>File</code> from which you wish to load the
     *                   configuration
     * @return A configuration configured via the file
     * @throws ConfigurationException
     */
    public CompassConfiguration configure(File configFile) throws ConfigurationException {
        log.info("Configuring from file [" + configFile.getAbsolutePath() + "]");
        configurationBuilder.configure(configFile, this);
        return this;
    }

    /**
     * Advance: Add mappings based on {@link org.compass.core.mapping.ContractMapping}
     * implementation which allows for adding pre built mapping constructs.
     */
    public CompassConfiguration addMapping(ContractMapping contractMapping) {
        boolean hasAddedResource = getMappingBinding().addContractMaping(contractMapping);
        if (!hasAddedResource) {
            throw new ConfigurationException("No mapping match contract mapping [" + contractMapping.getAlias() + "]");
        }
        if (log.isInfoEnabled()) {
            log.info("Adding Contract Mapping [" + contractMapping.getAlias() + "]");
        }
        return this;
    }

    /**
     * Allows to provide contract mapping through a level of indiraction.
     */
    public CompassConfiguration addMapping(ContractMappingProvider contractMappingProvider) {
        return addMapping(contractMappingProvider.getMapping());
    }

    /**
     * Advance: Add mappings based on {@link org.compass.core.mapping.ResourceMapping}
     * implementation which allows for adding pre built mapping constructs.
     */
    public CompassConfiguration addMapping(ResourceMapping resourceMapping) {
        boolean hasAddedResource = getMappingBinding().addResourceMapping(resourceMapping);
        if (!hasAddedResource) {
            throw new ConfigurationException("No mapping match resource mapping [" + resourceMapping.getAlias() + "]");
        }
        if (log.isInfoEnabled()) {
            log.info("Adding Resource Mapping [" + resourceMapping.getAlias() + "]");
        }
        return this;
    }

    /**
     * Allows to provide resource mapping through a level of indiraction.
     */
    public CompassConfiguration addMapping(ResourceMappingProvider resourceMappingProvider) {
        return addMapping(resourceMappingProvider.getMapping());
    }

    /**
     * Removes the mapping registered under the given alias.
     */
    public CompassConfiguration removeMappingByAlias(String alias) throws MappingException {
        boolean removed = mapping.removeMappingByAlias(alias);
        if (removed) {
            if (log.isInfoEnabled()) {
                log.info("Removing Resource Mapping with alias [" + alias + "]");
            }
        }
        return this;
    }

    /**
     * Removes all the mappings registered under the given class name.
     */
    public CompassConfiguration removeMappingByClass(Class clazz) throws MappingException {
        return removeMappingByClass(clazz.getName());
    }

    /**
     * Removes all the mappings registered under the given class name.
     */
    public CompassConfiguration removeMappingByClass(String className) throws MappingException {
        boolean removed = mapping.removeMappingByClass(className);
        if (removed) {
            if (log.isInfoEnabled()) {
                log.info("Removing Resource Mappings with class [" + className + "]");
            }
        }
        return this;
    }

    /**
     * Uses a class that implements the {@link InputStreamMappingResolver} for auto
     * generation of mapping definitions.
     *
     * @param mappingResolver The mapping resolver
     */
    public CompassConfiguration addMappingResolver(InputStreamMappingResolver mappingResolver) throws ConfigurationException {
        boolean hasAddedResource = getMappingBinding().addMappingResolver(mappingResolver);
        if (!hasAddedResource) {
            throw new ConfigurationException("No mapping match mapping resolver [" + mappingResolver + "]");
        }
        if (log.isInfoEnabled()) {
            log.info("Mapping resolver [" + mappingResolver + "]");
        }
        return this;
    }

    /**
     * Read mappings from an application resource
     *
     * @param path        a resource
     * @param classLoader a <code>ClassLoader</code> to use
     */
    public CompassConfiguration addResource(String path, ClassLoader classLoader) throws ConfigurationException {
        boolean hasAddedResource = getMappingBinding().addResource(path, classLoader);
        if (!hasAddedResource) {
            throw new ConfigurationException("No mapping match resource [" + path + "] and class loader [" + classLoader + "]");
        }
        if (log.isInfoEnabled()) {
            log.info("Mapping resource [" + path + "] from class loader [" + classLoader + "]");
        }
        return this;
    }

    /**
     * Read mappings from an application resource trying different classloaders.
     * This method will try to load the resource first from the thread context
     * classloader and then from the classloader that loaded Compass.
     *
     * @param path The path of the resource
     */
    public CompassConfiguration addResource(String path) throws ConfigurationException {
        boolean hasAddedResource = getMappingBinding().addResource(path, getClassLoader());
        if (!hasAddedResource) {
            throw new ConfigurationException("No mapping match resource [" + path + "]");
        }
        if (log.isInfoEnabled()) {
            log.info("Mapping resource [" + path + "] in class loader");
        }
        return this;
    }

    /**
     * Read mappings from a particular file.
     *
     * @param filePath a path to a file
     */
    public CompassConfiguration addFile(String filePath) throws ConfigurationException {
        boolean hasAddedResource = getMappingBinding().addFile(filePath);
        if (!hasAddedResource) {
            throw new ConfigurationException("No mapping match file [" + filePath + "]");
        }
        if (log.isInfoEnabled()) {
            log.info("Mapping file [" + filePath + "]");
        }
        return this;
    }

    /**
     * Read mappings from a particular file.
     *
     * @param file a path to a file
     */
    public CompassConfiguration addFile(File file) throws ConfigurationException {
        boolean hasAddedResource = getMappingBinding().addFile(file);
        if (!hasAddedResource) {
            throw new ConfigurationException("No mapping match file [" + file.getAbsolutePath() + "]");
        }
        if (log.isInfoEnabled()) {
            log.info("Mapping file [" + file.getAbsolutePath() + "]");
        }
        return this;
    }

    /**
     * Read annotated package definitions.
     *
     * @param packageName The package name to load
     */
    public CompassConfiguration addPackage(String packageName) throws ConfigurationException {
        boolean hasAddedResource = getMappingBinding().addPackage(packageName);
        if (!hasAddedResource) {
            throw new ConfigurationException("No mapping match package [" + packageName + "]");
        }
        if (log.isInfoEnabled()) {
            log.info("Mapping package [" + packageName + "]");
        }
        return this;
    }

    /**
     * Read a mapping from an application resource, using a convention. The
     * class <code>foo.bar.Foo</code> is mapped by the file
     * <code>foo/bar/Foo.cpm.xml</code> (in the case of Xml binding).
     *
     * @param searchableClass the mapped class
     */
    public CompassConfiguration addClass(Class searchableClass) throws ConfigurationException {
        boolean hasAddedResource = getMappingBinding().addClass(searchableClass);
        if (!hasAddedResource) {
            throw new ConfigurationException("No mapping match class [" + searchableClass.getName() + "]");
        }
        if (log.isInfoEnabled()) {
            log.info("Mapping class [" + searchableClass + "]");
        }
        return this;
    }

    /**
     * Scans the given base package recursivly for any applicable mappings definitions. This
     * incldues xml mapping defintiions as well as annotations.
     */
    public CompassConfiguration addScan(String basePackage) throws ConfigurationException {
        return addScan(basePackage, null);
    }

    /**
     * Scans the given base package recursivly for any applicable mappings definitions. This
     * incldues xml mapping defintiions as well as annotations.
     *
     * <p>An optional ant style pattern can be provided to narrow down the search. For example,
     * the base package can be <code>com.mycompany</code>, and the pattern can be <code>**&#47model&#47**</code>
     * which will match all the everythign that has a package named model within it under the given base package.
     */
    public CompassConfiguration addScan(String basePackage, final String pattern) throws ConfigurationException {
        basePackage = basePackage.replace('.', '/');
        Enumeration<URL> urls;
        try {
            urls = settings.getClassLoader().getResources(basePackage);
        } catch (IOException e) {
            throw new ConfigurationException("Failed to list resource for base package [" + basePackage + "]", e);
        }
        final PathMatcher matcher = new AntPathMatcher();
        final boolean performMatch = pattern != null && matcher.isPattern(pattern);
        Filter filter = new Filter() {
            public boolean accepts(String name) {
                for (String suffix : getMappingBinding().getSuffixes()) {
                    if (performMatch && !matcher.match(pattern, name)) {
                        return false;
                    }
                    if (name.endsWith(suffix)) {
                        return true;
                    }
                }
                return false;
            }
        };
        while (urls.hasMoreElements()) {
            URL url = urls.nextElement();
            Scanner scanner;
            try {
                scanner = ScannerFactoy.create(basePackage, url, filter);
            } catch (IOException e) {
                throw new ConfigurationException("Failed to create scan factory for basePackage [" + basePackage + "] and url [" + url + "]", e);
            }
            try {
                ScanItem si;
                while ((si = scanner.next()) != null) {
                    try {
                        getMappingBinding().addInputStream(si.getInputStream(), si.getName());
                    } finally {
                        si.close();
                    }
                }
            } finally {
                scanner.close();
            }
        }
        return this;
    }

    /**
     * Tries to add a class and returns a boolean indicator if it was added or not.
     *
     * @param searchableClass The searchable class to add
     * @return <code>true</code> if the class was added, <code>false</code> otherwise
     * @throws ConfigurationException
     */
    public boolean tryAddClass(Class searchableClass) throws ConfigurationException {
        boolean hasAddedResource = getMappingBinding().addClass(searchableClass);
        if (log.isInfoEnabled() && hasAddedResource) {
            log.info("Mapping class [" + searchableClass + "]");
        }
        return hasAddedResource;
    }

    /**
     * Read all mapping and meta-data documents from a directory tree. Assume
     * that any file named <code>*.cpm.xml</code> or <code>*.cmd.xml</code>
     * is a mapping document.
     *
     * @param dir a directory
     */
    public CompassConfiguration addDirectory(File dir) throws ConfigurationException {
        boolean hasAddedResource = getMappingBinding().addDirectory(dir);
        if (!hasAddedResource) {
            throw new ConfigurationException("No mapping match directory [" + dir.getAbsolutePath() + "]");
        }
        if (log.isInfoEnabled()) {
            log.info("Mapping directory [" + dir.getAbsolutePath() + "]");
        }
        return this;
    }

    /**
     * Read all mappings and meta-data from a jar file. Assume that any file
     * named <code>*.cpm.xml</code> or <code>*.cmd.xml</code> is a mapping
     * document.
     *
     * @param jar a jar file
     */
    public CompassConfiguration addJar(File jar) throws ConfigurationException {
        boolean hasAddedResource = getMappingBinding().addJar(jar);
        if (!hasAddedResource) {
            throw new ConfigurationException("No mapping match jar [" + jar.getAbsolutePath() + "]");
        }
        if (log.isInfoEnabled()) {
            log.info("Mapping jar [" + jar.getName() + "]");
        }
        return this;
    }

    /**
     * Read mappings from a <code>URL</code>.
     *
     * @param url the URL
     */
    public CompassConfiguration addURL(URL url) throws ConfigurationException {
        boolean hasAddedResource = getMappingBinding().addURL(url);
        if (!hasAddedResource) {
            throw new ConfigurationException("No mapping match URL [" + url.toExternalForm() + "]");
        }
        if (log.isInfoEnabled()) {
            log.info("Mapping URL [" + url.toExternalForm() + "]");
        }
        return this;
    }

    /**
     * Read mappings from an <code>InputStream</code>.
     *
     * @param inputStream an <code>InputStream</code> containing
     */
    public CompassConfiguration addInputStream(InputStream inputStream, String resourceName)
            throws ConfigurationException {
        boolean hasAddedResource = getMappingBinding().addInputStream(inputStream, resourceName);
        if (!hasAddedResource) {
            throw new ConfigurationException("No mapping match input stream [" + resourceName + "]");
        }
        if (log.isInfoEnabled()) {
            log.info("Mapping InputStream [" + resourceName + "]");
        }
        return this;
    }

    private class ConverterHolder {
        Class type;
        Converter converter;

        public ConverterHolder(Converter converter) {
            this.converter = converter;
        }

        private ConverterHolder(Class type, Converter converter) {
            this.type = type;
            this.converter = converter;
        }
    }
}
