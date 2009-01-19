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

package org.compass.core.test.component.pathlookup;

import org.compass.core.CompassHits;
import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.test.AbstractTestCase;

/**
 * @author kimchy
 */
public class PathLookupTests extends AbstractTestCase {

    protected String[] getMappings() {
        return new String[]{"component/pathlookup/mapping.cpm.xml"};
    }

    public void testBLookup() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        A a = new A(1, new B("test"));
        session.save("a", a);

        a = new A(2, new B("test"));
        session.save("a1", a);

        CompassHits hits = session.queryBuilder().term("a.b.value", "test").hits();
        assertEquals(1, hits.length());

        hits = session.queryBuilder().term("a1.b.value", "test").hits();
        assertEquals(1, hits.length());

        hits = session.queryBuilder().term("value", "test").hits();
        assertEquals(2, hits.length());

        hits = session.find("a.b.value:test");
        assertEquals(1, hits.length());

        hits = session.find("a1.b.value:test");
        assertEquals(1, hits.length());

        hits = session.find("value:test");
        assertEquals(2, hits.length());

        tr.commit();
        session.close();
    }
}
