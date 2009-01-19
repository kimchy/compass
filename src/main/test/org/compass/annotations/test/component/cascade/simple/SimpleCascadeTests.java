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

package org.compass.annotations.test.component.cascade.simple;

import org.compass.annotations.test.AbstractAnnotationsTestCase;
import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.config.CompassConfiguration;

/**
 * Simple tests for cascading operation between two root objects (A and B)
 * with component mapping.
 *
 * @author kimchy
 */
public class SimpleCascadeTests extends AbstractAnnotationsTestCase {

    protected void addExtraConf(CompassConfiguration conf) {
        conf.addClass(A.class).addClass(B.class);
    }

    public void testCascadeAll() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        B b = new B(1, "bvalue");
        A a = new A(1, "avalue", b);
        // this should cause cascading for b as well
        session.create("A", a);

        a = (A) session.load("A", "1");
        session.load("B", "1");

        a.b.value = "bupdated";
        session.save("A", a);
        b = (B) session.load("B", "1");
        assertEquals("bupdated", b.value);

        session.delete("A", a);
        assertNull(session.get("A", "1"));
        assertNull(session.get("B", "1"));

        tr.commit();
        session.close();
    }
}
