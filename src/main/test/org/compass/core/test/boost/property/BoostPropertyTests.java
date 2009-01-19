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

package org.compass.core.test.boost.property;

import org.compass.core.CompassHits;
import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.test.AbstractTestCase;

/**
 * @author kimchy
 */
public class BoostPropertyTests extends AbstractTestCase {

    protected String[] getMappings() {
        return new String[] {"boost/property/boost.cpm.xml"};
    }

    /**
     * Verify the order when no boosting is done, so in the next test we are sure that
     * the order is reversed when using boosting
     */
    public void testSimplePropertyNoBoost() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        A a = new A();
        a.id = 1;
        a.value1 = 1f;
        a.value2 = "test";
        session.save(a);
        session.flush();
        a = new A();
        a.id = 2;
        a.value1 = 1f;
        a.value2 = "test";
        session.save(a);
        session.flush();

        CompassHits hits = session.find("test");
        assertEquals(2, hits.length());
        assertEquals(1, ((A) hits.data(0)).id);
        assertEquals(2, ((A) hits.data(1)).id);

        tr.commit();
        session.close();
    }


    public void testSimplePropertyBoost() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        A a = new A();
        a.id = 1;
        a.value1 = 1f;
        a.value2 = "test";
        session.save(a);
        session.flush();
        a = new A();
        a.id = 2;
        a.value1 = 2f;
        a.value2 = "test";
        session.save(a);
        session.flush();

        CompassHits hits = session.find("test");
        assertEquals(2, hits.length());
        // order is revered
        assertEquals(2, ((A) hits.data(0)).id);
        assertEquals(2f, ((A) hits.data(0)).value1);
        assertEquals(1, ((A) hits.data(1)).id);
        assertEquals(1f, ((A) hits.data(1)).value1);

        tr.commit();
        session.close();
    }
}
