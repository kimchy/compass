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

package org.compass.core.test.component.deephierarchy2;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.test.AbstractTestCase;

/**
 * @author kimchy
 */
public class DeepHierarchy2Tests extends AbstractTestCase {

    protected String[] getMappings() {
        return new String[]{"component/deephierarchy2/mapping.cpm.xml"};
    }

    public void testNullValue() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        A a = new A();
        a.setName("A");
        a.setId("1");

        B b = new B("1", "AA");

        List bs = new ArrayList();
        bs.add(b);
        a.setBs(bs);

        session.save(a);

        b = new B("2", "AB");
        C c = new C("3", "AC3");

        List childrenOf1 = new ArrayList();
        childrenOf1.add(c);
        b.setCs(childrenOf1);

        List newBs = new ArrayList(a.getBs());
        newBs.add(b);
        a.setBs(newBs);

        session.save(a);

        a = (A) session.load(A.class, "1");
        for (Iterator it = a.getBs().iterator(); it.hasNext();) {
            b = (B) it.next();
            int childrenSize = b.getCs() == null ? 0 : b.getCs().size();
            if (b.getName().equals("AA")) {
                assertEquals(0, childrenSize);
            } else if (b.getName().equals("AB")) {
                assertEquals(1, childrenSize);
            }
        }

        tr.commit();
        session.close();
    }
}
