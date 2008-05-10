/*
 * Copyright 2004-2006 the original author or authors.
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
import java.util.Iterator;
import java.util.List;

import org.compass.core.CompassHits;
import org.compass.core.CompassQuery;
import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.cache.first.DefaultFirstLevelCache;
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
        settings.setSetting(CompassEnvironment.Cache.FirstLevel.TYPE, DefaultFirstLevelCache.class.getName());
    }

    public void testSameChild() throws Exception {
        Parent p1 = createParent(1);
        Parent p2 = createParent(2);

        ArrayList allChildren = new ArrayList();
        allChildren.add(createChild(1));
        allChildren.add(createChild(3));
        allChildren.add(createChild(2));

        p1.children = allChildren.subList(0, 2);
        p2.children = allChildren.subList(1, 3);

        // initial test
        assertIdsMatchNames("All original children", allChildren);
        assertIdsMatchNames("Original p1 children", p1.children);
        assertIdsMatchNames("Original p2 children", p2.children);

        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        for (Iterator it = allChildren.iterator(); it.hasNext();) {
            session.save(it.next());
        }
        session.save(p1);
        session.save(p2);

        tr.commit();
        session.close();

        session = openSession();
        tr = session.beginTransaction();

        p1 = (Parent) session.load(Parent.class, "1");
        p2 = (Parent) session.load(Parent.class, "2");

        List children = new ArrayList();
        CompassQuery query = session.queryBuilder().matchAll().setAliases(new String[]{"child"});
        CompassHits hits = query.hits();
        for (int i = 0, n = hits.length(); i < n; i++) {
            children.add(hits.data(i));
        }

        tr.commit();
        session.close();

        assertIdsMatchNames("All fetched children", children);
        assertIdsMatchNames("Fetched p1 children", p1.children);
        assertIdsMatchNames("Fetched p2 children", p2.children);
    }

    private void assertIdsMatchNames(String header, List children) {
//        System.out.println(header);
        for (Iterator it = children.iterator(); it.hasNext();) {
            Child c = (Child) it.next();
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
