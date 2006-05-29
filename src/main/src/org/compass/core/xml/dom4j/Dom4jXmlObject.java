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
 * @author kimchy
 */
public class Dom4jXmlObject implements XmlObject {

    private Node node;

    public Dom4jXmlObject(Node node) {
        this.node = node;
    }

    public String getName() {
        return node.getName();
    }

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

    public boolean canCompileXpath() {
        return true;
    }

    public XmlXPathExpression compile(String path) {
        return new Dom4jXmlXPathExpression(new DefaultXPath(path));
    }

    public Node getNode() {
        return node;
    }
}
