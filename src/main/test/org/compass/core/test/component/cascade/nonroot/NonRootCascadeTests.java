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

package org.compass.core.test.component.cascade.nonroot;

import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.test.AbstractTestCase;

/**
 * @author kimchy
 */
public class NonRootCascadeTests extends AbstractTestCase {

    protected String[] getMappings() {
        return new String[]{"component/cascade/nonroot/mapping.cpm.xml"};
    }

    public void testCascadeNonRootComponent() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        A a = new A(1, "avalue");
        B b = new B(1, "bvalue", a);
        session.create("b", b);
        assertNotNull(session.get("a", "1"));

        session.delete("b", b);
        assertNull(session.get("a", "1"));

        tr.commit();
        session.close();
    }
}
