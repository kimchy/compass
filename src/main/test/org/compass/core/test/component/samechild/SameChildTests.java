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

package org.compass.core.test.component.samechild;

import java.util.ArrayList;
import java.util.List;

import org.compass.core.CompassHits;
import org.compass.core.CompassQuery;
import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.cache.first.PlainFirstLevelCache;
import org.compass.core.config.CompassEnvironment;
import org.compass.core.config.CompassSettings;
import org.compass.core.test.AbstractTestCase;

/**
 * @author kimchy
 */
public class SameChildTests extends AbstractTestCase {

    protected String[] getMappings() {
        return new String[]{"component/samechild/mapping.cpm.xml"};
    }


    protected void addSettings(CompassSettings settings) {
        settings.setSetting(CompassEnvironment.Cache.FirstLevel.TYPE, PlainFirstLevelCache.class.getName());
    }

    public void testSameChildSameParent() throws Exception {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        Parent p = createParent(1);
        ArrayList<Child> children = new ArrayList<Child>();
        children.add(createChild(1));    // 1
        children.add(createChild(2));    // 2
        children.add(children.get(0));   // 3
        children.add(createChild(3));    // 4
        children.add(children.get(1));   // 5
        children.add(children.get(0));   // 6
        children.add(createChild(4));    // 7
        p.children = children;

        assertIdsMatchNames("All original children", children);
        assertIdsMatchNames("Original p children", p.children);
        session.save(p);

        p = session.load(Parent.class, 1);
        assertIdsMatchNames("All fetched children", p.children);
        assertEquals(1, p.children.get(0).id);
        assertEquals(2, p.children.get(1).id);
        assertEquals(1, p.children.get(2).id);
        assertEquals(3, p.children.get(3).id);
        assertEquals(2, p.children.get(4).id);
        assertEquals(1, p.children.get(5).id);
        assertEquals(4, p.children.get(6).id);

        assertEquals("child1", p.children.get(0).name);
        assertEquals("child2", p.children.get(1).name);
        assertEquals("child1", p.children.get(2).name);
        assertEquals("child3", p.children.get(3).name);
        assertEquals("child2", p.children.get(4).name);
        assertEquals("child1", p.children.get(5).name);
        assertEquals("child4", p.children.get(6).name);


        tr.commit();
        session.close();
    }

    public void testSameChildDifferentParents() throws Exception {
        Parent p1 = createParent(1);
        Parent p2 = createParent(2);

        ArrayList<Child> children = new ArrayList<Child>();
        children.add(createChild(1));
        children.add(createChild(3));
        children.add(createChild(2));

        p1.children = children.subList(0, 2);
        p2.children = children.subList(1, 3);

        // initial test
        assertIdsMatchNames("All original children", children);
        assertIdsMatchNames("Original p1 children", p1.children);
        assertIdsMatchNames("Original p2 children", p2.children);

        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        for (Child child : children) {
            session.save(child);
        }
        session.save(p1);
        session.save(p2);

        tr.commit();
        session.close();

        session = openSession();
        tr = session.beginTransaction();

        p1 = session.load(Parent.class, "1");
        p2 = session.load(Parent.class, "2");

        children = new ArrayList<Child>();
        CompassQuery query = session.queryBuilder().matchAll().setAliases("child");
        CompassHits hits = query.hits();
        for (int i = 0, n = hits.length(); i < n; i++) {
            children.add((Child) hits.data(i));
        }

        tr.commit();
        session.close();

        assertIdsMatchNames("All fetched children", children);
        assertIdsMatchNames("Fetched p1 children", p1.children);
        assertIdsMatchNames("Fetched p2 children", p2.children);
    }

    private void assertIdsMatchNames(String header, List<Child> children) {
//        System.out.println(header);
        for (Child c : children) {
            assertEquals("child" + c.id, c.name);
        }
    }

    private Child createChild(int i) {
        Child c = new Child();
        c.id = i;
        c.name = "child" + i;
        return c;
    }

    private Parent createParent(int i) {
        Parent p = new Parent();
        p.id = i;
        p.name = "parent" + i;
        return p;
    }

}
