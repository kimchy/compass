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

package org.compass.core.lucene.engine.store.localcache;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.store.SingleInstanceLockFactory;
import org.compass.core.CompassException;
import org.compass.core.config.CompassConfigurable;
import org.compass.core.config.CompassEnvironment;
import org.compass.core.config.CompassSettings;
import org.compass.core.engine.SearchEngineException;
import org.compass.core.lucene.LuceneEnvironment;
import org.compass.core.lucene.engine.LuceneSearchEngineFactory;
import org.compass.core.lucene.engine.store.LuceneSearchEngineStoreFactory;
import org.compass.core.lucene.util.LuceneUtils;

/**
 * @author kimchy
 */
public class LocalDirectoryCacheManager implements CompassConfigurable {

    private static final Log log = LogFactory.getLog(LocalDirectoryCacheManager.class);

    private boolean disableLocalCache = false;

    private Map<String, CompassSettings> subIndexLocalCacheGroups;

    private LuceneSearchEngineFactory searchEngineFactory;

    private String subContext;

    public LocalDirectoryCacheManager(LuceneSearchEngineFactory searchEngineFactory) {
        this.searchEngineFactory = searchEngineFactory;
    }

    public LuceneSearchEngineFactory getSearchEngineFactory() {
        return searchEngineFactory;
    }

    public void configure(CompassSettings settings) throws CompassException {
        subContext = settings.getSetting(CompassEnvironment.CONNECTION_SUB_CONTEXT, "index");
        disableLocalCache = settings.getSettingAsBoolean(LuceneEnvironment.LocalCache.DISABLE_LOCAL_CACHE, false);
        this.subIndexLocalCacheGroups = settings.getSettingGroups(LuceneEnvironment.LocalCache.PREFIX);

        // just iterate through this to print out our cache
        for (Map.Entry<String, CompassSettings> entry : subIndexLocalCacheGroups.entrySet()) {
            String connection = entry.getValue().getSetting(LuceneEnvironment.LocalCache.CONNECTION, LuceneSearchEngineStoreFactory.MEM_PREFIX);
            if (log.isDebugEnabled()) {
                log.debug("Local Cache for [" + entry.getKey() + "] configured with connection [" + connection + "]");
            }
        }
    }

    public Directory createLocalCache(String subIndex, Directory dir) throws SearchEngineException {
        if (disableLocalCache) {
            return dir;
        }
        CompassSettings settings = subIndexLocalCacheGroups.get(subIndex);
        if (settings == null) {
            settings = subIndexLocalCacheGroups.get(LuceneEnvironment.LocalCache.DEFAULT_NAME);
            if (settings == null) {
                return dir;
            }
        }
        String connection = settings.getSetting(LuceneEnvironment.LocalCache.CONNECTION, LuceneSearchEngineStoreFactory.MEM_PREFIX);
        Directory localCacheDirectory;
        if (connection.startsWith(LuceneSearchEngineStoreFactory.MEM_PREFIX)) {
            localCacheDirectory = new RAMDirectory();
        } else if (connection.startsWith(LuceneSearchEngineStoreFactory.FILE_PREFIX) ||
                connection.startsWith(LuceneSearchEngineStoreFactory.MMAP_PREFIX) ||
                connection.indexOf("://") == -1) {
            String path;
            if (connection.indexOf("://") != -1) {
                path = connection.substring(LuceneSearchEngineStoreFactory.FILE_PREFIX.length(), connection.length());
            } else {
                path = connection;
            }
            path += "/" + subContext + "/" + subIndex;
            File filePath = new File(path);
            LuceneUtils.deleteDir(filePath);
            if (!filePath.exists()) {
                synchronized (this) {
                    // sync since there is a bug with mkdirs failing on reetrant code
                    boolean created = filePath.mkdirs();
                    if (!created) {
                        throw new SearchEngineException("Failed to create directory for local cache with path [" + path + "]");
                    }
                }
            }
            try {
                localCacheDirectory = FSDirectory.getDirectory(path, new SingleInstanceLockFactory());
            } catch (IOException e) {
                throw new SearchEngineException("Failed to create direcotry with path [" + path + "]", e);
            }
        } else {
            throw new SearchEngineException("Local cache does not supprt the following connection [" + connection + "]");
        }
        return new LocalDirectoryCache(subIndex, dir, localCacheDirectory, this);
    }

    public void close() {
    }
}
