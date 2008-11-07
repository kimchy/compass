/*
 * Copyright 2004-2008 the original author or authors.
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

package org.compass.core.test.component.cyclic2;

import java.util.ArrayList;
import java.util.List;

import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.test.AbstractTestCase;

/**
 * @author kimchy
 */
public class ParentChildCycleComponentTests extends AbstractTestCase {

    protected String[] getMappings() {
        return new String[]{"component/cyclic2/ParentChildCycle.cpm.xml"};
    }


    public void testParentChildCyclic() throws Exception {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        ParentCycle parentCycle = new ParentCycle();
        parentCycle.setId(new Long(1));
        parentCycle.setValue("parentValue");

        ChildCycle childCycle11 = new ChildCycle();
        childCycle11.setValue("child11");
        ChildCycle childCycle12 = new ChildCycle();
        childCycle12.setValue("child12");

        List children = new ArrayList();
        children.add(childCycle11);
        children.add(childCycle12);

        parentCycle.setChildren(children);

        ChildCycle childCycle21 = new ChildCycle();
        childCycle21.setValue("child21");
        ChildCycle childCycle22 = new ChildCycle();
        childCycle22.setValue("child22");

        children = new ArrayList();
        children.add(childCycle21);
        children.add(childCycle22);
        childCycle11.setChildren(children);

        ChildCycle childCycle31 = new ChildCycle();
        childCycle31.setValue("child31");

        children = new ArrayList();
        children.add(childCycle31);
        childCycle21.setChildren(children);

        session.save(parentCycle);

        parentCycle = (ParentCycle) session.load(ParentCycle.class, new Long(1));
        assertEquals("parentValue", parentCycle.getValue());

        children = parentCycle.getChildren();
        assertEquals(2, children.size());
        childCycle11 = (ChildCycle) children.get(0);
        assertEquals("child11", childCycle11.getValue());
        childCycle12 = (ChildCycle) children.get(1);
        assertEquals("child12", childCycle12.getValue());

        children = childCycle11.getChildren();
        assertEquals(2, children.size());
        childCycle21 = (ChildCycle) children.get(0);
        assertEquals("child21", childCycle21.getValue());
        childCycle22 = (ChildCycle) children.get(1);
        assertEquals("child22", childCycle22.getValue());

        assertNull(childCycle12.getChildren());

        children = childCycle21.getChildren();
        assertEquals(1, children.size());
        childCycle31 = (ChildCycle) children.get(0);
        assertEquals("child31", childCycle31.getValue());

        assertNull(childCycle22.getChildren());

        tr.commit();
        session.close();
    }
}
