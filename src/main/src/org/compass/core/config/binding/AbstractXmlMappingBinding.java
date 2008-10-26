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

package org.compass.core.config.binding;

import java.io.InputStream;

import org.compass.core.config.ConfigurationException;
import org.compass.core.mapping.MappingException;
import org.compass.core.util.config.ConfigurationHelper;
import org.compass.core.util.config.XmlConfigurationHelperBuilder;
import org.xml.sax.EntityResolver;

/**
 * @author kimchy
 */
public abstract class AbstractXmlMappingBinding extends AbstractInputStreamMappingBinding {

    private EntityResolver entityResolver;

    public AbstractXmlMappingBinding() {
        entityResolver = doGetEntityResolver();
    }

    protected boolean doAddInputStream(InputStream is, String resourceName) throws ConfigurationException, MappingException {
        XmlConfigurationHelperBuilder builder = new XmlConfigurationHelperBuilder();
        builder.setEntityResolver(entityResolver);
        ConfigurationHelper conf = builder.build(is, resourceName);
        conf.makeReadOnly();
        return doAddConfiguration(conf);
    }

    protected abstract EntityResolver doGetEntityResolver();

    protected abstract boolean doAddConfiguration(ConfigurationHelper conf) throws ConfigurationException, MappingException;
}
