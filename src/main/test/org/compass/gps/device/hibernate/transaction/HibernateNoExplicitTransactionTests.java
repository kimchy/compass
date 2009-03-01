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

package org.compass.gps.device.hibernate.transaction;

import junit.framework.TestCase;
import org.compass.core.Compass;
import org.compass.core.CompassSession;
import org.compass.core.config.CompassConfiguration;
import org.compass.core.config.CompassEnvironment;
import org.compass.core.impl.ExistingCompassSession;
import org.compass.core.util.FileHandlerMonitor;
import org.compass.gps.device.hibernate.HibernateSyncTransactionFactory;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;

/**
 * @author kimchy
 */
public class HibernateNoExplicitTransactionTests extends TestCase {

    private Compass compass;

    private FileHandlerMonitor fileHandlerMonitor;

    private SessionFactory sessionFactory;

    protected void setUp() throws Exception {

        Configuration conf = new Configuration().configure("/org/compass/gps/device/hibernate/transaction/hibernate.cfg.xml")
                .setProperty(Environment.HBM2DDL_AUTO, "create");
        sessionFactory = conf.buildSessionFactory();

        // set the session factory for the Hibernate transcation factory BEFORE we construct the compass instnace
        HibernateSyncTransactionFactory.setSessionFactory(sessionFactory);

        CompassConfiguration cpConf = new CompassConfiguration()
                .configure("/org/compass/gps/device/hibernate/transaction/compass.cfg.xml");
        cpConf.getSettings().setBooleanSetting(CompassEnvironment.DEBUG, true);
        compass = cpConf.buildCompass();

        fileHandlerMonitor = FileHandlerMonitor.getFileHandlerMonitor(compass);
        fileHandlerMonitor.verifyNoHandlers();

        compass.getSearchEngineIndexManager().deleteIndex();
        compass.getSearchEngineIndexManager().verifyIndex();

    }

    protected void tearDown() throws Exception {
        compass.close();
        fileHandlerMonitor.verifyNoHandlers();
        sessionFactory.close();
    }

    public void testInnerHibernateManagement() throws Exception {
        CompassSession session = compass.openSession();

        // save a new instance of A
        long id = 1;
        A a = new A();
        a.id = id;
        session.save(a);

        a = session.get(A.class, id);
        assertNotNull(a);

        // check that if we open a new transaction within the current one it
        // will still work
        CompassSession newSession = compass.openSession();
        assertTrue(newSession instanceof ExistingCompassSession);
        assertTrue(session == ((ExistingCompassSession) newSession).getActualSession());
        a = session.get(A.class, id);
        assertNotNull(a);
        // this one should not commit the hibernate transaction since the out
        // controlls it
        newSession.commit();
        session.commit();

        // verify that the instance was saved
        session = compass.openSession();
        a = session.get(A.class, id);
        assertNotNull(a);

        session.commit();
    }

    public void testOuterHibernteManagementWithCommit() throws Exception {
        Session hibernateSession = sessionFactory.getCurrentSession();
        Transaction hibernateTr = hibernateSession.beginTransaction();

        CompassSession session = compass.openSession();
        long id = 1;
        A a = new A();
        a.id = id;
        session.save(a);
        a = session.get(A.class, id);
        assertNotNull(a);
        session.commit();

        CompassSession oldSession = session;
        session = compass.openSession();
        assertTrue(session instanceof ExistingCompassSession);
        assertTrue(oldSession == ((ExistingCompassSession) session).getActualSession());
        a = session.get(A.class, id);
        assertNotNull(a);
        session.commit();

        hibernateTr.commit();

        session = compass.openSession();
        a = session.get(A.class, id);
        assertNotNull(a);
        session.commit();
    }

    public void testOuterUTManagementWithCommitAndNoSessionOrTransactionManagement() throws Exception {
        Session hibernateSession = sessionFactory.getCurrentSession();
        Transaction hibernateTr = hibernateSession.beginTransaction();

        CompassSession session = compass.openSession();
        long id = 1;
        A a = new A();
        a.id = id;
        session.save(a);
        a = session.get(A.class, id);
        assertNotNull(a);

        CompassSession oldSession = session;
        session = compass.openSession();
        assertTrue(session instanceof ExistingCompassSession);
        assertTrue(oldSession == ((ExistingCompassSession) session).getActualSession());
        a = session.get(A.class, id);
        assertNotNull(a);

        hibernateTr.commit();

        // now check that things were committed
        // here we do need explicit session/transaciton mangement
        // just cause we are lazy and want to let Comapss to manage JTA
        session = compass.openSession();
        a = session.get(A.class, id);
        assertNotNull(a);
        session.commit();
    }

    public void testOuterUTManagementWithRollback() throws Exception {
        Session hibernateSession = sessionFactory.getCurrentSession();
        Transaction hibernateTr = hibernateSession.beginTransaction();

        CompassSession session = compass.openSession();
        long id = 1;
        A a = new A();
        a.id = id;
        session.save(a);
        a = session.get(A.class, id);
        assertNotNull(a);
        session.commit();
        session = compass.openSession();
        a = session.get(A.class, id);
        assertNotNull(a);
        session.commit();

        hibernateTr.rollback();

        session = compass.openSession();
        a = session.get(A.class, id);
        assertNull(a);
        session.commit();
    }
}