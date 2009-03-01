package org.compass.gps.device.hibernate.cascade.many;

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
public abstract class AbstractCascadeManyTests extends TestCase {
    private SessionFactory sessionFactory;
    private Compass compass;
    private FileHandlerMonitor fileHandlerMonitor;
    private HibernateEventListener hibernateEventListener = null;

    public void setUp() throws Exception {
        Configuration conf = new Configuration().configure("/org/compass/gps/device/hibernate/cascade/many/hibernate.cfg.xml")
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

        HibernateGpsDevice compassGpsDevice = new HibernateGpsDevice();
        compassGpsDevice.setName("hibernate");
        compassGpsDevice.setSessionFactory(sessionFactory);
        compassGpsDevice.setFetchCount(5000);

        SingleCompassGps compassGps = new SingleCompassGps();
        compassGps.setCompass(compass);
        compassGps.setGpsDevices(new CompassGpsDevice[] {
            compassGpsDevice
        });

        compassGps.start();

        SessionFactoryImpl sessionFactoryImpl = (SessionFactoryImpl) sessionFactory;
        EventListeners eventListeners = sessionFactoryImpl.getEventListeners();
        PostInsertEventListener[] listeners = eventListeners.getPostInsertEventListeners();
        for (int i = 0; i < listeners.length; i++) {
            if (listeners[i] instanceof HibernateEventListener) {
                hibernateEventListener = (HibernateEventListener) listeners[i];
                break;
            }
        }
    }

    protected void tearDown() throws Exception {
        compass.close();
        fileHandlerMonitor.verifyNoHandlers();
        sessionFactory.close();

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

    public void testSaveMany() {
        Session s = sessionFactory.openSession();
        Transaction tx = s.beginTransaction();

        User u1 = new User();
        u1.setName("barcho");

        Album a1 = new Album();
        a1.setTitle("the first album");
        a1.setOwner(u1);

        Album a2 = new Album();
        a2.setTitle("the second album");
        a2.setOwner(u1);

        u1.getAlbums().add(a1);
        u1.getAlbums().add(a2);

        assertEquals(0, numIndexed(User.class));
        assertEquals(0, numIndexed(Album.class));

        s.save(u1);

        assertEquals(1, numIndexed(User.class));
        assertEquals(2, numIndexed(Album.class));

        tx.commit();
        s.close();
    }

    public void testUpdateMany() {
        Session s = sessionFactory.openSession();
        Transaction tx = s.beginTransaction();

        User u1 = new User();
        u1.setName("barcho");

        Album a1 = new Album();
        a1.setTitle("the first album");
        a1.setOwner(u1);

        Album a2 = new Album();
        a2.setTitle("the second album");
        a2.setOwner(u1);

        u1.getAlbums().add(a1);
        u1.getAlbums().add(a2);

        assertEquals(0, numIndexed(User.class));
        assertEquals(0, numIndexed(Album.class));

        s.save(u1);

        assertEquals(1, numIndexed(User.class));
        assertEquals(2, numIndexed(Album.class));

        tx.commit();
        s.close();

        s = sessionFactory.openSession();
        tx = s.beginTransaction();

        Album a3 = new Album();
        a3.setTitle("the third album");
        a3.setOwner(u1);

        Album a4 = new Album();
        a4.setTitle("the fourth album");
        a4.setOwner(u1);

        u1.getAlbums().add(a3);
        u1.getAlbums().add(a4);

        s.update(u1);

        assertEquals(1, numIndexed(User.class));
        assertEquals(4, numIndexed(Album.class));

        tx.commit();
        s.close();
    }

    public void testErrorDuringSaveOwnerDoesNotLeakMemory() throws Exception {
        Session s = sessionFactory.openSession();
        Transaction tx = s.beginTransaction();

        User u1 = new User();
        u1.setName("barcho");

        Album a1 = new Album();
        a1.setTitle("the first album");
        a1.setOwner(u1);

        Album a2 = new Album();
        a2.setTitle("the second album");
        a2.setOwner(u1);

        u1.getAlbums().add(a1);
        u1.getAlbums().add(a2);
        u1.throwError = true;

        try {
            s.save(u1);
            fail("should throw error");
        } catch (RuntimeException ex) {
            // good
            tx.rollback();
            s.close();
        }

        Map pendingCreate = (Map) getProperty(hibernateEventListener, "pendingCreate");
        assertTrue(pendingCreate.isEmpty());
        Map pendingSave = (Map) getProperty(hibernateEventListener, "pendingSave");
        assertTrue(pendingSave.isEmpty());
    }

    public void testErrorDuringSaveOwnedDoesNotLeakMemory() throws Exception {
        Session s = sessionFactory.openSession();
        Transaction tx = s.beginTransaction();

        User u1 = new User();
        u1.setName("barcho");

        Album a1 = new Album();
        a1.setTitle("the first album");
        a1.setOwner(u1);

        Album a2 = new Album();
        a2.setTitle("the second album");
        a2.setOwner(u1);
        a2.throwError = true;

        u1.getAlbums().add(a1);
        u1.getAlbums().add(a2);

        try {
            s.save(u1);
            fail("should throw error");
        } catch (RuntimeException ex) {
            // good
            tx.rollback();
            s.close();
        }

        Map pendingCreate = (Map) getProperty(hibernateEventListener, "pendingCreate");
        assertTrue(pendingCreate.isEmpty());
        Map pendingSave = (Map) getProperty(hibernateEventListener, "pendingSave");
        assertTrue(pendingSave.isEmpty());
    }

    public void testErrorDuringUpdateOwnerDoesNotLeakMemory() throws Exception {
        Session s = sessionFactory.openSession();
        Transaction tx = s.beginTransaction();

        User u1 = new User();
        u1.setName("barcho");

        Album a1 = new Album();
        a1.setTitle("the first album");
        a1.setOwner(u1);

        Album a2 = new Album();
        a2.setTitle("the second album");
        a2.setOwner(u1);

        u1.getAlbums().add(a1);
        u1.getAlbums().add(a2);

        s.save(u1);

        tx.commit();
        s.close();

        s = sessionFactory.openSession();
        tx = s.beginTransaction();

        Album a3 = new Album();
        a3.setTitle("the third album");
        a3.setOwner(u1);

        // change a property so hibernate is forced to update this entity
        u1.setName("julio");
        u1.getAlbums().add(a3);
        u1.throwError = true;

        try {
            s.saveOrUpdate(u1);

            tx.commit();
            s.close();

            fail("should throw error");
        } catch (RuntimeException ex) {
            // good
            tx.rollback();
            s.close();
        }

        Map pendingCreate = (Map) getProperty(hibernateEventListener, "pendingCreate");
        assertTrue(pendingCreate.isEmpty());
        Map pendingSave = (Map) getProperty(hibernateEventListener, "pendingSave");
        assertTrue(pendingSave.isEmpty());
    }

    public void testErrorDuringUpdateOwnedDoesNotLeakMemory() throws Exception {
        Session s = sessionFactory.openSession();
        Transaction tx = s.beginTransaction();

        User u1 = new User();
        u1.setName("barcho");

        Album a1 = new Album();
        a1.setTitle("the first album");
        a1.setOwner(u1);

        Album a2 = new Album();
        a2.setTitle("the second album");
        a2.setOwner(u1);

        u1.getAlbums().add(a1);
        u1.getAlbums().add(a2);

        s.save(u1);

        tx.commit();
        s.close();

        s = sessionFactory.openSession();
        tx = s.beginTransaction();

//        u1 = (User) s.get(User.class, u1.getId());
        Album a3 = new Album();
        a3.setTitle("the third album");
        a3.setOwner(u1);
        a3.throwError = true;

        // change a property so hibernate is forced to update this entity
        u1.getAlbums().add(a3);
        u1.setName("julio");

        try {
            s.saveOrUpdate(u1);

            tx.commit();
            s.close();

            fail("should throw error");
        } catch (RuntimeException ex) {
            // good
            tx.rollback();
            s.close();
        }

        Map pendingCreate = (Map) getProperty(hibernateEventListener, "pendingCreate");
        assertTrue(pendingCreate.isEmpty());
        Map pendingSave = (Map) getProperty(hibernateEventListener, "pendingSave");
        assertTrue(pendingSave.isEmpty());
    }

    private Object getProperty(Object object, String propertyName) throws Exception {
        return new FieldInvoker(HibernateEventListener.class, propertyName).prepare().get(object);
    }

    private int numIndexed(Class type) {
        CompassSession session = compass.openSession();
        CompassTransaction tr = session.beginTransaction();
        try {
            return session.queryBuilder().matchAll().setTypes(new Class[] {type}).hits().length();
        } finally {
            tr.commit();
            session.close();
        }
    }

    public abstract String getCompassConfigLocation();
}
