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

package org.compass.core.xml.javax;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
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

    private Map<String, String> namespaces;

    /**
     * Constructs a new xml object using the given {@link Node}.
     */
    public NodeXmlObject(Node node) {
        this.node = node;
    }

    public NodeXmlObject(Node node, Map<String, String> namespaces) {
        this.node = node;
        this.namespaces = namespaces;
    }

    public void setNamespaces(Map<String, String> namespaces) {
        this.namespaces = namespaces;
    }

    public Map<String, String> getNamespaces() {
        return namespaces;
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
        XPath xpath = XPathFactory.newInstance().newXPath();
        if (namespaces != null) {
            xpath.setNamespaceContext(new InternalNamespaceContext(namespaces));
        }
        XPathExpression xPathExpression = xpath.compile(path);
        return new XPathXmlXPathExpression(xPathExpression);
    }

    /**
     * Returns the {@link Node} this xml object wraps.
     */
    public Node getNode() {
        return node;
    }

    private class InternalNamespaceContext implements NamespaceContext {

        private Map<String, String> namespaces;

        private InternalNamespaceContext(Map<String, String> namespaces) {
            this.namespaces = namespaces;
        }

        public String getNamespaceURI(String prefix) {
            return namespaces.get(prefix);
        }

        public String getPrefix(String namespaceURI) {
            for (Map.Entry<String, String> entry : namespaces.entrySet()) {
                if (namespaceURI.equals(entry.getValue())) {
                    return entry.getKey();
                }
            }
            return null;
        }

        public Iterator getPrefixes(String namespaceURI) {
            List<String> prefixes = new ArrayList<String>();
            for (Map.Entry<String, String> entry : namespaces.entrySet()) {
                if (namespaceURI.equals(entry.getValue())) {
                    prefixes.add(entry.getKey());
                }
            }
            return prefixes.iterator();
        }
    }
}
