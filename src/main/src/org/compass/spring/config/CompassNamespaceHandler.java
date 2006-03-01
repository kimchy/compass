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

package org.compass.spring.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.compass.core.config.CompassConfiguration;
import org.compass.core.config.ConfigurationException;
import org.compass.core.config.builder.SchemaConfigurationBuilder;
import org.compass.core.util.ClassUtils;
import org.compass.core.util.DomUtils;
import org.compass.core.util.JdkVersion;
import org.compass.spring.LocalCompassBean;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistryBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;
import org.w3c.dom.Element;

/**
 * @author kimchy
 */
public class CompassNamespaceHandler extends NamespaceHandlerSupport {

    private static final Log log = LogFactory.getLog(CompassNamespaceHandler.class);

    private static final String DEFAULT_COMPASS_CONFIG = "org.compass.core.config.CompassConfiguration";

    private static final String ANNOTATIONS_COMPASS_CONFIG = "org.compass.core.config.CompassAnnotationsConfiguration";

    public CompassNamespaceHandler() {
        registerBeanDefinitionParser("compass", new CompassBeanDefinitionParser());
    }

    private static class CompassBeanDefinitionParser extends AbstractBeanDefinitionParser {

        protected void doParse(Element element, BeanDefinitionRegistryBuilder beanDefinitionRegistryBuilder) {
            BeanDefinitionBuilder definitionBuilder = BeanDefinitionBuilder.rootBeanDefinition(getBeanClass(element));
            String id = element.getAttribute("name");

            SchemaConfigurationBuilder schemaConfigurationBuilder = new SchemaConfigurationBuilder();
            String compassConfigurationClassName = DEFAULT_COMPASS_CONFIG;
            if (JdkVersion.getMajorJavaVersion() >= JdkVersion.JAVA_15) {
                compassConfigurationClassName = ANNOTATIONS_COMPASS_CONFIG;
            }
            Class compassConfigurationClass;
            CompassConfiguration config;
            try {
                compassConfigurationClass = ClassUtils.forName(compassConfigurationClassName);
            } catch (ClassNotFoundException e) {
                try {
                    compassConfigurationClass = ClassUtils.forName(DEFAULT_COMPASS_CONFIG);
                } catch (ClassNotFoundException e1) {
                    throw new ConfigurationException("Failed to create configuration class ["
                            + compassConfigurationClassName + "] and default [" + DEFAULT_COMPASS_CONFIG + "]", e);
                }
            }
            try {
                config = (CompassConfiguration) compassConfigurationClass.newInstance();
            } catch (Exception e) {
                throw new ConfigurationException("Failed to create configuration class ["
                        + compassConfigurationClass.getName() + "]", e);
            }

            schemaConfigurationBuilder.processCompass(element, config);
            definitionBuilder.addPropertyValue("compassConfiguration", config);

            String txManagerRef = DomUtils.getElementAttribute(element, "txManager");
            if (txManagerRef != null) {
                definitionBuilder.addPropertyReference("transactionManager", txManagerRef);
            }

            String dataSourceRef = DomUtils.getElementAttribute(element, "dataSource");
            if (dataSourceRef != null) {
                definitionBuilder.addPropertyReference("dataSource", dataSourceRef);
            }

            beanDefinitionRegistryBuilder.register(id, definitionBuilder);
        }

        protected Class getBeanClass(Element element) {
            return LocalCompassBean.class;
        }
    }
}
