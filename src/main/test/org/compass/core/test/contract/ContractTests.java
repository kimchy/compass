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

package org.compass.core.test.contract;

import org.compass.core.CompassHits;
import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.Resource;
import org.compass.core.test.AbstractTestCase;

/**
 * 
 * @author kimchy
 * 
 */
public class ContractTests extends AbstractTestCase {

    protected String[] getMappings() {
        return new String[] { "contract/Contract.cpm.xml" };
    }

    public void testContract() throws Exception {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        Long id = new Long(1);
        A a = new A();
        a.setId(id);
        a.setValue1("value1");
        a.setValue2("value2");
        a.setValueA("valueA");

        session.save("a", a);

        Resource r = session.loadResource("a", id);
        assertNotNull(r.getValue("mvalue1"));
        assertNotNull(r.getValue("mvalue2"));
        assertNotNull(r.getValue("mvalueA"));

        tr.commit();
        session.close();
    }

    public void testContractQuery() throws Exception {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        Long id = new Long(1);
        A a = new A();
        a.setId(id);
        a.setValue1("value1");
        a.setValue2("value2");
        a.setValueA("valueA");

        session.save("a", a);

        CompassHits hits = session.find("contract2.value2:value2");
        assertEquals(1, hits.length());

        tr.commit();
        session.close();
    }

    public void testContractOverrideTrue() throws Exception {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        Long id = new Long(1);
        A a = new A();
        a.setId(id);
        a.setValue1("value1");
        a.setValue2("value2");
        a.setValueA("valueA");

        session.save("a1", a);

        Resource r = session.loadResource("a1", id);
        assertNull(r.getValue("mvalue1"));
        assertNotNull(r.getValue("mvalue2"));
        assertNotNull(r.getValue("mvalueEx"));

        tr.commit();
        session.close();
    }

    public void testContractOverrideFalse() throws Exception {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        Long id = new Long(1);
        A a = new A();
        a.setId(id);
        a.setValue1("value1");
        a.setValue2("value2");
        a.setValueA("valueA");

        session.save("a2", a);

        Resource r = session.loadResource("a2", id);
        assertNotNull(r.getValue("mvalue1"));
        assertNotNull(r.getValue("mvalue2"));
        assertNotNull(r.getValue("mvalueEx"));

        tr.commit();
        session.close();
    }
}
