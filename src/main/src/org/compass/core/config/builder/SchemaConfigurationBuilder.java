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

package org.compass.core.config.builder;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.compass.core.config.CompassConfiguration;
import org.compass.core.config.CompassEnvironment;
import org.compass.core.config.CompassSettings;
import org.compass.core.config.ConfigurationException;
import org.compass.core.util.DomUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

/**
 * @author kimchy
 */
public class SchemaConfigurationBuilder extends AbstractXmlConfigurationBuilder {

    protected void doProcess(Document doc, CompassConfiguration config) throws ConfigurationException {
        Element root = doc.getDocumentElement();
        // the root is the compass element
        NodeList nl = root.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node node = nl.item(i);
            if (node instanceof Element) {
                if ("compass".equals(node.getLocalName())) {
                    processCompass((Element) node, config);
                }
            }
        }
    }

    public void processCompass(Element compassElement, CompassConfiguration config) {

        config.getSettings().setSetting(CompassEnvironment.NAME, DomUtils.getElementAttribute(compassElement, "name"));

        NodeList nl = compassElement.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node node = nl.item(i);
            if (node instanceof Element) {
                Element ele = (Element) node;
                String nodeName = ele.getLocalName();
                String methodName = "bind" +
                        Character.toUpperCase(nodeName.charAt(0)) + nodeName.substring(1, nodeName.length());
                Method method;
                try {
                    method = SchemaConfigurationBuilder.class.getMethod(methodName,
                            new Class[]{Element.class, CompassSettings.class});
                } catch (NoSuchMethodException e) {
                    throw new ConfigurationException("Compass failed to process node [" + nodeName + "], this is " +
                            "either a mailformed xml configuration (not validated against the xsd), or an internal" +
                            " bug in compass");
                }
                try {
                    method.invoke(this, new Object[]{ele, config.getSettings()});
                } catch (InvocationTargetException e) {
                    throw new ConfigurationException("Failed to invoke binding metod for node [" + nodeName + "]");
                } catch (IllegalAccessException e) {
                    throw new ConfigurationException("Failed to access binding metod for node [" + nodeName + "]");
                }
            }
        }
    }

    public void bindSettings(Element ele, CompassSettings settings) {
        List domSettings = DomUtils.getChildElementsByTagName(ele, "setting", true);
        for (Iterator it = domSettings.iterator(); it.hasNext();) {
            Element eleSetting = (Element) it.next();
            settings.setSetting(DomUtils.getElementAttribute(eleSetting, "name"),
                    DomUtils.getElementAttribute(eleSetting, "value"));
        }
    }

    protected EntityResolver doGetEntityResolver() {
        return new EntityResolver() {

            private static final String URL = "http://www.opensymphony.com/compass/schema/";

            public InputSource resolveEntity(String publicId, String systemId) {
                if (systemId != null && systemId.startsWith(URL)) {
                    // Search for DTD
                    String location = "/org/compass/core/" + systemId.substring(URL.length());
                    InputStream is = getClass().getResourceAsStream(location);
                    if (is == null) {
                        throw new ConfigurationException("Schema system id [" + systemId + "] not found at [" + location + "], " +
                                "please check it has the correct location. Have you included compass in your class path?");
                    }
                    InputSource source = new InputSource(is);
                    source.setPublicId(publicId);
                    source.setSystemId(systemId);
                    return source;
                } else {
                    throw new ConfigurationException("Schema system id [" + systemId + "] not found, please check it has the " +
                            "correct location");
                }
            }
        };
    }

    protected DocumentBuilderFactory createDocumentBuilderFactory() throws ParserConfigurationException {
        DocumentBuilderFactory factory = super.createDocumentBuilderFactory();
        factory.setNamespaceAware(true);
        try {
            factory.setAttribute("http://java.sun.com/xml/jaxp/properties/schemaLanguage", "http://www.w3.org/2001/XMLSchema");
        } catch (IllegalArgumentException ex) {
            throw new ConfigurationException(
                    "Unable to validate using XSD: Your JAXP provider [" + factory + "] does not support XML Schema. "
                            + "Are you running on Java 1.4 or below with Apache Crimson? "
                            + "Upgrade to Apache Xerces (or Java 1.5) for full XSD support.");
        }
        return factory;
    }
}
