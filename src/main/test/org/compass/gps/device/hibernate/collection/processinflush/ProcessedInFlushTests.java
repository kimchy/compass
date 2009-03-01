/*
 * Copyright 2007 the original author or authors.
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
package org.compass.gps.device.hibernate.collection.processinflush;

import junit.framework.TestCase;
import org.compass.core.Compass;
import org.compass.core.config.CompassConfiguration;
import org.compass.core.config.CompassEnvironment;
import org.compass.core.util.FileHandlerMonitor;
import org.compass.gps.CompassGpsDevice;
import org.compass.gps.device.hibernate.HibernateGpsDevice;
import org.compass.gps.device.hibernate.HibernateSyncTransactionFactory;
import org.compass.gps.impl.SingleCompassGps;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;

/**
 * @author Maurice Nicholson
 */
public class ProcessedInFlushTests extends TestCase {

    private SessionFactory sessionFactory;

    private Compass compass;

    private FileHandlerMonitor fileHandlerMonitor;

    public void setUp() throws Exception {
        Configuration conf = new Configuration().configure("/org/compass/gps/device/hibernate/collection/processinflush/hibernate.cfg.xml")
                .setProperty(Environment.HBM2DDL_AUTO, "create");
        sessionFactory = conf.buildSessionFactory();

        // set the session factory for the Hibernate transcation factory BEFORE we construct the compass instnace
        HibernateSyncTransactionFactory.setSessionFactory(sessionFactory);

        CompassConfiguration cpConf = new CompassConfiguration()
                .configure("/org/compass/gps/device/hibernate/collection/processinflush/compass.cfg.xml");
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

    public void testUpdateWithCollecionOnSecondaryObject() {
        Session s = sessionFactory.openSession();
        Transaction tx = s.beginTransaction();
        tx.begin();

        Bookmark b = new Bookmark();
        b.name = "Cool site";

        Folder f = new Folder();
        f.name = "Hot links";

        f.bookmarks.add(b);
        b.folder = f;

        s.save(f);

        s.flush();
        tx.commit();
        s.close();

        s = sessionFactory.openSession();
        tx = s.beginTransaction();
        tx.begin();

        f = (Folder) s.get(Folder.class, f.id);

        f.name = "Super hot links!!";

        s.saveOrUpdate(f);

        s.flush();
        tx.commit();
        s.close();
    }
}
