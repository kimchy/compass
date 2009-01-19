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

package org.compass.core.test.collection.enumset;

import java.util.EnumSet;

import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.test.AbstractTestCase;

/**
 * @author kimchy
 */
public class EnumSetTests extends AbstractTestCase {

    protected String[] getMappings() {
        return new String[]{"collection/enumset/mapping.cpm.xml"};
    }

    public void testSimpleEnumSet() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        A a = new A();
        a.id = 1;
        a.set = EnumSet.noneOf(Type.class);
        a.set.add(Type.TEST1);
        a.set.add(Type.TEST3);

        session.save(a);

        a = session.load(A.class, 1);
        assertEquals(2, a.set.size());
        assertTrue(a.set.contains(Type.TEST1));
        assertFalse(a.set.contains(Type.TEST2));
        assertTrue(a.set.contains(Type.TEST3));

        tr.commit();
        session.close();
    }
}
