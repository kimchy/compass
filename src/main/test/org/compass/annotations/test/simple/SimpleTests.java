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

package org.compass.annotations.test.simple;

import org.compass.annotations.test.AbstractAnnotationsTestCase;
import org.compass.core.CompassHits;
import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.config.CompassConfiguration;

/**
 * @author kimchy
 */
public class SimpleTests extends AbstractAnnotationsTestCase {

    protected void addExtraConf(CompassConfiguration conf) {
        conf.addClass(A.class);
        conf.addClass(B.class);
        conf.addClass(CImpl.class);
    }

    public void testSimpleAnnotations() throws Exception {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        A a = new A();
        a.setId(1);
        a.setValue("value");
        session.save(a);

        a = (A) session.load(A.class, 1);
        assertEquals("value", a.getValue());

        CompassHits hits = session.find("value");
        assertEquals(1, hits.length());

        tr.commit();
        session.close();
    }

    public void testJavaExtends() throws Exception {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        B b = new B();
        b.setId(1);
        b.setValue("value");
        b.setValue1("value1");
        session.save(b);

        b = (B) session.load(B.class, 1);
        assertEquals("value", b.getValue());
        assertEquals("value1", b.getValue1());

        CompassHits hits = session.find("value");
        assertEquals(1, hits.length());
        hits = session.find("value1");
        assertEquals(1, hits.length());

        tr.commit();
        session.close();
    }

    public void testJavaInterface() throws Exception {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        CImpl cImpl = new CImpl();
        cImpl.setId(1);
        cImpl.setValue("value");
        session.save(cImpl);

        CInterface cInteface = (CInterface) session.load(CImpl.class, 1);
        assertEquals("value", cInteface.getValue());

        CompassHits hits = session.find("value");
        assertEquals(1, hits.length());

        tr.commit();
        session.close();
    }
}
