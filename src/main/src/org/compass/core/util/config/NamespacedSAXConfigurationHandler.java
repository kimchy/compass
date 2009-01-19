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

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Iterator;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.NamespaceSupport;

/**
 * A SAXConfigurationHandler helps build Configurations out of sax events,
 * including namespace information.
 */
public class NamespacedSAXConfigurationHandler extends SAXConfigurationHandler {

    /**
     * Likely number of nested configuration items. If more is encountered the
     * lists will grow automatically.
     */
    private static final int EXPECTED_DEPTH = 5;

    private final ArrayList m_elements = new ArrayList(EXPECTED_DEPTH);

    private final ArrayList m_prefixes = new ArrayList(EXPECTED_DEPTH);

    private final ArrayList m_values = new ArrayList(EXPECTED_DEPTH);

    /**
     * Contains true at index n if space in the configuration with depth n is to
     * be preserved.
     */
    private final BitSet m_preserveSpace = new BitSet();

    private ConfigurationHelper m_configuration;

    private Locator m_locator;

    private NamespaceSupport m_namespaceSupport = new NamespaceSupport();

    /**
     * Get the configuration object that was built.
     * 
     * @return a <code>Configuration</code> object
     */
    public ConfigurationHelper getConfiguration() {
        return m_configuration;
    }

    /**
     * Clears all data from this configuration handler.
     */
    public void clear() {
        m_elements.clear();
        Iterator i = m_prefixes.iterator();
        while (i.hasNext()) {
            ((ArrayList) i.next()).clear();
        }
        m_prefixes.clear();
        m_values.clear();
        m_locator = null;
    }

    /**
     * Set the document <code>Locator</code> to use.
     */
    public void setDocumentLocator(final Locator locator) {
        m_locator = locator;
    }

    /**
     * Handling hook for starting the document parsing.
     */
    public void startDocument() throws SAXException {
        m_namespaceSupport.reset();
        super.startDocument();
    }

    /**
     * Handling hook for ending the document parsing.
     */
    public void endDocument() throws SAXException {
        super.endDocument();
        m_namespaceSupport.reset();
    }

    /**
     * Handling hook for character data.
     */
    public void characters(final char[] ch, int start, int end) throws SAXException {
        // it is possible to play micro-optimization here by doing
        // manual trimming and thus preserve some precious bits
        // of memory, but it's really not important enough to justify
        // resulting code complexity
        final int depth = m_values.size() - 1;
        final StringBuffer valueBuffer = (StringBuffer) m_values.get(depth);
        valueBuffer.append(ch, start, end);
    }

    /**
     * Handling hook for finishing parsing of an element.
     */
    public void endElement(final String namespaceURI, final String localName, final String rawName) throws SAXException {
        final int depth = m_elements.size() - 1;
        final PlainConfigurationHelper finishedConfiguration = (PlainConfigurationHelper) m_elements.remove(depth);
        final String accumulatedValue = ((StringBuffer) m_values.remove(depth)).toString();
        final ArrayList prefixes = (ArrayList) m_prefixes.remove(depth);
        final Iterator i = prefixes.iterator();
        while (i.hasNext()) {
            endPrefixMapping((String) i.next());
        }
        prefixes.clear();
        if (finishedConfiguration.getChildren().length == 0) {
            // leaf node
            String finishedValue;
            if (m_preserveSpace.get(depth)) {
                finishedValue = accumulatedValue;
            } else if (0 == accumulatedValue.length()) {
                finishedValue = null;
            } else {
                finishedValue = accumulatedValue.trim();
            }
            finishedConfiguration.setValue(finishedValue);
        } else {
            final String trimmedValue = accumulatedValue.trim();
            if (trimmedValue.length() > 0) {
                throw new SAXException("Not allowed to define mixed content in the " + "element "
                        + finishedConfiguration.getName() + " at " + finishedConfiguration.getLocation());
            }
        }
        if (0 == depth) {
            m_configuration = finishedConfiguration;
        }
        m_namespaceSupport.popContext();
    }

    /**
     * Create a new <code>DefaultConfiguration</code> with the specified local
     * name, namespace, and location.
     */
    protected PlainConfigurationHelper createConfiguration(final String localName, final String namespaceURI,
            final String location) {
        String prefix = m_namespaceSupport.getPrefix(namespaceURI);
        if (prefix == null) {
            prefix = "";
        }
        return new PlainConfigurationHelper(localName, location, namespaceURI, prefix);
    }

    /**
     * Handling hook for starting parsing of an element.
     */
    public void startElement(final String namespaceURI, final String localName, final String rawName,
            final Attributes attributes) throws SAXException {
        m_namespaceSupport.pushContext();
        final PlainConfigurationHelper configuration = createConfiguration(localName, namespaceURI, getLocationString());
        // depth of new configuration (not decrementing here, configuration
        // is to be added)
        final int depth = m_elements.size();
        boolean preserveSpace = false; // top level element trims space by
        // default
        if (depth > 0) {
            final PlainConfigurationHelper parent = (PlainConfigurationHelper) m_elements.get(depth - 1);
            parent.addChild(configuration);
            // inherits parent's space preservation policy
            preserveSpace = m_preserveSpace.get(depth - 1);
        }
        m_elements.add(configuration);
        m_values.add(new StringBuffer());
        final ArrayList prefixes = new ArrayList();
        AttributesImpl componentAttr = new AttributesImpl();
        for (int i = 0; i < attributes.getLength(); i++) {
            if (attributes.getQName(i).startsWith("xmlns")) {
                prefixes.add(attributes.getLocalName(i));
                this.startPrefixMapping(attributes.getLocalName(i), attributes.getValue(i));
            } else if (attributes.getQName(i).equals("xml:space")) {
                preserveSpace = attributes.getValue(i).equals("preserve");
            } else {
                componentAttr.addAttribute(attributes.getURI(i), attributes.getLocalName(i), attributes.getQName(i),
                        attributes.getType(i), attributes.getValue(i));
            }
        }
        if (preserveSpace) {
            m_preserveSpace.set(depth);
        } else {
            m_preserveSpace.clear(depth);
        }
        m_prefixes.add(prefixes);
        final int attributesSize = componentAttr.getLength();
        for (int i = 0; i < attributesSize; i++) {
            final String name = componentAttr.getQName(i);
            final String value = componentAttr.getValue(i);
            configuration.setAttribute(name, value);
        }
    }

    /**
     * This just throws an exception on a parse error.
     */
    public void error(final SAXParseException exception) throws SAXException {
        throw exception;
    }

    /**
     * This just throws an exception on a parse error.
     */
    public void warning(final SAXParseException exception) throws SAXException {
        throw exception;
    }

    /**
     * This just throws an exception on a parse error.
     */
    public void fatalError(final SAXParseException exception) throws SAXException {
        throw exception;
    }

    /**
     * Returns a string showing the current system ID, line number and column
     * number.
     */
    protected String getLocationString() {
        if (null == m_locator) {
            return "Unknown";
        } else {
            final int columnNumber = m_locator.getColumnNumber();
            return m_locator.getSystemId() + ":" + m_locator.getLineNumber()
                    + (columnNumber >= 0 ? (":" + columnNumber) : "");
        }
    }

    /**
     * Handling hook for starting prefix mapping.
     */
    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        m_namespaceSupport.declarePrefix(prefix, uri);
        super.startPrefixMapping(prefix, uri);
    }
}
