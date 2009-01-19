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

package org.compass.core.test.reference.deeprel;

import java.util.Iterator;

import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.test.AbstractTestCase;

/**
 * @author kimchy
 */
public class DeepRelationshipTests extends AbstractTestCase {

    protected String[] getMappings() {
        return new String[]{"reference/deeprel/mapping.cpm.xml"};
    }

    public void testCorrectUnmarshalling() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        A a = new A();
        a.id = 1;
        a.value = "a";


        B b1 = new B();
        b1.id = 1;
        b1.value = "b1";
        b1.a = a;
        B b2 = new B();
        b2.id = 2;
        b2.value = "b2";
        b2.a = a;

        a.bs.add(b1);
        a.bs.add(b2);

        C c11 = new C();
        c11.id = 1;
        c11.value = "c11";
        c11.bs.add(b1);
        C c12 = new C();
        c12.id = 2;
        c12.value = "c12";
        c12.bs.add(b1);

        b1.cs.add(c11);
        b1.cs.add(c12);

        C c21 = new C();
        c21.id = 2;
        c21.value = "c21";
        c21.bs.add(b2);

        b2.cs.add(c21);

        session.save(a);
        session.save(b1);
        session.save(b2);
        session.save(c11);
        session.save(c12);
        session.save(c21);

        // check unmarshalling based on a
        a = (A) session.load(A.class, "1");
        assertEquals(2, a.bs.size());
        Iterator it = a.bs.iterator();
        // since it is a set, the order might change
        b1 = (B) it.next();
        assertEquals(b1.a.id, 1);
        if (b1.id == 1) {
            assertEquals(2, b1.cs.size());
            assertEquals(1, ((C) b1.cs.iterator().next()).bs.size());
        } else {
            assertEquals(1, b1.cs.size());
            assertEquals(1, ((C) b1.cs.iterator().next()).bs.size());
        }
        b2 = (B) it.next();
        assertEquals(b2.a.id, 1);
        if (b2.id == 1) {
            assertEquals(2, b2.cs.size());
            assertEquals(1, ((C) b2.cs.iterator().next()).bs.size());
        } else {
            assertEquals(1, b2.cs.size());
            assertEquals(1, ((C) b2.cs.iterator().next()).bs.size());
        }


        tr.commit();
        session.close();
    }
}
