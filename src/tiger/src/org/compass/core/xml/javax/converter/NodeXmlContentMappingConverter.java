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

package org.compass.core.xml.javax.converter;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.compass.core.converter.ConversionException;
import org.compass.core.converter.mapping.xsem.AbstractXmlContentMappingConverter;
import org.compass.core.mapping.xsem.XmlContentMapping;
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
// TODO For better performance, we might want to pool DocumentBuilders and Transformers
public class NodeXmlContentMappingConverter extends AbstractXmlContentMappingConverter {

    /**
     * Uses {@link DocumentBuilderFactory} to create a {@link javax.xml.parsers.DocumentBuilder}
     * and parse the given xml into a {@link NodeAliasedXmlObject}.
     *
     * @param alias The alias that will be associated with the {@link NodeAliasedXmlObject}
     * @param xml   The xml string to parse into {@link NodeAliasedXmlObject}
     * @return A {@link NodeAliasedXmlObject} parsed from the given xml string and associated with the given alias
     * @throws ConversionException In case the xml parsing failed
     */
    @Override
    public AliasedXmlObject fromString(String alias, String xml) throws ConversionException {
        Document document;
        try {
            document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new StringReader(xml)));
        } catch (Exception e) {
            throw new ConversionException("Failed to parse alias[" + alias + "] xml[" + xml + "]", e);
        }
        return new NodeAliasedXmlObject(alias, document);
    }

    /**
     * Converts a {@link NodeXmlObject} into an xml string. Uses {@link TransformerFactory}
     * and {@link javax.xml.transform.Transformer} to do it.
     *
     * @param xmlObject The {@link NodeXmlObject} to convert into an xml string
     * @return The xml string representation of the given {@link NodeXmlObject}
     * @throws ConversionException Should not really happen...
     */
    @Override
    public String toString(XmlObject xmlObject) throws ConversionException {
        NodeXmlObject nodeXmlObject = (NodeXmlObject) xmlObject;
        Source source = new DOMSource(nodeXmlObject.getNode());
        StringWriter sw = new StringWriter();
        Result result = new StreamResult(sw);
        try {
            TransformerFactory.newInstance().newTransformer().transform(source, result);
        } catch (Exception e) {
            throw new ConversionException("Failed to marshall to xml, this should not happen", e);
        }
        return sw.toString();
    }
}
