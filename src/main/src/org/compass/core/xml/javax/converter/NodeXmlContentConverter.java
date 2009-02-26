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

package org.compass.core.xml.javax.converter;

import java.io.Reader;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.compass.core.CompassException;
import org.compass.core.config.CompassConfigurable;
import org.compass.core.config.CompassEnvironment;
import org.compass.core.config.CompassSettings;
import org.compass.core.config.ConfigurationException;
import org.compass.core.converter.ConversionException;
import org.compass.core.converter.xsem.SupportsXmlContentWrapper;
import org.compass.core.converter.xsem.XmlContentConverter;
import org.compass.core.util.StringBuilderWriter;
import org.compass.core.xml.AliasedXmlObject;
import org.compass.core.xml.XmlObject;
import org.compass.core.xml.javax.NodeAliasedXmlObject;
import org.compass.core.xml.javax.NodeXmlObject;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

/**
 * <p>Uses JSE to convert an xml content to and from {@link NodeAliasedXmlObject}.
 *
 * @author kimchy
 */
public class NodeXmlContentConverter implements XmlContentConverter, CompassConfigurable, SupportsXmlContentWrapper {

    private static Log log = LogFactory.getLog(NodeXmlContentConverter.class);

    protected DocumentBuilder documentBuilder;

    protected Transformer transformer;

    public void configure(CompassSettings settings) throws CompassException {
        try {
            this.documentBuilder = doCreateDocumentBuilder(settings);
        } catch (ParserConfigurationException e) {
            throw new ConfigurationException("Failed to create document builder", e);
        }
        if (log.isDebugEnabled()) {
            log.debug("Using document builder [" + documentBuilder.getClass().getName() + "]");
        }
        try {
            this.transformer = doCreateTransformer(settings);
        } catch (TransformerConfigurationException e) {
            throw new ConfigurationException("Failed to create transformer", e);
        }
        if (log.isDebugEnabled()) {
            log.debug("Using transformer [" + transformer.getClass().getName() + "]");
        }
    }

    /**
     * An extension point allowing to control how a {@link javax.xml.parsers.DocumentBuilder} is
     * created. By default uses <code>DocumentBuilderFactory.newInstance().newDocumentBuilder()</code>.
     */
    protected DocumentBuilder doCreateDocumentBuilder(CompassSettings settings) throws ParserConfigurationException {
        return DocumentBuilderFactory.newInstance().newDocumentBuilder();
    }

    /**
     * An extension point allowing to control how a {@link javax.xml.transform.Transformer} is
     * created. By default uses <code>TransformerFactory.newInstance().newTransformer()</code>.
     */
    protected Transformer doCreateTransformer(CompassSettings settings) throws TransformerConfigurationException {
        return TransformerFactory.newInstance().newTransformer();
    }

    /**
     * This converter does not support a singleton wrapper strategy.
     */
    public boolean supports(String wrapper) {
        return !CompassEnvironment.Xsem.XmlContent.WRAPPER_SINGLETON.equals(wrapper);
    }

    /**
     * Uses the already created {@link javax.xml.parsers.DocumentBuilder}
     * and parse the given xml into a {@link NodeAliasedXmlObject}.
     *
     * @param alias The alias that will be associated with the {@link NodeAliasedXmlObject}
     * @param xml   The xml string to parse into {@link NodeAliasedXmlObject}
     * @return A {@link NodeAliasedXmlObject} parsed from the given xml string and associated with the given alias
     * @throws ConversionException In case the xml parsing failed
     */
    public AliasedXmlObject fromXml(String alias, Reader xml) throws ConversionException {
        Document document;
        try {
            document = documentBuilder.parse(new InputSource(xml));
        } catch (Exception e) {
            throw new ConversionException("Failed to parse alias[" + alias + "] xml[" + xml + "]", e);
        }
        return new NodeAliasedXmlObject(alias, document);
    }

    /**
     * Converts a {@link NodeXmlObject} into an xml string.
     * Uses the created {@link javax.xml.transform.Transformer} to do it.
     *
     * @param xmlObject The {@link NodeXmlObject} to convert into an xml string
     * @return The xml string representation of the given {@link NodeXmlObject}
     * @throws ConversionException Should not really happen...
     */
    public String toXml(XmlObject xmlObject) throws ConversionException {
        NodeXmlObject nodeXmlObject = (NodeXmlObject) xmlObject;
        Source source = new DOMSource(nodeXmlObject.getNode());
        StringBuilderWriter sw = StringBuilderWriter.Cached.cached();
        Result result = new StreamResult(sw);
        try {
            transformer.transform(source, result);
        } catch (Exception e) {
            throw new ConversionException("Failed to marshall to xml, this should not happen", e);
        }
        return sw.toString();
    }
}
