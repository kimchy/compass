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

package org.compass.core.util.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * This class has a bunch of utility methods to work with configuration objects.
 */
public class ConfigurationHelperUtil {

    /**
     * Private constructor to block instantiation.
     */
    private ConfigurationHelperUtil() {
    }

    /**
     * Convert a DOM Element tree into a configuration tree.
     */
    public static ConfigurationHelper toConfiguration(final Element element) {
        final PlainConfigurationHelper configuration = new PlainConfigurationHelper(element.getNodeName(), "dom-created");
        final NamedNodeMap attributes = element.getAttributes();
        final int length = attributes.getLength();
        for (int i = 0; i < length; i++) {
            final Node node = attributes.item(i);
            final String name = node.getNodeName();
            final String value = node.getNodeValue();
            configuration.setAttribute(name, value);
        }
        boolean flag = false;
        String content = "";
        final NodeList nodes = element.getChildNodes();
        final int count = nodes.getLength();
        for (int i = 0; i < count; i++) {
            final Node node = nodes.item(i);
            if (node instanceof Element) {
                final ConfigurationHelper child = toConfiguration((Element) node);
                configuration.addChild(child);
            } else if (node instanceof CharacterData) {
                final CharacterData data = (CharacterData) node;
                content += data.getData();
                flag = true;
            }
        }
        if (flag) {
            configuration.setValue(content);
        }
        return configuration;
    }

    /**
     * Convert a configuration tree into a DOM Element tree.
     */
    public static Element toElement(final ConfigurationHelper configuration) {
        try {
            final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            final DocumentBuilder builder = factory.newDocumentBuilder();
            final Document document = builder.newDocument();
            return createElement(document, configuration);
        } catch (final ParserConfigurationException pce) {
            throw new IllegalStateException(pce.toString());
        }
    }

    /**
     * Test to see if two Configuration's can be considered the same. Name,
     * value, attributes and children are test. The <b>order</b> of children is
     * not taken into consideration for equality.
     */
    public static boolean equals(final ConfigurationHelper c1, final ConfigurationHelper c2) {
        return c1.getName().equals(c2.getName()) && areValuesEqual(c1, c2) && areAttributesEqual(c1, c2)
                && areChildrenEqual(c1, c2);
    }

    /**
     * Return true if the children of both configurations are equal.
     */
    private static boolean areChildrenEqual(final ConfigurationHelper c1, final ConfigurationHelper c2) {
        final ConfigurationHelper[] kids1 = c1.getChildren();
        final ArrayList kids2 = new ArrayList(Arrays.asList(c2.getChildren()));
        if (kids1.length != kids2.size()) {
            return false;
        }
        for (int i = 0; i < kids1.length; i++) {
            if (!findMatchingChild(kids1[i], kids2)) {
                return false;
            }
        }
        return kids2.isEmpty() ? true : false;
    }

    /**
     * Return true if find a matching child and remove child from list.
     */
    private static boolean findMatchingChild(final ConfigurationHelper c, final ArrayList matchAgainst) {
        final Iterator i = matchAgainst.iterator();
        while (i.hasNext()) {
            if (equals(c, (ConfigurationHelper) i.next())) {
                i.remove();
                return true;
            }
        }
        return false;
    }

    /**
     * Return true if the attributes of both configurations are equal.
     */
    private static boolean areAttributesEqual(final ConfigurationHelper c1, final ConfigurationHelper c2) {
        final String[] names1 = c1.getAttributeNames();
        final String[] names2 = c2.getAttributeNames();
        if (names1.length != names2.length) {
            return false;
        }
        for (int i = 0; i < names1.length; i++) {
            final String name = names1[i];
            final String value1 = c1.getAttribute(name, null);
            final String value2 = c2.getAttribute(name, null);
            if (!value1.equals(value2)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Return true if the values of two configurations are equal.
     */
    private static boolean areValuesEqual(final ConfigurationHelper c1, final ConfigurationHelper c2) {
        final String value1 = c1.getValue(null);
        final String value2 = c2.getValue(null);
        return (value1 == null && value2 == null) || (value1 != null && value1.equals(value2));
    }

    /**
     * Create an DOM {@link Element} from a {@link ConfigurationHelper} object.
     */
    private static Element createElement(final Document document, final ConfigurationHelper configuration) {
        final Element element = document.createElement(configuration.getName());
        final String content = configuration.getValue(null);
        if (null != content) {
            final Text child = document.createTextNode(content);
            element.appendChild(child);
        }
        final String[] names = configuration.getAttributeNames();
        for (int i = 0; i < names.length; i++) {
            final String name = names[i];
            final String value = configuration.getAttribute(name, null);
            element.setAttribute(name, value);
        }
        final ConfigurationHelper[] children = configuration.getChildren();
        for (int i = 0; i < children.length; i++) {
            final Element child = createElement(document, children[i]);
            element.appendChild(child);
        }
        return element;
    }
}
