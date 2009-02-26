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

package org.compass.core.test.xml.javax;

import java.io.Reader;
import javax.xml.parsers.DocumentBuilderFactory;

import org.compass.core.config.CompassEnvironment;
import org.compass.core.config.CompassSettings;
import org.compass.core.converter.xsem.XmlContentConverter;
import org.compass.core.test.xml.AbstractXmlObjectTests;
import org.compass.core.xml.AliasedXmlObject;
import org.compass.core.xml.XmlObject;
import org.compass.core.xml.javax.NodeAliasedXmlObject;
import org.compass.core.xml.javax.NodeXmlObject;
import org.compass.core.xml.javax.converter.StaxNodeXmlContentConverter;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

/**
 * @author kimchy
 */
public class StaxNodeXmlObjectTests extends AbstractXmlObjectTests {

    protected void addSettings(CompassSettings settings) {
        settings.setSetting(CompassEnvironment.Xsem.XmlContent.TYPE, CompassEnvironment.Xsem.XmlContent.Javax.TYPE_STAX);
    }

    protected Class<? extends XmlContentConverter> getContentConverterType() {
        return StaxNodeXmlContentConverter.class;
    }

    protected AliasedXmlObject buildAliasedXmlObject(String alias, Reader data) throws Exception {
        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(data));
        return new NodeAliasedXmlObject(alias, document);
    }

    public void testData4XmlContent() throws Exception {
        innerTestData4XmlContent();
    }

    public void testData5() throws Exception {
        innerTestData5WithoutNamespacePrefixXpath();
    }

    protected void verifyXmlObjectType(XmlObject xmlObject) {
        assertTrue(xmlObject instanceof NodeXmlObject);
    }
}