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

import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.config.CompassSettings;
import org.compass.core.engine.SearchEngineIndexManager;
import org.compass.core.lucene.LuceneEnvironment;
import org.compass.core.test.AbstractTestCase;

/**
 */
public class SearchEngineIndexManagerTests extends AbstractTestCase {

    protected String[] getMappings() {
        return new String[]{"indexmanager/indexmanager.cpm.xml"};
    }

    protected void addSettings(CompassSettings settings) {
        super.addSettings(settings);
        settings.setSetting(LuceneEnvironment.SearchEngineIndex.INDEX_MANAGER_SCHEDULE_INTERVAL, "0.2s");
    }

    public void testCleanIndex() {
        SearchEngineIndexManager indexManager = getCompass().getSearchEngineIndexManager();

        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();
        A a = new A();
        a.setId(1l);
        a.setValue("test");
        session.save("a1", a);
        tr.commit();
        session.close();

        indexManager.cleanIndex();

        session = openSession();
        tr = session.beginTransaction();
        Object o = session.get("a1", 1);
        assertNull(o);
        tr.commit();
        session.close();
    }

    public void testIsCached() throws Exception {
        SearchEngineIndexManager indexManager = getCompass().getSearchEngineIndexManager();
        assertFalse(indexManager.isCached());
        assertFalse(indexManager.isCached("a1"));
        assertFalse(indexManager.isCached("a2"));

        acquireCache("a1");
        assertTrue(indexManager.isCached());
        assertTrue(indexManager.isCached("a1"));
        assertFalse(indexManager.isCached("a2"));

        acquireCache("a2");
        assertTrue(indexManager.isCached());
        assertTrue(indexManager.isCached("a1"));
        assertTrue(indexManager.isCached("a2"));
    }

    public void testLocalCacheInvalidationBySubIndex() throws Exception {
        SearchEngineIndexManager indexManager = getCompass().getSearchEngineIndexManager();
        acquireCache("a1");
        assertTrue(indexManager.isCached());
        assertTrue(indexManager.isCached("a1"));
        assertFalse(indexManager.isCached("a2"));

        indexManager.clearCache("a2");
        assertTrue(indexManager.isCached());
        assertTrue(indexManager.isCached("a1"));
        assertFalse(indexManager.isCached("a2"));

        indexManager.clearCache("a1");
        assertFalse(indexManager.isCached());
        assertFalse(indexManager.isCached("a1"));
        assertFalse(indexManager.isCached("a2"));
    }

    public void testLocalCacheInvalidation() throws Exception {
        SearchEngineIndexManager indexManager = getCompass().getSearchEngineIndexManager();
        acquireCache("a1");
        acquireCache("a2");
        assertTrue(indexManager.isCached());
        assertTrue(indexManager.isCached("a1"));
        assertTrue(indexManager.isCached("a2"));
        indexManager.clearCache();
        assertFalse(indexManager.isCached());
        assertFalse(indexManager.isCached("a1"));
        assertFalse(indexManager.isCached("a2"));
    }


    public void testGlobalCacheInvalidation() throws Exception {
        SearchEngineIndexManager indexManager = getCompass().getSearchEngineIndexManager();
        acquireCache("a1");
        acquireCache("a2");
        assertTrue(indexManager.isCached());
        assertTrue(indexManager.isCached("a1"));
        assertTrue(indexManager.isCached("a2"));
        Thread.sleep(3000);
        indexManager.notifyAllToClearCache();
        for (int i = 0; i < 30; i++) {
            Thread.sleep(500);
            if (!indexManager.isCached()) {
                break;
            }
        }
        assertFalse(indexManager.isCached());
        assertFalse(indexManager.isCached("a1"));
        assertFalse(indexManager.isCached("a2"));
    }

    private void acquireCache(String alias) {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();
        session.get(alias, new Long(1));
        tr.commit();
        session.close();
    }

}
