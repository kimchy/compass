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

package org.compass.core.test.all.boost;

import org.compass.core.CompassHits;
import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.test.AbstractTestCase;

/**
 * This test verifies support for specific boost fields taken into account when used
 * within the all query.
 *
 * @author kimchy
 */
public class AllBoostTests extends AbstractTestCase {

    protected String[] getMappings() {
        return new String[]{"all/boost/mapping.cpm.xml"};
    }

    public void testValue1BoostLevel2() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        A a = new A();
        a.id = 1;
        a.value1 = "test2";
        a.value2 = "test1";
        session.save("anoboost", a);
        // need to flush to maintain order
        session.flush();
        session.save("a1", a);
        // need to flush to maintain order
        session.flush();

        a = new A();
        a.id = 2;
        a.value1 = "test1";
        a.value2 = "test2";
        session.save("anoboost", a);
        // need to flush to maintain order
        session.flush();
        session.save("a1", a);
        // need to flush to maintain order
        session.flush();

        CompassHits hits = session.queryBuilder().queryString("test1").toQuery().setAliases("anoboost").hits();
        assertEquals(1, ((A) hits.data(0)).id);
        assertEquals(2, ((A) hits.data(1)).id);
        hits = session.queryBuilder().queryString("test1").toQuery().setAliases("a1").hits();
        assertEquals(2, ((A) hits.data(0)).id);
        assertEquals(1, ((A) hits.data(1)).id);

        tr.commit();
        session.close();
    }

    public void testValue1BoostLevel2WithSeveralTokens() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        A a = new A();
        a.id = 1;
        a.value1 = "moo test2";
        a.value2 = "moo test1";
        session.save("anoboost", a);
        // need to flush to maintain order
        session.flush();
        // need to flush to maintain order
        session.flush();
        session.save("a1", a);

        a = new A();
        a.id = 2;
        a.value1 = "moo test1";
        a.value2 = "moo test2";
        session.save("anoboost", a);
        // need to flush to maintain order
        session.flush();
        session.save("a1", a);
        // need to flush to maintain order
        session.flush();

        CompassHits hits = session.queryBuilder().queryString("test1").toQuery().setAliases("anoboost").hits();
        assertEquals(1, ((A) hits.data(0)).id);
        assertEquals(2, ((A) hits.data(1)).id);
        hits = session.queryBuilder().queryString("test1").toQuery().setAliases("a1").hits();
        assertEquals(2, ((A) hits.data(0)).id);
        assertEquals(1, ((A) hits.data(1)).id);

        tr.commit();
        session.close();
    }

    public void testBoostMapping() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        B b = new B();
        b.id = 1;
        b.boost = 1;
        b.value1 = "test2";
        b.value2 = "test1";
        session.save("bnoboost", b);
        // need to flush to maintain order
        session.flush();
        session.save("b1", b);
        // need to flush to maintain order
        session.flush();

        b = new B();
        b.id = 2;
        b.boost = 2;
        b.value1 = "test1";
        b.value2 = "test2";
        session.save("bnoboost", b);
        // need to flush to maintain order
        session.flush();
        session.save("b1", b);
        // need to flush to maintain order
        session.flush();

        CompassHits hits = session.queryBuilder().queryString("test1").toQuery().setAliases("bnoboost").hits();
        assertEquals(1, ((B) hits.data(0)).id);
        assertEquals(2, ((B) hits.data(1)).id);
        hits = session.queryBuilder().queryString("test1").toQuery().setAliases("b1").hits();
        assertEquals(2, ((B) hits.data(0)).id);
        assertEquals(1, ((B) hits.data(1)).id);

        tr.commit();
        session.close();
    }
}
