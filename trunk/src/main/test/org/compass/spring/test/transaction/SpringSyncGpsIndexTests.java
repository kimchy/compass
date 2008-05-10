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

package org.compass.spring.test.transaction;

import javax.transaction.UserTransaction;

import junit.framework.TestCase;
import org.compass.core.Compass;
import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.config.CompassConfiguration;
import org.compass.core.config.CompassEnvironment;
import org.compass.core.lucene.LuceneEnvironment;
import org.compass.gps.device.MockIndexGpsDevice;
import org.compass.gps.device.MockIndexGpsDeviceObject;
import org.compass.gps.impl.SingleCompassGps;
import org.compass.spring.device.SpringSyncTransactionGpsDeviceWrapper;
import org.compass.spring.transaction.SpringSyncTransactionFactory;
import org.springframework.transaction.jta.JotmFactoryBean;
import org.springframework.transaction.jta.JtaTransactionManager;

/**
 * @author kimchy
 */
public class SpringSyncGpsIndexTests extends TestCase {

    private Compass compass;

    private SingleCompassGps compassGps;

    private MockIndexGpsDevice device;

    private JotmFactoryBean jotmFactoryBean;

    private JtaTransactionManager transactionManager;
    private SpringSyncTransactionGpsDeviceWrapper gpsDevice;

    protected void setUp() throws Exception {

        jotmFactoryBean = new JotmFactoryBean();

        transactionManager = new JtaTransactionManager();
        transactionManager.setUserTransaction((UserTransaction) jotmFactoryBean.getObject());
        transactionManager.afterPropertiesSet();
    }

    protected void tearDown() throws Exception {
        jotmFactoryBean.destroy();
    }

    public void testSimpleIndex() throws Exception {
        SpringSyncTransactionFactory.setTransactionManager(transactionManager);

        CompassConfiguration conf = new CompassConfiguration();
        conf.setSetting(CompassEnvironment.CONNECTION, "target/test-index");
        conf.setSetting(CompassEnvironment.Transaction.FACTORY, SpringSyncTransactionFactory.class.getName());
        conf.setSetting(LuceneEnvironment.Optimizer.SCHEDULE, "false");
        conf.setSetting(LuceneEnvironment.SearchEngineIndex.INDEX_MANAGER_SCHEDULE_INTERVAL, "-1");
        conf.addClass(MockIndexGpsDeviceObject.class);
        compass = conf.buildCompass();

        device = new MockIndexGpsDevice();
        device.setName("test");
        compassGps = new SingleCompassGps(compass);
        gpsDevice = new SpringSyncTransactionGpsDeviceWrapper(device);
        gpsDevice.afterPropertiesSet();
        compassGps.addGpsDevice(gpsDevice);
        compassGps.start();

        compass.getSearchEngineIndexManager().deleteIndex();
        compass.getSearchEngineIndexManager().createIndex();

        assertNoObjects();

        device.add(new Long(1), "testvalue");
        compassGps.index();

        assertExists(new Long(1));

        compassGps.stop();
        compass.close();
    }

    private void assertExists(Long id) {
        CompassSession session = compass.openSession();
        CompassTransaction tr = session.beginTransaction();
        session.load(MockIndexGpsDeviceObject.class, id);
        tr.commit();
        session.close();
    }

    private void assertNoObjects() {
        CompassSession session = compass.openSession();
        CompassTransaction tr = session.beginTransaction();
        assertEquals(0, session.queryBuilder().matchAll().hits().length());
        tr.commit();
        session.close();
    }
}
