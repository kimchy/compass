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

package org.compass.core.test.find.aliases1;

import org.compass.core.CompassHits;
import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.test.AbstractTestCase;

/**
 * @author kimchy
 */
public class AliasesFindTests extends AbstractTestCase {

    protected String[] getMappings() {
        return new String[]{"find/aliases1/mapping.cpm.xml"};
    }

    public void testSubIndexAliasNarrow() {
        addDataA(0, 10);
        addDataB(0, 10);

        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();
        CompassHits hits = session.find("alias:a1 OR alias:b1");
        assertEquals(20, hits.getLength());

        hits = session.queryBuilder().queryString("alias:a1 OR alias:b1").toQuery()
                .setAliases(new String[]{"a1"}).hits();
        assertEquals(10, hits.getLength());

        hits = session.queryBuilder().queryString("alias:a1 OR alias:b1").toQuery()
                .setSubIndexes(new String[]{"ab"}).hits();
        assertEquals(20, hits.getLength());

        hits = session.queryBuilder().queryString("alias:a1 OR alias:b1").toQuery()
                .setSubIndexes(new String[]{"ab"})
                .setAliases(new String[]{"a1"}).hits();
        assertEquals(10, hits.getLength());

        tr.commit();
        session.close();
    }

    private void addDataA(int from, int to) {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();
        for (int i = from; i < to; i++) {
            A a = new A();
            a.setId(new Long(i));
            a.setValue("value");
            session.save(a);
        }
        tr.commit();
        session.close();
    }

    private void addDataB(int from, int to) {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();
        for (int i = from; i < to; i++) {
            B b = new B();
            b.setId(new Long(i));
            b.setValue("value");
            session.save(b);
        }
        tr.commit();
        session.close();
    }

}