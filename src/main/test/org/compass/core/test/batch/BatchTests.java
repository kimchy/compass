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

package org.compass.core.test.batch;

import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.config.CompassEnvironment;
import org.compass.core.config.CompassSettings;
import org.compass.core.impl.InternalCompassSession;
import org.compass.core.impl.ResourceIdKey;
import org.compass.core.test.AbstractTestCase;

/**
 * 
 * @author kimchy
 * 
 */
public class BatchTests extends AbstractTestCase {

    protected String[] getMappings() {
        return new String[] { "batch/batch.cpm.xml" };
    }

    protected void addSettings(CompassSettings settings) {
        super.addSettings(settings);
        settings.setSetting(CompassEnvironment.Transaction.ISOLATION,
                CompassEnvironment.Transaction.ISOLATION_BATCH_INSERT);
    }

    public void testBatch() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        Long id = new Long(1);
        Cyclic1 cyclic1 = new Cyclic1();
        cyclic1.setId(id);
        cyclic1.setValue("cyclic1");

        Cyclic2 cyclic2 = new Cyclic2();
        cyclic2.setId(id);
        cyclic2.setValue("cyclic2");

        cyclic1.setCyclic2(cyclic2);
        cyclic2.setCyclic1(cyclic1);

        session.create(cyclic2);
        session.create(cyclic1);

        // verify they are not in the first level cache
        ResourceIdKey key = new ResourceIdKey("cyclic1", new Long[] { id });
        Object val = ((InternalCompassSession) session).getFirstLevelCache().get(key);
        assertNull(val);
        key = new ResourceIdKey("cyclic2", new Long[] { id });
        val = ((InternalCompassSession) session).getFirstLevelCache().get(key);
        assertNull(val);

        tr.commit();

        tr = session.beginTransaction();

        try {
            session.find("test");
            fail();
        } catch (Exception e) {

        }

        try {
            session.save(cyclic2);
            fail();
        } catch (Exception e) {

        }

        try {
            session.delete(cyclic2);
            fail();
        } catch (Exception e) {

        }

        try {
            session.get(Cyclic2.class, id);
            fail();
        } catch (Exception e) {

        }

        tr.commit();
    }
}
