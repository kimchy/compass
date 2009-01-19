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

package org.compass.spring.config;

import org.compass.core.CompassException;
import org.compass.core.config.CompassConfiguration;
import org.compass.core.config.CompassConfigurationFactory;
import org.compass.core.config.builder.SchemaConfigurationBuilder;
import org.compass.core.util.ClassUtils;
import org.compass.core.util.DomUtils;
import org.compass.spring.LocalCompassBean;
import org.compass.spring.LocalCompassSessionBean;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;
import org.w3c.dom.Element;

/**
 * @author kimchy
 */
public class CompassNamespaceHandler extends NamespaceHandlerSupport {

    public CompassNamespaceHandler() {
    }

    public void init() {
        registerBeanDefinitionParser("compass", new CompassBeanDefinitionParser());
        registerBeanDefinitionParser("context", new CompassBeanDefinitionParser());
        registerBeanDefinitionParser("session", new CompassBeanDefinitionParser());
    }

    private static class CompassBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

        protected void doParse(Element element, BeanDefinitionBuilder beanDefinitionBuilder) {
            if (element.getLocalName().equals("compass")) {
                String id = element.getAttribute("name");

                // set the id so it will be registered under it in the base class
                element.setAttribute(ID_ATTRIBUTE, id);

                SchemaConfigurationBuilder schemaConfigurationBuilder = new SchemaConfigurationBuilder();
                CompassConfiguration config = CompassConfigurationFactory.newConfiguration();
                schemaConfigurationBuilder.processCompass(element, config);
                beanDefinitionBuilder.addPropertyValue("compassConfiguration", config);

                String txManagerRef = DomUtils.getElementAttribute(element, "txManager");
                if (txManagerRef != null) {
                    beanDefinitionBuilder.addPropertyReference("transactionManager", txManagerRef);
                }

                String dataSourceRef = DomUtils.getElementAttribute(element, "dataSource");
                if (dataSourceRef != null) {
                    beanDefinitionBuilder.addPropertyReference("dataSource", dataSourceRef);
                }

                String lazyInit = DomUtils.getElementAttribute(element, "lazy-init");
                if (lazyInit == null || lazyInit.equalsIgnoreCase("false")) {
                    beanDefinitionBuilder.setLazyInit(false);
                } else {
                    beanDefinitionBuilder.setLazyInit(true);
                }

                String scope = DomUtils.getElementAttribute(element, "scope");
                if (scope != null) {
                    beanDefinitionBuilder.setScope(scope);
                }

                String postProcessRef = DomUtils.getElementAttribute(element, "postProcessor");
                if (postProcessRef != null) {
                    beanDefinitionBuilder.addPropertyReference("postProcessor", postProcessRef);
                }
            } else if (element.getLocalName().equals("context")) {
                element.setAttribute(ID_ATTRIBUTE, "" + System.currentTimeMillis());
            } else if (element.getLocalName().equals("session")) {
                String compassRef = DomUtils.getElementAttribute(element, "compass");
                if (compassRef != null) {
                    beanDefinitionBuilder.addPropertyReference("compass", compassRef);
                }
            }
        }

        protected Class getBeanClass(Element element) {
            if (element.getLocalName().equals("compass")) {
                return LocalCompassBean.class;
            } else if (element.getLocalName().equals("context")) {
                try {
                    return ClassUtils.forName(COMPASS_CONTEXT_BEAN_POST_PROCESSOR, Thread.currentThread().getContextClassLoader());
                } catch (ClassNotFoundException e) {
                    throw new CompassException("Failed to find class [" + COMPASS_CONTEXT_BEAN_POST_PROCESSOR + "]");
                }
            } else if (element.getLocalName().equals("session")) {
                return LocalCompassSessionBean.class;
            } else {
                throw new CompassException("Failed to parse element [" + element.getLocalName() + "]");
            }
        }
    }

    private static final String COMPASS_CONTEXT_BEAN_POST_PROCESSOR = "org.compass.spring.support.CompassContextBeanPostProcessor";
}
