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

package org.compass.core.test.querybuilder.operator;

import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.test.AbstractTestCase;

/**
 * @author kimchy
 */
public class QueryOperatorTests extends AbstractTestCase {

    protected String[] getMappings() {
        return new String[]{"querybuilder/operator/mapping.cpm.xml"};
    }

    public void testDefaultOperator() {
        setUpData();

        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        assertEquals(1, session.queryBuilder().queryString("one two").toQuery().hits().length());
        assertEquals(0, session.queryBuilder().queryString("one moo").toQuery().hits().length());

        tr.commit();
        session.close();
    }

    public void testOrOperator() {
        setUpData();

        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        assertEquals(1, session.queryBuilder().queryString("one two").useOrDefaultOperator().toQuery().hits().length());
        assertEquals(2, session.queryBuilder().queryString("one moo").useOrDefaultOperator().toQuery().hits().length());

        tr.commit();
        session.close();
    }

    public void testAndOperator() {
        setUpData();

        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        assertEquals(1, session.queryBuilder().queryString("one two").useAndDefaultOperator().toQuery().hits().length());
        assertEquals(0, session.queryBuilder().queryString("one moo").useAndDefaultOperator().toQuery().hits().length());

        tr.commit();
        session.close();
    }

    private void setUpData() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        A a = new A();
        a.id = 1;
        a.value1 = "one";
        a.value2 = "two";
        session.save("a", a);

        a.id = 2;
        a.value1 = "moo";
        a.value2 = "poo";
        session.save("a", a);

        tr.commit();
        session.close();
    }
}