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

package org.compass.annotations.test.component.cascade.parent;

import java.util.HashSet;

import org.compass.annotations.test.AbstractAnnotationsTestCase;
import org.compass.core.CompassHits;
import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.config.CompassConfiguration;

/**
 * @author kimchy
 */
public class ComponentParentCascadeTests extends AbstractAnnotationsTestCase {

    protected void addExtraConf(CompassConfiguration conf) {
        conf.addClass(A.class).addClass(B.class);
    }

    public void testPlainCascades() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        A a = new A();
        a.id = 1;

        B b1 = new B();
        b1.value = "b1";
        b1.as = new HashSet<A>();
        b1.as.add(a);

        B b2 = new B();
        b2.value = "b2";
        b2.as = new HashSet<A>();
        b2.as.add(a);

        a.bs = new HashSet<B>();
        a.bs.add(b1);
        a.bs.add(b2);

        // saving b will cause it to cascade and save a
        session.save(b1);
        session.save(b2);

        CompassHits hits = session.find("b1");
        assertEquals(1, hits.length());

        tr.commit();
        session.close();
    }

    public void testNullParentValueCascade() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        B b1 = new B();
        b1.value = "b1";
        session.save(b1);

        tr.commit();
        session.close();
    }
}
