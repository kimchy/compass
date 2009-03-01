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

package org.compass.gps.device.hibernate.onetoone;

import junit.framework.TestCase;
import org.compass.core.Compass;
import org.compass.core.CompassTemplate;
import org.compass.core.config.CompassConfiguration;
import org.compass.core.config.CompassEnvironment;
import org.compass.core.util.FileHandlerMonitor;
import org.compass.gps.device.hibernate.CompassTransactionInterceptor;
import org.compass.gps.device.hibernate.HibernateGpsDevice;
import org.compass.gps.impl.SingleCompassGps;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;


/**
 */
public class SharedPrimaryKeyAssociationTests extends TestCase {

    private Compass compass;

    private FileHandlerMonitor fileHandlerMonitor;

    private CompassTemplate compassTemplate;

    private SingleCompassGps compassGps;

    private SessionFactory sessionFactory;

    protected void setUp() throws Exception {

        CompassConfiguration cpConf = new CompassConfiguration()
                .configure("/org/compass/gps/device/hibernate/onetoone/compass-nonjta.cfg.xml");
        cpConf.getSettings().setBooleanSetting(CompassEnvironment.DEBUG, true);
        compass = cpConf.buildCompass();

        fileHandlerMonitor = FileHandlerMonitor.getFileHandlerMonitor(compass);
        fileHandlerMonitor.verifyNoHandlers();

        compass.getSearchEngineIndexManager().deleteIndex();
        compass.getSearchEngineIndexManager().verifyIndex();

        compassTemplate = new CompassTemplate(compass);

        compassGps = new SingleCompassGps(compass);

        Configuration conf = new Configuration().configure("/org/compass/gps/device/hibernate/onetoone/hibernate-nonjta.cfg.xml")
                .setProperty(Environment.HBM2DDL_AUTO, "create");
        sessionFactory = conf.buildSessionFactory();

        CompassTransactionInterceptor.injectInterceptor(sessionFactory, new CompassTransactionInterceptor(compass));

        HibernateGpsDevice device = new HibernateGpsDevice();
        device.setSessionFactory(sessionFactory);
        device.setName("hibernateDevice");
        compassGps.addGpsDevice(device);
        compassGps.start();
    }

    public void testSharedPrimaryKeyAssociation() {
        Session s = sessionFactory.openSession();
        Transaction tx = s.beginTransaction();
        User user = new User("username1");

        Address address = new Address(user);
        address.setZipcode("12345");
        s.save(user);
        tx.commit();
        s.close();

        s = sessionFactory.openSession();
        User userProxyWithLazyloadingAddress = (User) s.load(User.class, new Long(1));
        Address addressProxyWithLazyloadingUser = (Address) s.load(Address.class, new Long(1));

        s.close();

        user = (User) compassTemplate.load(User.class, userProxyWithLazyloadingAddress.getId());
        assertEquals("username1", user.getUsername());

        address = (Address) compassTemplate.load(Address.class, addressProxyWithLazyloadingUser.getId());
        assertEquals("12345", address.getZipcode());

        assertEquals(1, compassTemplate.find("username1").length());
        assertEquals(1, compassTemplate.find("12345").length());
    }

    protected void tearDown() throws Exception {
        sessionFactory.close();
        compassGps.stop();
        compass.close();

        fileHandlerMonitor.verifyNoHandlers();
    }

}
