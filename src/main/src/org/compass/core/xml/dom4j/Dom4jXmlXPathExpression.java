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

package org.compass.core.xml.dom4j;

import java.util.List;

import org.compass.core.xml.XmlObject;
import org.compass.core.xml.XmlXPathExpression;
import org.dom4j.Node;
import org.dom4j.XPath;

/**
 * A dom4j (http://www.dom4j.org) implementation of {@link XmlXPathExpression}.
 *
 * @author kimchy
 */
public class Dom4jXmlXPathExpression implements XmlXPathExpression {

    private XPath xpath;

    public Dom4jXmlXPathExpression(XPath xpath) {
        this.xpath = xpath;
    }

    public XmlObject[] select(XmlObject xmlObject) {
        Dom4jXmlObject dom4jXmlObject = (Dom4jXmlObject) xmlObject;
        List nodes = xpath.selectNodes(dom4jXmlObject.getNode());
        XmlObject[] xmlObjects = new XmlObject[nodes.size()];
        for (int i = 0; i < xmlObjects.length; i++) {
            xmlObjects[i] = new Dom4jXmlObject((Node) nodes.get(i), dom4jXmlObject.getNamespaces());
        }
        return xmlObjects;
    }
}
