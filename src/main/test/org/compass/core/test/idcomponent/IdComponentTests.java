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

package org.compass.core.test.idcomponent;

import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.test.AbstractTestCase;

/**
 * @author kimchy
 */
public class IdComponentTests extends AbstractTestCase {

    protected String[] getMappings() {
        return new String[]{"idcomponent/mapping.cpm.xml"};
    }

    public void testSimpleIdComponent() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        A a = new A();
        a.b = new B(1, 2);
        a.value = "value1";
        session.save(a);

        a = new A();
        a.b = new B(1, 2);
        a = session.load(A.class, a);
        assertNotNull(a);

        a = session.load(A.class, a.b);
        assertNotNull(a);

        tr.commit();
        session.close();
    }

    public void testIdComponentReferenceMapping() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        A a = new A();
        a.b = new B(1, 2);
        a.value = "value1";
        session.save(a);
        C c = new C();
        c.id = 1;
        c.a = a;
        session.save(c);

        c = session.load(C.class, "1");
        assertNotNull(c);
        assertNotNull(c.a);

        tr.commit();
        session.close();
    }

    public void testIdComponentReferenceMappingWithNullValue() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        C c = new C();
        c.id = 1;
        c.a = null;
        session.save(c);

        c = session.load(C.class, "1");
        assertNotNull(c);
        assertNull(c.a);

        tr.commit();
        session.close();
    }
}
