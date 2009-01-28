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

package org.compass.core.test.component.cyclic1;

import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.test.AbstractTestCase;

/**
 * @author kimchy
 */
public class SelfCyclicTests extends AbstractTestCase {


    protected String[] getMappings() {
        return new String[]{"component/cyclic1/SelfCycle.cpm.xml"};
    }

    public void testSelfCyclic() throws Exception {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        SelfCycle selfCycle1 = new SelfCycle();
        selfCycle1.setId(new Long(1));
        selfCycle1.setValue("value1");

        SelfCycle selfCycle2 = new SelfCycle();
        selfCycle2.setValue("value2");

        SelfCycle selfCycle3 = new SelfCycle();
        selfCycle3.setValue("value3");

        SelfCycle selfCycle4 = new SelfCycle();
        selfCycle4.setValue("value4");

        SelfCycle selfCycle5 = new SelfCycle();
        selfCycle5.setValue("value5");

        selfCycle1.setSelfCycle(selfCycle2);
        selfCycle2.setSelfCycle(selfCycle3);
        selfCycle3.setSelfCycle(selfCycle4);
        selfCycle4.setSelfCycle(selfCycle5);

        session.save(selfCycle1);

        selfCycle1 = (SelfCycle) session.load(SelfCycle.class, new Long(1));
        assertEquals("value1", selfCycle1.getValue());
        assertEquals("value2", selfCycle1.getSelfCycle().getValue());
        assertEquals("value3", selfCycle1.getSelfCycle().getSelfCycle().getValue());
        assertEquals("value4", selfCycle1.getSelfCycle().getSelfCycle().getSelfCycle().getValue());
        // value 5 we will not find since max depth is set to 3
        assertNull(selfCycle1.getSelfCycle().getSelfCycle().getSelfCycle().getSelfCycle());

        tr.commit();
        session.close();
    }

    public void testSameSelfCyclic() throws Exception {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        SelfCycle selfCycle1 = new SelfCycle();
        selfCycle1.setId(new Long(1));
        selfCycle1.setValue("value1");

        SelfCycle selfCycle2 = new SelfCycle();
        selfCycle2.setId(new Long(2));
        selfCycle2.setValue("value2");

        selfCycle1.setSelfCycle(selfCycle2);
        selfCycle2.setSelfCycle(selfCycle1);

        session.save(selfCycle1);
        session.save(selfCycle2);

        selfCycle1 = session.load(SelfCycle.class, new Long(1));
        assertEquals(System.identityHashCode(selfCycle1),
                System.identityHashCode(selfCycle1.getSelfCycle().getSelfCycle()));

        tr.commit();
        session.close();
    }
}
