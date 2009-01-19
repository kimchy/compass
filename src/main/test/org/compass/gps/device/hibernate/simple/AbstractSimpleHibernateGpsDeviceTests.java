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

package org.compass.gps.device.hibernate.simple;

import junit.framework.Assert;
import org.compass.core.CompassHits;
import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.config.CompassConfiguration;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;

/**
 * @author kimchy
 */
public abstract class AbstractSimpleHibernateGpsDeviceTests extends AbstractHibernateGpsDeviceTests {

    protected void setUpCoreCompass(CompassConfiguration conf) {
        conf.addClass(Simple.class).addClass(SimpleBase.class).addClass(SimpleExtend.class);
    }

    protected String getHiberanteCfgLocation() {
        return "/org/compass/gps/device/hibernate/simple/hibernate.cfg.xml";
    }

    protected SessionFactory doSetUpSessionFactory() {
        Configuration conf = new Configuration().configure(getHiberanteCfgLocation())
                .setProperty(Environment.HBM2DDL_AUTO, "create");
        return conf.buildSessionFactory();
    }

    protected void setUpDB(Session session) {
        Simple simple = new Simple();
        simple.setId(1);
        simple.setValue("value1");
        session.save("simple", simple);
        simple = new Simple();
        simple.setId(2);
        simple.setValue("value2");
        session.save("simple", simple);
        simple = new Simple();
        simple.setId(3);
        simple.setValue("value3");
        session.save("simple", simple);

        SimpleBase simpleBase = new SimpleBase();
        simpleBase.setId(1);
        simpleBase.setValue("value");
        session.save(simpleBase);

        SimpleExtend simpleExtend = new SimpleExtend();
        simpleExtend.setId(2);
        simpleExtend.setValue("value");
        simpleExtend.setValueExtend("valueExtended");
        session.save(simpleExtend);
    }

    protected void tearDownDB(Session session) {
        Query query = session.createQuery("delete from simple");
        query.executeUpdate();
        query = session.createQuery("delete from SimpleBase");
        query.executeUpdate();
    }

    public void testSimple() throws Exception {
        compassGps.index();

        CompassSession sess = compass.openSession();
        CompassTransaction tr = sess.beginTransaction();

        Simple simple = sess.load(Simple.class, 1);
        Assert.assertEquals("value1", simple.getValue());
        simple = sess.load(Simple.class, 2);
        Assert.assertEquals("value2", simple.getValue());

        CompassHits hits = sess.find("value1");
        Assert.assertEquals(1, hits.length());

        tr.commit();
        sess.close();
    }

    public void testMirrorWithCommit() throws Exception {
        compassGps.index();

        Session session = sessionFactory.openSession();
        Transaction tr = session.beginTransaction();

        // insert a new one
        Simple simple = new Simple();
        simple.setId(4);
        simple.setValue("value4");
        session.save("simple", simple);

        // delete the second one
        simple = (Simple) session.load("simple", 2);
        session.delete(simple);

        // update the first one
        simple = (Simple) session.load("simple", 1);
        simple.setValue("updatedValue1");
        session.save(simple);

        session.flush();

        tr.commit();
        session.close();

        CompassSession sess = compass.openSession();
        CompassTransaction compassTransaction = sess.beginTransaction();

        simple = sess.load(Simple.class, 4);
        assertEquals("value4", simple.getValue());

        simple = sess.get(Simple.class, 2);
        assertNull(simple);

        simple = sess.load(Simple.class, 1);
        assertEquals("updatedValue1", simple.getValue());

        compassTransaction.commit();
        sess.close();
    }

    public void testExtend() throws Exception {
        compassGps.index();

        CompassSession sess = compass.openSession();
        CompassTransaction tr = sess.beginTransaction();

        SimpleBase simpleBase = sess.load(SimpleBase.class, 1);
        Assert.assertEquals("value", simpleBase.getValue());
        SimpleExtend simpleExtend = sess.load(SimpleExtend.class, 2);
        Assert.assertEquals("value", simpleExtend.getValue());
        Assert.assertEquals("valueExtended", simpleExtend.getValueExtend());

        CompassHits hits = sess.queryBuilder()
                .queryString("value").toQuery()
                .setSubIndexes(new String[]{"simple1"})
                .hits();
        assertEquals(2, hits.length());


        tr.commit();
        sess.close();
    }
}