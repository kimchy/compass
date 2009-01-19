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

package org.compass.core.test.untokenized;

import org.compass.core.CompassHits;
import org.compass.core.CompassQuery;
import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.test.AbstractTestCase;

public class UntokenizedTests extends AbstractTestCase {

    protected String[] getMappings() {
        return new String[]{"untokenized/A.cpm.xml"};
    }

    public void testUntokenized() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        A a = new A();
        a.id = new Long(1);
        a.value = "Client";
        session.save(a);

        a = new A();
        a.id = new Long(2);
        a.value = "Client Type";
        session.save(a);

        // this one will not find anything, since an analyzer is applied to the query string
        // TODO need to find a simple solution for this one
        CompassQuery query = session.queryBuilder().queryString("type:Client").toQuery();
        CompassHits hits = query.hits();
        assertEquals(0, hits.length());

        tr.commit();
        session.close();
    }
}
