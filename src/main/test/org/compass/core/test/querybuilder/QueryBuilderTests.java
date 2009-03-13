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

package org.compass.core.test.querybuilder;

import java.util.Calendar;

import org.compass.core.CompassException;
import org.compass.core.CompassHits;
import org.compass.core.CompassQueryBuilder;
import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.config.CompassEnvironment;
import org.compass.core.config.CompassSettings;
import org.compass.core.test.AbstractTestCase;

/**
 * @author kimchy
 */
public class QueryBuilderTests extends AbstractTestCase {

    protected String[] getMappings() {
        return new String[]{"querybuilder/querybuilder.cpm.xml"};
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
        session.save("a", a);
        a.setId(new Long(2));
        a.setValue1("0002");
        a.setValue2("test2");
        calendar.set(2000, 1, 2);
        a.setDate(calendar.getTime());
        session.save("a", a);
        a.setId(new Long(3));
        a.setValue1("0003");
        a.setValue2("test3");
        calendar.set(2000, 1, 3);
        a.setDate(calendar.getTime());
        session.save("a", a);
        a.setId(new Long(4));
        a.setValue1("0004");
        a.setValue2("the quick brown fox jumped over the lazy dog");
        calendar.set(2000, 1, 4);
        a.setDate(calendar.getTime());
        session.save("a", a);
    }

    public void testCustomFormatForDate() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        Calendar calendar = Calendar.getInstance();
        calendar.set(2000, 1, 1);
        A a = new A();
        a.setId(new Long(1));
        a.setValue1("0001");
        a.setValue2("test1");
        a.setDate(calendar.getTime());
        session.save("a1", a);

        CompassQueryBuilder queryBuilder = session.queryBuilder();
        CompassHits hits = queryBuilder.term("a1.date", calendar.getTime()).hits();
        assertEquals(1, hits.length());
        hits = queryBuilder.term("a1.date.date", calendar.getTime()).hits();
        assertEquals(1, hits.length());
        calendar.set(2001, 1, 1);
        hits = queryBuilder.term("a1.date", calendar.getTime()).hits();
        assertEquals(0, hits.length());

        tr.commit();
        session.close();
    }

    public void testMultiPropertyQueryString() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        setUpData(session);

        CompassQueryBuilder queryBuilder = session.queryBuilder();
        CompassHits hits = queryBuilder.multiPropertyQueryString("test1 OR 0002").add("mvalue1")
                .add("mvalue2").toQuery().hits();
        assertEquals(2, hits.length());

        tr.commit();
        session.close();
    }

    public void testEqAlias() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        setUpData(session);

        CompassQueryBuilder queryBuilder = session.queryBuilder();
        CompassHits hits = queryBuilder.alias("a").hits();
        assertEquals(4, hits.length());

        try {
            queryBuilder.alias("b").hits();
            fail();
        } catch (CompassException e) {
            // this is ok
        }

        tr.commit();
        session.close();
    }

    public void testQueryString() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        setUpData(session);

        assertEquals(1, session.queryBuilder().queryString("mvalue1:0001").toQuery().hits().length());

        tr.commit();
        session.close();
    }

    public void testEq() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        setUpData(session);

        CompassQueryBuilder queryBuilder = session.queryBuilder();
        CompassHits hits = queryBuilder.term("mvalue1", "0001").hits();
        assertEquals(1, hits.length());

        hits = queryBuilder.term("mvalue2", "brown").hits();
        assertEquals(1, hits.length());

        hits = queryBuilder.term("mvalue2", "test").hits();
        assertEquals(0, hits.length());

        Calendar calendar = Calendar.getInstance();
        calendar.set(2000, 1, 1);
        hits = queryBuilder.term("a.date", calendar.getTime()).hits();
        assertEquals(1, hits.length());
        hits = queryBuilder.term("a.date.date", calendar.getTime()).hits();
        assertEquals(1, hits.length());
        calendar.set(2001, 1, 1);
        hits = queryBuilder.term("a.date", calendar.getTime()).hits();
        assertEquals(0, hits.length());

        tr.commit();
        session.close();
    }

    public void testBetween() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        setUpData(session);

        CompassQueryBuilder queryBuilder = session.queryBuilder();
        CompassHits hits = queryBuilder.between("mvalue1", "0001", "0003", true).hits();
        assertEquals(3, hits.length());

        queryBuilder = session.queryBuilder();
        hits = queryBuilder.between("mvalue1", "0001", "0003", false).hits();
        assertEquals(1, hits.length());

        tr.commit();
        session.close();
    }

    public void testGt() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        setUpData(session);

        CompassQueryBuilder queryBuilder = session.queryBuilder();
        CompassHits hits = queryBuilder.gt("mvalue1", "0002").hits();
        assertEquals(2, hits.length());

        tr.commit();
        session.close();
    }

    public void testGe() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        setUpData(session);

        CompassQueryBuilder queryBuilder = session.queryBuilder();
        CompassHits hits = queryBuilder.ge("mvalue1", "0002").hits();
        assertEquals(3, hits.length());

        tr.commit();
        session.close();
    }

    public void testLt() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        setUpData(session);

        CompassQueryBuilder queryBuilder = session.queryBuilder();
        CompassHits hits = queryBuilder.lt("mvalue1", "0002").hits();
        assertEquals(1, hits.length());

        tr.commit();
        session.close();
    }

    public void testLe() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        setUpData(session);

        CompassQueryBuilder queryBuilder = session.queryBuilder();
        CompassHits hits = queryBuilder.le("mvalue1", "0002").hits();
        assertEquals(2, hits.length());

        tr.commit();
        session.close();
    }

    public void testPrefix() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        setUpData(session);

        CompassQueryBuilder queryBuilder = session.queryBuilder();
        CompassHits hits = queryBuilder.prefix("mvalue1", "000").hits();
        assertEquals(4, hits.length());

        hits = queryBuilder.prefix("a.value1", "000").hits();
        assertEquals(4, hits.length());

        // test escaping
        hits = queryBuilder.prefix("'a.value1'", "000").hits();
        assertEquals(0, hits.length());

        queryBuilder = session.queryBuilder();
        hits = queryBuilder.prefix("mvalue1", "0002").hits();
        assertEquals(1, hits.length());

        queryBuilder = session.queryBuilder();
        hits = queryBuilder.prefix("mvalue1", "001").hits();
        assertEquals(0, hits.length());

        tr.commit();
        session.close();
    }

    public void testWildcard() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        setUpData(session);

        CompassQueryBuilder queryBuilder = session.queryBuilder();
        CompassHits hits = queryBuilder.wildcard("mvalue1", "000*").hits();
        assertEquals(4, hits.length());

        hits = queryBuilder.wildcard("a.value1", "000*").hits();
        assertEquals(4, hits.length());

        hits = queryBuilder.wildcard("a.value1.mvalue1", "000*").hits();
        assertEquals(4, hits.length());

        tr.commit();
        session.close();
    }

    public void testPhrase() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        setUpData(session);

        CompassQueryBuilder queryBuilder = session.queryBuilder();
        CompassHits hits = queryBuilder.multiPhrase("mvalue2").setSlop(0).add("quick").add("fox").toQuery().hits();
        assertEquals(0, hits.length());

        queryBuilder = session.queryBuilder();
        hits = queryBuilder.multiPhrase("mvalue2").setSlop(1).add("quick").add("fox").toQuery().hits();
        assertEquals(1, hits.length());

        tr.commit();
        session.close();
    }

    public void testFuzzy() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        A a = new A();
        a.setId(new Long(1));
        a.setValue1("wuzzy");
        session.save("a", a);

        CompassQueryBuilder queryBuilder = session.queryBuilder();
        CompassHits hits = queryBuilder.fuzzy("mvalue1", "wuzza").hits();
        assertEquals(1, hits.length());

        hits = queryBuilder.fuzzy("a.value1.mvalue1", "wuzza").hits();
        assertEquals(1, hits.length());

        queryBuilder = session.queryBuilder();
        hits = queryBuilder.fuzzy("mvalue1", "wuzza", 0.9999f).hits();
        assertEquals(0, hits.length());

        tr.commit();
        session.close();
    }

    public void testBool() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        setUpData(session);

        CompassQueryBuilder queryBuilder = session.queryBuilder();
        CompassHits hits = queryBuilder.bool().addMust(queryBuilder.term("mvalue1", "0001")).toQuery().hits();
        assertEquals(1, hits.length());

        queryBuilder = session.queryBuilder();
        hits = queryBuilder.bool().addMust(queryBuilder.term("mvalue1", "0001")).addMust(
                queryBuilder.term("mvalue2", "test1")).toQuery().hits();
        assertEquals(1, hits.length());

        queryBuilder = session.queryBuilder();
        hits = queryBuilder.bool().addMust(queryBuilder.term("mvalue1", "0001")).addMust(
                queryBuilder.term("mvalue2", "test2")).toQuery().hits();
        assertEquals(0, hits.length());

        queryBuilder = session.queryBuilder();
        hits = queryBuilder.bool().addMust(queryBuilder.term("mvalue1", "0001")).addMustNot(
                queryBuilder.term("mvalue2", "test1")).toQuery().hits();
        assertEquals(0, hits.length());

        queryBuilder = session.queryBuilder();
        hits = queryBuilder.bool().addMust(queryBuilder.term("mvalue1", "0001")).addShould(
                queryBuilder.term("mvalue2", "test2XXX")).toQuery().hits();
        assertEquals(1, hits.length());

        tr.commit();
        session.close();
    }

    public void testSpanEq() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        setUpData(session);

        CompassQueryBuilder queryBuilder = session.queryBuilder();
        CompassHits hits = queryBuilder.spanEq("mvalue1", "0001").hits();
        assertEquals(1, hits.length());

        hits = queryBuilder.spanEq("mvalue2", "test").hits();
        assertEquals(0, hits.length());

        Calendar calendar = Calendar.getInstance();
        calendar.set(2000, 1, 1);
        hits = queryBuilder.spanEq("a.date", calendar.getTime()).hits();
        assertEquals(1, hits.length());
        hits = queryBuilder.spanEq("a.date.date", calendar.getTime()).hits();
        assertEquals(1, hits.length());
        calendar.set(2001, 1, 1);
        hits = queryBuilder.spanEq("a.date", calendar.getTime()).hits();
        assertEquals(0, hits.length());

        tr.commit();
        session.close();
    }

    public void testSpanFirst() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        setUpData(session);

        CompassQueryBuilder queryBuilder = session.queryBuilder();
        CompassHits hits = queryBuilder.spanFirst("mvalue2", "brown", 1).hits();
        assertEquals(0, hits.length());

        queryBuilder = session.queryBuilder();
        hits = queryBuilder.spanFirst("mvalue2", "brown", 2).hits();
        assertEquals(1, hits.length());

        tr.commit();
        session.close();
    }

    public void testSpanNear() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        setUpData(session);

        CompassQueryBuilder queryBuilder = session.queryBuilder();
        CompassHits hits = queryBuilder.spanNear("mvalue2").add("quick").add("brown").add("dog").setSlop(0).setInOrder(
                true).toQuery().hits();
        assertEquals(0, hits.length());

        hits = queryBuilder.spanNear("mvalue2").add("quick").add("brown").add("dog").setSlop(3).setInOrder(true)
                .toQuery().hits();
        assertEquals(0, hits.length());

        hits = queryBuilder.spanNear("mvalue2").add("quick").add("brown").add("dog").setSlop(4).setInOrder(true)
                .toQuery().hits();
        assertEquals(1, hits.length());

        hits = queryBuilder.spanNear("mvalue2").add("lazy").add("fox").setSlop(3).setInOrder(false).toQuery().hits();
        assertEquals(1, hits.length());

        tr.commit();
        session.close();
    }

    public void testSpanNot() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        setUpData(session);

        CompassQueryBuilder queryBuilder = session.queryBuilder();
        CompassHits hits = queryBuilder.spanNot(
                queryBuilder.spanNear("mvalue2").add("quick").add("fox").setSlop(1).setInOrder(true).toQuery(),
                queryBuilder.spanEq("mvalue2", "dog")).hits();
        assertEquals(1, hits.length());

        hits = queryBuilder.spanNot(
                queryBuilder.spanNear("mvalue2").add("quick").add("fox").setSlop(1).setInOrder(true).toQuery(),
                queryBuilder.spanEq("mvalue2", "brown")).hits();
        assertEquals(0, hits.length());

        tr.commit();
        session.close();
    }

    public void testSpanOr() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        setUpData(session);

        CompassQueryBuilder queryBuilder = session.queryBuilder();
        CompassHits hits = queryBuilder.spanOr().add(queryBuilder.spanEq("mvalue2", "quick")).add(
                queryBuilder.spanEq("mvalue2", "notthere")).toQuery().hits();
        assertEquals(1, hits.length());

        tr.commit();
        session.close();
    }
}
