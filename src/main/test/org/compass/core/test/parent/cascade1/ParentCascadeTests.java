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

package org.compass.core.test.parent.cascade1;

import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.test.AbstractTestCase;

/**
 * @author kimchy
 */
public class ParentCascadeTests extends AbstractTestCase {

    protected String[] getMappings() {
        return new String[]{"parent/cascade1/mapping.cpm.xml"};
    }

    public void testCascadeAllWithDeleteByAlias() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        A a = new A();
        a.id = 1;
        B b = new B();
        b.value = "test";
        a.b = b;
        b.a = a;

        session.save("b1", b);

        a = (A) session.load("a1", "1");
        assertEquals("test", a.b.value);

        session.delete("b1", b);
        a = (A) session.get("a1", "1");
        assertNull(a);

        tr.commit();
        session.close();
    }
}