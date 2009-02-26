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

package org.compass.core.test.xml.dom4j;

import java.io.Reader;

import org.compass.core.config.CompassEnvironment;
import org.compass.core.config.CompassSettings;
import org.compass.core.converter.xsem.XmlContentConverter;
import org.compass.core.test.xml.AbstractXmlObjectTests;
import org.compass.core.xml.AliasedXmlObject;
import org.compass.core.xml.XmlObject;
import org.compass.core.xml.dom4j.Dom4jAliasedXmlObject;
import org.compass.core.xml.dom4j.Dom4jXmlObject;
import org.compass.core.xml.dom4j.converter.XPPReaderXmlContentConverter;
import org.dom4j.Document;
import org.dom4j.io.SAXReader;

/**
 * @author kimchy
 */
public class Dom4jXmlXPPReaderObjectTests extends AbstractXmlObjectTests {

    protected void addSettings(CompassSettings settings) {
        settings.setClassSetting(CompassEnvironment.Xsem.XmlContent.TYPE, XPPReaderXmlContentConverter.class);
    }

    protected Class<? extends XmlContentConverter> getContentConverterType() {
        return XPPReaderXmlContentConverter.class;
    }

    protected AliasedXmlObject buildAliasedXmlObject(String alias, Reader data) throws Exception {
        SAXReader reader = new SAXReader();
        Document document = reader.read(data);
        return new Dom4jAliasedXmlObject(alias, document.getRootElement());
    }

    public void testData4XmlContent() throws Exception {
        innerTestData4XmlContent();
    }

    public void testData5() throws Exception {
        innerTestData5WithNamespacePrefixXpath();
    }

    protected void verifyXmlObjectType(XmlObject xmlObject) {
        assertTrue(xmlObject instanceof Dom4jXmlObject);
    }
}
