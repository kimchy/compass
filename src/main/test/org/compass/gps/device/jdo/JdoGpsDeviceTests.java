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

import org.compass.core.CompassHits;
import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;

/**
 * 
 * @author kimchy
 * 
 */
public class JdoGpsDeviceTests extends AbstractJdoGpsDeviceTests {

    public void testIndex() {
        compassGps.index();

        CompassSession sess = compassGps.getMirrorCompass().openSession();
        CompassTransaction tr = sess.beginTransaction();

        CompassHits hits = sess.find("sony");
        assertEquals(1, hits.getLength());

        tr.commit();

    }
}
