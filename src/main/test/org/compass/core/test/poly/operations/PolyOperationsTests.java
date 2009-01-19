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

package org.compass.core.test.poly.operations;

import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.test.AbstractTestCase;

/**
 * @author kimchy
 */
public class PolyOperationsTests extends AbstractTestCase {

    protected String[] getMappings() {
        return new String[]{"poly/operations/mapping.cpm.xml"};
    }

    public void testPolyGetOperation() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        A a = new A();
        a.id = 1;
        session.save(a);

        B b = new B();
        b.id = 2;
        session.save(b);

        a = session.get(A.class, 1);
        assertNotNull(a);
        assertFalse(a instanceof B);

        a = session.get(B.class, 1);
        assertNull(a);

        a = session.get(B.class, 2);
        assertNotNull(a);
        assertTrue(a instanceof B);

        a = session.get(A.class, 2);
        assertNotNull(a);
        assertTrue(a instanceof B);

        tr.commit();
        session.close();
    }

    public void testPolyDeleteOperation() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        A a = new A();
        a.id = 1;
        session.save(a);

        B b = new B();
        b.id = 2;
        session.save(b);

        a = session.get(A.class, 1);
        assertNotNull(a);
        session.delete(B.class, 1);
        a = session.get(A.class, 1);
        assertNotNull(a);

        a = session.get(B.class, 2);
        assertNotNull(a);
        session.delete(A.class, 2);
        a = session.get(B.class, 2);
        assertNull(a);

        tr.commit();
        session.close();
    }
}