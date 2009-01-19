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

package org.compass.core.xml.jdom;

import java.util.List;

import org.compass.core.converter.ConversionException;
import org.compass.core.xml.XmlObject;
import org.compass.core.xml.XmlXPathExpression;
import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.xpath.XPath;

/**
 * A JDOM (http://www.jdom.org) implementation of {@link org.compass.core.xml.XmlXPathExpression}.
 *
 * @author kimchy
 */
public class JDomXmlXPathExpression implements XmlXPathExpression {

    private XPath xpath;

    public JDomXmlXPathExpression(XPath xpath) {
        this.xpath = xpath;
    }

    public XmlObject[] select(XmlObject xmlObject) throws JDOMException {
        JDomXmlObject jdomXmlObject = (JDomXmlObject) xmlObject;
        List nodes = xpath.selectNodes(jdomXmlObject.getElement());
        XmlObject[] xmlObjects = new XmlObject[nodes.size()];
        for (int i = 0; i < xmlObjects.length; i++) {
            Object node = nodes.get(i);
            if (node instanceof Element) {
                xmlObjects[i] = new JDomXmlObject((Element) nodes.get(i), jdomXmlObject.getNamespaces());
            } else if (node instanceof Attribute) {
                xmlObjects[i] = new JDomXmlObject((Attribute) nodes.get(i), jdomXmlObject.getNamespaces());
            } else {
                throw new ConversionException("Type [" + node.getClass() + "] is not supported");
            }
        }
        return xmlObjects;
    }
}