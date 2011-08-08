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

package org.compass.core.test.component.deephierarchy4;

import java.util.ArrayList;
import java.util.Arrays;

import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.test.AbstractTestCase;

/**
 * @author kimchy
 */
public class DeepHierarchy4Tests extends AbstractTestCase {

    protected String[] getMappings() {
        return new String[]{"component/deephierarchy4/mapping.cpm.xml"};
    }

    public void testSimpleValues() throws Exception {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        B b1 = new B("value1");
        b1.id = 2;
        B b2 = new B("value2");
        b2.id = 3;

        A a = new A();
        a.id = 1;
        a.bList = new ArrayList();
        a.bList.add(b1);
        a.bList.add(b1);
        a.bList.add(b2);
        session.save(a);

        a = (A) session.load(A.class, "1");
        assertEquals(3, a.bList.size());
        B b = (B) a.bList.get(0);
        assertEquals(2, b.id);
        assertEquals("value1", b.name);
        b = (B) a.bList.get(1);
        assertEquals(2, b.id);
        assertEquals("value1", b.name);
        b = (B) a.bList.get(2);
        assertEquals(3, b.id);
        assertEquals("value2", b.name);

        tr.commit();
        session.close();
    }
}
