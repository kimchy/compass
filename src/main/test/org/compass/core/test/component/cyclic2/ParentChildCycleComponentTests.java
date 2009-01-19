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
        parentCycle.id = 1;
        parentCycle.value = "parentValue";

        ChildCycle childCycle11 = new ChildCycle();
        childCycle11.value = "child11";
        ChildCycle childCycle12 = new ChildCycle();
        childCycle12.value = "child12";

        parentCycle.children = new ArrayList<ChildCycle>();
        parentCycle.children.add(childCycle11);
        parentCycle.children.add(childCycle12);

        ChildCycle childCycle21 = new ChildCycle();
        childCycle21.value = "child21";
        ChildCycle childCycle22 = new ChildCycle();
        childCycle22.value = "child22";

        childCycle11.children = new ArrayList<ChildCycle>();
        childCycle11.children.add(childCycle21);
        childCycle11.children.add(childCycle22);

        ChildCycle childCycle31 = new ChildCycle();
        childCycle31.value = "child31";

        childCycle21.children = new ArrayList<ChildCycle>();
        childCycle21.children.add(childCycle31);

        session.save(parentCycle);

        parentCycle = session.load(ParentCycle.class, 1);
        assertEquals("parentValue", parentCycle.value);

        List<ChildCycle> children = parentCycle.children;
        assertEquals(2, children.size());
        childCycle11 = children.get(0);
        assertEquals("child11", childCycle11.value);
        childCycle12 = children.get(1);
        assertEquals("child12", childCycle12.value);

        children = childCycle11.children;
        assertEquals(2, children.size());
        childCycle21 = children.get(0);
        assertEquals("child21", childCycle21.value);
        childCycle22 = children.get(1);
        assertEquals("child22", childCycle22.value);

        assertNull(childCycle12.children);

        children = childCycle21.children;
        assertEquals(1, children.size());
        childCycle31 = children.get(0);
        assertEquals("child31", childCycle31.value);

        assertNull(childCycle22.children);

        tr.commit();
        session.close();
    }
}
