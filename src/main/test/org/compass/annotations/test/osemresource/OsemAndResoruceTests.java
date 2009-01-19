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

package org.compass.annotations.test.osemresource;

import org.compass.annotations.test.AbstractAnnotationsTestCase;
import org.compass.core.CompassHits;
import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.Resource;
import org.compass.core.config.CompassConfiguration;

/**
 * Tests that verify working with OSEM and sometimes with pure Resource that
 * resulted from OSEM can be done.
 *
 * @author kimchy
 */
public class OsemAndResoruceTests extends AbstractAnnotationsTestCase {

    @Override
    protected void addExtraConf(CompassConfiguration conf) {
        conf.addClass(A.class).addClass(B.class);
    }

    public void testSimpleOsemAndResource() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        B b = new B();
        b.id = 1;
        b.bvalue = "bvalue";

        A a = new A();
        a.id = 1;
        a.value = "avalue1";
        a.b = b;
        session.save(a);

        a = new A();
        a.id = 2;
        a.value = "avalue2";
        a.b = b;
        session.save(a);
        
        tr.commit();
        session.close();

        session = openSession();
        tr = session.beginTransaction();

        CompassHits hits = session.find("A.b.bvalue:bvalue");
        assertEquals(2, hits.length());

        hits = session.queryBuilder().term("A.b.id", 1).hits();
        assertEquals(2, hits.length());
        for (int i = 0; i < hits.length(); i++) {
            Resource aResource = hits.resource(i);
            aResource.setProperty("bvalue", "bvalue2");
            session.save(aResource);
        }

        hits = session.find("A.b.bvalue:bvalue");
        assertEquals(0, hits.length());

        hits = session.find("A.b.bvalue:bvalue2");
        assertEquals(2, hits.length());

        tr.commit();
        session.close();
    }
}
