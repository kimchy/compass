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

import org.compass.core.config.CompassConfiguration;
import org.compass.core.config.builder.SchemaConfigurationBuilder;
import org.compass.core.util.DomUtils;
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

    public CompassNamespaceHandler() {
        registerBeanDefinitionParser("compass", new CompassBeanDefinitionParser());
    }

    private static class CompassBeanDefinitionParser extends AbstractBeanDefinitionParser {

        protected void doParse(Element element, BeanDefinitionRegistryBuilder beanDefinitionRegistryBuilder) {
            BeanDefinitionBuilder definitionBuilder = BeanDefinitionBuilder.rootBeanDefinition(getBeanClass(element));
            String id = element.getAttribute("name");

            SchemaConfigurationBuilder schemaConfigurationBuilder = new SchemaConfigurationBuilder();
            CompassConfiguration config = new CompassConfiguration();
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
