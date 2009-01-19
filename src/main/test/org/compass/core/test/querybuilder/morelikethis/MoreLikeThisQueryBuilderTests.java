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

package org.compass.core.test.querybuilder.morelikethis;

import org.compass.core.CompassDetachedHits;
import org.compass.core.CompassHits;
import org.compass.core.CompassQuery;
import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.test.AbstractTestCase;

/**
 * @author kimchy
 */
public class MoreLikeThisQueryBuilderTests extends AbstractTestCase {

    protected String[] getMappings() {
        return new String[]{"querybuilder/morelikethis/mapping.cpm.xml"};
    }

    public void testSimpleMoreLikeThis() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        A a = new A();
        a.id = 1;
        a.value = "test with some specific keyword keyword";
        session.save("a", a);

        a = new A();
        a.id = 2;
        a.value = "another one with keyword keyword";
        session.save("a", a);

        a = new A();
        a.id = 3;
        a.value = "nothing related";
        session.save("a", a);

        // verify explicit value
        CompassQuery query = session.queryBuilder().moreLikeThis("a", "1")
                .setMinResourceFreq(1).setMinTermFreq(1)
                .addProperty("value").toQuery();
        CompassHits hits = query.hits();
        assertEquals(1, hits.length());

        // verify on all (note we store term vector on it)
        query = session.queryBuilder().moreLikeThis("a", "1")
                .setMinResourceFreq(1).setMinTermFreq(2)
                .toQuery();
        // (we will find two on this ones since the alias is added).
        CompassDetachedHits detachedHits = query.hits().detach();
        assertEquals(1, detachedHits.length());

        // just test adding several properties
        session.queryBuilder().moreLikeThis("a", "1").addProperty("value").addProperty("alias");

        tr.commit();
        session.close();
    }
}