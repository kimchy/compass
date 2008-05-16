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

package org.compass.core.xml.jdom;

import org.compass.core.xml.XmlObject;
import org.compass.core.xml.XmlXPathExpression;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.xpath.XPath;

/**
 * A dom4j (http://www.dom4j.org) implementation of {@link org.compass.core.xml.XmlObject}.
 *
 * @author kimchy
 */
public class JDomXmlObject implements XmlObject {

    private Element element;

    private Attribute attribute;

    public JDomXmlObject(Document document) {
        this.element = document.getRootElement();
    }

    /**
     * Constructs a new xml object based on a JDMO <code>Element</code>.
     */
    public JDomXmlObject(Element element) {
        this.element = element;
    }

    /**
     * Constructs a new xml object based on a JDMO <code>Attribute</code>.
     */
    public JDomXmlObject(Attribute attribute) {
        this.attribute = attribute;
    }

    /**
     * Returns the JDOM node name.
     */
    public String getName() {
        if (attribute != null) {
            return attribute.getName();
        }
        return element.getName();
    }

    /**
     * Returns the JDOM node text.
     */
    public String getValue() {
        if (attribute != null) {
            return attribute.getValue();
        }
        return element.getText();
    }

    public XmlObject[] selectPath(String path) throws Exception {
        return compile(path).select(this);
    }

    /**
     * Return <code>true</code> since JDOM supports xml compilation.
     */
    public boolean canCompileXpath() {
        return true;
    }

    /**
     * Compiles the given xpath expression using JDMO <code>XPath.newInstance()</code>.
     */
    public XmlXPathExpression compile(String path) throws JDOMException {
        return new JDomXmlXPathExpression(XPath.newInstance(path));
    }

    /**
     * Returns the JDMO Element.
     */
    public Element getElement() {
        return this.element;
    }

    /**
     * Returns the JDMO Attribute.
     */
    public Attribute getAttribute() {
        return attribute;
    }
}