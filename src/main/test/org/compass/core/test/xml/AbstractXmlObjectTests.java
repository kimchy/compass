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

package org.compass.core.test.xml;

import java.io.InputStreamReader;
import java.io.Reader;

import org.compass.core.CompassHits;
import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.Resource;
import org.compass.core.config.CompassConfiguration;
import org.compass.core.config.CompassEnvironment;
import org.compass.core.converter.mapping.xsem.XmlContentMappingConverter;
import org.compass.core.converter.xsem.XmlContentConverter;
import org.compass.core.converter.xsem.XmlContentConverterWrapper;
import org.compass.core.mapping.xsem.XmlContentMapping;
import static org.compass.core.mapping.xsem.builder.XSEM.*;
import org.compass.core.test.AbstractTestCase;
import org.compass.core.xml.AliasedXmlObject;
import org.compass.core.xml.XmlObject;

/**
 * @author kimchy
 */
public abstract class AbstractXmlObjectTests extends AbstractTestCase {

    protected String[] getMappings() {
        return new String[]{"xml/xml.cpm.xml"};
    }

    @Override
    protected void addExtraConf(CompassConfiguration conf) {
        conf.getSettings().setGroupSettings(CompassEnvironment.Xsem.Namespace.PREFIX, "test1",
                new String[]{CompassEnvironment.Xsem.Namespace.URI}, new String[]{"http://test1"});
        conf.getSettings().setGroupSettings(CompassEnvironment.Xsem.Namespace.PREFIX, "test2",
                new String[]{CompassEnvironment.Xsem.Namespace.URI}, new String[]{"http://test2"});
    }

    protected void addProgrammaticConfiguration(CompassConfiguration conf) {
        conf.addMapping(
                xml("data1")
                        .add(id("/xml-fragment/data/id/@value").indexName("id"))
                        .add(property("/xml-fragment/data/data1/@value"))
                        .add(property("/xml-fragment/data/data1").indexName("eleText"))
        );
        conf.addMapping(
                xml("data2").xpath("/xml-fragment/data[1]")
                        .add(id("id/@value").indexName("id"))
                        .add(property("data1/@value"))
                        .add(property("data1").indexName("eleText"))
        );
        conf.addMapping(
                xml("data3").xpath("/xml-fragment/data")
                        .add(id("id/@value").indexName("id"))
                        .add(property("data1/@value"))
                        .add(property("data1").indexName("eleText"))
        );
        conf.addMapping(
                xml("data4").xpath("/xml-fragment/data")
                        .add(id("id/@value").indexName("id"))
                        .add(property("data1/@value"))
                        .add(property("data1").indexName("eleText"))
                        .add(content("content"))
        );
        conf.addMapping(
                xml("data5-1").xpath("/xml-fragment/test1:data")
                        .add(id("test1:id/@value").indexName("id"))
                        .add(property("test1:data1/@value"))
                        .add(property("test1:data1").indexName("eleText"))
                        .add(content("content"))
        );
        conf.addMapping(
                xml("data5-2").xpath("/xml-fragment/data")
                        .add(id("id/@value").indexName("id"))
                        .add(property("data1/@value"))
                        .add(property("data1").indexName("eleText"))
                        .add(content("content"))
        );
        conf.addMapping(
                xml("data6").xpath("/xml-fragment/data")
                        .add(id("id/@value").indexName("id"))
                        .add(property("data1/@value").format("000000.0000").valueConverter("float"))
                        .add(property("data1").indexName("eleText").format("yyyy-MM-dd||dd-MM-yyyy").valueConverter("date"))
                        .add(content("content"))
        );
        conf.addMapping(
                contract("contract1")
                        .add(id("/xml-fragment/data/id/@value").indexName("id"))
                        .add(property("/xml-fragment/data/data1/@value"))
        );
        conf.addMapping(
                contract("contract2")
                        .add(property("/xml-fragment/data/data1").indexName("eleText"))
        );
        conf.addMapping(
                xml("data7").extendsAliases("contract1", "contract2")
        );
    }

    protected abstract AliasedXmlObject buildAliasedXmlObject(String alias, Reader data) throws Exception;

    private Reader readData(String path) {
        path = "org/compass/core/test/xml/" + path + ".xml";
        return new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream(path));
    }

    public void testConverterType() {
        XmlContentMappingConverter converter = (XmlContentMappingConverter) getCompass().getConverterLookup().lookupConverter(XmlContentMapping.class);
        XmlContentConverter xmlContentConverter = converter.getXmlContentConverter();
        if (xmlContentConverter instanceof XmlContentConverterWrapper) {
            xmlContentConverter = ((XmlContentConverterWrapper) xmlContentConverter).createContentConverter();
        }
        assertTrue(getContentConverterType().isAssignableFrom(xmlContentConverter.getClass()));
    }

    protected abstract Class<? extends XmlContentConverter> getContentConverterType(); 

    public void testData1() throws Exception {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        AliasedXmlObject xmlObject = buildAliasedXmlObject("data1", readData("data1"));
        session.save(xmlObject);

        assertNull(session.get("data1", "1"));

        Resource resource = session.loadResource("data1", "1");
        assertEquals("1", resource.getValue("$/data1/id"));
        assertEquals(2, resource.getProperties("eleText").length);
        assertEquals(2, resource.getProperties("value").length);

        resource = session.loadResource("data1", xmlObject);
        assertEquals("1", resource.getValue("$/data1/id"));
        assertEquals(2, resource.getProperties("eleText").length);
        assertEquals(2, resource.getProperties("value").length);

        CompassHits hits = session.find("data11");
        assertEquals(1, hits.length());
        hits = session.find("data11attr");
        assertEquals(1, hits.length());

        tr.commit();
        session.close();
    }

    public void testData2() throws Exception {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        AliasedXmlObject xmlObject = buildAliasedXmlObject("data2", readData("data2"));
        session.save(xmlObject);

        Resource resource = session.loadResource("data2", "1");
        assertEquals("1", resource.getValue("$/data2/id"));
        assertEquals(2, resource.getProperties("eleText").length);
        assertEquals(2, resource.getProperties("value").length);

        resource = session.getResource("data2", "2");
        assertNull(resource);

        CompassHits hits = session.find("data11");
        assertEquals(1, hits.length());
        hits = session.find("data11attr");
        assertEquals(1, hits.length());

        tr.commit();
        session.close();
    }

    public void testData3() throws Exception {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        AliasedXmlObject xmlObject = buildAliasedXmlObject("data3", readData("data3"));
        session.save(xmlObject);

        Resource resource = session.loadResource("data3", "1");
        assertEquals("1", resource.getValue("$/data3/id"));
        assertEquals(2, resource.getProperties("eleText").length);
        assertEquals(2, resource.getProperties("value").length);

        resource = session.getResource("data3", "2");
        assertEquals("2", resource.getValue("$/data3/id"));
        assertEquals(2, resource.getProperties("eleText").length);
        assertEquals(2, resource.getProperties("value").length);

        CompassHits hits = session.find("data11");
        assertEquals(1, hits.length());
        hits = session.find("data11attr");
        assertEquals(1, hits.length());
        hits = session.find("data21attr");
        assertEquals(1, hits.length());

        session.delete(xmlObject);
        hits = session.find("data11attr");
        assertEquals(0, hits.length());
        hits = session.find("data21attr");
        assertEquals(0, hits.length());

        tr.commit();
        session.close();
    }

    public void innerTestData4XmlContent() throws Exception {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        AliasedXmlObject xmlObject = buildAliasedXmlObject("data4", readData("data4"));
        session.save(xmlObject);

        Resource resource = session.loadResource("data4", "1");
        assertEquals("1", resource.getValue("$/data4/id"));
        assertEquals(2, resource.getProperties("eleText").length);
        assertEquals(2, resource.getProperties("value").length);
        assertNotNull(resource.getValue("content"));

        resource = session.getResource("data4", "2");
        assertEquals("2", resource.getValue("$/data4/id"));
        assertEquals(2, resource.getProperties("eleText").length);
        assertEquals(2, resource.getProperties("value").length);
        assertNotNull(resource.getValue("content"));

        xmlObject = (AliasedXmlObject) session.get("data4", "1");
        assertNotNull(xmlObject);
        verifyXmlObjectType(xmlObject);
        
        XmlObject[] ids = xmlObject.selectPath("/data/id/@value");
        assertEquals(1, ids.length);
        assertEquals("1", ids[0].getValue());
        xmlObject = (AliasedXmlObject) session.get("data4", "2");
        assertNotNull(xmlObject);
        ids = xmlObject.selectPath("/data/id/@value");
        assertEquals(1, ids.length);
        assertEquals("2", ids[0].getValue());

        tr.commit();
        session.close();
    }

    protected void innerTestData5WithNamespacePrefixXpath() throws Exception {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        AliasedXmlObject xmlObject = buildAliasedXmlObject("data5-1", readData("data5"));
        session.save(xmlObject);

        assertNotNull(session.get("data5-1", "1"));
        assertNull(session.get("data5-2", "2"));

        Resource resource = session.loadResource("data5-1", "1");
        assertEquals("1", resource.getValue("$/data5-1/id"));
        assertEquals(2, resource.getProperties("eleText").length);
        assertEquals(2, resource.getProperties("value").length);

        resource = session.loadResource("data5-1", xmlObject);
        assertEquals("1", resource.getValue("$/data5-1/id"));
        assertEquals(2, resource.getProperties("eleText").length);
        assertEquals(2, resource.getProperties("value").length);

        CompassHits hits = session.find("data11");
        assertEquals(1, hits.length());
        hits = session.find("data11attr");
        assertEquals(1, hits.length());

        tr.commit();
        session.close();
    }

    protected void innerTestData5WithoutNamespacePrefixXpath() throws Exception {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        AliasedXmlObject xmlObject = buildAliasedXmlObject("data5-2", readData("data5"));
        session.save(xmlObject);

        assertNotNull(session.get("data5-2", "1"));

        Resource resource = session.loadResource("data5-2", "1");
        assertEquals("1", resource.getValue("$/data5-2/id"));
        assertEquals(2, resource.getProperties("eleText").length);
        assertEquals(2, resource.getProperties("value").length);

        CompassHits hits = session.find("data11");
        assertEquals(1, hits.length());
        hits = session.find("data11attr");
        assertEquals(1, hits.length());

        tr.commit();
        session.close();
    }

    public void testData6WhichTestsFormatters() throws Exception {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        AliasedXmlObject xmlObject = buildAliasedXmlObject("data6", readData("data6"));
        session.save(xmlObject);

        assertNotNull(session.get("data6", "1"));

        Resource resource = session.loadResource("data6", "1");
        assertEquals("1", resource.getValue("$/data6/id"));
        assertEquals("2001-12-03", resource.getValue("eleText"));
        assertEquals("000021.2000", resource.getValue("value"));

        tr.commit();
        session.close();
    }

    public void testContractData7() throws Exception {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        AliasedXmlObject xmlObject = buildAliasedXmlObject("data7", readData("data7"));
        session.save(xmlObject);

        assertNull(session.get("data7", "1"));

        Resource resource = session.loadResource("data7", "1");
        assertEquals("1", resource.getValue("$/data7/id"));
        assertEquals(2, resource.getProperties("eleText").length);
        assertEquals(2, resource.getProperties("value").length);

        resource = session.loadResource("data7", xmlObject);
        assertEquals("1", resource.getValue("$/data7/id"));
        assertEquals(2, resource.getProperties("eleText").length);
        assertEquals(2, resource.getProperties("value").length);

        CompassHits hits = session.find("data11");
        assertEquals(1, hits.length());
        hits = session.find("data11attr");
        assertEquals(1, hits.length());

        tr.commit();
        session.close();
    }

    protected abstract void verifyXmlObjectType(XmlObject xmlObject);
}
