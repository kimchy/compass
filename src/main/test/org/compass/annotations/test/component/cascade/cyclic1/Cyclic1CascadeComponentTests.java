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

package org.compass.annotations.test.component.cascade.cyclic1;

import java.util.ArrayList;

import org.compass.annotations.test.AbstractAnnotationsTestCase;
import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.config.CompassConfiguration;

/**
 * @author kimchy
 */
public class Cyclic1CascadeComponentTests extends AbstractAnnotationsTestCase {

    protected void addExtraConf(CompassConfiguration conf) {
        conf.addClass(A.class).addClass(B.class);
    }

    public void testCyclicCascadeCompoent() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        A a = new A();
        a.id = 1;
        a.bs = new ArrayList<B>();
        B b = new B();
        b.id = 2;
        b.a = a;
        a.bs.add(b);
        b = new B();
        b.id = 3;
        b.a = a;
        a.bs.add(b);

        session.create(a);

        assertNotNull(session.get(A.class, 1));
        assertEquals(a.bs.size(), 2);
        assertNotNull(session.get(B.class, 2));
        assertNotNull(session.get(B.class, 3));

        session.save(a);

        assertNotNull(session.get(A.class, 1));
        assertEquals(a.bs.size(), 2);
        assertNotNull(session.get(B.class, 2));
        assertNotNull(session.get(B.class, 3));

        session.delete(a);

        assertNull(session.get(A.class, 1));
        assertNull(session.get(B.class, 2));
        assertNull(session.get(B.class, 3));

        tr.commit();
        session.close();
    }

    public void testCyclicCascadeCompoentWithDeleteById() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        A a = new A();
        a.id = 1;
        a.bs = new ArrayList<B>();
        B b = new B();
        b.id = 1;
        b.a = a;
        a.bs.add(b);
        b = new B();
        b.id = 2;
        b.a = a;
        a.bs.add(b);

        session.create(a);

        assertNotNull(session.get(A.class, 1));
        assertEquals(a.bs.size(), 2);
        assertNotNull(session.get(B.class, 1));
        assertNotNull(session.get(B.class, 2));

        session.delete(A.class, 1);

        assertNull(session.get(A.class, 1));
        assertNull(session.get(B.class, 1));
        assertNull(session.get(B.class, 2));

        tr.commit();
        session.close();
    }
}
