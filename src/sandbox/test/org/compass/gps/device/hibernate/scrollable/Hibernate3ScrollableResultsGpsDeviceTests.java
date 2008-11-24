/*
 * Copyright 2004-2008 the original author or authors.
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

package org.compass.gps.device.hibernate.scrollable;

import javax.naming.Context;
import javax.naming.InitialContext;

import junit.framework.TestCase;
import org.compass.core.Compass;
import org.compass.core.CompassDetachedHits;
import org.compass.core.CompassTemplate;
import org.compass.core.Resource;
import org.compass.core.config.CompassConfiguration;
import org.compass.core.config.CompassEnvironment;
import org.compass.gps.device.jdbc.ResultSetResourceMappingResolver;
import org.compass.gps.device.jdbc.mapping.DataColumnToPropertyMapping;
import org.compass.gps.device.jdbc.mapping.IdColumnToPropertyMapping;
import org.compass.gps.device.jdbc.mapping.ResultSetToResourceMapping;
import org.compass.gps.device.jdbc.mapping.VersionColumnMapping;
import org.compass.gps.impl.SingleCompassGps;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.objectweb.jotm.Jotm;

public class Hibernate3ScrollableResultsGpsDeviceTests extends TestCase {

    private Jotm jotm;

    protected Compass compass;

    protected CompassTemplate compassTemplate;

    private Hibernate3ScrollableResultsGpsDevice gpsDevice;

    private SingleCompassGps gps;

    private SessionFactory sessionFactory;

    private Parent parent1;
    private Parent parent2;
    private Parent parent3;
    private Parent parent4;
    private Child child1;
    private Child child2;
    private Child child3;
    private Child child4;
    private Child child5;
    private Child child6;

    protected void setUp() throws Exception {
        super.setUp();

        System.setProperty(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.rmi.registry.RegistryContextFactory");
        System.setProperty(Context.PROVIDER_URL, "rmi://localhost:1099");

        try {
            java.rmi.registry.LocateRegistry.createRegistry(1099);
        } catch (Exception e) {
            // do nothing
        }

        jotm = new Jotm(true, true);
        Context ctx = new InitialContext();
        ctx.rebind("java:comp/UserTransaction", jotm.getUserTransaction());

        Configuration conf = new Configuration().configure("/org/compass/gps/device/hibernate/scrollable/hibernate3.cfg.xml")
                .setProperty(Environment.HBM2DDL_AUTO, "create");
        sessionFactory = conf.buildSessionFactory();

//      set up the initial set of data
        Session s = sessionFactory.openSession();
        Transaction tx = s.beginTransaction();

        parent1 = new Parent(1, "parent first 1", "last 1");
        parent2 = new Parent(2, "parent first 2", "last 2");
        parent3 = new Parent(3, "parent first 3", "last 3");
        parent4 = new Parent(4, "parent first 4", "last 4");
        child1 = new Child(1, parent1, "child first 1 1 ", "last 1 1");
        child2 = new Child(2, parent1, "child first 1 2 ", "last 1 2");
        child3 = new Child(3, parent1, "child first 1 3 ", "last 1 3");
        child4 = new Child(4, parent2, "child first 2 1 ", "last 2 1");
        child5 = new Child(5, parent3, "child first 3 1 ", "last 3 1");
        child6 = new Child(6, parent4, "child first 4 1 ", "last 4 1");

        s.save(parent1);
        s.save(parent2);
        s.save(parent3);
        s.save(parent4);
        s.save(child1);
        s.save(child2);
        s.save(child3);
        s.save(child4);
        s.save(child5);
        s.save(child6);

        tx.commit();
        s.close();

    }

    protected void setUpExactMappingNoMirror() throws Exception {
        // set up the database mappings, since they are used both to generate
        // the resource mappings and configure the gps device
        ResultSetToResourceMapping mapping = new ResultSetToResourceMapping();
        mapping.setAlias("result-set");
        mapping.setSelectQuery("select "
                + "p.id as parent_id, p.firstName as parent_first_name, p.lastName as parent_last_name, "
                + "c.id as child_id, c.firstName as child_first_name, c.lastName as child_last_name "
                + "from Parent p left join p.children as c");
        mapping.addIdMapping(new IdColumnToPropertyMapping("parent_id", "parent_id"));
        mapping.addIdMapping(new IdColumnToPropertyMapping("child_id", "child_id"));
        mapping.addDataMapping(new DataColumnToPropertyMapping("parent_first_name", "parent_first_name"));
        mapping.addDataMapping(new DataColumnToPropertyMapping("parent_first_name", "first_name"));
        mapping.addDataMapping(new DataColumnToPropertyMapping("child_first_name", "child_first_name"));
        mapping.addDataMapping(new DataColumnToPropertyMapping("child_first_name", "first_name"));


        CompassConfiguration conf = new CompassConfiguration().setSetting(CompassEnvironment.CONNECTION,
                "target/test-index");
        conf.addMappingResolver(new ResultSetResourceMappingResolver(mapping, null)); //TODO
        compass = conf.buildCompass();
        compass.getSearchEngineIndexManager().deleteIndex();
        compass.getSearchEngineIndexManager().verifyIndex();

        compassTemplate = new CompassTemplate(compass);

        gpsDevice = new Hibernate3ScrollableResultsGpsDevice();
        gpsDevice.setSessionFactory(sessionFactory);
        gpsDevice.setName("hibernate3ScrollableResultsGpsDevice");
        // setting up no mirroring, even though it should not mirror since we
        // mapped no version columns
        gpsDevice.setMirrorDataChanges(false);
        gpsDevice.addMapping(mapping);

        gps = new SingleCompassGps(compass);
        gps.addGpsDevice(gpsDevice);
        gps.start();
    }

    protected void tearDown() throws Exception {
        sessionFactory.close();
        if (gps != null) {
            gps.stop();
        }
        if (compass != null) {
            compass.close();
        }
        jotm.stop();
        super.tearDown();
    }


    public void testExactMappingNoMirror() throws Exception {
        setUpExactMappingNoMirror();
        gps.index();
        Resource r = compassTemplate.getResource("result-set", new String[]{"1", "1"});
        assertNotNull(r.getProperty("parent_id"));
        assertNotNull(r.getProperty("parent_first_name"));
        assertNotNull(r.getProperty("child_id"));
        assertNotNull(r.getProperty("child_first_name"));
        assertNotNull(r.getProperty("first_name"));
        assertNull(r.getProperty("ID"));
        assertNull(r.getProperty("FIRST_NAME"));
        assertNull(r.getProperty("LAST_NAME"));
        assertNotNull(r);
        r = compassTemplate.getResource("result-set", new String[]{"4", "6"});
        assertNotNull(r);
        CompassDetachedHits hits = compassTemplate.findWithDetach("parent");
        assertEquals(6, hits.getLength());
    }

    public void testUnmappedMappingNoMirror() throws Exception {
        setUpUnmappedMappingNoMirror();
        gps.index();
        Resource r = compassTemplate.getResource("result-set", new String[]{"1", "1"});
        assertNotNull(r);
        assertNotNull(r.getProperty("parent_id"));
        assertNotNull(r.getProperty("parent_first_name"));
        assertNotNull(r.getProperty("child_id"));
        assertNotNull(r.getProperty("child_first_name"));
        assertNotNull(r.getProperty("LAST_NAME"));
        assertNull(r.getProperty("FIRST_NAME"));
        r = compassTemplate.getResource("result-set", new String[]{"4", "6"});
        assertNotNull(r);
        CompassDetachedHits hits = compassTemplate.findWithDetach("parent");
        assertEquals(6, hits.getLength());
    }

    protected void setUpUnmappedMappingNoMirror() throws Exception {
        // set up the database mappings, since they are used both to generate
        // the resource mappings and configure the jdbc gps device
        ResultSetToResourceMapping mapping = new ResultSetToResourceMapping("result-set",
                "from Parent p left join p.children as c");

        mapping.setSelectQuery("select "
                + "p.id as parent_id, p.firstName as parent_first_name, p.lastName as parent_last_name, "
                + "p.version as parent_version, c.id as child_id, c.version as child_version, c.firstName as FIRST_NAME, c.lastName as LAST_NAME "
                + "from Parent p left join p.children as c");
        mapping.setIndexUnMappedColumns(true);
        mapping.addIdMapping(new IdColumnToPropertyMapping(0, "parent_id"));
        mapping.addIdMapping(new IdColumnToPropertyMapping(4, "child_id"));
        mapping.addDataMapping(new DataColumnToPropertyMapping(1, "parent_first_name"));
        mapping.addDataMapping(new DataColumnToPropertyMapping(6, "child_first_name"));

        CompassConfiguration conf = new CompassConfiguration().setSetting(CompassEnvironment.CONNECTION,
                "target/test-index");
        conf.addMappingResolver(new ResultSetResourceMappingResolver(mapping, null)); //XXX
        compass = conf.buildCompass();
        compass.getSearchEngineIndexManager().deleteIndex();
        compass.getSearchEngineIndexManager().verifyIndex();

        compassTemplate = new CompassTemplate(compass);

        gpsDevice = new Hibernate3ScrollableResultsGpsDevice();
        gpsDevice.setSessionFactory(sessionFactory);
        gpsDevice.setName("hibernate3ScrollableResultsGpsDevice");
        // it should not mirror the data since we did not mapped any version
        // columns
        gpsDevice.setMirrorDataChanges(true);
        gpsDevice.addMapping(mapping);

        gps = new SingleCompassGps(compass);
        gps.addGpsDevice(gpsDevice);
        gps.start();
    }

    protected void setUpExactMappingWithMirror() throws Exception {
        // set up the database mappings, since they are used both to generate
        // the resource mappings and configure the jdbc gps device
        ResultSetToResourceMapping mapping = new ResultSetToResourceMapping();
        mapping.setAlias("result-set");
        mapping
                .setSelectQuery("select "
                        + "p.id as parent_id, p.firstName as parent_first_name, p.lastName as parent_last_name, p.version as parent_version, "
                        + "COALESCE(c.id, 0) as child_id, c.firstName as child_first_name, c.lastName as child_last_name, COALESCE(c.version, 0) as child_version "
                        + "from Parent p left join p.children as c");
        mapping
                .setVersionQuery("select p.id as parent_id, COALESCE(c.id, 0) as child_id, p.version as parent_version, COALESCE(c.version, 0) as child_version from Parent p left join p.children as c");
        mapping.addIdMapping(new IdColumnToPropertyMapping("parent_id", "parent_id", "p.id"));
        mapping.addIdMapping(new IdColumnToPropertyMapping("child_id", "child_id", "COALESCE(c.id, 0)"));
        mapping.addDataMapping(new DataColumnToPropertyMapping("parent_first_name", "parent_first_name"));
        mapping.addDataMapping(new DataColumnToPropertyMapping("child_first_name", "child_first_name"));
        mapping.addVersionMapping(new VersionColumnMapping("parent_version"));
        mapping.addVersionMapping(new VersionColumnMapping("child_version"));

        CompassConfiguration conf = new CompassConfiguration().setSetting(CompassEnvironment.CONNECTION,
                "target/testindex");
        conf.addMappingResolver(new ResultSetResourceMappingResolver(mapping, null)); //XXX
        compass = conf.buildCompass();
        compass.getSearchEngineIndexManager().deleteIndex();
        compass.getSearchEngineIndexManager().verifyIndex();

        compassTemplate = new CompassTemplate(compass);

        gpsDevice = new Hibernate3ScrollableResultsGpsDevice();
        gpsDevice.setSessionFactory(sessionFactory);
        gpsDevice.setName("hibernate3ScrollableResultsGpsDevice");
        gpsDevice.setMirrorDataChanges(true);
        gpsDevice.addMapping(mapping);

        gps = new SingleCompassGps(compass);
        gps.addGpsDevice(gpsDevice);
        gps.start();
    }

    public void testExactMappingWithMirror() throws Exception {
        setUpExactMappingWithMirror();
        gps.index();
        Resource r = compassTemplate.loadResource("result-set", new String[]{"1", "1"});
        r = compassTemplate.getResource("result-set", new String[]{"4", "6"});
        assertNotNull(r);
        CompassDetachedHits hits = compassTemplate.findWithDetach("parent");
        assertEquals(6, hits.getLength());

        // test that create works
        Session s = sessionFactory.openSession();
        Transaction tx = s.beginTransaction();

        Parent newParent = new Parent(999, "parent first 999", "last 999");
        s.save(newParent);
        tx.commit();
        s.close();

        r = compassTemplate.getResource("result-set", new String[]{"999", "0"});
        assertNull(r);
        gpsDevice.performMirroring();
        r = compassTemplate.loadResource("result-set", new String[]{"999", "0"});

        // test that update works
        s = sessionFactory.openSession();
        tx = s.beginTransaction();
        Parent p = (Parent) s.get(Parent.class, new Integer(1));
        p.setFirstName("new first name"); //XXX: version is increased automatically?
        tx.commit();
        s.close();

        gpsDevice.performMirroring();
        r = compassTemplate.loadResource("result-set", new String[]{"1", "1"});
        assertEquals("new first name", r.getValue("parent_first_name"));

        // test that delete works
        s = sessionFactory.openSession();
        tx = s.beginTransaction();
        p = (Parent) s.get(Parent.class, new Integer(999));
        s.delete(p);
        tx.commit();
        s.close();

        gpsDevice.performMirroring();
        r = compassTemplate.getResource("result-set", new String[]{"999", "0"});
        assertNull(r);
    }

}
