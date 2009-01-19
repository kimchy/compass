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

package org.compass.core.test.config.scan;

import org.compass.core.Compass;
import org.compass.core.config.CompassConfiguration;
import org.compass.core.spi.InternalCompass;
import org.compass.core.test.AbstractTestCase;

/**
 * @author kimchy
 */
public class ScanConfigTests extends AbstractTestCase {

    protected String[] getMappings() {
        return new String[0];
    }

    public void testSimpleScan() throws Exception {
        CompassConfiguration conf = buildConf();
        conf.addScan("org/compass/core/test/config/scan");
        Compass compass = conf.buildCompass();

        assertNotNull(((InternalCompass) compass).getMapping().getRootMappingByClass(A.class));
        assertNotNull(((InternalCompass) compass).getMapping().getRootMappingByClass(B.class));
        assertNotNull(((InternalCompass) compass).getMapping().getRootMappingByClass(C.class));

        compass.close();
    }

    public void testMatcherScan() throws Exception {
        CompassConfiguration conf = buildConf();
        conf.addScan("org/compass/core/test", "config/sca*/A**");
        Compass compass = conf.buildCompass();

        assertNotNull(((InternalCompass) compass).getMapping().getRootMappingByClass(A.class));
        assertNull(((InternalCompass) compass).getMapping().getRootMappingByClass(B.class));
        assertNull(((InternalCompass) compass).getMapping().getRootMappingByClass(C.class));

        compass.close();
    }

    public void testDTDConfiguration() throws Exception {
        CompassConfiguration conf = buildConf();
        conf.configure("/org/compass/core/test/config/scan/compass-dtd.cfg.xml");
        Compass compass = conf.buildCompass();

        assertNull(((InternalCompass) compass).getMapping().getRootMappingByClass(A.class));
        assertNotNull(((InternalCompass) compass).getMapping().getRootMappingByClass(B.class));
        assertNull(((InternalCompass) compass).getMapping().getRootMappingByClass(C.class));

        compass.close();
    }

    public void testXSDConfiguration() throws Exception {
        CompassConfiguration conf = buildConf();
        conf.configure("/org/compass/core/test/config/scan/compass-xsd.cfg.xml");
        Compass compass = conf.buildCompass();

        assertNotNull(((InternalCompass) compass).getMapping().getRootMappingByClass(A.class));
        assertNull(((InternalCompass) compass).getMapping().getRootMappingByClass(B.class));
        assertNull(((InternalCompass) compass).getMapping().getRootMappingByClass(C.class));

        compass.close();
    }
}
