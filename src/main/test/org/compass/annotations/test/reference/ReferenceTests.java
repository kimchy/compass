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

package org.compass.annotations.test.reference;

import java.util.ArrayList;

import org.compass.annotations.test.AbstractAnnotationsTestCase;
import org.compass.core.CompassHits;
import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.config.CompassConfiguration;

/**
 * @author kimchy
 */
public class ReferenceTests extends AbstractAnnotationsTestCase {

    protected void addExtraConf(CompassConfiguration conf) {
        conf.addClass(A.class).addClass(B.class);
    }

    public void testSimpleReference() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        A a = new A();
        a.id = 1;
        a.value = "avalue";

        B b = new B();
        b.id = 1;
        b.value = "bvalue";
        a.b = b;

        B b1 = new B();
        b1.id = 2;
        b1.value = "bvalue1";

        B b2 = new B();
        b2.id = 3;
        b2.value = "bvalue2";
        ArrayList<B> bValues = new ArrayList<B>();
        bValues.add(b1);
        bValues.add(b2);
        a.bValues = bValues;

        session.save(b);
        session.save(b1);
        session.save(b2);
        session.save(a);

        a = (A) session.load(A.class, 1);
        assertEquals("avalue", a.value);
        assertEquals("bvalue", a.b.value);
        assertEquals("bvalue1", a.bValues.get(0).value);
        assertEquals("bvalue2", a.bValues.get(1).value);
        b = (B) session.load(B.class, 1);
        assertEquals("bvalue", b.value);

        CompassHits hits = session.find("bvalue");
        assertEquals(1, hits.length());
        b = (B) hits.data(0);
        assertEquals("bvalue", b.value);

        hits = session.find("avalue");
        assertEquals(1, hits.length());
        a = (A) hits.data(0);
        assertEquals("avalue", a.value);
        assertEquals("bvalue", a.b.value);

        tr.commit();
        session.close();
    }

}
