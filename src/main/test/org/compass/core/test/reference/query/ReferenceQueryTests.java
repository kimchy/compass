/*
 * Copyright 2004-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.compass.core.test.reference.query;

import org.compass.core.CompassSession;
import org.compass.core.test.AbstractTestCase;

/**
 * @author kimchy
 */
public class ReferenceQueryTests extends AbstractTestCase {

    @Override
    protected String[] getMappings() {
        return new String[]{"reference/query/mapping.cpm.xml"};
    }

    public void testDotPathQuery() {
        CompassSession session = openSession();

        A a = new A();
        a.id = 1;
        a.b = new B();
        a.b.id = 1;
        a.b.value = "bvalue";

        session.save(a.b);
        session.save(a);

        assertEquals(1, session.find("a.b.id:1").length());
        assertEquals(0, session.find("a.b.id:2").length());
        assertEquals(1, session.queryBuilder().spanEq("a.b.id", 1).hits().length());

        session.close();
    }

    public void testDotPathQueryWithNull() {
        CompassSession session = openSession();

        A a1 = new A();
        a1.id = 1;
        a1.b = new B();
        a1.b.id = 1;
        a1.b.value = "bvalue";

        A a2 = new A();
        a2.id = 2;

        session.save(a1.b);
        session.save(a1);
        session.save(a2);

        assertEquals(1, session.find("a.b.id:1").length());
        assertEquals(0, session.find("a.b.id:2").length());
        assertEquals(1, session.queryBuilder().spanEq("a.b.id", 1).hits().length());

        session.close();
    }
}
