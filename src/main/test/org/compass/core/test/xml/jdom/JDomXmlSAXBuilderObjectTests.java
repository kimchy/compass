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

import java.io.Reader;

import org.compass.core.config.CompassEnvironment;
import org.compass.core.config.CompassSettings;
import org.compass.core.converter.xsem.XmlContentConverter;
import org.compass.core.test.xml.AbstractXmlObjectTests;
import org.compass.core.xml.AliasedXmlObject;
import org.compass.core.xml.XmlObject;
import org.compass.core.xml.jdom.JDomAliasedXmlObject;
import org.compass.core.xml.jdom.JDomXmlObject;
import org.compass.core.xml.jdom.converter.SAXBuilderXmlContentConverter;
import org.jdom.Document;
import org.jdom.input.SAXBuilder;

/**
 * @author kimchy
 */
public class JDomXmlSAXBuilderObjectTests extends AbstractXmlObjectTests {

    protected void addSettings(CompassSettings settings) {
        settings.setSetting(CompassEnvironment.Xsem.XmlContent.TYPE, CompassEnvironment.Xsem.XmlContent.JDom.TYPE_SAX);
    }

    protected Class<? extends XmlContentConverter> getContentConverterType() {
        return SAXBuilderXmlContentConverter.class;
    }

    protected AliasedXmlObject buildAliasedXmlObject(String alias, Reader data) throws Exception {
        SAXBuilder reader = new SAXBuilder();
        Document document = reader.build(data);
        return new JDomAliasedXmlObject(alias, document.getRootElement());
    }

    public void testData4XmlContent() throws Exception {
        innerTestData4XmlContent();
    }

    public void testData5() throws Exception {
        innerTestData5WithNamespacePrefixXpath();
    }

    protected void verifyXmlObjectType(XmlObject xmlObject) {
        assertTrue(xmlObject instanceof JDomXmlObject);
    }
}