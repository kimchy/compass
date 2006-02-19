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

import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.compass.core.Compass;
import org.compass.core.CompassException;
import org.compass.core.config.binding.XmlMappingBinding;
import org.compass.core.config.binding.XmlMetaDataBinding;
import org.compass.core.config.process.MappingProcessor;
import org.compass.core.converter.Converter;
import org.compass.core.converter.ConverterLookup;
import org.compass.core.converter.DefaultConverterLookup;
import org.compass.core.engine.naming.DefaultPropertyNamingStrategyFactory;
import org.compass.core.engine.naming.PropertyNamingStrategy;
import org.compass.core.engine.naming.PropertyNamingStrategyFactory;
import org.compass.core.impl.DefaultCompass;
import org.compass.core.mapping.CompassMapping;
import org.compass.core.metadata.CompassMetaData;
import org.compass.core.metadata.impl.DefaultCompassMetaData;
import org.compass.core.util.DTDEntityResolver;
import org.compass.core.util.config.ConfigurationHelper;
import org.compass.core.util.config.XmlConfigurationHelperBuilder;

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

    protected CompassMappingBinding mappingBinding;

    private HashMap temporaryConvertersByName = new HashMap();

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

        ConverterLookup converterLookup = new DefaultConverterLookup();
        converterLookup.configure(copySettings);
        for (Iterator it = temporaryConvertersByName.keySet().iterator(); it.hasNext();) {
            String converterName = (String) it.next();
            Converter converter = (Converter) temporaryConvertersByName.get(converterName);
            converterLookup.registerConverter(converterName, converter);
        }

        CompassMapping copyCompassMapping = mapping.copy(converterLookup);

        PropertyNamingStrategyFactory propertyNamingStrategyFactory = new DefaultPropertyNamingStrategyFactory();
        PropertyNamingStrategy propertyNamingStrategy = propertyNamingStrategyFactory.createNamingStrategy(copySettings);

        MappingProcessor mappingProcessor = new CompassMappingProcessor();
        mappingProcessor.process(copyCompassMapping, propertyNamingStrategy, converterLookup, copySettings);

        CompassMetaData copyMetaData = metaData.copy();

        return new DefaultCompass(copyCompassMapping, converterLookup, copyMetaData, propertyNamingStrategy,
                copySettings);
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
     * resource. The resource is found via
     * {@link CompassConfiguration#getConfigurationInputStream(String)}.
     *
     * @param resource The compass configuration resource path
     * @return <code>CompassConfiguration</code> for method chaining
     */
    public CompassConfiguration configure(String resource) throws ConfigurationException {
        if (log.isInfoEnabled()) {
            log.info("Configuring from resource [" + resource + "]");
        }
        InputStream stream = getConfigurationInputStream(resource);
        return doConfigure(stream, resource);
    }

    /**
     * Use the mappings and properties specified in the given document.
     *
     * @param url URL from which you wish to load the configuration
     * @return A configuration configured via the file
     * @throws ConfigurationException
     */
    public CompassConfiguration configure(URL url) throws ConfigurationException {
        log.info("Configuring from url [" + url.toString() + "]");
        try {
            return doConfigure(url.openStream(), url.toString());
        } catch (IOException ioe) {
            throw new ConfigurationException("could not configure from URL [" + url.toString() + "]", ioe);
        }
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
        try {
            return doConfigure(new FileInputStream(configFile), configFile.toString());
        } catch (FileNotFoundException fnfe) {
            throw new ConfigurationException(
                    "Could not find configuration file [" + configFile.getAbsolutePath() + "]", fnfe);
        }
    }

    /**
     * Use the mappings and properties specified in the given application
     * resource.
     *
     * @param stream       Inputstream to be read from
     * @param resourceName The name to use in warning/error messages
     * @return A configuration configured via the stream
     */
    protected CompassConfiguration doConfigure(InputStream stream, String resourceName) throws ConfigurationException {

        ConfigurationHelper conf = null;
        try {
            XmlConfigurationHelperBuilder builder = new XmlConfigurationHelperBuilder();
            builder.setEntityResolver(new DTDEntityResolver());
            conf = builder.build(stream, resourceName);
        } finally {
            try {
                stream.close();
            } catch (IOException ioe) {
                log.error("Could not close stream on [" + resourceName + "]", ioe);
            }
        }

        return doConfigure(conf);

    }

    protected CompassConfiguration doConfigure(ConfigurationHelper conf) throws ConfigurationException {

        ConfigurationHelper compassConf = conf.getChild("compass");
        String name = compassConf.getAttribute("name", null);
        if (name != null) {
            settings.setSetting(CompassEnvironment.NAME, name);
        }

        addSettings(compassConf);

        ConfigurationHelper[] elements = compassConf.getChildren();
        for (int i = 0; i < elements.length; i++) {
            String elemname = elements[i].getName();
            if ("mapping".equals(elemname) || "meta-data".equals(elemname)) {
                String rsrc = elements[i].getAttribute("resource", null);
                String file = elements[i].getAttribute("file", null);
                String jar = elements[i].getAttribute("jar", null);
                if (rsrc != null) {
                    addResource(rsrc);
                } else if (jar != null) {
                    addJar(new File(jar));
                } else {
                    if (file == null)
                        throw new ConfigurationException(
                                "<mapping> or <meta-data> element in configuration specifies no attributes");
                    addFile(file);
                }
            }
        }

        log.info("Configured Compass [" + name + "]");
        if (log.isDebugEnabled()) {
            log.debug("--with settings [" + settings + "]");
        }

        return this;

    }

    /**
     * Get the configuration file as an <code>InputStream</code>. Might be
     * overridden by subclasses to allow the configuration to be located by some
     * arbitrary mechanism.
     */
    protected InputStream getConfigurationInputStream(String resource) throws ConfigurationException {

        log.info("Configuration resource [" + resource + "]");

        InputStream stream = CompassEnvironment.class.getResourceAsStream(resource);
        if (stream == null)
            stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource);
        if (stream == null) {
            log.warn(resource + " not found");
            throw new ConfigurationException(resource + " not found");
        }
        return stream;

    }

    private void addSettings(ConfigurationHelper conf) {
        ConfigurationHelper[] settingsConf = conf.getChildren("setting");
        for (int i = 0; i < settingsConf.length; i++) {
            String name = settingsConf[i].getAttribute("name");
            String value = settingsConf[i].getValue("").trim();
            settings.setSetting(name, value);
            if (!name.startsWith("compass"))
                settings.setSetting("compass." + name, value);
        }
    }

    /**
     * Uses a class that implements the {@link InputStreamMappingResolver} for auto
     * generation of mapping definitions.
     *
     * @param mappingResolver
     */
    public CompassConfiguration addMappingResover(InputStreamMappingResolver mappingResolver) throws ConfigurationException {
        log.info("Mapping resolver [" + mappingResolver + "]");
        mappingBinding.addMappingResolver(mappingResolver);
        return this;
    }

    /**
     * Read mappings from an application resource
     *
     * @param path        a resource
     * @param classLoader a <code>ClassLoader</code> to use
     */
    public CompassConfiguration addResource(String path, ClassLoader classLoader) throws ConfigurationException {
        log.info("Mapping resource [" + path + "] from class loaded [" + classLoader + "]");
        mappingBinding.addResource(path, classLoader);
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
        log.info("Mapping resource [" + path + "] in class loader");
        mappingBinding.addResource(path);
        return this;
    }

    /**
     * Read mappings from a particular file.
     *
     * @param filePath a path to a file
     */
    public CompassConfiguration addFile(String filePath) throws ConfigurationException {
        log.info("Mapping file [" + filePath + "]");
        mappingBinding.addFile(filePath);
        return this;
    }

    /**
     * Read mappings from a particular file.
     *
     * @param file a path to a file
     */
    public CompassConfiguration addFile(File file) throws ConfigurationException {
        log.info("Mapping file [" + file.getAbsolutePath() + "]");
        mappingBinding.addFile(file);
        return this;
    }

    /**
     * Read annotated package definitions.
     *
     * @param packageName The package name to load
     */
    public CompassConfiguration addPackage(String packageName) throws ConfigurationException {
        log.info("Mapping package [" + packageName + "]");
        mappingBinding.addPackage(packageName);
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
        log.info("Mapping class [" + searchableClass + "]");
        mappingBinding.addClass(searchableClass);
        return this;
    }

    /**
     * Read all mapping and meta-data documents from a directory tree. Assume
     * that any file named <code>*.cpm.xml</code> or <code>*.cmd.xml</code>
     * is a mapping document.
     *
     * @param dir a directory
     */
    public CompassConfiguration addDirectory(File dir) throws ConfigurationException {
        log.info("Mapping directory [" + dir.getAbsolutePath() + "]");
        mappingBinding.addDirectory(dir);
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
        log.info("Mapping jar [" + jar.getName() + "]");
        mappingBinding.addJar(jar);
        return this;
    }

    /**
     * Read mappings from a <code>URL</code>.
     *
     * @param url
     */
    public CompassConfiguration addURL(URL url) throws ConfigurationException {
        log.info("Mapping URL [" + url.toExternalForm() + "]");
        mappingBinding.addURL(url);
        return this;
    }

    /**
     * Read mappings from an <code>InputStream</code>.
     *
     * @param inputStream an <code>InputStream</code> containing
     */
    public CompassConfiguration addInputStream(InputStream inputStream, String resourceName)
            throws ConfigurationException {
        log.info("Mapping InputStream [" + resourceName + "]");
        mappingBinding.addInputStream(inputStream, resourceName);
        return this;
    }
}
