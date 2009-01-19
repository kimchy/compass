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

package org.compass.core.test.id;

import org.compass.core.CompassHits;
import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.Resource;
import org.compass.core.mapping.CompassMapping;
import org.compass.core.mapping.ResourcePropertyMapping;
import org.compass.core.mapping.osem.ClassMapping;
import org.compass.core.spi.InternalCompassSession;
import org.compass.core.test.AbstractTestCase;

/**
 * @author kimchy
 */
public class IdTests extends AbstractTestCase {

    protected String[] getMappings() {
        return new String[]{"id/id.cpm.xml"};
    }

    public void testMultiSaveMultiId() {
        CompassSession session = openSession();

        CompassMapping mapping = ((InternalCompassSession) session).getMapping();
        ClassMapping firstMapping = (ClassMapping) mapping.getRootMappingByClass(MultipleId.class);
        ResourcePropertyMapping[] mappings = firstMapping.getResourcePropertyMappings();
        assertEquals(3, mappings.length);

        CompassTransaction tr = session.beginTransaction();
        MultipleId o = new MultipleId();
        o.setId(new Long(1));
        o.setId2("2");
        o.setValue("test");

        session.save(o);
        session.save(o);

        CompassHits results = session.find("mvalue:test");
        assertEquals(1, results.getLength());
        o = (MultipleId) results.data(0);
        assertEquals(new Long(1), o.getId());
        assertEquals("2", o.getId2());
        assertEquals("test", o.getValue());

        tr.commit();
        session.close();
    }

    public void testMultiId() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();
        MultipleId o = new MultipleId();
        o.setId(new Long(1));
        o.setId2("2");
        o.setValue("test");

        MultipleId ref = o;

        session.save(o);

        CompassHits results = session.find("mvalue:test");
        assertEquals(1, results.getLength());
        o = (MultipleId) results.data(0);
        assertEquals(new Long(1), o.getId());
        assertEquals("2", o.getId2());
        assertEquals("test", o.getValue());

        o = session.load(MultipleId.class, 1, "2");
        assertEquals(new Long(1), o.getId());
        assertEquals("2", o.getId2());
        assertEquals("test", o.getValue());

        o = session.load(MultipleId.class, o);
        assertEquals(new Long(1), o.getId());
        assertEquals("2", o.getId2());
        assertEquals("test", o.getValue());

        session.delete(o);
        o = session.get(MultipleId.class, o);
        assertNull(o);

        tr.commit();

        tr = session.beginTransaction();
        o = session.get(MultipleId.class, ref);
        assertNull(o);
        tr.commit();
    }

    public void testMultiIdDelete() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();
        MultipleId o = new MultipleId();
        o.setId(new Long(1));
        o.setId2("2");
        o.setValue("test");
        session.save(o);

        o = session.load(MultipleId.class, 1, "2");
        assertEquals(new Long(1), o.getId());
        assertEquals("2", o.getId2());

        session.delete("multiple-id", 1, "2");
        o = session.get(MultipleId.class, 1, "2");
        assertNull(o);

        tr.commit();
        session.close();
    }

    public void testSingleId() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();
        SingleId o = new SingleId();
        o.setId(new Long(1));
        o.setValue("test");
        session.save(o);

        CompassHits results = session.find("mvalue:test");
        assertEquals(1, results.getLength());
        o = (SingleId) results.data(0);
        assertEquals(new Long(1), o.getId());
        assertEquals("test", o.getValue());

        results = session.find("$/single-id/id:1");
        assertEquals(1, results.getLength());

        o = (SingleId) session.load("single-id", new Long(1));
        assertEquals(new Long(1), o.getId());
        assertEquals("test", o.getValue());

        o = (SingleId) session.load("single-id", o);
        assertEquals(new Long(1), o.getId());
        assertEquals("test", o.getValue());

        SingleId notFound = (SingleId) session.get("single-id", new Long(2));
        assertNull(notFound);

        Resource resource = session.loadResource("single-id", new Long(1));
        assertEquals("test", resource.getProperty("mvalue").getStringValue());

        results = session.find("mvalue:test");
        assertEquals(1, results.getLength());
        resource = results.resource(0);
        assertEquals("test", resource.getProperty("mvalue").getStringValue());

        session.delete(o);
        o = (SingleId) session.get("single-id", o);
        assertNull(o);

        tr.commit();
    }

    public void testSingleIdDelete() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();
        SingleId o = new SingleId();
        o.setId(new Long(1));
        o.setValue("test");
        session.save(o);

        o = (SingleId) session.load("single-id", new Long(1));
        assertEquals(new Long(1), o.getId());

        // must delete with the alias here, since otherwise it won't be able to
        // find the mapping
        session.delete("single-id", new Long(1));
        o = (SingleId) session.get("single-id", new Long(1));
        assertNull(o);

        tr.commit();
        session.close();
    }

    public void testNullValue() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();
        SingleId o = new SingleId();
        o.setId(new Long(1));
        o.setValue(null);
        session.save(o);

        o = (SingleId) session.load("single-id", new Long(1));
        assertEquals(new Long(1), o.getId());
        assertNull(o.getValue());

        tr.commit();
    }

    public void testURIId() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        URIId o = new URIId();
        o.setId("http://test/uri");
        session.save(o);

        o = (URIId) session.load(URIId.class, "http://test/uri");
        assertEquals("http://test/uri", o.getId());

        o.setId("this is a long string");
        session.save(o);
        o = (URIId) session.load(URIId.class, "this is a long string");
        assertEquals("this is a long string", o.getId());

        tr.commit();
    }
}
