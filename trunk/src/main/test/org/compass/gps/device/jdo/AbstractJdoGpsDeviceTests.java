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

import junit.framework.TestCase;
import org.compass.core.Compass;
import org.compass.core.config.CompassConfiguration;
import org.compass.gps.impl.SingleCompassGps;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Transaction;
import java.util.Properties;

/**
 * @author kimchy
 */
public abstract class AbstractJdoGpsDeviceTests extends TestCase {

    protected PersistenceManagerFactory persistenceManagerFactory;

    private Compass compass;

    protected SingleCompassGps compassGps;

    private Product product;

    private Book book;

    protected void setUp() throws Exception {
        Properties properties = new Properties();

        properties.setProperty("javax.jdo.PersistenceManagerFactoryClass", "org.jpox.PersistenceManagerFactoryImpl");
        properties.setProperty("javax.jdo.option.ConnectionDriverName", "org.hsqldb.jdbcDriver");
        properties.setProperty("javax.jdo.option.ConnectionURL", "jdbc:hsqldb:mem:test");
        properties.setProperty("javax.jdo.option.ConnectionUserName", "sa");
        properties.setProperty("javax.jdo.option.ConnectionPassword", "");
        properties.setProperty("org.jpox.autoCreateSchema", "true");
        properties.setProperty("org.jpox.validateTables", "false");
        properties.setProperty("org.jpox.validateConstraints", "false");

        persistenceManagerFactory = JDOHelper.getPersistenceManagerFactory(properties);
        setUpCoreCompass();
        setUpGps();
        setUpGpsDevice();
        compassGps.start();
        setUpDB();
    }

    protected void tearDown() throws Exception {
        tearDownDB();
        compassGps.stop();
        compass.close();
        persistenceManagerFactory.close();
    }

    protected void setUpGpsDevice() {
        JdoGpsDevice jdoGpsDevice = new JdoGpsDevice();
        jdoGpsDevice.setName("jdoDevice");
        jdoGpsDevice.setPersistenceManagerFactory(persistenceManagerFactory);
        compassGps.addGpsDevice(jdoGpsDevice);
    }

    protected void setUpGps() {
        compassGps = new SingleCompassGps(compass);
    }

    protected void setUpCoreCompass() {
        CompassConfiguration cpConf = new CompassConfiguration()
                .configure("/org/compass/gps/device/jdo/compass.cfg.xml");
        compass = cpConf.buildCompass();
        compass.getSearchEngineIndexManager().deleteIndex();
        compass.getSearchEngineIndexManager().verifyIndex();
    }

    protected void setUpDB() throws Exception {
        PersistenceManager pm = persistenceManagerFactory.getPersistenceManager();
        Transaction tx = pm.currentTransaction();
        try {
            tx.begin();
            product = new Product("Sony Discman", "A standard discman from Sony", 49.99);
            book = new Book("Lord of the Rings by Tolkien", "The classic story", 49.99, "JRR Tolkien", "12345678",
                    "MyBooks Factory");
            pm.makePersistent(product);
            pm.makePersistent(book);
            tx.commit();
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            pm.close();
        }
    }

    protected void tearDownDB() throws Exception {
        PersistenceManager pm = persistenceManagerFactory.getPersistenceManager();
        Transaction tx = pm.currentTransaction();
        try {
            tx.begin();
            book = (Book) pm.getObjectById(Book.class, book.getId());
            pm.deletePersistent(book);
            product = (Product) pm.getObjectById(Product.class, product.getId());
            pm.deletePersistent(product);
            tx.commit();
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            pm.close();
        }
    }

}
