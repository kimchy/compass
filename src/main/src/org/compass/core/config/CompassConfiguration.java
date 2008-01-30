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

package org.compass.core.config;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.compass.annotations.config.binding.AnnotationsMappingBinding;
import org.compass.annotations.config.binding.OverrideAnnotationsWithCpmMappingBinding;
import org.compass.core.Compass;
import org.compass.core.CompassException;
import org.compass.core.config.binding.XmlMappingBinding;
import org.compass.core.config.binding.XmlMetaDataBinding;
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
import org.compass.core.mapping.CompassMapping;
import org.compass.core.mapping.ResourceMapping;
import org.compass.core.metadata.CompassMetaData;
import org.compass.core.metadata.impl.DefaultCompassMetaData;
import org.compass.core.util.ClassUtils;

/**
 * Used to configure <code>Compass</code> instances.
 * <p/>
 * An instance of it allows the application to specify settings and mapping
 * files to be used when creating <code>Compass</code>.
 * </p>
 * <p/>
 * There are several options to configure a <code>Compass</code> instance,
 * programmatically using the <code>CompassConfiguration</code> class, using
 * the xml configuration file (compass.cfg.xml), or a combination of both.
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

    private CompassMapping mapping;

    private CompassSettings settings;

    private ClassLoader classLoader;

    protected CompassMappingBinding mappingBinding;

    protected ConfigurationBuilder configurationBuilder = new SmartConfigurationBuilder();

    private HashMap<String, Converter> temporaryConvertersByName = new HashMap<String, Converter>();

    public CompassConfiguration() {
        mapping = new CompassMapping();
        metaData = new DefaultCompassMetaData();

        settings = new CompassSettings();

        mappingBinding = new CompassMappingBinding();
        addMappingBindings(mappingBinding);
        mappingBinding.setUpBinding(mapping, metaData, settings);
    }

    protected void addMappingBindings(CompassMappingBinding mappingBinding) {
        mappingBinding.addMappingBinding(new XmlMetaDataBinding());
        mappingBinding.addMappingBinding(new XmlMappingBinding());
        mappingBinding.addMappingBinding(new AnnotationsMappingBinding());
        mappingBinding.addMappingBinding(new OverrideAnnotationsWithCpmMappingBinding());
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
    public CompassConfiguration setSetting(String setting, String value) {
        settings.setSetting(setting, value);
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
        this.temporaryConvertersByName.put(converterName, converter);
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
            if (setting.startsWith(CompassEnvironment.MAPPING_PREFIX)) {
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
            }
        }

        ConverterLookup converterLookup = new DefaultConverterLookup();
        registerExtraConverters(converterLookup);
        converterLookup.configure(copySettings);
        for (Iterator<String> it = temporaryConvertersByName.keySet().iterator(); it.hasNext();) {
            String converterName = it.next();
            Converter converter = temporaryConvertersByName.get(converterName);
            converterLookup.registerConverter(converterName, converter);
        }

        CompassMapping copyCompassMapping = mapping.copy(converterLookup);

        PropertyNamingStrategyFactory propertyNamingStrategyFactory = new DefaultPropertyNamingStrategyFactory();
        PropertyNamingStrategy propertyNamingStrategy = propertyNamingStrategyFactory.createNamingStrategy(copySettings);

        MappingProcessor mappingProcessor = new CompassMappingProcessor();
        mappingProcessor.process(copyCompassMapping, propertyNamingStrategy, converterLookup, copySettings);

        CompassMetaData copyMetaData = metaData.copy();

        DefaultExecutorManager executorManager = new DefaultExecutorManager();
        executorManager.configure(settings);

        return new DefaultCompass(copyCompassMapping, converterLookup, copyMetaData, propertyNamingStrategy,
                executorManager, copySettings);
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
     * Advance: Add mappings based on {@link org.compass.core.mapping.ResourceMapping}
     * implementation which allows for adding pre built mapping constructs.
     */
    public CompassConfiguration addResourceMapping(ResourceMapping resourceMapping) {
        boolean hasAddedResource = mappingBinding.addResoruceMapping(resourceMapping);
        if (!hasAddedResource) {
            throw new ConfigurationException("No mapping match resource mapping [" + resourceMapping.getAlias() + "]");
        }
        if (log.isInfoEnabled()) {
            log.info("Resource Mapping [" + resourceMapping.getAlias() + "]");
        }
        return this;
    }

    /**
     * Uses a class that implements the {@link InputStreamMappingResolver} for auto
     * generation of mapping definitions.
     *
     * @param mappingResolver
     */
    public CompassConfiguration addMappingResover(InputStreamMappingResolver mappingResolver) throws ConfigurationException {
        boolean hasAddedResource = mappingBinding.addMappingResolver(mappingResolver);
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
        boolean hasAddedResource = mappingBinding.addResource(path, classLoader);
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
        boolean hasAddedResource = mappingBinding.addResource(path, getClassLoader());
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
        boolean hasAddedResource = mappingBinding.addFile(filePath);
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
        boolean hasAddedResource = mappingBinding.addFile(file);
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
        boolean hasAddedResource = mappingBinding.addPackage(packageName);
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
        boolean hasAddedResource = mappingBinding.addClass(searchableClass);
        if (!hasAddedResource) {
            throw new ConfigurationException("No mapping match class [" + searchableClass.getName() + "]");
        }
        if (log.isInfoEnabled()) {
            log.info("Mapping class [" + searchableClass + "]");
        }
        return this;
    }

    public boolean tryAddClass(Class searchableClass) throws ConfigurationException {
        boolean hasAddedResource = mappingBinding.addClass(searchableClass);
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
        boolean hasAddedResource = mappingBinding.addDirectory(dir);
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
        boolean hasAddedResource = mappingBinding.addJar(jar);
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
        boolean hasAddedResource = mappingBinding.addURL(url);
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
        boolean hasAddedResource = mappingBinding.addInputStream(inputStream, resourceName);
        if (!hasAddedResource) {
            throw new ConfigurationException("No mapping match input stream [" +resourceName + "]");
        }
        if (log.isInfoEnabled()) {
            log.info("Mapping InputStream [" + resourceName + "]");
        }
        return this;
    }
}
