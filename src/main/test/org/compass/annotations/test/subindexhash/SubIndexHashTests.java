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

package org.compass.annotations.test.subindexhash;

import org.apache.lucene.index.LuceneSubIndexInfo;
import org.compass.annotations.test.AbstractAnnotationsTestCase;
import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.config.CompassConfiguration;

/**
 * @author kimchy
 */
public class SubIndexHashTests extends AbstractAnnotationsTestCase {

    protected void addExtraConf(CompassConfiguration conf) {
        conf.addClass(A.class).addClass(B.class);
    }

    public void testSingleModuloA() throws Exception {
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
        a.id = 1;
        a.value = "value1";
        session.save(a);

        a = new A();
        a.id = 2;
        a.value = "value2";
        session.save(a);

        a = (A) session.load(A.class, 1);
        assertEquals("value1", a.value);
        a = (A) session.load(A.class, 2);
        assertEquals("value2", a.value);

        tr.commit();


        tr = session.beginTransaction();
        a = (A) session.load(A.class, 1);
        assertEquals("value1", a.value);
        a = (A) session.load(A.class, 2);
        assertEquals("value2", a.value);
        tr.commit();

        subIndexInfo = LuceneSubIndexInfo.getIndexInfo("index_0", session);
        assertEquals(1, subIndexInfo.size());
        assertEquals(1, subIndexInfo.info(0).docCount());
        subIndexInfo = LuceneSubIndexInfo.getIndexInfo("index_1", session);
        assertEquals(1, subIndexInfo.size());
        assertEquals(1, subIndexInfo.info(0).docCount());

        session.close();
    }

}
