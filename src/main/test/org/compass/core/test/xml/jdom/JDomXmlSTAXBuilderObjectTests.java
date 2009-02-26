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

package org.compass.core.test.xml.jdom;

import org.compass.core.config.CompassEnvironment;
import org.compass.core.config.CompassSettings;
import org.compass.core.converter.xsem.XmlContentConverter;
import org.compass.core.xml.jdom.converter.STAXBuilderXmlContentConverter;

/**
 * @author kimchy
 */
public class JDomXmlSTAXBuilderObjectTests extends JDomXmlSAXBuilderObjectTests {

    protected void addSettings(CompassSettings settings) {
        settings.setSetting(CompassEnvironment.Xsem.XmlContent.TYPE, CompassEnvironment.Xsem.XmlContent.JDom.TYPE_STAX);
    }

    @Override
    protected Class<? extends XmlContentConverter> getContentConverterType() {
        return STAXBuilderXmlContentConverter.class;
    }
}