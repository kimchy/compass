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

package org.compass.core.config.builder;

import java.io.File;

import org.compass.core.config.CompassConfiguration;
import org.compass.core.config.CompassEnvironment;
import org.compass.core.config.ConfigurationException;
import org.compass.core.util.ClassUtils;
import org.compass.core.util.DTDEntityResolver;
import org.compass.core.util.DomUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;

/**
 * @author kimchy
 */
public class DTDConfigurationBuilder extends AbstractXmlConfigurationBuilder {

    protected void doProcess(Document doc, CompassConfiguration config) throws ConfigurationException {
        Element root = doc.getDocumentElement();
        // the root is the compass element
        NodeList nl = root.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node node = nl.item(i);
            if (node instanceof Element) {
                if ("compass".equals(node.getNodeName())) {
                    processCompass((Element) node, config);
                }
            }
        }
    }

    protected void processCompass(Element compassElement, CompassConfiguration config) throws ConfigurationException {
        String name = DomUtils.getElementAttribute(compassElement, "name", "default");
        config.getSettings().setSetting(CompassEnvironment.NAME, name);
        NodeList nl = compassElement.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node node = nl.item(i);
            if (node instanceof Element) {
                Element ele = (Element) node;
                if ("setting".equals(ele.getNodeName())) {
                    String settingName = ele.getAttribute("name");
                    String settingValue = DomUtils.getTrimmedTextValue(ele);
                    config.setSetting(settingName, settingValue);
                } else if ("mapping".equals(ele.getNodeName()) || "meta-data".equals(ele.getNodeName())) {
                    String rsrc = DomUtils.getElementAttribute(ele, "resource", null);
                    String file = DomUtils.getElementAttribute(ele, "file", null);
                    String jar = DomUtils.getElementAttribute(ele, "jar", null);
                    String pckg = DomUtils.getElementAttribute(ele, "package", null);
                    String clazz = DomUtils.getElementAttribute(ele, "class", null);
                    if (rsrc != null) {
                        config.addResource(rsrc);
                    } else if (jar != null) {
                        config.addJar(new File(jar));
                    } else if (pckg != null) {
                        config.addPackage(pckg);
                    } else if (clazz != null) {
                        try {
                            config.addClass(ClassUtils.forName(clazz, config.getClassLoader()));
                        } catch (ClassNotFoundException e) {
                            throw new ConfigurationException("Failed map class [" + clazz + "]", e);
                        }
                    } else {
                        if (file == null) {
                            throw new ConfigurationException(
                                    "<mapping> or <meta-data> element in configuration specifies no attributes");
                        }
                        config.addFile(file);
                    }
                } else if ("scan".equals(ele.getNodeName())) {
                    config.addScan(DomUtils.getElementAttribute(ele, "base-package"), DomUtils.getElementAttribute(ele, "pattern"));
                }
            }
        }

        log.info("Configured Compass [" + name + "]");
        if (log.isDebugEnabled()) {
            log.debug("--with settings [" + config.getSettings() + "]");
        }
    }

    protected EntityResolver doGetEntityResolver() {
        return new DTDEntityResolver();
    }
}
