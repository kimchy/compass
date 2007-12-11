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

import org.compass.core.util.DomUtils;
import org.compass.core.xml.XmlObject;
import org.compass.core.xml.XmlXPathExpression;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * A java 5 implementation of {@link XmlObject} wrapping a {@link Node}.
 *
 * @author kimchy
 */
public class NodeXmlObject implements XmlObject {

    private Node node;

    /**
     * Constructs a new xml object using the given {@link Node}.
     */
    public NodeXmlObject(Node node) {
        this.node = node;
    }

    /**
     * Returns the node name, if the {@link org.w3c.dom.Node#getLocalName()} is not <code>null</code>
     * will return it, otherwise will return {@link org.w3c.dom.Node#getNodeName()}.
     */
    public String getName() {
        if (node.getLocalName() != null) {
            return node.getLocalName();
        }
        return node.getNodeName();
    }

    /**
     * Returns the node value, using {@link org.w3c.dom.Node#getNodeValue()} with the
     * exception of element, which has special handling using {@link DomUtils#getTextValue(org.w3c.dom.Element)}.
     */
    public String getValue() {
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            return DomUtils.getTextValue((Element) node);
        }
        return node.getNodeValue();
    }

    /**
     * Compiles and selects the given xpath expression.
     */
    public XmlObject[] selectPath(String path) throws Exception {
        return compile(path).select(this);
    }

    /**
     * Returns <code>true</code> since xpath expression compilation is supported.
     */
    public boolean canCompileXpath() {
        return true;
    }

    /**
     * Compiles the given xpath expression.
     */
    public XmlXPathExpression compile(String path) throws Exception {
        XPathExpression xPathExpression = XPathFactory.newInstance().newXPath().compile(path);
        return new XPathXmlXPathExpression(xPathExpression);
    }

    /**
     * Returns the {@link Node} this xml object wraps.
     */
    public Node getNode() {
        return node;
    }

}
