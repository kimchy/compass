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

package org.compass.annotations.test.nounmarshall.component;

import java.util.ArrayList;

import org.compass.annotations.test.AbstractAnnotationsTestCase;
import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.Resource;
import org.compass.core.config.CompassConfiguration;

/**
 * @author kimchy
 */
public class ABTests extends AbstractAnnotationsTestCase {


    protected void addExtraConf(CompassConfiguration conf) {
        conf.addClass(A.class).addClass(B.class);
    }

    public void testBSingleValue() throws Exception {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        A a = new A();
        a.id = 1;
        a.value = "value";
        B b = new B();
        b.value = "bvalue";
        a.b = b;
        session.save(a);

        Resource resource = session.loadResource(A.class, 1);
        assertNotNull(resource);
        assertEquals(5, resource.getProperties().length);
        assertEquals("A", resource.getAlias());
        assertEquals(2, resource.getProperties("value").length);

        a = session.load(A.class, 1);
        assertEquals(1, a.id.longValue());
        assertNull(a.value);
        assertNull(a.b);

        tr.commit();
        session.close();
    }

    public void testBwoValues() throws Exception {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        A a = new A();
        a.id = 1;
        a.value = "value";
        B b = new B();
        b.value = "bvalue";
        b.value2 = "bvalue2";
        a.b = b;
        session.save(a);

        Resource resource = session.loadResource(A.class, 1);
        assertNotNull(resource);
        assertEquals(6, resource.getProperties().length);
        assertEquals("A", resource.getAlias());
        assertEquals(3, resource.getProperties("value").length);

        a = (A) session.load(A.class, 1);
        assertEquals(1, a.id.longValue());
        assertNull(a.value);
        assertNull(a.b);

        tr.commit();
        session.close();
    }

    public void testBCollection() throws Exception {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        A a = new A();
        a.id = 1;
        a.value = "value";
        a.bs = new ArrayList<B>();
        B b = new B();
        b.value = "bvalue11";
        b.value2 = "bvalue12";
        a.bs.add(b);
        b = new B();
        b.value = "bvalue21";
        b.value = "bvalue22";
        a.bs.add(b);
        session.save(a);

        Resource resource = session.loadResource(A.class, 1);
        assertNotNull(resource);
        assertEquals(7, resource.getProperties().length);
        assertEquals("A", resource.getAlias());
        assertEquals(4, resource.getProperties("value").length);

        tr.commit();
        session.close();
    }

    public void testBCollectionWithNullValue() throws Exception {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        A a = new A();
        a.id = 1;
        a.value = "value";
        a.bs = new ArrayList<B>();
        B b = new B();
        b.value = null;
        b.value2 = "bvalue12";
        a.bs.add(b);
        b = new B();
        b.value = "bvalue21";
        b.value = "bvalue22";
        a.bs.add(b);
        session.save(a);

        Resource resource = session.loadResource(A.class, 1);
        assertNotNull(resource);
        assertEquals(6, resource.getProperties().length);
        assertEquals("A", resource.getAlias());
        assertEquals(3, resource.getProperties("value").length);

        tr.commit();
        session.close();
    }
}
