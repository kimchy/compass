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

package org.compass.core.test.dynamic.mvel;

import java.util.ArrayList;
import java.util.Calendar;

import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.Resource;
import org.compass.core.test.AbstractTestCase;

/**
 * @author kimchy
 */
public class MVELDynamicTests extends AbstractTestCase {

    protected String[] getMappings() {
        return new String[]{"dynamic/mvel/A.cpm.xml"};
    }

    public void testSimpleExpression() throws Exception {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        A a = new A();
        a.setId(new Long(1));
        a.setValue("value");
        a.setValue2("value2");
        session.save("a1", a);

        Resource resource = session.loadResource("a1", new Long(1));
        assertEquals("valuevalue2", resource.getValue("test"));

        tr.commit();
        session.close();
    }

    public void testExpressionWithFormat() throws Exception {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        A a = new A();
        a.setId(new Long(1));
        Calendar cal = Calendar.getInstance();
        cal.set(1977, 4, 1);
        a.setDate(cal.getTime());
        session.save("a2", a);

        Resource resource = session.loadResource("a2", new Long(1));
        assertEquals("1977", resource.getValue("test"));

        tr.commit();
        session.close();
    }

    public void testArrayRetVal() throws Exception {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        A a = new A();
        a.setId(new Long(1));
        a.setValuesArr(new String[]{"value1", "value2"});
        session.save("a3", a);
        Resource resource = session.loadResource("a3", new Long(1));
        assertEquals(2, resource.getProperties("test").length);
        assertEquals("value1", resource.getProperties("test")[0].getStringValue());
        assertEquals("value2", resource.getProperties("test")[1].getStringValue());

        tr.commit();
        session.close();
    }

    public void testCollectionRetVal() throws Exception {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        A a = new A();
        a.setId(new Long(1));
        ArrayList values = new ArrayList();
        values.add("value1");
        values.add("value2");
        a.setValuesCol(values);
        session.save("a4", a);

        Resource resource = session.loadResource("a4", new Long(1));
        assertEquals(2, resource.getProperties("test").length);
        assertEquals("value1", resource.getProperties("test")[0].getStringValue());
        assertEquals("value2", resource.getProperties("test")[1].getStringValue());

        tr.commit();
        session.close();
    }
}