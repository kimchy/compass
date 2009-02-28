package org.compass.gps.device.hibernate.simple.embedded;

import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.gps.device.hibernate.embedded.HibernateHelper;
import org.compass.gps.device.hibernate.simple.ScrollableSimpleHibernateGpsDeviceTests;
import org.compass.gps.device.hibernate.simple.Simple;
import org.hibernate.Session;
import org.hibernate.Transaction;

/**
 * @author kimchy
 */
public class EmbeddedHibernateTests extends ScrollableSimpleHibernateGpsDeviceTests {

    protected String getHiberanteCfgLocation() {
        return "/org/compass/gps/device/hibernate/simple/embedded/hibernate.cfg.xml";
    }

    protected void setUpCompass() {
        compass = HibernateHelper.getCompass(sessionFactory);
    }

    protected void setUpGpsDevice() {
        super.setUpGpsDevice();
        // disable mirroring since we work in embedded mode
        hibernateGpsDevice.setMirrorDataChanges(false);
    }

    public void testMirrorWithRollback() throws Exception {
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

        tr.rollback();
        session.close();

        CompassSession sess = compass.openSession();
        CompassTransaction compassTransaction = sess.beginTransaction();

        simple = sess.get(Simple.class, 4);
        assertNull(simple);

        simple = sess.get(Simple.class, 2);
        assertNotNull(simple);

        simple = sess.load(Simple.class, 1);
        assertEquals("value1", simple.getValue());

        compassTransaction.commit();
        sess.close();
    }
}
