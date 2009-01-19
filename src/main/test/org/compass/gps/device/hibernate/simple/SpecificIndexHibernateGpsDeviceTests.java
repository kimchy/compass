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

import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.config.CompassConfiguration;
import org.compass.gps.DefaultIndexPlan;
import org.compass.gps.device.hibernate.HibernateGpsDevice;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;

/**
 * @author kimchy
 */
public class SpecificIndexHibernateGpsDeviceTests extends AbstractHibernateGpsDeviceTests {

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

    /**
     * No mirroring, we want to test the index operation.
     */
    protected void addDeviceSettings(HibernateGpsDevice device) {
        device.setMirrorDataChanges(false);
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

    public void testFullIndex() {
        compassGps.index();

        CompassSession sess = compass.openSession();
        CompassTransaction tr = sess.beginTransaction();

        Simple simple = sess.get(Simple.class, 1);
        assertNotNull(simple);

        SimpleBase simpleBase = sess.get(SimpleBase.class, 1);
        assertNotNull(simpleBase);

        SimpleExtend simpleExtend = sess.get(SimpleExtend.class, 2);
        assertNotNull(simpleExtend);

        tr.commit();
        sess.close();

    }

    public void testIndexOnlySimpleType() throws Exception {
        compassGps.index(new DefaultIndexPlan().setTypes(Simple.class));

        CompassSession sess = compass.openSession();
        CompassTransaction tr = sess.beginTransaction();

        Simple simple = sess.get(Simple.class, 1);
        assertNotNull(simple);

        SimpleBase simpleBase = sess.get(SimpleBase.class, 1);
        assertNull(simpleBase);

        SimpleExtend simpleExtend = sess.get(SimpleExtend.class, 2);
        assertNull(simpleExtend);

        tr.commit();
        sess.close();
    }

    public void testIndexOnlySimpleAlias() throws Exception {
        compassGps.index(new DefaultIndexPlan().setAliases("simple"));

        CompassSession sess = compass.openSession();
        CompassTransaction tr = sess.beginTransaction();

        Simple simple = sess.get(Simple.class, 1);
        assertNotNull(simple);

        SimpleBase simpleBase = sess.get(SimpleBase.class, 1);
        assertNull(simpleBase);

        SimpleExtend simpleExtend = sess.get(SimpleExtend.class, 2);
        assertNull(simpleExtend);

        tr.commit();
        sess.close();
    }

    public void testIndexOnlySimpleSubIndex() throws Exception {
        compassGps.index(new DefaultIndexPlan().setSubIndexes("simple"));

        CompassSession sess = compass.openSession();
        CompassTransaction tr = sess.beginTransaction();

        Simple simple = sess.get(Simple.class, 1);
        assertNotNull(simple);

        SimpleBase simpleBase = sess.get(SimpleBase.class, 1);
        assertNull(simpleBase);

        SimpleExtend simpleExtend = sess.get(SimpleExtend.class, 2);
        assertNull(simpleExtend);

        tr.commit();
        sess.close();
    }


    public void testIndexAndReplace() throws Exception {
        compassGps.index();

        CompassSession sess = compass.openSession();
        CompassTransaction tr = sess.beginTransaction();

        Simple simple = sess.get(Simple.class, 1);
        assertNotNull(simple);

        SimpleBase simpleBase = sess.get(SimpleBase.class, 1);
        assertNotNull(simpleBase);

        SimpleExtend simpleExtend = sess.get(SimpleExtend.class, 2);
        assertNotNull(simpleExtend);

        tr.commit();
        sess.close();

        Session session = sessionFactory.openSession();
        Transaction hibtr = session.beginTransaction();

        // insert a new one
        simple = new Simple();
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

        hibtr.commit();
        session.close();

        sess = compass.openSession();
        tr = sess.beginTransaction();

        simple = sess.get(Simple.class, 4);
        assertNull(simple);

        tr.commit();
        sess.close();

        // we only reindex simple base, so it won't be there yet
        compassGps.index(new DefaultIndexPlan().setTypes(SimpleBase.class));

        sess = compass.openSession();
        tr = sess.beginTransaction();

        simple = sess.get(Simple.class, 4);
        assertNull(simple);

        tr.commit();
        sess.close();

        compassGps.index(new DefaultIndexPlan().setTypes(Simple.class));

        sess = compass.openSession();
        tr = sess.beginTransaction();

        simple = sess.get(Simple.class, 4);
        assertNotNull(simple);

        tr.commit();
        sess.close();
    }
}
