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

package org.compass.core.test.boost;

import org.compass.core.CompassHits;
import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.test.AbstractTestCase;

/**
 * @author kimchy
 */
public class BoostTests extends AbstractTestCase {

    protected String[] getMappings() {
        return new String[]{"boost/boost.cpm.xml"};
    }

    public void testNoBoostOrderAndScore() throws Exception {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        // first save under a1, where no boosting is done
        // check the order for the search so we can then use the boost
        // test to check that the order was reversed
        A a = new A();
        a.id = 1;
        a.value1 = "match";
        a.value2 = "nomatch";
        session.save("a1", a);

        // flush to maintain order
        session.flush();

        a = new A();
        a.id = 2;
        a.value1 = "nomatch";
        a.value2 = "match";
        session.save("a1", a);

        // flush to maintain order
        session.flush();
        
        for (int i = 0; i < 10; i++) {
            CompassHits hits = session.find("value1:match OR value2:match");
            assertEquals(2, hits.length());
            assertTrue(hits.score(0) == hits.score(1));
            assertEquals(1, ((A) hits.data(0)).id);
            assertEquals(2, ((A) hits.data(1)).id);
        }

        // check the order when we use the all proeprty
        CompassHits hits = session.find("match");
        assertEquals(2, hits.length());
        assertTrue(hits.score(0) == hits.score(1));
        assertEquals(1, ((A) hits.data(0)).id);
        assertEquals(2, ((A) hits.data(1)).id);

        tr.commit();
        session.close();
    }

    public void testWithBoostOrderAndScore() throws Exception {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        // since we checked before the correct order, now we save
        // under a2, that boosts value2, which means that a match
        // on it will result in the hits having higher score
        A a = new A();
        a.id = 1;
        a.value1 = "match";
        a.value2 = "nomatch";
        session.save("a2", a);

        // flush to maintain order
        session.flush();

        a = new A();
        a.id = 2;
        a.value1 = "nomatch";
        a.value2 = "match";
        session.save("a2", a);

        // flush to maintain order
        session.flush();
        
        for (int i = 0; i < 10; i++) {
            CompassHits hits = session.find("value1:match OR value2:match");
            assertEquals(2, hits.length());
            assertTrue(hits.score(0) > hits.score(1));
            assertEquals(2, ((A) hits.data(0)).id);
            assertEquals(1, ((A) hits.data(1)).id);
        }

        // check the order when we use the all proeprty
        // note, we now support order in the all property as well
        CompassHits hits = session.find("match");
        assertEquals(2, hits.length());
        assertEquals(2, ((A) hits.data(0)).id);
        assertEquals(1, ((A) hits.data(1)).id);

        tr.commit();
        session.close();
    }

    public void testClassLevelBoost() throws Exception {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        // we save exact same A values, one under a3, and one
        // under a4, where a4 has a higher boost level
        A a = new A();
        a.id = 1;
        a.value1 = "match";
        a.value2 = "nomatch";
        session.save("a3", a);

        // flush to maintain order
        session.flush();

        a = new A();
        a.id = 1;
        a.value1 = "match";
        a.value2 = "nomatch";
        session.save("a4", a);

        // flush to maintain order
        session.flush();
        
        for (int i = 0; i < 10; i++) {
            CompassHits hits = session.find("value1:match");
            assertEquals(2, hits.length());
            assertTrue(hits.score(0) > hits.score(1));
            assertEquals("a4", hits.resource(0).getAlias());
            assertEquals("a3", hits.resource(1).getAlias());
        }

        tr.commit();
        session.close();
    }

    public void testComponentBoostDoesNotPropogateToParent() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        Parent parent = new Parent();
        parent.id = 1;
        parent.value = "match";
        session.save("parent11", parent);

        // flush to maintain order
        session.flush();

        parent = new Parent();
        parent.id = 2;
        parent.value = "match";
        parent.child = new Child();
        parent.child.value = "nomatch";
        session.save("parent12", parent);

        // flush to maintain order
        session.flush();
        
        // if the component boost level propogated from child to parent 12
        // than it will score higher when searching for match
        CompassHits hits = session.find("value:match");
        assertEquals(2, hits.length());
        assertTrue(hits.score(0) > hits.score(1));
        assertEquals("parent11", hits.resource(0).getAlias());
        assertEquals("parent12", hits.resource(1).getAlias());

        tr.commit();
        session.close();
    }
}
