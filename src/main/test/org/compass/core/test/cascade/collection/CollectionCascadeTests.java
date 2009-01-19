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

package org.compass.core.test.cascade.collection;

import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.test.AbstractTestCase;

/**
 * @author kimchy
 */
public class CollectionCascadeTests extends AbstractTestCase {

    protected String[] getMappings() {
        return new String[]{"cascade/collection/mapping.cpm.xml"};
    }

    public void testSimpleCascade() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        A a = new A();
        a.id = 1;
        a.value = "avalue";

        B b1 = new B();
        b1.id = 1;
        b1.value = "bvalue1";

        B b2 = new B();
        b2.id = 2;
        b2.value = "bvalue2";

        a.b = new B[] {b1, b2};

        // saving A, which will cause B to be saved as well
        session.save(a);

        assertEquals(1, session.find("bvalue1").length());
        assertEquals(1, session.find("bvalue2").length());
        assertEquals(1, session.find("avalue").length());

        tr.commit();
        session.close();
    }
}