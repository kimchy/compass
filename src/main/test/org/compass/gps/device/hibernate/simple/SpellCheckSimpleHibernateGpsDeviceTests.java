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

package org.compass.gps.device.hibernate.simple;

import org.compass.core.CompassHits;
import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.config.CompassConfiguration;
import org.compass.core.lucene.LuceneEnvironment;

/**
 * A test to verify that spell check works with Gps (Hibernate chosen here)
 *
 * @author kimchy
 */
public class SpellCheckSimpleHibernateGpsDeviceTests extends ScrollableSimpleHibernateGpsDeviceTests {

    protected void setUpCoreCompass(CompassConfiguration conf) {
        super.setUpCoreCompass(conf);
        conf.setSetting(LuceneEnvironment.SpellCheck.ENABLE, "true");
    }

    protected void doTearDown() throws Exception {
        super.doTearDown();
        compass.getSpellCheckManager().deleteIndex();
    }

    public void testSimpleSpellCheck() {
        compassGps.index();

        CompassSession sess = compass.openSession();
        CompassTransaction tr = sess.beginTransaction();

        CompassHits hits = sess.queryBuilder().queryString("valu").toQuery().hits();
        assertEquals(0, hits.length());
        assertNotNull(hits.getQuery());
        assertFalse(hits.getQuery().isSuggested());
        assertNotNull(hits.getSuggestedQuery());
        assertTrue(hits.getSuggestedQuery().isSuggested());
        assertEquals("value", hits.getSuggestedQuery().toString());

        tr.commit();
        sess.close();
    }
}
