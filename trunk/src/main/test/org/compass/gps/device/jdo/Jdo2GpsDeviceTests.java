/*
 * Copyright 2004-2006 the original author or authors.
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

package org.compass.gps.device.jdo;

import javax.jdo.PersistenceManager;
import javax.jdo.Transaction;

import org.compass.core.CompassHits;
import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;

public class Jdo2GpsDeviceTests extends AbstractJdoGpsDeviceTests {

    protected void setUpGpsDevice() {
        Jdo2GpsDevice jdoGpsDevice = new Jdo2GpsDevice();
        jdoGpsDevice.setName("jdoDevice");
        jdoGpsDevice.setMirrorDataChanges(true);
        jdoGpsDevice.setPersistenceManagerFactory(persistenceManagerFactory);
        compassGps.addGpsDevice(jdoGpsDevice);
    }

    public void testMirrorWithIndex() {
        compassGps.index();

        CompassSession sess = compassGps.getMirrorCompass().openSession();
        CompassTransaction tr = sess.beginTransaction();

        CompassHits hits = sess.find("sony");
        assertEquals(1, hits.getLength());

        tr.commit();

        PersistenceManager pm = persistenceManagerFactory.getPersistenceManager();
        Transaction tx = pm.currentTransaction();
        Product product = new Product("Apple", "A standard ipod from Apple", 49.99);
        try {
            tx.begin();
            pm.makePersistent(product);
            tx.commit();
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            pm.close();
        }

        tr = sess.beginTransaction();

        hits = sess.find("Apple");
        assertEquals(1, hits.getLength());

        tr.commit();

        Object id = null;
        pm = persistenceManagerFactory.getPersistenceManager();
        tx = pm.currentTransaction();
        try {
            tx.begin();
            product.setDescription("applex");
            pm.makePersistent(product);
            id = pm.getObjectId(product);
            tx.commit();
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            pm.close();
        }

        tr = sess.beginTransaction();

        hits = sess.find("applex");
        assertEquals(1, hits.getLength());

        tr.commit();

        pm = persistenceManagerFactory.getPersistenceManager();
        tx = pm.currentTransaction();
        try {
            tx.begin();
            Object obj = pm.getObjectById(id);
            pm.deletePersistent(obj);
            tx.commit();
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            pm.close();
        }

        tr = sess.beginTransaction();

        hits = sess.find("applex");
        assertEquals(0, hits.getLength());

        tr.commit();

    }
}
