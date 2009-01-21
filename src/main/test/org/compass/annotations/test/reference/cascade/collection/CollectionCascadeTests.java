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

package org.compass.annotations.test.reference.cascade.collection;

import java.util.HashSet;

import org.compass.annotations.test.AbstractAnnotationsTestCase;
import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.config.CompassConfiguration;

/**
 * @author kimchy
 */
public class CollectionCascadeTests extends AbstractAnnotationsTestCase {

    protected void addExtraConf(CompassConfiguration conf) {
        conf.addClass(A.class).addClass(B.class);
    }

    public void testCascadeAllFromA() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        B b1 = new B(1, "bvalue1");
        B b2 = new B(2, "bvalue2");
        A a = new A(1, "avalue");
        a.b = new HashSet<B>();
        a.b.add(b1);
        a.b.add(b2);
        b1.a = a;
        b2.a = a;
        // this should cause cascading for b as well
        session.create("A", a);

        // just make sure everything was stored
        session.load("A", "1");
        session.load("B", "1");
        session.load("B", "2");

        // make sure everything got deleted
        session.delete("A", 1);
        assertNull(session.get("A", 1));
        assertNull(session.get("B", 1));
        assertNull(session.get("B", 2));

        tr.commit();
        session.close();
    }

    public void testCascadeAllFromB() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        B b1 = new B(1, "bvalue1");
        B b2 = new B(2, "bvalue2");
        A a = new A(1, "avalue");
        a.b = new HashSet<B>();
        a.b.add(b1);
        a.b.add(b2);
        b1.a = a;
        b2.a = a;
        // this should cause cascading for b as well
        session.create("A", a);

        // just make sure everything was stored
        a = session.load(A.class, "1");
        assertEquals(2, a.b.size());
        session.load(B.class, "1");
        session.load(B.class, "2");

        session.delete("B", 1);
        assertNull(session.get(B.class, 1));
        assertNull(session.get(A.class, 1));

        tr.commit();
        session.close();
    }
}