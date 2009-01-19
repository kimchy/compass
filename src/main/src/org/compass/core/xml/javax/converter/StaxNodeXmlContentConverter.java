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
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.compass.core.CompassException;
import org.compass.core.config.CompassSettings;
import org.compass.core.converter.ConversionException;
import org.compass.core.xml.AliasedXmlObject;
import org.compass.core.xml.javax.NodeAliasedXmlObject;
import org.compass.core.xml.javax.converter.support.Stax2DomBuilder;
import org.w3c.dom.Document;

/**
 * <p>Uses JSE to convert an xml content to and from {@link org.compass.core.xml.javax.NodeAliasedXmlObject}.
 *
 * @author kimchy
 */
public class StaxNodeXmlContentConverter extends NodeXmlContentConverter {

    private static Log log = LogFactory.getLog(StaxNodeXmlContentConverter.class);

    private javax.xml.stream.XMLInputFactory inputFactory = javax.xml.stream.XMLInputFactory.newInstance();

    private javax.xml.stream.XMLOutputFactory outputFactory = javax.xml.stream.XMLOutputFactory.newInstance();

    private Stax2DomBuilder staxBuilder;

    public void configure(CompassSettings settings) throws CompassException {
        super.configure(settings);
        this.staxBuilder = new Stax2DomBuilder();
    }

    /**
     * Uses the already created {@link javax.xml.parsers.DocumentBuilder} and {@link org.compass.core.xml.javax.converter.support.Stax2DomBuilder}
     * and parse the given xml into a {@link org.compass.core.xml.javax.NodeAliasedXmlObject}.
     *
     * @param alias The alias that will be associated with the {@link org.compass.core.xml.javax.NodeAliasedXmlObject}
     * @param xml   The xml string to parse into {@link org.compass.core.xml.javax.NodeAliasedXmlObject}
     * @return A {@link org.compass.core.xml.javax.NodeAliasedXmlObject} parsed from the given xml string and associated with the given alias
     * @throws org.compass.core.converter.ConversionException
     *          In case the xml parsing failed
     */
    public AliasedXmlObject fromXml(String alias, Reader xml) throws ConversionException {
        Document document;
        try {
            XMLStreamReader sr = inputFactory.createXMLStreamReader(xml);
            document = staxBuilder.build(sr, documentBuilder);
        } catch (Exception e) {
            throw new ConversionException("Failed to parse alias[" + alias + "] xml[" + xml + "]", e);
        }
        return new NodeAliasedXmlObject(alias, document);
    }

    /**
     * Disabled for now
     */
//    public String toXml(XmlObject xmlObject) throws ConversionException {
//        NodeXmlObject nodeXmlObject = (NodeXmlObject) xmlObject;
//        StringWriter sw = new StringWriter();
//        try {
//            XMLStreamWriter xmlStreamWriter = outputFactory.createXMLStreamWriter(sw);
//            Dom2StaxSerializer serializer = new Dom2StaxSerializer(xmlStreamWriter);
//            if (nodeXmlObject.getNode().getNodeType() == Node.DOCUMENT_NODE) {
//                serializer.output((Document) nodeXmlObject.getNode());
//            } else {
//                serializer.outputFragment(nodeXmlObject.getNode());
//                xmlStreamWriter.close();
//            }
//        } catch (Exception e) {
//            throw new ConversionException("Failed to marshall to xml, this should not happen", e);
//        }
//        return sw.toString();
//    }
}