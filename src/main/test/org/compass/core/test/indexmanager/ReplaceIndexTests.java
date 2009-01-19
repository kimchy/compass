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

package org.compass.core.test.indexmanager;

import junit.framework.TestCase;
import org.apache.lucene.index.TermFreqVector;
import org.apache.lucene.index.TermPositionVector;
import org.apache.lucene.index.TermVectorOffsetInfo;
import org.compass.core.Compass;
import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.Resource;
import org.compass.core.cache.first.NullFirstLevelCache;
import org.compass.core.config.CompassConfiguration;
import org.compass.core.config.CompassEnvironment;
import org.compass.core.config.CompassSettings;
import org.compass.core.engine.SearchEngineException;
import org.compass.core.engine.SearchEngineIndexManager;
import org.compass.core.lucene.LuceneEnvironment;
import org.compass.core.lucene.util.LuceneHelper;
import org.compass.core.test.AbstractTestCase;

/**
 * @author kimchy
 */
public class ReplaceIndexTests extends TestCase {

    private Compass compass;

    protected String[] getMappings() {
        return new String[]{"indexmanager/indexmanager.cpm.xml"};
    }

    public void testReplaceFSCompundWithFSCompund() throws Exception {
        CompassSettings actualSettings = new CompassSettings().setSetting(CompassEnvironment.CONNECTION,
                "target/test-index").setBooleanSetting(LuceneEnvironment.SearchEngineIndex.USE_COMPOUND_FILE, true);
        CompassSettings fromSettings = new CompassSettings().setSetting(CompassEnvironment.CONNECTION,
                "target/test-tempindex").setBooleanSetting(LuceneEnvironment.SearchEngineIndex.USE_COMPOUND_FILE, true);
        setUpOrigCompass(actualSettings);
        try {
            innerTestReplaceIndex(fromSettings);
        } finally {
            tearDownOrigCompass();
        }
    }

    public void testReplaceFSCompundWithFSUnCompound() throws Exception {
        CompassSettings actualSettings = new CompassSettings().setSetting(CompassEnvironment.CONNECTION,
                "target/test-index").setBooleanSetting(LuceneEnvironment.SearchEngineIndex.USE_COMPOUND_FILE, true);
        CompassSettings fromSettings = new CompassSettings().setSetting(CompassEnvironment.CONNECTION,
                "target/test-index-temp").setBooleanSetting(LuceneEnvironment.SearchEngineIndex.USE_COMPOUND_FILE, false);
        setUpOrigCompass(actualSettings);
        try {
            innerTestReplaceIndex(fromSettings);
        } finally {
            tearDownOrigCompass();
        }
    }

    public void testReplaceFSUnCompundWithFSUnCompound() throws Exception {
        CompassSettings actualSettings = new CompassSettings().setSetting(CompassEnvironment.CONNECTION,
                "target/test-index").setBooleanSetting(LuceneEnvironment.SearchEngineIndex.USE_COMPOUND_FILE, false);
        CompassSettings fromSettings = new CompassSettings().setSetting(CompassEnvironment.CONNECTION,
                "target/test-index-temp").setBooleanSetting(LuceneEnvironment.SearchEngineIndex.USE_COMPOUND_FILE, false);
        setUpOrigCompass(actualSettings);
        try {
            innerTestReplaceIndex(fromSettings);
        } finally {
            tearDownOrigCompass();
        }
    }

    public void testReplaceFSUnCompundWithFSCompound() throws Exception {
        CompassSettings actualSettings = new CompassSettings().setSetting(CompassEnvironment.CONNECTION,
                "target/test-index").setBooleanSetting(LuceneEnvironment.SearchEngineIndex.USE_COMPOUND_FILE, false);
        CompassSettings fromSettings = new CompassSettings().setSetting(CompassEnvironment.CONNECTION,
                "target/test-index-temp").setBooleanSetting(LuceneEnvironment.SearchEngineIndex.USE_COMPOUND_FILE, true);
        setUpOrigCompass(actualSettings);
        try {
            innerTestReplaceIndex(fromSettings);
        } finally {
            tearDownOrigCompass();
        }
    }

    public void testReplaceFSWithRAM() throws Exception {
        CompassSettings actualSettings = new CompassSettings().setSetting(CompassEnvironment.CONNECTION,
                "target/test-index");
        CompassSettings fromSettings = new CompassSettings().setSetting(CompassEnvironment.CONNECTION,
                "ram://target/test-index-temp");
        setUpOrigCompass(actualSettings);
        try {
            innerTestReplaceIndex(fromSettings);
        } finally {
            tearDownOrigCompass();
        }
    }

    public void testReplaceRAMWithRAM() throws Exception {
        CompassSettings actualSettings = new CompassSettings().setSetting(CompassEnvironment.CONNECTION,
                "ram://target/test-index");
        CompassSettings fromSettings = new CompassSettings().setSetting(CompassEnvironment.CONNECTION,
                "ram://target/test-index-temp");
        setUpOrigCompass(actualSettings);
        try {
            innerTestReplaceIndex(fromSettings);
        } finally {
            tearDownOrigCompass();
        }
    }

    public void testReplaceJdbcWithFS() throws Exception {
        CompassSettings actualSettings = new CompassSettings().setSetting(CompassEnvironment.CONNECTION,
                "jdbc://jdbc:hsqldb:mem:test").setSetting(LuceneEnvironment.JdbcStore.DIALECT,
                "org.apache.lucene.store.jdbc.dialect.HSQLDialect").setSetting(
                LuceneEnvironment.JdbcStore.Connection.DRIVER_CLASS, "org.hsqldb.jdbcDriver").setSetting(
                LuceneEnvironment.JdbcStore.Connection.USERNAME, "sa").setSetting(
                LuceneEnvironment.JdbcStore.Connection.PASSWORD, "");
        CompassSettings fromSettings = new CompassSettings().setSetting(CompassEnvironment.CONNECTION,
                "target/test-index-temp");
        setUpOrigCompass(actualSettings);
        try {
            innerTestReplaceIndex(fromSettings);
        } finally {
            tearDownOrigCompass();
        }
    }

    public void testReplaceJdbcWithJdbc() throws Exception {
        CompassSettings actualSettings = new CompassSettings().setSetting(CompassEnvironment.CONNECTION,
                "jdbc://jdbc:hsqldb:mem:test1").setSetting(LuceneEnvironment.JdbcStore.DIALECT,
                "org.apache.lucene.store.jdbc.dialect.HSQLDialect").setSetting(
                LuceneEnvironment.JdbcStore.Connection.DRIVER_CLASS, "org.hsqldb.jdbcDriver").setSetting(
                LuceneEnvironment.JdbcStore.Connection.USERNAME, "sa").setSetting(
                LuceneEnvironment.JdbcStore.Connection.PASSWORD, "");
        CompassSettings fromSettings = new CompassSettings().setSetting(CompassEnvironment.CONNECTION,
                "jdbc://jdbc:hsqldb:mem:test2").setSetting(LuceneEnvironment.JdbcStore.DIALECT,
                "org.apache.lucene.store.jdbc.dialect.HSQLDialect").setSetting(
                LuceneEnvironment.JdbcStore.Connection.DRIVER_CLASS, "org.hsqldb.jdbcDriver").setSetting(
                LuceneEnvironment.JdbcStore.Connection.USERNAME, "sa").setSetting(
                LuceneEnvironment.JdbcStore.Connection.PASSWORD, "");
        setUpOrigCompass(actualSettings);
        try {
            innerTestReplaceIndex(fromSettings);
        } finally {
            tearDownOrigCompass();
        }
    }

    public void testReplaceFSWithJdbc() throws Exception {
        CompassSettings actualSettings = new CompassSettings().setSetting(CompassEnvironment.CONNECTION,
                "target/test-index");
        CompassSettings fromSettings = new CompassSettings().setSetting(CompassEnvironment.CONNECTION,
                "jdbc://jdbc:hsqldb:mem:test").setSetting(LuceneEnvironment.JdbcStore.DIALECT,
                "org.apache.lucene.store.jdbc.dialect.HSQLDialect").setSetting(
                LuceneEnvironment.JdbcStore.Connection.DRIVER_CLASS, "org.hsqldb.jdbcDriver").setSetting(
                LuceneEnvironment.JdbcStore.Connection.USERNAME, "sa").setSetting(
                LuceneEnvironment.JdbcStore.Connection.PASSWORD, "");
        setUpOrigCompass(actualSettings);
        try {
            innerTestReplaceIndex(fromSettings);
        } finally {
            tearDownOrigCompass();
        }
    }

    private void setUpOrigCompass(CompassSettings actualSettings) {
        CompassConfiguration conf = new CompassConfiguration()
                .configure("/org/compass/core/test/compass.cfg.xml");
        String[] mappings = getMappings();
        for (int i = 0; i < mappings.length; i++) {
            conf.addResource("org/compass/core/test/" + mappings[i], AbstractTestCase.class.getClassLoader());
        }
        conf.getSettings().setSetting(CompassEnvironment.Cache.FirstLevel.TYPE, NullFirstLevelCache.class.getName());
        conf.getSettings().addSettings(actualSettings);
        compass = conf.buildCompass();
        try {
            compass.getSearchEngineIndexManager().deleteIndex();
        } catch (Exception e) {
            e.printStackTrace();
        }
        compass.getSearchEngineIndexManager().verifyIndex();
    }

    private void tearDownOrigCompass() {
        compass.close();
        try {
            compass.getSearchEngineIndexManager().deleteIndex();
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    private void innerTestReplaceIndex(CompassSettings fromSettings) throws Exception {
        // first create some data in the current index
        // this will aquire a read cache on the index
        CompassSession session = compass.openSession();
        CompassTransaction tr = session.beginTransaction();
        A a = new A();
        a.setId(new Long(1));
        a.setValue("first test string");
        session.save("a1", a);
        session.save("a2", a);
        tr.commit();
        session.close();

        // check that we have the date
        session = compass.openSession();
        tr = session.beginTransaction();
        a = (A) session.get("a1", new Long(1));
        assertNotNull(a);
        a = (A) session.get("a1", new Long(2));
        assertNull(a);
        a = (A) session.get("a2", new Long(1));
        assertNotNull(a);
        a = (A) session.get("a2", new Long(2));
        assertNull(a);
        tr.commit();
        session.close();

        // create some other data in a different index
        final Compass tempCompass = compass.clone(fromSettings);
        tempCompass.getSearchEngineIndexManager().deleteIndex();
        tempCompass.getSearchEngineIndexManager().verifyIndex();

        SearchEngineIndexManager indexManager = compass.getSearchEngineIndexManager();
        indexManager.replaceIndex(tempCompass.getSearchEngineIndexManager(),
                new SearchEngineIndexManager.ReplaceIndexCallback() {
                    public void buildIndexIfNeeded() throws SearchEngineException {
                        CompassSession session = tempCompass.openSession();
                        CompassTransaction tr = session.beginTransaction();
                        A a = new A();
                        a.setId(new Long(2));
                        a.setValue("first test string");
                        session.save("a1", a);
                        session.save("a2", a);
                        tr.commit();
                        session.close();
                    }
                });

        tempCompass.close();
        tempCompass.getSearchEngineIndexManager().deleteIndex();

        // see if the index was replaced
        session = compass.openSession();
        tr = session.beginTransaction();
        a = (A) session.get("a1", new Long(1));
        assertNull(a);
        a = (A) session.get("a1", new Long(2));
        assertNotNull(a);
        innerTestTermVectorYesWithPostionsAndOffsets(session, "a1");

        a = (A) session.get("a2", new Long(1));
        assertNull(a);
        a = (A) session.get("a2", new Long(2));
        assertNotNull(a);
        innerTestTermVectorYesWithPostionsAndOffsets(session, "a2");

        tr.commit();
        session.close();
    }

    private void innerTestTermVectorYesWithPostionsAndOffsets(CompassSession session, String subIndex) {
        Long id = new Long(2);
        A a = (A) session.load(subIndex, id);
        assertEquals("first test string", a.getValue());
        Resource r = session.loadResource(subIndex, id);
        assertNotNull(r.getValue("mvalue1"));
        assertEquals(true, r.getProperty("mvalue1").isStored());
        assertEquals(true, r.getProperty("mvalue1").isIndexed());
        assertEquals(true, r.getProperty("mvalue1").isTermVectorStored());

        TermFreqVector termInfoVector = LuceneHelper.getTermFreqVector(session, r, "mvalue1");
        assertEquals("mvalue1", termInfoVector.getField());
        String[] terms = termInfoVector.getTerms();
        assertEquals(3, terms.length);
        assertEquals("first", terms[0]);
        assertEquals("string", terms[1]);
        assertEquals("test", terms[2]);

        int[] freqs = termInfoVector.getTermFrequencies();
        assertEquals(3, freqs.length);

        int[] positions = ((TermPositionVector) termInfoVector).getTermPositions(0);
        assertNotNull(positions);
        assertEquals(1, positions.length);

        TermVectorOffsetInfo[] offsets = ((TermPositionVector) termInfoVector).getOffsets(0);
        assertNotNull(offsets);
        assertEquals(1, offsets.length);

        TermFreqVector[] termInfoVectors = LuceneHelper.getTermFreqVectors(session, r);
        assertEquals(1, termInfoVectors.length);
    }

}
