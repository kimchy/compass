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

package org.compass.core.lucene.engine.store.localcache;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockFactory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.store.SingleInstanceLockFactory;
import org.compass.core.config.CompassSettings;
import org.compass.core.engine.SearchEngineException;
import org.compass.core.lucene.LuceneEnvironment;
import org.compass.core.lucene.engine.LuceneSearchEngineFactory;
import org.compass.core.lucene.engine.store.FSDirectoryStore;
import org.compass.core.lucene.engine.store.MMapDirectoryStore;
import org.compass.core.lucene.engine.store.NIOFSDirectoryStore;
import org.compass.core.lucene.engine.store.RAMDirectoryStore;
import org.compass.core.util.FileSystemUtils;

/**
 * @author kimchy
 */
public class LocalCacheManager {

    private static final Log log = LogFactory.getLog(LocalCacheManager.class);

    private final boolean disableLocalCache;

    private final Map<String, CompassSettings> subIndexLocalCacheGroups;

    private final LuceneSearchEngineFactory searchEngineFactory;

    private final LockFactory lockFactory = new SingleInstanceLockFactory();

    public LocalCacheManager(LuceneSearchEngineFactory searchEngineFactory) {
        this.searchEngineFactory = searchEngineFactory;

        disableLocalCache = searchEngineFactory.getSettings().getSettingAsBoolean(LuceneEnvironment.LocalCache.DISABLE_LOCAL_CACHE, false);

        this.subIndexLocalCacheGroups = searchEngineFactory.getSettings().getSettingGroups(LuceneEnvironment.LocalCache.PREFIX);

        // just iterate through this to print out our cache
        for (Map.Entry<String, CompassSettings> entry : subIndexLocalCacheGroups.entrySet()) {
            String connection = entry.getValue().getSetting(LuceneEnvironment.LocalCache.CONNECTION, RAMDirectoryStore.PROTOCOL);
            if (log.isDebugEnabled()) {
                log.debug("Local Cache for [" + entry.getKey() + "] configured with connection [" + connection + "]");
            }
        }
    }

    public LuceneSearchEngineFactory getSearchEngineFactory() {
        return searchEngineFactory;
    }

    public Directory createLocalCache(String subContext, String subIndex, Directory dir) throws SearchEngineException {
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
        String connection = settings.getSetting(LuceneEnvironment.LocalCache.CONNECTION, RAMDirectoryStore.PROTOCOL);
        Directory localCacheDirectory;
        if (connection.startsWith("memory://")) {
            String connectionString = connection.substring("memory://".length());
            return new MemoryDirectoryCache(connectionString, dir, this);
        } else if (connection.startsWith(RAMDirectoryStore.PROTOCOL)) {
            localCacheDirectory = new RAMDirectory();
        } else if (connection.startsWith(FSDirectoryStore.PROTOCOL) ||
                connection.startsWith(MMapDirectoryStore.PROTOCOL) ||
                connection.startsWith(NIOFSDirectoryStore.PROTOCOL) ||
                connection.indexOf("://") == -1) {
            String path;
            if (connection.indexOf("://") != -1) {
                if (connection.startsWith(FSDirectoryStore.PROTOCOL)) {
                    path = connection.substring(FSDirectoryStore.PROTOCOL.length());
                } else if (connection.startsWith(MMapDirectoryStore.PROTOCOL)) {
                    path = connection.substring(MMapDirectoryStore.PROTOCOL.length());
                } else if (connection.startsWith(NIOFSDirectoryStore.PROTOCOL)) {
                    path = connection.substring(NIOFSDirectoryStore.PROTOCOL.length());
                } else {
                    throw new RuntimeException("Internal error in Compass, should not happen");
                }
            } else {
                path = connection;
            }
            path += "/" + subContext + "/" + subIndex;
            File filePath = new File(path);
            FileSystemUtils.deleteRecursively(filePath);
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
                localCacheDirectory = FSDirectory.getDirectory(path, lockFactory);
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
