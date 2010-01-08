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

package org.compass.core.test.querybuilder.range1;

import java.util.Date;

import org.compass.core.CompassHits;
import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.test.AbstractTestCase;

/**
 * @author kimchy
 */
public class RangeQueryBuilderTests extends AbstractTestCase {

    protected String[] getMappings() {
        return new String[]{"querybuilder/range1/mapping.cpm.xml"};
    }

    public void testIntExactNoPadding() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        A a = new A();
        a.id = 1;
        a.value = 1;
        session.save("a", a);

        CompassHits hits = session.find("value:1");
        assertEquals(1, hits.length());

        tr.commit();
        session.close();
    }

    public void testIntRangeNoPadding() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        A a = new A();
        a.id = 1;
        a.value = 1;
        session.save("a", a);

        a.id = 2;
        a.value = 2;
        session.save("a", a);

        a.id = 3;
        a.value = 11;
        session.save("a", a);

        a.id = 4;
        a.value = 22;
        session.save("a", a);

        CompassHits hits = session.find("value:[1 TO 3]");
        assertEquals(2, hits.length());

        tr.commit();
        session.close();
    }

    public void testDateRangeUsingNow() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        A a = new A();
        a.id = 1;
        a.date = new Date();
        session.save("a", a);

        CompassHits hits = session.find("date:[now-1hour now+1hour]");
        assertEquals(1, hits.length());

        hits = session.find("date:[now-2hour now-1hour]");
        assertEquals(0, hits.length());

        tr.commit();
        session.close();
    }

    public void testDateRangeUsingTwoDifferentFormats() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        A a = new A();
        a.id = 1;
        a.date2 = new Date();
        session.save("a", a);

        // verify that the default format works
        CompassHits hits = session.find("date2:[2000-01-01 TO 2011-01-01]");
        assertEquals(1, hits.length());

        hits = session.queryBuilder().between("a.date2", "2000-01-01", "2011-01-01", true).hits();
        assertEquals(1, hits.length());

        // verify that the other format works as well
        hits = session.find("date2:[01-01-2000 TO 01-01-2011]");
        assertEquals(1, hits.length());

        hits = session.queryBuilder().between("a.date2", "01-01-2000 ", "01-01-2011", true).hits();
        assertEquals(1, hits.length());

        tr.commit();
        session.close();
    }
}
