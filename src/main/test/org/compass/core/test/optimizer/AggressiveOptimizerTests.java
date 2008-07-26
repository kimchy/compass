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

package org.compass.core.test.optimizer;

import java.io.IOException;

import org.apache.lucene.index.LuceneSubIndexInfo;
import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.config.CompassSettings;
import org.compass.core.lucene.LuceneEnvironment;
import org.compass.core.lucene.engine.optimizer.AggressiveOptimizer;

/**
 * @author kimchy
 */
public class AggressiveOptimizerTests extends AbstractOptimizerTests {

    protected void addSettings(CompassSettings settings) {
        super.addSettings(settings);
        settings.setSetting(LuceneEnvironment.Optimizer.TYPE, AggressiveOptimizer.class.getName());
        settings.setBooleanSetting(LuceneEnvironment.Optimizer.SCHEDULE, false);
        settings.setSetting(LuceneEnvironment.Optimizer.Aggressive.MERGE_FACTOR, "3");
        settings.setIntSetting(LuceneEnvironment.SearchEngineIndex.CACHE_INTERVAL_INVALIDATION, -1);
    }

    public void testOptimizer() throws IOException {

        addData(0, 1);

        CompassSession session = openSession();
        LuceneSubIndexInfo infos = LuceneSubIndexInfo.getIndexInfo("a", session);
        assertEquals(1, infos.size());
        session.close();

        addData(1, 2);

        session = openSession();
        infos = LuceneSubIndexInfo.getIndexInfo("a", session);
        assertEquals(2, infos.size());
        session.close();

        addData(2, 3);

        getCompass().getSearchEngineOptimizer().optimize();

        session = openSession();
        infos = LuceneSubIndexInfo.getIndexInfo("a", session);
        assertEquals(1, infos.size());
        session.close();

        session = openSession();
        CompassTransaction tr = session.beginTransaction();

        A a = session.load(A.class, 2);
        assertNotNull(a);

        tr.commit();
        session.close();
    }

    public void testForceOptimizer() throws IOException {

        addData(0, 1);

        CompassSession session = openSession();
        LuceneSubIndexInfo infos = LuceneSubIndexInfo.getIndexInfo("a", session);
        assertEquals(1, infos.size());
        session.close();

        addData(1, 2);

        session = openSession();
        infos = LuceneSubIndexInfo.getIndexInfo("a", session);
        assertEquals(2, infos.size());
        session.close();

        getCompass().getSearchEngineOptimizer().optimize();

        session = openSession();
        infos = LuceneSubIndexInfo.getIndexInfo("a", session);
        assertEquals(2, infos.size());
        session.close();

        getCompass().getSearchEngineOptimizer().forceOptimize();

        session = openSession();
        infos = LuceneSubIndexInfo.getIndexInfo("a", session);
        assertEquals(1, infos.size());
        session.close();
    }

    public void testOptimizerWithIndexCache() throws Exception {

        addData(0, 1);
        assertData(0, 1);
        addData(1, 2);
        addData(2, 3);
        addData(3, 4);
        addData(4, 5);
        addData(5, 6);
        addData(6, 7);
        assertData(6, 7);
        addData(7, 8);
        assertData(0, 8);

        getCompass().getSearchEngineOptimizer().optimize();

        CompassSession session = openSession();
        LuceneSubIndexInfo infos = LuceneSubIndexInfo.getIndexInfo("a", session);
        assertEquals(1, infos.size());
        LuceneSubIndexInfo.LuceneSegmentInfo segmentInfo = infos.info(0);
        assertEquals("_8", segmentInfo.name());
        session.close();

        assertData(0, 8);
    }
}
