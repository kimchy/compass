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

package org.compass.core.test.resource;

import java.io.StringReader;
import java.util.Calendar;

import org.compass.core.CompassHits;
import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.Property;
import org.compass.core.Resource;
import org.compass.core.config.CompassEnvironment;
import org.compass.core.config.CompassSettings;
import org.compass.core.mapping.CompassMapping;
import org.compass.core.mapping.ResourceMapping;
import org.compass.core.mapping.ResourcePropertyMapping;
import org.compass.core.spi.InternalCompassSession;
import org.compass.core.test.AbstractTestCase;

/**
 * @author kimchy
 */
public class ResourceTests extends AbstractTestCase {

    @Override
    protected String[] getMappings() {
        return new String[]{"resource/resource.cpm.xml"};
    }

    @Override
    protected void addSettings(CompassSettings settings) {
        settings.setGroupSettings(CompassEnvironment.Converter.PREFIX,
                "mydate",
                new String[]{CompassEnvironment.Converter.TYPE, CompassEnvironment.Converter.Format.FORMAT},
                new String[]{CompassEnvironment.Converter.DefaultTypes.Simple.DATE, "yyyy-MM-dd"});

    }

    public void testMapping() {
        InternalCompassSession session = (InternalCompassSession) openSession();

        CompassMapping mapping = session.getMapping();
        ResourceMapping resourceMapping = mapping.getMappingByAlias("a");
        ResourcePropertyMapping resourcePropertyMapping = resourceMapping.getResourcePropertyMappingByDotPath("id");
        assertNotNull(resourcePropertyMapping);
        assertEquals("id", resourcePropertyMapping.getName());
        assertEquals("id", resourcePropertyMapping.getPath().getPath());

        resourceMapping = mapping.getMappingByAlias("b");
        resourcePropertyMapping = resourceMapping.getResourcePropertyMappingByDotPath("id1");
        assertNotNull(resourcePropertyMapping);
        assertEquals("id1", resourcePropertyMapping.getName());
        assertEquals("id1", resourcePropertyMapping.getPath().getPath());
        resourcePropertyMapping = resourceMapping.getResourcePropertyMappingByDotPath("id2");
        assertNotNull(resourcePropertyMapping);
        assertEquals("id2", resourcePropertyMapping.getName());
        assertEquals("id2", resourcePropertyMapping.getPath().getPath());

        session.close();
    }

    public void testUIDResource() throws Exception {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        Resource r = getResourceFactory().createResource("a");
        r.addProperty("id", 1);
        session.save(r);

        r = session.loadResource("a", 1);
        assertEquals(1, r.getProperties("$/uid").length);
        session.save(r);
        r = session.loadResource("a", 1);
        assertEquals(1, r.getProperties("$/uid").length);

        tr.commit();
        session.close();
    }

    public void testSingleIdResource() throws Exception {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        Resource r = getResourceFactory().createResource("a");
        Property id = getResourceFactory().createProperty("id", "1", Property.Store.YES, Property.Index.NOT_ANALYZED);
        r.addProperty(id);
        r.addProperty(getCompass().getResourceFactory().createProperty("mvalue", "this is a test", Property.Store.YES, Property.Index.ANALYZED));

        session.save(r);

        r = session.getResource("a", "1");
        assertEquals("this is a test", r.getValue("mvalue"));

        r = session.getResource("a", (Object) new String[]{"1"});
        assertEquals("this is a test", r.getValue("mvalue"));

        r = session.getResource("a", id);
        assertEquals("this is a test", r.getValue("mvalue"));

        r = session.getResource("a", (Object) new Property[]{id});
        assertEquals("this is a test", r.getValue("mvalue"));

        r = session.getResource("a", r);
        assertEquals("this is a test", r.getValue("mvalue"));

        CompassHits hits = session.find("test");
        assertEquals(1, hits.getLength());
        assertEquals("this is a test", hits.resource(0).getValue("mvalue"));


        assertEquals(1, session.queryBuilder().matchAll().count());
        r = getResourceFactory().createResource("a");
        r.addProperty("id", 1);
        r.addProperty(getResourceFactory().createProperty("mvalue", "updatetests", Property.Store.YES, Property.Index.ANALYZED));
        session.save(r);
        assertEquals(1, session.queryBuilder().matchAll().count());
        hits = session.find("updatetests");
        assertEquals(1, hits.getLength());

        session.delete("a", "1");
        r = session.getResource("a", r);
        assertNull(r);

        tr.commit();
        session.close();
    }

    public void testSingleIdResourceDelete() throws Exception {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        Resource r = getResourceFactory().createResource("a");
        Property id = getResourceFactory().createProperty("id", "1", Property.Store.YES, Property.Index.NOT_ANALYZED);
        r.addProperty(id);
        r.addProperty(getCompass().getResourceFactory().createProperty("mvalue", "this is a test", Property.Store.YES, Property.Index.ANALYZED));

        session.save(r);

        r = session.getResource("a", "1");
        assertEquals("this is a test", r.getValue("mvalue"));

        session.delete(r);
        r = session.getResource("a", r);
        assertNull(r);

        tr.commit();
        session.close();
    }

    public void testMultipleIdResource() throws Exception {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        Resource r = getResourceFactory().createResource("b");
        Property id1 = getResourceFactory().createProperty("id1", "1", Property.Store.YES, Property.Index.NOT_ANALYZED);
        Property id2 = getResourceFactory().createProperty("id2", "2", Property.Store.YES, Property.Index.NOT_ANALYZED);
        r.addProperty(id1);
        r.addProperty(id2);
        r.addProperty(getResourceFactory().createProperty("mvalue", "this is a test", Property.Store.YES, Property.Index.ANALYZED));

        session.save(r);

        r = session.getResource("b", "1", "2");
        assertEquals("this is a test", r.getValue("mvalue"));

        // TODO need to make this work
        // r = session.getResource("b", id1, id2);
        r = session.getResource("b", (Object) new Property[]{id1, id2});
        assertEquals("this is a test", r.getValue("mvalue"));

        r = session.getResource("b", r);
        assertEquals("this is a test", r.getValue("mvalue"));

        CompassHits hits = session.find("test");
        assertEquals(1, hits.getLength());
        assertEquals("this is a test", hits.resource(0).getValue("mvalue"));

        tr.commit();
        session.close();
    }

    public void testSimplePropertyMapping() throws Exception {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        Resource r = getResourceFactory().createResource("c");
        r.addProperty("id", "1");
        r.addProperty("value1", "this is a test");
        r.addProperty("value2", "this is a test");
        r.addProperty("value3", "this is a test");
        r.addProperty("value4", "this is a test");
        Calendar cal = Calendar.getInstance();
        cal.set(2000, 1, 1, 0, 0, 0);
        r.addProperty("value5", cal.getTime());
        r.addProperty("value6", new StringReader("reader"));

        session.save(r);

        r = session.getResource("c", "1");
        Property prop = r.getProperty("value1");
        assertEquals("this is a test", prop.getStringValue());
        assertTrue(prop.isIndexed());
        assertTrue(prop.isStored());
        assertTrue(prop.isTokenized());
        assertFalse(prop.isCompressed());

        prop = r.getProperty("value2");
        assertEquals("this is a test", prop.getStringValue());
        assertTrue(prop.isIndexed());
        assertTrue(prop.isStored());
        assertTrue(prop.isTokenized());
        assertFalse(prop.isCompressed());

        prop = r.getProperty("value3");
        assertEquals("this is a test", prop.getStringValue());
        assertTrue(prop.isIndexed());
        assertTrue(prop.isStored());
        assertTrue(prop.isTokenized());
        assertTrue(prop.isCompressed());

        prop = r.getProperty("value4");
        assertEquals("this is a test", prop.getStringValue());
        assertTrue(prop.isIndexed());
        assertTrue(prop.isStored());
        assertFalse(prop.isTokenized());
        assertFalse(prop.isCompressed());

        prop = r.getProperty("value5");
        assertEquals("2000-02-01", prop.getStringValue());
        assertEquals(cal.getTime().toString(), prop.getObjectValue().toString());
        assertTrue(prop.isIndexed());
        assertTrue(prop.isStored());
        assertTrue(prop.isTokenized());
        assertFalse(prop.isCompressed());

        CompassHits hits = session.find("value6:reader");
        assertEquals(1, hits.length());

        hits = session.find("value5:2000-02-01");
        assertEquals(1, hits.length());

        hits = session.find("value5:[2000-01-01 TO 2000-02-01]");
        assertEquals(1, hits.length());

        tr.commit();
        session.close();
    }

    public void testResourceExtends() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        Resource r = getResourceFactory().createResource("d");
        r.addProperty("id", "1");
        r.addProperty("value1", "this is a test");
        session.save(r);

        r = session.getResource("d", "1");
        assertEquals("this is a test", r.getValue("value1"));

        tr.commit();
        session.close();
    }

    public void testResourceContractExtends() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        Resource r = getResourceFactory().createResource("e");
        r.addProperty("id", "1");
        r.addProperty("value1", "test1");
        r.addProperty("value2", "test2");
        session.save(r);

        r = session.getResource("e", "1");
        assertNull(r.getValue("value1"));
        assertEquals("test2", r.getValue("value2"));

        tr.commit();
        session.close();
    }

    public void testResourceExcludeFromAll() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        Resource r = getResourceFactory().createResource("f");
        r.addProperty("id1", "1");
        r.addProperty("id2", "2");
        r.addProperty("value1", "test1");
        r.addProperty("value2", "test2");
        session.save(r);

        CompassHits hits = session.find("1");
        assertEquals(0, hits.length());
        hits = session.find("2");
        assertEquals(1, hits.length());
        hits = session.find("test1");
        assertEquals(0, hits.length());
        hits = session.find("test2");
        assertEquals(1, hits.length());

        tr.commit();
        session.close();
    }

    public void testDynamicPropertyWithAll() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        Resource r = getResourceFactory().createResource("a");
        r.addProperty("id", "1");
        r.addProperty(getResourceFactory().createProperty("value1", "test1", Property.Store.YES, Property.Index.ANALYZED));
        r.addProperty(getResourceFactory().createProperty("value2", "test2", Property.Store.YES, Property.Index.NOT_ANALYZED));
        session.save(r);

        CompassHits hits = session.find("test1");
        assertEquals(1, hits.length());
        hits = session.find("test2");
        assertEquals(1, hits.length());

        tr.commit();
        session.close();
    }

    public void testResourcePropertyFormat() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        Resource r = getResourceFactory().createResource("g");
        r.addProperty("id", "1");
        r.addProperty("value", 1);
        session.save(r);

        r = session.loadResource("g", 1);
        assertEquals("000001.00", r.getValue("value"));

        tr.commit();     
        session.close();
    }
}
