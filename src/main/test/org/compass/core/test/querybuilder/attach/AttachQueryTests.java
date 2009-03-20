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

package org.compass.core.test.querybuilder.attach;

import org.compass.core.CompassException;
import org.compass.core.CompassQuery;
import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.test.AbstractTestCase;

/**
 * @author kimchy
 */
public class AttachQueryTests extends AbstractTestCase {

    protected String[] getMappings() {
        return new String[]{"querybuilder/attach/mapping.cpm.xml"};
    }

    public void testSimpleAttach() {
        CompassQuery query1 = getCompass().queryBuilder().queryString("test").toQuery();
        CompassQuery query2 = getCompass().queryBuilder().queryString("notests").toQuery();

        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        A a = new A();
        a.id = 1;
        a.value = "test";
        session.save("a", a);

        assertEquals(1, query1.attach(session).hits().length());
        assertEquals(0, query2.attach(session).hits().length());

        tr.commit();
        session.close();

        session = openSession();
        tr = session.beginTransaction();

        query1.attach(session);

        assertEquals(1, query1.attach(session).count());
        assertEquals(0, query2.attach(session).count());

        tr.commit();
        session.close();
    }

    public void testTransactionBoundAttach() {
        CompassQuery query1 = getCompass().queryBuilder().queryString("test").toQuery();
        CompassQuery query2 = getCompass().queryBuilder().queryString("notests").toQuery();

        CompassSession session = openSession();

        A a = new A();
        a.id = 1;
        a.value = "test";
        session.save("a", a);

        assertEquals(1, query1.hits().length());
        assertEquals(0, query2.hits().length());

        session.close();

        session = openSession();
        session.beginTransaction();

        assertEquals(1, query1.count());
        assertEquals(0, query2.count());

        session.close();
    }

    public void testNoAttachException() {
        CompassQuery query1 = getCompass().queryBuilder().queryString("test").toQuery();

        try {
            query1.count();
            fail();
        } catch (CompassException e) {
            // all is well
        }
    }
}