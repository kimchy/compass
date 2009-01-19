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

package org.compass.core.test.filterquerybuilder;

import java.util.Calendar;

import org.compass.core.CompassHits;
import org.compass.core.CompassQueryBuilder;
import org.compass.core.CompassQueryFilterBuilder;
import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.config.CompassEnvironment;
import org.compass.core.config.CompassSettings;
import org.compass.core.test.AbstractTestCase;

/**
 * @author kimchy
 */
public class QueryFilterBuilderTests extends AbstractTestCase {

    protected String[] getMappings() {
        return new String[]{"filterquerybuilder/querybuilder.cpm.xml"};
    }

    protected void addSettings(CompassSettings settings) {
        settings.setGroupSettings(CompassEnvironment.Converter.PREFIX,
                CompassEnvironment.Converter.DefaultTypes.Simple.DATE,
                new String[]{CompassEnvironment.Converter.Format.FORMAT},
                new String[]{"yyyy-MM-dd-HH"});
    }

    private void setUpData(CompassSession session) {
        Calendar calendar = Calendar.getInstance();
        A a = new A();
        a.setId(new Long(1));
        a.setValue1("0001");
        a.setValue2("test1");
        calendar.set(2000, 1, 1);
        a.setDate(calendar.getTime());
        session.save(a);
        a.setId(new Long(2));
        a.setValue1("0002");
        a.setValue2("test2");
        calendar.set(2000, 1, 2);
        a.setDate(calendar.getTime());
        session.save(a);
        a.setId(new Long(3));
        a.setValue1("0003");
        a.setValue2("test3");
        calendar.set(2000, 1, 3);
        a.setDate(calendar.getTime());
        session.save(a);
        a.setId(new Long(4));
        a.setValue1("0004");
        a.setValue2("the quick brown fox jumped over the lazy dog");
        calendar.set(2000, 1, 4);
        a.setDate(calendar.getTime());
        session.save(a);
    }

    public void testBetween() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        setUpData(session);

        CompassQueryBuilder queryBuilder = session.queryBuilder();
        CompassQueryFilterBuilder queryFilterBuilder = session.queryFilterBuilder();
        CompassHits hits = queryBuilder.alias("a").
                setFilter(queryFilterBuilder.between("mvalue1", "0001", "0003", true, true)).hits();
        assertEquals(3, hits.length());

        hits = queryBuilder.matchAll().
                setFilter(queryFilterBuilder.between("mvalue1", "0001", "0003", false, true)).hits();
        assertEquals(2, hits.length());

        hits = queryBuilder.matchAll().
                setFilter(queryFilterBuilder.between("mvalue1", "0001", "0003", true, false)).hits();
        assertEquals(2, hits.length());

        hits = queryBuilder.matchAll().
                setFilter(queryFilterBuilder.between("mvalue1", "0001", "0003", false, false)).hits();
        assertEquals(1, hits.length());

        tr.commit();
        session.close();
    }

    public void testGt() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        setUpData(session);

        CompassQueryBuilder queryBuilder = session.queryBuilder();
        CompassQueryFilterBuilder queryFilterBuilder = session.queryFilterBuilder();
        CompassHits hits = queryBuilder.matchAll().
                setFilter(queryFilterBuilder.gt("mvalue1", "0002")).hits();
        assertEquals(2, hits.length());

        tr.commit();
        session.close();
    }

    public void testGe() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        setUpData(session);

        CompassQueryBuilder queryBuilder = session.queryBuilder();
        CompassQueryFilterBuilder queryFilterBuilder = session.queryFilterBuilder();
        CompassHits hits = queryBuilder.matchAll().
                setFilter(queryFilterBuilder.ge("mvalue1", "0002")).hits();
        assertEquals(3, hits.length());

        tr.commit();
        session.close();
    }

    public void testLt() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        setUpData(session);

        CompassQueryBuilder queryBuilder = session.queryBuilder();
        CompassQueryFilterBuilder queryFilterBuilder = session.queryFilterBuilder();
        CompassHits hits = queryBuilder.matchAll().
                setFilter(queryFilterBuilder.lt("mvalue1", "0002")).hits();
        assertEquals(1, hits.length());

        tr.commit();
        session.close();
    }

    public void testLe() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        setUpData(session);

        CompassQueryBuilder queryBuilder = session.queryBuilder();
        CompassQueryFilterBuilder queryFilterBuilder = session.queryFilterBuilder();
        CompassHits hits = queryBuilder.matchAll().
                setFilter(queryFilterBuilder.le("mvalue1", "0002")).hits();
        assertEquals(2, hits.length());

        tr.commit();
        session.close();
    }

    public void testQuery() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        setUpData(session);

        CompassQueryBuilder queryBuilder = session.queryBuilder();
        CompassQueryFilterBuilder queryFilterBuilder = session.queryFilterBuilder();
        CompassHits hits = queryBuilder.matchAll().
                setFilter(queryFilterBuilder.query(queryBuilder.le("mvalue1", "0002"))).hits();
        assertEquals(2, hits.length());

        tr.commit();
        session.close();
    }

    public void testBool() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        setUpData(session);

        CompassQueryBuilder queryBuilder = session.queryBuilder();
        CompassQueryFilterBuilder queryFilterBuilder = session.queryFilterBuilder();
        CompassHits hits = queryBuilder.matchAll().
                setFilter(queryFilterBuilder.bool().and(queryFilterBuilder.le("mvalue1", "0002")).and(queryFilterBuilder.ge("mvalue1", "0002")).toFilter()).hits();
        assertEquals(1, hits.length());

        tr.commit();
        session.close();
    }
}
