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

package org.compass.gps.device.jpa;

import junit.framework.TestCase;
import org.compass.annotations.config.CompassAnnotationsConfiguration;
import org.compass.core.Compass;
import org.compass.core.config.CompassConfiguration;
import org.compass.gps.impl.SingleCompassGps;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;

/**
 * @author kimchy
 */
public abstract class AbstractJpaGpsDeviceTests extends TestCase {

    protected EntityManagerFactory entityManagerFactory;

    protected Compass compass;

    protected SingleCompassGps compassGps;

    @Override
    protected void setUp() throws Exception {
        entityManagerFactory = doSetUpEntityManagerFactory();
        CompassConfiguration cpConf = new CompassAnnotationsConfiguration()
                .setConnection("target/test-index");
        setUpCoreCompass(cpConf);
        compass = cpConf.buildCompass();
        compass.getSearchEngineIndexManager().deleteIndex();
        compass.getSearchEngineIndexManager().verifyIndex();
        setUpGps();
        setUpGpsDevice();
        compassGps.start();
        setUpDB();
    }

    @Override
    protected void tearDown() throws Exception {
        tearDownDB();
        compassGps.stop();
        compass.close();
        entityManagerFactory.close();
    }

    protected abstract EntityManagerFactory doSetUpEntityManagerFactory();

    protected abstract void setUpCoreCompass(CompassConfiguration conf);

    protected void setUpGps() {
        compassGps = new SingleCompassGps(compass);
    }

    protected void setUpGpsDevice() {
        JpaGpsDevice jpaGpsDevice = new JpaGpsDevice();
        jpaGpsDevice.setName("jdoDevice");
        jpaGpsDevice.setEntityManagerFactory(entityManagerFactory);
        addDeviceSettings(jpaGpsDevice);
        compassGps.addGpsDevice(jpaGpsDevice);
    }

    protected void addDeviceSettings(JpaGpsDevice device) {

    }

    protected void setUpDB() {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        EntityTransaction entityTransaction = entityManager.getTransaction();
        entityTransaction.begin();
        setUpDB(entityManager);
        entityTransaction.commit();
        entityManager.close();
    }

    protected void setUpDB(EntityManager entityManager) {
    }

    protected void tearDownDB() {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        EntityTransaction entityTransaction = entityManager.getTransaction();
        entityTransaction.begin();
        tearDownDB(entityManager);
        entityTransaction.commit();
        entityManager.close();
    }

    protected void tearDownDB(EntityManager entityManager) {
    }
}
