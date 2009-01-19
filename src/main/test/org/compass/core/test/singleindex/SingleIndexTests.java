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

package org.compass.core.test.singleindex;

import org.apache.lucene.index.LuceneSubIndexInfo;
import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.test.AbstractTestCase;

public class SingleIndexTests extends AbstractTestCase {

    protected String[] getMappings() {
        return new String[]{"singleindex/singleindex.cpm.xml"};
    }

    public void testValidLoad() throws Exception {
        CompassSession session = openSession();
        CompassTransaction transaction = session.beginTransaction();

        Long id = new Long(1);
        A a = new A();
        a.setId(id);
        a.setValue("value");
        session.save(a);

        B b = new B();
        b.setId(id);
        b.setValue("value");
        session.save(b);

        session.load(B.class, id);
        session.load(A.class, id);

        transaction.commit();

        transaction = session.beginTransaction();
        session.load(B.class, id);
        session.load(A.class, id);
        transaction.commit();

        LuceneSubIndexInfo indexInfo = LuceneSubIndexInfo.getIndexInfo("index", session);
        assertNotNull(indexInfo);
        try {
            indexInfo = LuceneSubIndexInfo.getIndexInfo("a", session);
            if (indexInfo != null) {
                fail("a subindex should not exists");
            }
        } catch (Exception e) {
            // all is well
        }
        try {
            indexInfo = LuceneSubIndexInfo.getIndexInfo("b", session);
            if (indexInfo != null) {
                fail("b subindex should not exists");
            }
        } catch (Exception e) {
            // all is well
        }
        session.close();
    }
}
