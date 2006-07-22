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

package org.compass.core.test.all;

import org.compass.core.CompassHits;
import org.compass.core.CompassSession;
import org.compass.core.CompassTermInfoVector;
import org.compass.core.CompassTransaction;
import org.compass.core.Resource;
import org.compass.core.config.CompassEnvironment;
import org.compass.core.test.AbstractTestCase;

/**
 * @author kimchy
 */
public class AllTests extends AbstractTestCase {

    protected String[] getMappings() {
        return new String[] { "all/All.cpm.xml" };
    }

    public void testAll() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        Long id = new Long(1);
        A a = new A();
        a.setId(id);
        a.setValue1("test1");
        a.setValue2("test2");
        session.save("a1", a);

        CompassHits result = session.find("test1");
        assertEquals(1, result.getLength());
        a = (A) result.data(0);
        assertEquals("test1", a.getValue1());
        assertEquals("test2", a.getValue2());

        result = session.find("test2");
        assertEquals(1, result.getLength());
        a = (A) result.data(0);
        assertEquals("test1", a.getValue1());
        assertEquals("test2", a.getValue2());

        result = session.find(CompassEnvironment.All.DEFAULT_NAME + ":test2");
        assertEquals(1, result.getLength());
        a = (A) result.data(0);
        assertEquals("test1", a.getValue1());
        assertEquals("test2", a.getValue2());

        tr.commit();
    }

    public void testAllWithDifferentAllMetaData() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        Long id = new Long(1);
        A a = new A();
        a.setId(id);
        a.setValue1("test1");
        a.setValue2("test2");
        session.save("a2", a);

        CompassHits result = session.find("everything:test1");
        assertEquals(1, result.getLength());
        a = (A) result.data(0);
        assertEquals("test1", a.getValue1());
        assertEquals("test2", a.getValue2());

        result = session.find("test2");
        assertEquals(0, result.getLength());

        result = session.find("everything:test2");
        assertEquals(1, result.getLength());
        a = (A) result.data(0);
        assertEquals("test1", a.getValue1());
        assertEquals("test2", a.getValue2());

        result = session.find(CompassEnvironment.All.DEFAULT_NAME + ":test2");
        assertEquals(0, result.getLength());

        tr.commit();
    }

    public void testAllNoAll() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        Long id = new Long(1);
        A a = new A();
        a.setId(id);
        a.setValue1("test1");
        a.setValue2("test2");
        session.save("a3", a);

        CompassHits result = session.find("test2");
        assertEquals(0, result.getLength());

        result = session.find(CompassEnvironment.All.DEFAULT_NAME + ":test2");
        assertEquals(0, result.getLength());

        tr.commit();
    }

    public void testExcludeAll() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        Long id = new Long(1);
        A a = new A();
        a.setId(id);
        a.setValue1("test1111");
        a.setValue2("test2222");
        session.save("a4", a);

        CompassHits result = session.find("test1111");
        assertEquals(0, result.getLength());

        result = session.find("test2222");
        assertEquals(1, result.getLength());

        result = session.find("cValue11");
        assertEquals(0, result.getLength());

        result = session.find("cValue21");
        assertEquals(1, result.getLength());
        
        tr.commit();
        session.close();
    }

    public void testAllWithTermVector() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        Long id = new Long(1);
        A a = new A();
        a.setId(id);
        a.setValue1("test1");
        a.setValue2("test2");
        session.save("a5", a);

        Resource r = session.loadResource("a5", id);
        CompassTermInfoVector termInfoVector = session.getTermInfo(r, "all");
        assertNotNull(termInfoVector);
        try {
            termInfoVector.getTermPositions(0);
            fail();
        } catch (Exception e) {

        }

        try {
            termInfoVector.getOffsets(0);
            fail();
        } catch (Exception e) {

        }

        tr.commit();
        session.close();
    }

    public void testAllWithTermVectorPositions() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        Long id = new Long(1);
        A a = new A();
        a.setId(id);
        a.setValue1("test1");
        a.setValue2("test2");
        session.save("a6", a);

        Resource r = session.loadResource("a6", id);
        CompassTermInfoVector termInfoVector = session.getTermInfo(r, "all");
        assertNotNull(termInfoVector);
        int[] positions = termInfoVector.getTermPositions(0);
        assertNotNull(positions);

        CompassTermInfoVector.OffsetInfo[] offsets = termInfoVector.getOffsets(0);
        assertNull(offsets);

        tr.commit();
        session.close();
    }

    public void testAllWithTermVectorOffsets() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        Long id = new Long(1);
        A a = new A();
        a.setId(id);
        a.setValue1("test1");
        a.setValue2("test2");
        session.save("a7", a);

        Resource r = session.loadResource("a7", id);
        CompassTermInfoVector termInfoVector = session.getTermInfo(r, "all");
        assertNotNull(termInfoVector);
        int[] positions = termInfoVector.getTermPositions(0);
        assertNull(positions);

        CompassTermInfoVector.OffsetInfo[] offsets = termInfoVector.getOffsets(0);
        assertNotNull(offsets);

        tr.commit();
        session.close();
    }

    public void testAllWithTermVectorPositionsAndOffsets() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        Long id = new Long(1);
        A a = new A();
        a.setId(id);
        a.setValue1("test1");
        a.setValue2("test2");
        session.save("a8", a);

        Resource r = session.loadResource("a8", id);
        CompassTermInfoVector termInfoVector = session.getTermInfo(r, "all");
        assertNotNull(termInfoVector);
        int[] positions = termInfoVector.getTermPositions(0);
        assertNotNull(positions);

        CompassTermInfoVector.OffsetInfo[] offsets = termInfoVector.getOffsets(0);
        assertNotNull(offsets);

        tr.commit();
        session.close();
    }
}
