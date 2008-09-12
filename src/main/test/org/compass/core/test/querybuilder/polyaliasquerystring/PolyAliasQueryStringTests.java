/*
 * Copyright 2004-2006 the original author or authors.
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

package org.compass.core.test.querybuilder.polyaliasquerystring;

import org.compass.annotations.test.AbstractAnnotationsTestCase;
import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.config.CompassConfiguration;

/**
 * @author kimchy
 */
public class PolyAliasQueryStringTests extends AbstractAnnotationsTestCase {

    protected void addExtraConf(CompassConfiguration conf) {
        conf.addClass(A.class).addClass(B.class).addClass(C.class);
    }

    public void testPolyAliasQueryStringSimple() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        A a = new A();
        a.id = 1;
        session.save(a);

        B b = new B();
        b.id = 2;
        session.save(b);

        assertEquals(2, session.find("alias:A").length());
        assertEquals(1, session.find("alias:B").length());

        tr.commit();
        session.close();
    }

    public void testSimpleAliasQueryString() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        A a = new A();
        a.id = 1;
        a.value = "test";

        a.component = new C();
        a.component.id = 1;
        a.component.cvalue = "acomponent";

        a.reference = new C();
        a.reference.id = 2;
        a.reference.cvalue = "areference";

        session.save(a);

        B b = new B();
        b.id = 2;
        b.value = "me";

        b.component = new C();
        b.component.id = 3;
        b.component.cvalue = "bcomponent";

        b.reference = new C();
        b.reference.id = 4;
        b.reference.cvalue = "breference";

        session.save(b);

        assertEquals(1, session.find("A.id:1").length());
        assertEquals(0, session.find("A.id:2").length());
        assertEquals(1, session.find("B.id:2").length());

        assertEquals(1, session.find("A.value:test").length());
        assertEquals(1, session.find("B.value:me").length());
        assertEquals(1, session.find("A.value:me").length());

        // check component
        assertEquals(1, session.find("A.component.cvalue:acomponent").length());
        assertEquals(0, session.find("B.component.cvalue:acomponent").length());
        assertEquals(1, session.find("B.component.cvalue:bcomponent").length());
        assertEquals(1, session.find("A.component.cvalue:bcomponent").length());

        // check reference
        assertEquals(1, session.find("A.reference.id:2").length());
        assertEquals(1, session.find("B.reference.id:4").length());
        // TODO MISSING FEATURE TEST: We should make this work
        assertEquals(0, session.find("A.reference.id:4").length());

        tr.commit();
        session.close();
    }
}
