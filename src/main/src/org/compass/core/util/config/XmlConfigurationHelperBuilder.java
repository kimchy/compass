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

package org.compass.core.util.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.compass.core.config.ConfigurationException;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

/**
 * A DefaultConfigurationBuilder builds <code>Configuration</code>s from XML,
 * via a SAX2 compliant parser.
 */
public class XmlConfigurationHelperBuilder {
    private SAXConfigurationHandler m_handler;

    private XMLReader m_parser;

    /**
     * Create a Configuration Builder with a default XMLReader that ignores
     * namespaces. In order to enable namespaces, use either the constructor
     * that has a boolean or that allows you to pass in your own
     * namespace-enabled XMLReader.
     */
    public XmlConfigurationHelperBuilder() {
        this(false);
    }

    /**
     * Create a Configuration Builder, specifying a flag that determines
     * namespace support.
     */
    public XmlConfigurationHelperBuilder(final boolean enableNamespaces) {
        // yaya the bugs with some compilers and final variables ..
        try {
            final SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
            if (enableNamespaces) {
                saxParserFactory.setNamespaceAware(true);
            }
            saxParserFactory.setValidating(true);
            final SAXParser saxParser = saxParserFactory.newSAXParser();
            setParser(saxParser.getXMLReader());
        } catch (final Exception se) {
            throw new Error("Unable to setup SAX parser" + se);
        }
    }

    /**
     * Create a Configuration Builder with your own XMLReader.
     */
    public XmlConfigurationHelperBuilder(XMLReader parser) {
        setParser(parser);
    }

    /**
     * Internally sets up the XMLReader
     */
    private void setParser(XMLReader parser) {
        m_parser = parser;
        m_handler = getHandler();
        m_parser.setContentHandler(m_handler);
        m_parser.setErrorHandler(m_handler);
    }

    /**
     * Get a SAXConfigurationHandler for your configuration reading.
     */
    protected SAXConfigurationHandler getHandler() {
        try {
            if (m_parser.getFeature("http://xml.org/sax/features/namespaces")) {
                return new NamespacedSAXConfigurationHandler();
            }
        } catch (Exception e) {
            // ignore error and fall through to the non-namespaced version
        }
        return new SAXConfigurationHandler();
    }

    /**
     * Build a configuration object from a file using a filename.
     */
    public ConfigurationHelper buildFromFile(final String filename) throws ConfigurationException {
        return buildFromFile(new File(filename));
    }

    /**
     * Build a configuration object from a file using a File object.
     */
    public ConfigurationHelper buildFromFile(final File file) throws ConfigurationException {
        InputStream inputStream;
        try {
            inputStream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            throw new ConfigurationException("Failed to find file [" + file.getAbsolutePath() + "]");
        }
        return build(inputStream, file.getAbsolutePath());
    }

    /**
     * Build a configuration object using an InputStream; supplying a systemId
     * to make messages about all kinds of errors more meaningfull.
     */
    public ConfigurationHelper build(final InputStream inputStream, final String systemId)
            throws ConfigurationException {
        final InputSource inputSource = new InputSource(inputStream);
        inputSource.setSystemId(systemId);
        return build(inputSource);
    }

    /**
     * Build a configuration object using an URI
     */
    public ConfigurationHelper build(final String uri) throws ConfigurationException {
        return build(new InputSource(uri));
    }

    /**
     * Build a configuration object using an XML InputSource object
     */
    public ConfigurationHelper build(final InputSource input) throws ConfigurationException {
        synchronized (this) {
            m_handler.clear();
            try {
                m_parser.parse(input);
            } catch (SAXParseException e) {
                throw new ConfigurationException("Line [" + e.getLineNumber() + "] Column [" + e.getColumnNumber()
                        + "] in XML document [" + input.getSystemId() + "] is invalid", e);
            } catch (SAXException e) {
                throw new ConfigurationException("XML document [" + input.getSystemId() + "] is invalid", e);
            } catch (IOException e) {
                throw new ConfigurationException("IOException parsing XML document [" + input.getSystemId() + "]", e);
            }
            return m_handler.getConfiguration();
        }
    }

    /**
     * Sets the <code>EntityResolver</code> to be used by parser. Useful when
     * dealing with xml files that reference external entities.
     */
    public void setEntityResolver(final EntityResolver resolver) {
        synchronized (this) {
            m_parser.setEntityResolver(resolver);
        }
    }
}
