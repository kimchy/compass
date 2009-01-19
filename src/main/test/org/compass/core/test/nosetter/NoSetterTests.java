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

package org.compass.core.test.nosetter;

import org.compass.core.CompassHits;
import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.test.AbstractTestCase;

/**
 * @author kimchy
 */
public class NoSetterTests extends AbstractTestCase {

    protected String[] getMappings() {
        return new String[]{"nosetter/mapping.cpm.xml"};
    }

    public void testNoSetterWithPropertyAndComponent() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        A a = new A();
        a.id = 1;
        a.value = "avalue";
        B b = new B();
        b.value = "bvalue";
        a.b = b;
        session.save(a);

        CompassHits hits = session.find("avalue");
        assertEquals(1, hits.length());

        hits = session.find("bvalue");
        assertEquals(1, hits.length());

        tr.commit();
        session.close();

        session = openSession();
        tr = session.beginTransaction();

        a = session.load(A.class, 1);
        assertEquals(1, a.id);
        assertNull(a.value);
        assertNull(a.b);

        tr.commit();
        session.close();
    }
}