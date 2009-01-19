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

package org.compass.core.test.metadata;

import java.text.SimpleDateFormat;
import java.util.Date;

import junit.framework.TestCase;
import org.compass.core.Compass;
import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.Property;
import org.compass.core.Resource;
import org.compass.core.config.CompassConfiguration;

public class MetaDataTests extends TestCase {

    private Compass compass;

    protected void setUp() throws Exception {
        super.setUp();
        CompassConfiguration conf = new CompassConfiguration()
                .configure("/org/compass/core/test/metadata/compass.cfg.xml");
        compass = conf.buildCompass();
        compass.getSearchEngineIndexManager().deleteIndex();
        compass.getSearchEngineIndexManager().verifyIndex();
    }

    protected void tearDown() throws Exception {
        compass.close();
        compass.getSearchEngineIndexManager().deleteIndex();
        super.tearDown();
    }

    public void testMD() {
        CompassSession sess = compass.openSession();
        CompassTransaction tr = sess.beginTransaction();

        Long id = new Long(1);
        Date date = new Date();
        A a = new A();
        a.setId(id);
        a.setValue("string value");
        a.setDateValue(date);

        sess.save(a);

        Resource r = sess.loadResource(A.class, id);
        assertEquals("TestAlias", r.getAlias());
        Property p = r.getProperty("testMD1");
        assertNotNull(p);
        assertEquals("string value", p.getStringValue());
        p = r.getProperty("TestMD2");
        assertNotNull(p);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        assertEquals(sdf.format(date), p.getStringValue());
        Property[] pArr = r.getProperties("TestMD3");
        assertNotNull(pArr);
        assertEquals(2, pArr.length);

        tr.commit();
    }
}
