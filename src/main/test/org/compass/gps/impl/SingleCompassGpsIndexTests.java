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

package org.compass.gps.impl;

import java.util.Properties;

import junit.framework.TestCase;
import org.compass.core.Compass;
import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.config.CompassConfiguration;
import org.compass.core.config.CompassEnvironment;
import org.compass.core.lucene.LuceneEnvironment;
import org.compass.gps.device.MockIndexGpsDevice;
import org.compass.gps.device.MockIndexGpsDeviceObject;

/**
 * @author kimchy
 */
public class SingleCompassGpsIndexTests extends TestCase {

    private Compass compass;

    private SingleCompassGps compassGps;

    private MockIndexGpsDevice device;

    public void testSimpleIndex() {
        CompassConfiguration conf = new CompassConfiguration();
        conf.setSetting(CompassEnvironment.CONNECTION, "target/test-index");
        conf.addClass(MockIndexGpsDeviceObject.class);
        conf.getSettings().setBooleanSetting(CompassEnvironment.DEBUG, true);
        compass = conf.buildCompass();

        device = new MockIndexGpsDevice();
        device.setName("test");
        compassGps = new SingleCompassGps(compass);
        compassGps.addGpsDevice(device);
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

    public void testWithPropertiesForSingleComopassGps() {
        CompassConfiguration conf = new CompassConfiguration();
        conf.setSetting(CompassEnvironment.CONNECTION, "target/test-index");
        conf.addClass(MockIndexGpsDeviceObject.class);
        conf.getSettings().setBooleanSetting(CompassEnvironment.DEBUG, true);
        compass = conf.buildCompass();
        compass.getSearchEngineIndexManager().deleteIndex();
        compass.getSearchEngineIndexManager().createIndex();

        device = new MockIndexGpsDevice();
        device.setName("test");
        compassGps = new SingleCompassGps(compass);
        compassGps.addGpsDevice(device);
        Properties props = new Properties();
        props.setProperty(LuceneEnvironment.SearchEngineIndex.MAX_BUFFERED_DOCS, "100");
        compassGps.setIndexSettings(props);
        compassGps.start();

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
