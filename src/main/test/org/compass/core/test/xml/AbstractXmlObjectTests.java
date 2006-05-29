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

package org.compass.core.test.xml;

import java.io.InputStreamReader;
import java.io.Reader;

import org.compass.core.CompassHits;
import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.Resource;
import org.compass.core.test.AbstractTestCase;
import org.compass.core.xml.AliasedXmlObject;

/**
 * @author kimchy
 */
public abstract class AbstractXmlObjectTests extends AbstractTestCase {

    protected String[] getMappings() {
        return new String[]{"xml/xml.cpm.xml"};
    }

    protected abstract AliasedXmlObject buildAliasedXmlObject(String alias, Reader data) throws Exception;

    private Reader readData(String path) {
        path = "org/compass/core/test/xml/" + path + ".xml";
        return new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream(path));
    }

    public void testData1() throws Exception {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        AliasedXmlObject xmlObject = buildAliasedXmlObject("data1", readData("data1"));
        session.save(xmlObject);

        Resource resource = session.loadResource("data1", "1");
        assertEquals("1", resource.get("$/data1/id"));
        assertEquals(2, resource.getProperties("eleText").length);
        assertEquals(2, resource.getProperties("value").length);

        resource = session.loadResource("data1", xmlObject);
        assertEquals("1", resource.get("$/data1/id"));
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
        assertEquals("1", resource.get("$/data2/id"));
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
        assertEquals("1", resource.get("$/data3/id"));
        assertEquals(2, resource.getProperties("eleText").length);
        assertEquals(2, resource.getProperties("value").length);

        resource = session.getResource("data3", "2");
        assertEquals("2", resource.get("$/data3/id"));
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
}
