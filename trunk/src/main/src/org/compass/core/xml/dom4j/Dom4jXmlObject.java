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

package org.compass.core.xml.dom4j;

import java.util.List;

import org.compass.core.xml.XmlObject;
import org.compass.core.xml.XmlXPathExpression;
import org.dom4j.Node;
import org.dom4j.xpath.DefaultXPath;

/**
 * A dom4j (http://www.dom4j.org) implementation of {@link XmlObject}.
 *
 * @author kimchy
 */
public class Dom4jXmlObject implements XmlObject {

    private Node node;

    /**
     * Constructs a new xml object based on a dom4j <code>Node</code>.
     *
     * @param node The node to construct the dom4j xml object with
     */
    public Dom4jXmlObject(Node node) {
        this.node = node;
    }

    /**
     * Returns the dom4j node name.
     */
    public String getName() {
        return node.getName();
    }

    /**
     * Returns the dom4j node text.
     */
    public String getValue() {
        return node.getText();
    }

    public XmlObject[] selectPath(String path) {
        List nodes = node.selectNodes(path);
        XmlObject[] xmlObjects = new XmlObject[nodes.size()];
        for (int i = 0; i < xmlObjects.length; i++) {
            xmlObjects[i] = new Dom4jXmlObject((Node) nodes.get(i));
        }
        return xmlObjects;
    }

    /**
     * Return <code>true</code> since dom4j supports xml compilation.
     */
    public boolean canCompileXpath() {
        return true;
    }

    /**
     * Compiles the given xpath expression using dom4j <code>DefaultXPath</code>.
     */
    public XmlXPathExpression compile(String path) {
        return new Dom4jXmlXPathExpression(new DefaultXPath(path));
    }

    /**
     * Returns the dom4j Node that this xml object wraps.
     */
    public Node getNode() {
        return node;
    }
}
