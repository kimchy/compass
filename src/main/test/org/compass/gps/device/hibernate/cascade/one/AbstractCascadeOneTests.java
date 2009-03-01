package org.compass.gps.device.hibernate.cascade.one;

import java.util.Map;

import junit.framework.TestCase;
import org.compass.core.Compass;
import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.config.CompassConfiguration;
import org.compass.core.config.CompassEnvironment;
import org.compass.core.util.FieldInvoker;
import org.compass.core.util.FileHandlerMonitor;
import org.compass.gps.CompassGpsDevice;
import org.compass.gps.device.hibernate.HibernateGpsDevice;
import org.compass.gps.device.hibernate.HibernateSyncTransactionFactory;
import org.compass.gps.device.hibernate.lifecycle.HibernateEventListener;
import org.compass.gps.impl.SingleCompassGps;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.classic.Session;
import org.hibernate.event.EventListeners;
import org.hibernate.event.PostInsertEventListener;
import org.hibernate.impl.SessionFactoryImpl;

/**
 * @author Maurice Nicholson
 */
public abstract class AbstractCascadeOneTests extends TestCase {

    private SessionFactory sessionFactory;

    private Compass compass;

    private FileHandlerMonitor fileHandlerMonitor;

    private HibernateEventListener hibernateEventListener = null;

    public void setUp() throws Exception {
        Configuration conf = new Configuration().configure("/org/compass/gps/device/hibernate/cascade/one/hibernate.cfg.xml")
                .setProperty(Environment.HBM2DDL_AUTO, "create");
        sessionFactory = conf.buildSessionFactory();

        // set the session factory for the Hibernate transcation factory BEFORE we construct the compass instnace
        HibernateSyncTransactionFactory.setSessionFactory(sessionFactory);

        CompassConfiguration cpConf = new CompassConfiguration()
                .configure(getCompassConfigLocation());
        cpConf.getSettings().setBooleanSetting(CompassEnvironment.DEBUG, true);
        compass = cpConf.buildCompass();

        fileHandlerMonitor = FileHandlerMonitor.getFileHandlerMonitor(compass);
        fileHandlerMonitor.verifyNoHandlers();

        compass.getSearchEngineIndexManager().deleteIndex();
        compass.getSearchEngineIndexManager().verifyIndex();

//        HibernateEntityLifecycleInjector lifecycleInjector = new org.compass.gps.device.hibernate.lifecycle.DefaultHibernateEntityCollectionLifecycleInjector(true);

        HibernateGpsDevice compassGpsDevice = new HibernateGpsDevice();
        compassGpsDevice.setName("hibernate");
        compassGpsDevice.setSessionFactory(sessionFactory);
        compassGpsDevice.setFetchCount(5000);
//        compassGpsDevice.setLifecycleInjector(lifecycleInjector);

        SingleCompassGps compassGps = new SingleCompassGps();
        compassGps.setCompass(compass);
        compassGps.setGpsDevices(new CompassGpsDevice[]{
                compassGpsDevice
        });

        compassGps.start();

        SessionFactoryImpl sessionFactoryImpl = (SessionFactoryImpl) sessionFactory;
        EventListeners eventListeners = sessionFactoryImpl.getEventListeners();
        PostInsertEventListener[] listeners = eventListeners.getPostInsertEventListeners();
        for (PostInsertEventListener listener : listeners) {
            if (listener instanceof HibernateEventListener) {
                hibernateEventListener = (HibernateEventListener) listener;
                break;
            }
        }
    }

    protected void tearDown() throws Exception {
        compass.close();

        fileHandlerMonitor.verifyNoHandlers();

        sessionFactory.close();

        hibernateEventListener = null;

        try {
            compass.getSearchEngineIndexManager().deleteIndex();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (compass.getSpellCheckManager() != null) {
            try {
                compass.getSpellCheckManager().deleteIndex();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void testSave() throws Exception {
        CompassSession session = compass.openSession();
        CompassTransaction tr = session.beginTransaction();

        Session hibSession = sessionFactory.openSession();
        Transaction hibTr = hibSession.beginTransaction();

        Roof r1 = new Roof();
        r1.setName("the roof");

        Foundations f1 = new Foundations();
        f1.setName("foundations");

        House h1 = new House();
        h1.setName("my house");
        h1.setRoof(r1);
        r1.setHouse(h1);
        h1.setFoundations(f1);

        assertEquals(0, session.queryBuilder().matchAll().setTypes(Roof.class).hits().length());
        assertEquals(0, session.queryBuilder().matchAll().setTypes(House.class).hits().length());

        hibSession.save(h1);

        // objects are present in index after save, before commit
        assertEquals(1, session.queryBuilder().matchAll().setTypes(Roof.class).hits().length());
        assertEquals(1, session.queryBuilder().matchAll().setTypes(House.class).hits().length());

        tr.commit();
        session.close();

        hibTr.commit();
        hibSession.close();
    }

    public void testUpdate() {
        CompassSession session = compass.openSession();
        CompassTransaction tr = session.beginTransaction();

        Session hibSession = sessionFactory.openSession();
        Transaction hibTr = hibSession.beginTransaction();

        Roof r1 = new Roof();
        r1.setName("the original roof");

        House h1 = new House();
        h1.setName("my house");
        h1.setRoof(r1);
        r1.setHouse(h1);

        assertEquals(0, session.queryBuilder().matchAll().setTypes(Roof.class).hits().length());
        assertEquals(0, session.queryBuilder().matchAll().setTypes(House.class).hits().length());

        hibSession.save(h1);

        assertEquals(1, session.queryBuilder().matchAll().setTypes(Roof.class).hits().length());
        assertEquals(session.queryBuilder().matchAll().setTypes(House.class).hits().length(), 1);

        tr.commit();
        session.close();

        hibTr.commit();
        hibSession.close();

        session = compass.openSession();
        tr = session.beginTransaction();

        hibSession = sessionFactory.openSession();
        hibTr = hibSession.beginTransaction();

        Foundations f1 = new Foundations();
        f1.setName("foundations");

        Roof r2 = new Roof();
        r2.setName("the new roof");
        r2.setHouse(h1);
        h1.setRoof(r2);
        h1.setFoundations(f1);

        hibSession.update(h1);

        // objects are present in index after update, before commit
        assertEquals(2, session.queryBuilder().matchAll().setTypes(Roof.class).hits().length());
        assertEquals(1, session.queryBuilder().matchAll().setTypes(House.class).hits().length());

        tr.commit();
        session.close();

        hibTr.commit();
        hibSession.close();
    }

    public void testErrorDuringSaveOwnerDoesNotLeakMemory() throws Exception {
        Session hibSession = sessionFactory.openSession();
        Transaction hibTr = hibSession.beginTransaction();

        CompassSession session = compass.openSession();
        CompassTransaction tr = session.beginTransaction();

        Roof r1 = new Roof();
        r1.setName("the roof");

        House h1 = new House();
        h1.throwError = true;
        h1.setName("my house");
        h1.setRoof(r1);
        r1.setHouse(h1);

        Map pendingCreate = (Map) getProperty(hibernateEventListener, "pendingCreate");
        assertTrue(pendingCreate.isEmpty());
        Map pendingSave = (Map) getProperty(hibernateEventListener, "pendingSave");
        assertTrue(pendingSave.isEmpty());

        try {
            hibSession.save(h1);
            fail("should throw error");
        } catch (RuntimeException ex) {
            // good
            tr.rollback();
            session.close();

            hibTr.rollback();
            hibSession.close();
        }

        pendingCreate = (Map) getProperty(hibernateEventListener, "pendingCreate");
        assertTrue(pendingCreate.isEmpty());
        pendingSave = (Map) getProperty(hibernateEventListener, "pendingSave");
        assertTrue(pendingSave.isEmpty());
    }

    public void testErrorDuringSaveOwneeDoesNotLeakMemory() throws Exception {
        Session hibSession = sessionFactory.openSession();
        Transaction hibTr = hibSession.beginTransaction();

        CompassSession session = compass.openSession();
        CompassTransaction tr = session.beginTransaction();

        Roof r1 = new Roof();
        r1.throwError = true;
        r1.setName("the roof");

        House h1 = new House();
        h1.setName("my house");
        h1.setRoof(r1);
        r1.setHouse(h1);

        Map pendingCreate = (Map) getProperty(hibernateEventListener, "pendingCreate");
        assertTrue(pendingCreate.isEmpty());
        Map pendingSave = (Map) getProperty(hibernateEventListener, "pendingSave");
        assertTrue(pendingSave.isEmpty());

        try {
            hibSession.save(h1);
            fail("should throw error");
        } catch (RuntimeException ex) {
            // good
            tr.rollback();
            session.close();

            hibTr.rollback();
            hibSession.close();
        }

        pendingCreate = (Map) getProperty(hibernateEventListener, "pendingCreate");
        assertTrue(pendingCreate.isEmpty());
        pendingSave = (Map) getProperty(hibernateEventListener, "pendingSave");
        assertTrue(pendingSave.isEmpty());
    }

    public void testErrorDuringUpdateOwnerDoesNotLeakMemory() throws Exception {
        Session hibSession = sessionFactory.openSession();
        Transaction hibTr = hibSession.beginTransaction();

        CompassSession session = compass.openSession();
        CompassTransaction tr = session.beginTransaction();

        Roof r1 = new Roof();
        r1.setName("the roof");

        House h1 = new House();
        h1.setName("my house");
        h1.setRoof(r1);
        r1.setHouse(h1);

        hibSession.save(h1);

        tr.commit();
        session.close();

        hibTr.commit();
        hibSession.close();

        hibSession = sessionFactory.openSession();
        hibTr = hibSession.beginTransaction();

        session = compass.openSession();
        tr = session.beginTransaction();

        Roof r2 = new Roof();
        r2.setName("the new roof");
        h1.setRoof(r2);
        r1.setHouse(h1);
        h1.throwError = true;

        try {
            hibSession.update(h1);
            fail("should throw error");
        } catch (RuntimeException ex) {
            // good
            tr.rollback();
            session.close();

            hibTr.rollback();
            hibSession.close();
        }

        Map pendingCreate = (Map) getProperty(hibernateEventListener, "pendingCreate");
        assertTrue(pendingCreate.isEmpty());
        Map pendingSave = (Map) getProperty(hibernateEventListener, "pendingSave");
        assertTrue(pendingSave.isEmpty());
    }

    public void testErrorDuringUpdateOwneeDoesNotLeakMemory() throws Exception {
        Session hibSession = sessionFactory.openSession();
        Transaction hibTr = hibSession.beginTransaction();

        CompassSession session = compass.openSession();
        CompassTransaction tr = session.beginTransaction();

        Roof r1 = new Roof();
        r1.setName("the roof");

        House h1 = new House();
        h1.setName("my house");
        h1.setRoof(r1);
        r1.setHouse(h1);

        hibSession.save(h1);

        tr.commit();
        session.close();

        hibTr.commit();
        hibSession.close();

        hibSession = sessionFactory.openSession();
        hibTr = hibSession.beginTransaction();

        session = compass.openSession();
        tr = session.beginTransaction();

//        h1 = (House) hibSession.get(House.class, h1.getId());
        Roof r2 = new Roof();
        r2.setName("the new roof");
        h1.setRoof(r2);
        r2.setHouse(h1);
        r2.throwError = true;

        try {
            hibSession.update(h1);
            fail("should throw error");
        } catch (RuntimeException ex) {
            // good
            tr.rollback();
            session.close();

            hibTr.rollback();
            hibSession.close();
        }

        Map pendingCreate = (Map) getProperty(hibernateEventListener, "pendingCreate");
        assertTrue(pendingCreate.isEmpty());
        Map pendingSave = (Map) getProperty(hibernateEventListener, "pendingSave");
        assertTrue(pendingSave.isEmpty());
    }

    private Object getProperty(Object object, String propertyName) throws Exception {
        return new FieldInvoker(HibernateEventListener.class, propertyName).prepare().get(object);
    }

    public abstract String getCompassConfigLocation();
}
