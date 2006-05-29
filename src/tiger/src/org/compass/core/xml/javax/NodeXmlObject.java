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

package org.compass.core.xml.javax;

import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.compass.core.xml.XmlObject;
import org.compass.core.xml.XmlXPathExpression;
import org.compass.core.util.DomUtils;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

/**
 * @author kimchy
 */
public class NodeXmlObject implements XmlObject {

    private Node node;

    public NodeXmlObject(Node node) {
        this.node = node;
    }

    public String getName() {
        if (node.getLocalName() != null) {
            return node.getLocalName();
        }
        return node.getNodeName();
    }

    public String getValue() {
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            return DomUtils.getTextValue((Element) node);
        }
        return node.getNodeValue();
    }

    public XmlObject[] selectPath(String path) {
        throw new IllegalStateException("This should not be called since tiger support compilation of xpath expressions");
    }

    public boolean canCompileXpath() {
        return true;
    }

    public XmlXPathExpression compile(String path) throws Exception {
        XPathExpression xPathExpression = XPathFactory.newInstance().newXPath().compile(path);
        return new XPathXmlXPathExpression(xPathExpression);
    }

    public Node getNode() {
        return node;
    }

}
