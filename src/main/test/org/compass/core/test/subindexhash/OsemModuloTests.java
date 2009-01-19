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

package org.compass.core.test.subindexhash;

import org.apache.lucene.index.LuceneSubIndexInfo;
import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.test.AbstractTestCase;

/**
 * @author kimchy
 */
public class OsemModuloTests extends AbstractTestCase {

    protected String[] getMappings() {
        return new String[]{"subindexhash/osem-modulo.cpm.xml"};
    }

    public void testSingleObjectA() throws Exception {
        CompassSession session = openSession();

        LuceneSubIndexInfo subIndexInfo = LuceneSubIndexInfo.getIndexInfo("index_0", session);
        assertEquals(0, subIndexInfo.size());
        subIndexInfo = LuceneSubIndexInfo.getIndexInfo("index_1", session);
        assertEquals(0, subIndexInfo.size());
        try {
            subIndexInfo = LuceneSubIndexInfo.getIndexInfo("index_2", session);
            if (subIndexInfo != null) {
                fail();
            }
        } catch (Exception e) {
            // all is well
        }
        try {
            subIndexInfo = LuceneSubIndexInfo.getIndexInfo("a", session);
            if (subIndexInfo != null) {
                fail();
            }
        } catch (Exception e) {
            // all is well
        }


        CompassTransaction tr = session.beginTransaction();

        Long id = new Long(1);
        A a = new A();
        a.setId(id);
        a.setValue("value1");
        session.save(a);

        id = new Long(2);
        a = new A();
        a.setId(id);
        a.setValue("value2");
        session.save(a);

        a = (A) session.load("a", new Long(1));
        assertEquals("value1", a.getValue());
        a = (A) session.load("a", new Long(2));
        assertEquals("value2", a.getValue());

        tr.commit();

        tr = session.beginTransaction();
        a = (A) session.load("a", new Long(1));
        assertEquals("value1", a.getValue());
        a = (A) session.load("a", new Long(2));
        assertEquals("value2", a.getValue());
        tr.commit();

        subIndexInfo = LuceneSubIndexInfo.getIndexInfo("index_0", session);
        assertEquals(1, subIndexInfo.size());
        assertEquals(1, subIndexInfo.info(0).docCount());
        subIndexInfo = LuceneSubIndexInfo.getIndexInfo("index_1", session);
        assertEquals(1, subIndexInfo.size());
        assertEquals(1, subIndexInfo.info(0).docCount());

        session.close();
    }

    public void testMultiObjectsToSameModulo() throws Exception {
        CompassSession session = openSession();

        LuceneSubIndexInfo subIndexInfo = LuceneSubIndexInfo.getIndexInfo("index_0", session);
        assertEquals(0, subIndexInfo.size());
        subIndexInfo = LuceneSubIndexInfo.getIndexInfo("index_1", session);
        assertEquals(0, subIndexInfo.size());
        try {
            subIndexInfo = LuceneSubIndexInfo.getIndexInfo("index_2", session);
            if (subIndexInfo != null) {
                fail();
            }
        } catch (Exception e) {
            // all is well
        }
        try {
            subIndexInfo = LuceneSubIndexInfo.getIndexInfo("a", session);
            if (subIndexInfo != null) {
                fail();
            }
        } catch (Exception e) {
            // all is well
        }


        CompassTransaction tr = session.beginTransaction();

        A a = new A();
        a.setId(new Long(1));
        a.setValue("value1");
        session.save(a);

        a = new A();
        a.setId(new Long(2));
        a.setValue("value2");
        session.save(a);

        B b = new B();
        b.setId(new Long(1));
        b.setValue("valueb1");
        session.save("b", b);

        b = new B();
        b.setId(new Long(2));
        b.setValue("valueb2");
        session.save("b", b);


        a = (A) session.load("a", new Long(1));
        assertEquals("value1", a.getValue());
        a = (A) session.load("a", new Long(2));
        assertEquals("value2", a.getValue());

        b = (B) session.load("b", new Long(1));
        assertEquals("valueb1", b.getValue());
        b = (B) session.load("b", new Long(2));
        assertEquals("valueb2", b.getValue());

        tr.commit();

        tr = session.beginTransaction();

        a = (A) session.load("a", new Long(1));
        assertEquals("value1", a.getValue());
        a = (A) session.load("a", new Long(2));
        assertEquals("value2", a.getValue());

        b = (B) session.load("b", new Long(1));
        assertEquals("valueb1", b.getValue());
        b = (B) session.load("b", new Long(2));
        assertEquals("valueb2", b.getValue());

        tr.commit();

        subIndexInfo = LuceneSubIndexInfo.getIndexInfo("index_0", session);
        assertEquals(1, subIndexInfo.size());
        assertEquals(2, subIndexInfo.info(0).docCount());
        subIndexInfo = LuceneSubIndexInfo.getIndexInfo("index_1", session);
        assertEquals(1, subIndexInfo.size());
        assertEquals(2, subIndexInfo.info(0).docCount());

        session.close();
    }
}
