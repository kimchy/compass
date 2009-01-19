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
import org.compass.core.Resource;
import org.compass.core.test.AbstractTestCase;

/**
 * @author kimchy
 */
public class RsemModuloTests extends AbstractTestCase {

    protected String[] getMappings() {
        return new String[]{"subindexhash/rsem-modulo.cpm.xml"};
    }

    public void testSingleResourceA() throws Exception {
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

        Resource r = getCompass().getResourceFactory().createResource("a");
        r.addProperty("id", new Long(1));
        r.addProperty("value", "value1");
        session.save(r);

        r = getCompass().getResourceFactory().createResource("a");
        r.addProperty("id", new Long(2));
        r.addProperty("value", "value2");
        session.save(r);

        r = session.loadResource("a", new Long(1));
        assertEquals("value1", r.getValue("value"));
        r = session.loadResource("a", new Long(2));
        assertEquals("value2", r.getValue("value"));

        tr.commit();

        tr = session.beginTransaction();
        r = session.loadResource("a", new Long(1));
        assertEquals("value1", r.getValue("value"));
        r = session.loadResource("a", new Long(2));
        assertEquals("value2", r.getValue("value"));
        tr.commit();

        subIndexInfo = LuceneSubIndexInfo.getIndexInfo("index_0", session);
        assertEquals(1, subIndexInfo.size());
        subIndexInfo = LuceneSubIndexInfo.getIndexInfo("index_1", session);
        assertEquals(1, subIndexInfo.size());

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

        Resource r = getCompass().getResourceFactory().createResource("a");
        r.addProperty("id", new Long(1));
        r.addProperty("value", "value1");
        session.save(r);

        r = getCompass().getResourceFactory().createResource("a");
        r.addProperty("id", new Long(2));
        r.addProperty("value", "value2");
        session.save(r);

        r = getCompass().getResourceFactory().createResource("b");
        r.addProperty("id", new Long(1));
        r.addProperty("value", "valueb1");
        session.save(r);

        r = getCompass().getResourceFactory().createResource("b");
        r.addProperty("id", new Long(2));
        r.addProperty("value", "valueb2");
        session.save(r);

        r = session.loadResource("a", new Long(1));
        assertEquals("value1", r.getValue("value"));
        r = session.loadResource("a", new Long(2));
        assertEquals("value2", r.getValue("value"));

        r = session.loadResource("b", new Long(1));
        assertEquals("valueb1", r.getValue("value"));
        r = session.loadResource("b", new Long(2));
        assertEquals("valueb2", r.getValue("value"));

        tr.commit();

        tr = session.beginTransaction();

        r = session.loadResource("a", new Long(1));
        assertEquals("value1", r.getValue("value"));
        r = session.loadResource("a", new Long(2));
        assertEquals("value2", r.getValue("value"));

        r = session.loadResource("b", new Long(1));
        assertEquals("valueb1", r.getValue("value"));
        r = session.loadResource("b", new Long(2));
        assertEquals("valueb2", r.getValue("value"));

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
