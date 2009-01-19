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

package org.compass.core.test.simple;

import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.Resource;
import org.compass.core.test.AbstractTestCase;

/**
 * @author kimchy
 */
public class SimpleTests extends AbstractTestCase {

    protected String[] getMappings() {
        return new String[]{"simple/A.cpm.xml"};
    }

    public void testSimple() throws Exception {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        A a = new A();
        a.setId(1);
        a.setValue("value");
        session.save(a);

        a = session.load(A.class, 1);
        assertEquals("value", a.getValue());

        Resource resource = session.loadResource(A.class, 1);
        assertEquals("value", resource.getValue("value"));
        assertEquals("1", resource.getId());
        assertEquals(1, resource.getIds().length);
        assertEquals("1", resource.getIds()[0]);
        assertEquals(1, resource.getIdProperties().length);
        assertEquals("1", resource.getIdProperties()[0].getStringValue());
        assertEquals("1", resource.getIdProperty().getStringValue());

        tr.commit();
        session.close();

        session = openSession();
        tr = session.beginTransaction();

        a = session.load(A.class, 1);
        assertEquals("value", a.getValue());
        
        tr.commit();
        session.close();
    }

    public void testSimpleDelete() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();
        for (int i = 0; i < 30; i++) {
            A a = new A();
            a.setId(i);
            a.setValue("value");
            session.save(a);
        }
        tr.commit();
        session.close();

        session = openSession();
        tr = session.beginTransaction();
        assertEquals(30, session.queryBuilder().matchAll().hits().length());
        for (int i = 0; i < 30; i = i + 2) {
            session.delete(A.class, new Long(i));
        }
        tr.commit();
        session.close();

        session = openSession();
        tr = session.beginTransaction();
        assertEquals(15, session.queryBuilder().matchAll().hits().length());
        tr.commit();
        session.close();
    }
}
