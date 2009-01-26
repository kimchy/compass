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

package org.compass.core.test.optimizer;

import java.io.IOException;

import org.apache.lucene.index.LuceneSubIndexInfo;
import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.config.CompassSettings;
import org.compass.core.lucene.LuceneEnvironment;
import org.compass.core.lucene.engine.optimizer.DefaultLuceneSearchEngineOptimizer;

public class DefaultOptimizerTests extends AbstractOptimizerTests {

    protected void addSettings(CompassSettings settings) {
        super.addSettings(settings);
        settings.setSetting(LuceneEnvironment.Optimizer.TYPE, DefaultLuceneSearchEngineOptimizer.class.getName());
        settings.setBooleanSetting(LuceneEnvironment.Optimizer.SCHEDULE, false);
        settings.setIntSetting(LuceneEnvironment.Optimizer.MAX_NUMBER_OF_SEGMENTS, 3);
        settings.setIntSetting(LuceneEnvironment.SearchEngineIndex.CACHE_INTERVAL_INVALIDATION, 0);
    }

    public void testOptimizerWithIndexCache() throws Exception {

        addData(0, 1);
        addData(1, 2);
        addData(2, 3);
        addData(3, 4);
        addData(4, 5);
        addData(5, 6);
        addData(6, 7);
        addData(7, 8);

        assertData(0, 8);

        getCompass().getSearchEngineOptimizer().optimize();

        CompassSession session = openSession();
        LuceneSubIndexInfo infos = LuceneSubIndexInfo.getIndexInfo("a", session);
        assertEquals(3, infos.size());
        session.close();

        assertData(0, 8);
    }

    public void testForceOptimizer() throws Exception {

        addData(0, 1);
        addData(1, 2);

        assertData(0, 2);

        CompassSession session = openSession();
        LuceneSubIndexInfo infos = LuceneSubIndexInfo.getIndexInfo("a", session);
        assertEquals(2, infos.size());
        session.close();

        getCompass().getSearchEngineOptimizer().optimize();

        session = openSession();
        infos = LuceneSubIndexInfo.getIndexInfo("a", session);
        assertEquals(2, infos.size());
        session.close();

        getCompass().getSearchEngineOptimizer().optimize(1);

        session = openSession();
        infos = LuceneSubIndexInfo.getIndexInfo("a", session);
        assertEquals(1, infos.size());
        session.close();

        assertData(0, 2);
    }


    public void testOptimizerMergeFactorSingleAdds() throws IOException {
        addData(0, 1);

        addData(1, 2);

        addData(2, 3);

        getCompass().getSearchEngineOptimizer().optimize();

        CompassSession session = openSession();
        LuceneSubIndexInfo infos = LuceneSubIndexInfo.getIndexInfo("a", session);
        assertEquals(3, infos.size());
        session.close();

        session = openSession();
        CompassTransaction tr = session.beginTransaction();
        A a = session.load(A.class, (long) 0);
        assertNotNull(a);
        tr.commit();
        session.close();

    }

    public void testOptimizerWithBigFirstSegment() throws IOException {
        addData(0, 20);
        addData(20, 21);
        addData(21, 22);
        getCompass().getSearchEngineOptimizer().optimize();
        CompassSession session = openSession();
        LuceneSubIndexInfo infos = LuceneSubIndexInfo.getIndexInfo("a", session);
        session.close();
        assertEquals(3, infos.size());
        assertEquals(20, infos.info(0).docCount());
        assertEquals(1, infos.info(1).docCount());


        session = openSession();
        CompassTransaction tr = session.beginTransaction();
        A a = (A) session.load(A.class, new Long(0));
        assertNotNull(a);
        a = (A) session.load(A.class, new Long(21));
        assertNotNull(a);
        tr.commit();
        session.close();
    }

    public void testCamelCaseSegmentsLeftWithTwoSegments() throws IOException {
        addData(0, 10);
        addData(10, 11);
        addData(11, 15);
        getCompass().getSearchEngineOptimizer().optimize();
        CompassSession session = openSession();
        LuceneSubIndexInfo infos = LuceneSubIndexInfo.getIndexInfo("a", session);
        session.close();
        assertEquals(3, infos.size());
        assertEquals(10, infos.info(0).docCount());
        assertEquals(1, infos.info(1).docCount());
        assertEquals(4, infos.info(2).docCount());

        session = openSession();
        CompassTransaction tr = session.beginTransaction();
        A a = session.load(A.class, 0);
        assertNotNull(a);
        a = session.load(A.class, 14);
        assertNotNull(a);
        tr.commit();
        session.close();
    }

    public void testCamelCaseSegmentsLeftWithOneSegment() throws IOException {
        addData(0, 10);
        addData(10, 11);
        addData(11, 25);
        getCompass().getSearchEngineOptimizer().optimize();
        CompassSession session = openSession();
        LuceneSubIndexInfo infos = LuceneSubIndexInfo.getIndexInfo("a", session);
        session.close();
        assertEquals(3, infos.size());
        assertEquals(10, infos.info(0).docCount());

        session = openSession();
        CompassTransaction tr = session.beginTransaction();
        A a = (A) session.load(A.class, new Long(0));
        assertNotNull(a);
        a = (A) session.load(A.class, new Long(24));
        assertNotNull(a);
        tr.commit();
        session.close();
    }
}
