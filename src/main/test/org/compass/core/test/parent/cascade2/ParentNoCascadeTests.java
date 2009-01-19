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

package org.compass.core.test.parent.cascade2;

import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.marshall.MarshallingException;
import org.compass.core.test.AbstractTestCase;

/**
 * @author kimchy
 */
public class ParentNoCascadeTests extends AbstractTestCase {

    protected String[] getMappings() {
        return new String[]{"parent/cascade2/mapping.cpm.xml"};
    }

    public void testNoCascades() {

        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        A a = new A();
        a.id = 1;
        B b = new B();
        b.value = "test";
        a.b = b;
        b.a = a;

        try {
            session.save("b1", b);
            fail("no cascading is defined, should throw an exception");
        } catch (MarshallingException e) {

        }

        tr.commit();
        session.close();
    }
}