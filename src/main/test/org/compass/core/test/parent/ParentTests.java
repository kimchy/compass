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

package org.compass.core.test.parent;

import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.test.AbstractTestCase;

/**
 * @author kimchy
 */
public class ParentTests extends AbstractTestCase {

    protected String[] getMappings() {
        return new String[]{"parent/mapping.cpm.xml"};
    }

    public void testSimple() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        A1 a = new A1();
        a.id = 1;
        B1 b = new B1();
        b.value = "test";
        a.b = b;
        session.save(a);

        a = (A1) session.load(A1.class, "1");
        assertEquals("test", a.b.value);
        assertEquals(a, a.b.parent);

        tr.commit();
        session.close();
    }

    public void testCollection() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        A2 a = new A2();
        a.id = 1;
        B2 b1 = new B2();
        b1.value = "test1";
        B2 b2 = new B2();
        b2.value = "test2";
        a.b = new B2[] {b1, b2};
        session.save(a);

        a = (A2) session.load(A2.class, "1");
        assertEquals(2, a.b.length);
        assertEquals(a, a.b[0].parent);
        assertEquals(a, a.b[1].parent);

        tr.commit();
        session.close();
    }
}
