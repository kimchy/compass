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

package org.compass.core.test.formatter.sort;

import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.test.AbstractTestCase;

/**
 * @author kimchy
 */
public class FormatterSortableTests extends AbstractTestCase {

    @Override
    protected String[] getMappings() {
        return new String[]{"formatter/sort/mapping.cpm.xml"};
    }

    public void testSortableFormatters() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        A a = new A();
        a.id = 1;
        a.intVal = 12;
        a.shortVal = (short) 12;
        a.longVal = 12;
        a.doubleVal = 12.56789;
        a.floatVal = 12.56789f;

        session.save("sort", a);

        a = (A) session.load("sort", 1);
        assertEquals(12, a.intVal);
        assertEquals((short) 12, a.shortVal);
        assertEquals(12, a.longVal);
        assertEquals(12.56789, a.doubleVal, 0.000001);
        assertEquals(12.56789f, a.floatVal, 0.000001);

        tr.commit();
        session.close();
    }
}
