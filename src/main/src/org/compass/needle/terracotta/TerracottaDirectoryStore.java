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

package org.compass.needle.terracotta;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.store.Directory;
import org.compass.core.CompassException;
import org.compass.core.config.CompassConfigurable;
import org.compass.core.config.CompassEnvironment;
import org.compass.core.config.CompassSettings;
import org.compass.core.engine.SearchEngineException;
import org.compass.core.lucene.LuceneEnvironment;
import org.compass.core.lucene.engine.store.AbstractDirectoryStore;
import org.compass.core.lucene.engine.store.CopyFromHolder;

/**
 * A Compass direcoty store that will use the {@link org.compass.needle.terracotta.TerracottaDirectory}.
 *
 * @author kimchy
 */
public class TerracottaDirectoryStore extends AbstractDirectoryStore implements CompassConfigurable {

    public static final String PROTOCOL = "tc://";

    /**
     * @see org.compass.needle.terracotta.TerracottaDirectory#DEFAULT_BUFFER_SIZE
     */
    public static final String BUFFER_SIZE_PROP = "compass.engine.store.tc.bufferSize";

    /**
     * @see org.compass.needle.terracotta.TerracottaDirectory#DEFAULT_FLUSH_RATE
     */
    public static final String FLUSH_RATE_PROP = "compass.engine.store.tc.flushRate";

    /**
     * @see org.compass.needle.terracotta.TerracottaDirectory#DEFAULT_CHM_INITIAL_CAPACITY
     */
    public static final String CHM_INITIAL_CAPACITY_PROP = "compass.engine.store.tc.chm.initialCapacity";

    /**
     * @see org.compass.needle.terracotta.TerracottaDirectory#DEFAULT_CHM_LOAD_FACTOR
     */
    public static final String CHM_LOAD_FACTOR_PROP = "compass.engine.store.tc.chm.loadFactor";

    /**
     * @see org.compass.needle.terracotta.TerracottaDirectory#DEFAULT_CHM_CONCURRENCY_LEVEL
     */
    public static final String CHM_CONCURRENCY_LEVEL_PROP = "compass.engine.store.tc.chm.concurrencyLevel";

    private final Map<String, Map<String, Map<String, TerracottaDirectory>>> dirs = new HashMap<String, Map<String, Map<String, TerracottaDirectory>>>();

    private int bufferSize;

    private int flushRate;

    private int chmInitialCapacity;

    private float chmLoadFactor;

    private int chmConcurrencyLevel;

    private transient String indexName;

    public void configure(CompassSettings settings) throws CompassException {
        indexName = settings.getSetting(CompassEnvironment.CONNECTION).substring(PROTOCOL.length());
        bufferSize = settings.getSettingAsInt(BUFFER_SIZE_PROP, TerracottaDirectory.DEFAULT_BUFFER_SIZE);
        flushRate = settings.getSettingAsInt(FLUSH_RATE_PROP, TerracottaDirectory.DEFAULT_FLUSH_RATE);
        chmInitialCapacity = settings.getSettingAsInt(CHM_CONCURRENCY_LEVEL_PROP, TerracottaDirectory.DEFAULT_CHM_INITIAL_CAPACITY);
        chmLoadFactor = settings.getSettingAsFloat(CHM_LOAD_FACTOR_PROP, TerracottaDirectory.DEFAULT_CHM_LOAD_FACTOR);
        chmConcurrencyLevel = settings.getSettingAsInt(CHM_CONCURRENCY_LEVEL_PROP, TerracottaDirectory.DEFAULT_CHM_CONCURRENCY_LEVEL);
        if (log.isDebugEnabled()) {
            log.debug("Terracotta directory store configured with index [" + indexName + "], bufferSize [" + bufferSize + "], and flushRate [" + flushRate + "]");
        }
    }

    public Directory open(String subContext, String subIndex) throws SearchEngineException {
        synchronized (dirs) {
            Map<String, Map<String, TerracottaDirectory>> index = dirs.get(indexName);
            if (index == null) {
                index = new HashMap<String, Map<String, TerracottaDirectory>>();
                dirs.put(indexName, index);
            }
            Map<String, TerracottaDirectory> subIndexDirs = index.get(subContext);
            if (subIndexDirs == null) {
                subIndexDirs = new HashMap<String, TerracottaDirectory>();
                index.put(subContext, subIndexDirs);
            }
            TerracottaDirectory dir = subIndexDirs.get(subIndex);
            if (dir == null) {
                dir = new TerracottaDirectory(bufferSize, flushRate, chmInitialCapacity, chmLoadFactor, chmConcurrencyLevel);
                subIndexDirs.put(subIndex, dir);
            }
            return dir;
        }
    }

    public void cleanIndex(Directory dir, String subContext, String subIndex) throws SearchEngineException {
        deleteIndex(dir, subContext, subIndex);
    }

    public void deleteIndex(Directory dir, String subContext, String subIndex) throws SearchEngineException {
        synchronized (dirs) {
            Map<String, Map<String, TerracottaDirectory>> index = dirs.get(indexName);
            if (index == null) {
                return;
            }
            Map<String, TerracottaDirectory> subIndexDirs = index.get(subContext);
            if (subIndexDirs != null) {
                subIndexDirs.remove(subIndex);
            }
        }
    }

    public CopyFromHolder beforeCopyFrom(String subContext, String subIndex, Directory dir) throws SearchEngineException {
        try {
            String[] files = dir.list();
            for (String file : files) {
                dir.deleteFile(file);
            }
        } catch (IOException e) {
            throw new SearchEngineException("Faield to delete ram directory before copy", e);
        }
        return new CopyFromHolder();
    }

    public String suggestedIndexDeletionPolicy() {
        return LuceneEnvironment.IndexDeletionPolicy.ExpirationTime.NAME;
    }
}
