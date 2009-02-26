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

package org.compass.core.load.xsem;

import java.io.InputStreamReader;
import java.io.Reader;

import org.compass.core.config.CompassEnvironment;
import org.compass.core.config.CompassSettings;
import org.compass.core.converter.mapping.xsem.XmlContentMappingConverter;
import org.compass.core.converter.xsem.XmlContentConverter;
import org.compass.core.xml.XmlObject;
import org.compass.core.xml.XmlXPathExpression;
import org.compass.core.xml.dom4j.converter.SAXReaderXmlContentConverter;
import org.compass.core.xml.dom4j.converter.STAXReaderXmlContentConverter;
import org.compass.core.xml.dom4j.converter.XPP3ReaderXmlContentConverter;
import org.compass.core.xml.dom4j.converter.XPPReaderXmlContentConverter;
import org.compass.core.xml.javax.converter.NodeXmlContentConverter;
import org.compass.core.xml.javax.converter.StaxNodeXmlContentConverter;
import org.compass.core.xml.jdom.converter.SAXBuilderXmlContentConverter;
import org.compass.core.xml.jdom.converter.STAXBuilderXmlContentConverter;

/**
 * @author kimchy
 */
public class XmlContentConverterLoadTester {

    public static void main(String[] args) throws Exception {

        XmlContentConverter[] converters = new XmlContentConverter[]{new SAXReaderXmlContentConverter(),
                new XPPReaderXmlContentConverter(), new XPP3ReaderXmlContentConverter(), new STAXReaderXmlContentConverter(),
                new NodeXmlContentConverter(), new StaxNodeXmlContentConverter(),
                new SAXBuilderXmlContentConverter(), new STAXBuilderXmlContentConverter()};

        for (int i = 0; i < converters.length; i++) {
            System.gc();
            CompassSettings settings = new CompassSettings();
            settings.setSetting(CompassEnvironment.Xsem.XmlContent.TYPE, converters[i].getClass().getName());
            settings.setSetting(CompassEnvironment.Xsem.XmlContent.WRAPPER, CompassEnvironment.Xsem.XmlContent.WRAPPER_PROTOTYPE);
            XmlContentMappingConverter xmlContentMappingConverter = new XmlContentMappingConverter();
            xmlContentMappingConverter.configure(settings);
            testConverter(xmlContentMappingConverter.getXmlContentConverter(), 500);
        }
    }

    private static void testConverter(XmlContentConverter converter, int iterNum) throws Exception {
        long now = System.nanoTime();
        for (int i = 0; i < iterNum; i++) {
            converter.fromXml("a", readData("data2"));
        }
        long total = System.nanoTime() - now;
        System.out.println("[" + converter + "] Average fromXml nano time: " + (total / iterNum));

        XmlObject xmlObject = converter.fromXml("a", readData("data2"));

        now = System.nanoTime();
        for (int i = 0; i < iterNum; i++) {
            converter.toXml(xmlObject);
        }
        total = System.nanoTime() - now;
        System.out.println("[" + converter + "] Average toXml nano time: " + (total / iterNum));

        XmlXPathExpression expr = xmlObject.compile("/xml-fragment/data/id");
        now = System.nanoTime();
        for (int i = 0; i < iterNum; i++) {
            expr.select(xmlObject);
        }
        total = System.nanoTime() - now;
        System.out.println("[" + converter + "] Average xpath nano time: " + (total / iterNum));
    }

    private static Reader readData(String path) {
        path = "org/compass/core/load/xsem/" + path + ".xml";
        return new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream(path));
    }

}
