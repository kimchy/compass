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

package org.compass.core.lucene.engine.store;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.LuceneUtils;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.DirectoryWrapper;
import org.apache.lucene.store.LockFactory;
import org.apache.lucene.store.NativeFSLockFactory;
import org.apache.lucene.store.NoLockFactory;
import org.apache.lucene.store.SimpleFSLockFactory;
import org.apache.lucene.store.SingleInstanceLockFactory;
import org.compass.core.config.CompassConfigurable;
import org.compass.core.config.CompassEnvironment;
import org.compass.core.config.CompassSettings;
import org.compass.core.config.ConfigurationException;
import org.compass.core.engine.SearchEngine;
import org.compass.core.engine.SearchEngineException;
import org.compass.core.engine.event.SearchEngineEventManager;
import org.compass.core.lucene.LuceneEnvironment;
import org.compass.core.lucene.engine.LuceneSearchEngineFactory;
import org.compass.core.lucene.engine.store.localcache.LocalCacheManager;
import org.compass.core.lucene.engine.store.wrapper.DirectoryWrapperProvider;
import org.compass.core.mapping.CompassMapping;
import org.compass.core.mapping.ResourceMapping;
import org.compass.core.util.ClassUtils;
import org.compass.core.util.StringUtils;

/**
 * @author kimchy
 */
public class DefaultLuceneSearchEngineStore implements LuceneSearchEngineStore {

    private static Log log = LogFactory.getLog(DefaultLuceneSearchEngineStore.class);

    private final CompassMapping mapping;

    private final CompassSettings settings;

    private final DirectoryStore directoryStore;

    private final Map<String, List<String>> aliasesBySubIndex = new HashMap<String, List<String>>();

    private final Map<String, List<String>> subIndexesByAlias = new HashMap<String, List<String>>();

    private final String defaultSubContext;

    private final String[] subIndexes;

    private final Set<String> subIndexesSet;

    private final String connectionString;

    private final DirectoryWrapperProvider[] directoryWrapperProviders;

    private final LocalCacheManager localCacheManager;

    private final Map<String, Map<String, Directory>> dirs;

    private final boolean useCompoundFile;

    private final boolean supportsConcurrentOperations;

    private final boolean supportsConcurrentCommits;

    private volatile boolean closed = false;

    public DefaultLuceneSearchEngineStore(LuceneSearchEngineFactory searchEngineFactory, CompassSettings settings, CompassMapping mapping) {
        this.settings = settings;
        this.mapping = mapping;
        this.connectionString = settings.getSetting(CompassEnvironment.CONNECTION);
        this.dirs = new ConcurrentHashMap<String, Map<String, Directory>>();

        this.defaultSubContext = settings.getSetting(CompassEnvironment.CONNECTION_SUB_CONTEXT, "index");

        // setup the directory store
        String connection = settings.getSetting(CompassEnvironment.CONNECTION);
        if (connection.startsWith(RAMDirectoryStore.PROTOCOL)) {
            directoryStore = new RAMDirectoryStore();
        } else if (connection.startsWith(FSDirectoryStore.PROTOCOL)) {
            directoryStore = new FSDirectoryStore();
        } else if (connection.startsWith(MMapDirectoryStore.PROTOCOL)) {
            directoryStore = new MMapDirectoryStore();
        } else if (connection.startsWith(NIOFSDirectoryStore.PROTOCOL)) {
            directoryStore = new NIOFSDirectoryStore();
        } else if (connection.startsWith(JdbcDirectoryStore.PROTOCOL)) {
            directoryStore = new JdbcDirectoryStore();
        } else if (connection.indexOf("://") > -1) {
            String pluggableStore = connection.substring(0, connection.indexOf("://"));
            InputStream is = LuceneSearchEngineStore.class.getResourceAsStream("/META-INF/compass/store-" + pluggableStore + ".properties");
            Properties props;
            try {
                props = new Properties();
                props.load(is);
            } catch (Exception e) {
                try {
                    is.close();
                } catch (Exception e1) {
                    // ignore
                }
                throw new SearchEngineException("Failed to create store [" + connection + "]", e);
            }
            String className = props.getProperty("type");
            try {
                directoryStore = (DirectoryStore) ClassUtils.forName(className, settings.getClassLoader()).newInstance();
            } catch (Exception e) {
                throw new SearchEngineException("Failed to create connection [" + connection + "]", e);
            }
        } else {
            directoryStore = new FSDirectoryStore();
        }
        if (directoryStore instanceof CompassConfigurable) {
            ((CompassConfigurable) directoryStore).configure(settings);
        }

        String useCompoundFileSetting = settings.getSetting(LuceneEnvironment.SearchEngineIndex.USE_COMPOUND_FILE);
        if (useCompoundFileSetting == null) {
            useCompoundFile = directoryStore.suggestedUseCompoundFile();
        } else {
            useCompoundFile = Boolean.valueOf(useCompoundFileSetting);
        }
        if (log.isDebugEnabled()) {
            log.debug("Using compound format [" + useCompoundFile + "]");
        }

        String useConcurrentOperationsSetting = settings.getSetting(LuceneEnvironment.SearchEngineIndex.USE_CONCURRENT_OPERATIONS);
        if (useConcurrentOperationsSetting == null) {
            supportsConcurrentOperations = directoryStore.supportsConcurrentOperations();
        } else {
            supportsConcurrentOperations = Boolean.valueOf(useConcurrentOperationsSetting);
        }
        String useConcurrentCommitsSetting = settings.getSetting(LuceneEnvironment.SearchEngineIndex.USE_CONCURRENT_COMMITS);
        if (useConcurrentCommitsSetting == null) {
            supportsConcurrentCommits = directoryStore.supportsConcurrentCommits();
        } else {
            supportsConcurrentCommits = Boolean.valueOf(useConcurrentCommitsSetting);
        }
        if (log.isDebugEnabled()) {
            log.debug("Support concurrent operations [" + supportsConcurrentOperations + "] and concurrent commits [" + supportsConcurrentCommits + "]");
        }

        // setup sub indexes and aliases
        subIndexesSet = new HashSet<String>();
        for (ResourceMapping resourceMapping : mapping.getRootMappings()) {
            String alias = resourceMapping.getAlias();
            String[] tempSubIndexes = resourceMapping.getSubIndexHash().getSubIndexes();
            for (String subIndex : tempSubIndexes) {
                subIndexesSet.add(subIndex.intern());

                List<String> list = subIndexesByAlias.get(alias);
                if (list == null) {
                    list = new ArrayList<String>();
                    subIndexesByAlias.put(alias, list);
                }
                list.add(subIndex);

                list = aliasesBySubIndex.get(subIndex);
                if (aliasesBySubIndex.get(subIndex) == null) {
                    list = new ArrayList<String>();
                    aliasesBySubIndex.put(subIndex, list);
                }
                list.add(alias);
            }
        }
        subIndexes = subIndexesSet.toArray(new String[subIndexesSet.size()]);

        // set up directory wrapper providers
        DirectoryWrapperProvider[] directoryWrapperProviders = null;
        Map<String, CompassSettings> dwSettingGroups = settings.getSettingGroups(LuceneEnvironment.DirectoryWrapper.PREFIX);
        if (dwSettingGroups.size() > 0) {
            ArrayList<DirectoryWrapperProvider> dws = new ArrayList<DirectoryWrapperProvider>();
            for (Map.Entry<String, CompassSettings> entry : dwSettingGroups.entrySet()) {
                String dwName = entry.getKey();
                if (log.isInfoEnabled()) {
                    log.info("Building directory wrapper [" + dwName + "]");
                }
                CompassSettings dwSettings = entry.getValue();
                String dwType = dwSettings.getSetting(LuceneEnvironment.DirectoryWrapper.TYPE);
                if (dwType == null) {
                    throw new ConfigurationException("Directory wrapper [" + dwName + "] has no type associated with it");
                }
                DirectoryWrapperProvider dw;
                try {
                    dw = (DirectoryWrapperProvider) ClassUtils.forName(dwType, settings.getClassLoader()).newInstance();
                } catch (Exception e) {
                    throw new ConfigurationException("Failed to create directory wrapper [" + dwName + "]", e);
                }
                if (dw instanceof CompassConfigurable) {
                    ((CompassConfigurable) dw).configure(dwSettings);
                }
                dws.add(dw);
            }
            directoryWrapperProviders = dws.toArray(new DirectoryWrapperProvider[dws.size()]);
        }
        this.directoryWrapperProviders = directoryWrapperProviders;

        this.localCacheManager = new LocalCacheManager(searchEngineFactory);
    }

    public void close() {
        if (closed) {
            return;
        }
        closed = true;
        localCacheManager.close();
        closeDirectories();
    }

    private void closeDirectories() {
        for (Map<String, Directory> subIndexsDirs : dirs.values()) {
            synchronized (subIndexsDirs) {
                for (Directory dir : subIndexsDirs.values()) {
                    try {
                        dir.close();
                    } catch (IOException e) {
                        log.debug("Failed to close directory while shutting down, ignoring", e);
                    }
                }
            }
        }
        dirs.clear();
    }

    public void performScheduledTasks() {
        for (Map.Entry<String, Map<String, Directory>> entry : dirs.entrySet()) {
            String subContext = entry.getKey();
            synchronized (entry.getValue()) {
                for (Map.Entry<String, Directory> entry2 : entry.getValue().entrySet()) {
                    String subIndex = entry2.getKey();
                    Directory dir = entry2.getValue();
                    directoryStore.performScheduledTasks(unwrapDir(dir), subContext, subIndex);
                }
            }
        }
    }

    public String[] getAliasesBySubIndex(String subIndex) {
        List<String> aliasesPerSubIndex = aliasesBySubIndex.get(subIndex);
        return aliasesPerSubIndex.toArray(new String[aliasesPerSubIndex.size()]);
    }

    public int getNumberOfAliasesBySubIndex(String subIndex) {
        return (aliasesBySubIndex.get(subIndex)).size();
    }

    public String[] getSubIndexes() {
        return subIndexes;
    }

    public boolean subIndexExists(String subIndex) {
        return subIndexesSet.contains(subIndex);
    }

    public String[] calcSubIndexes(String[] subIndexes, String[] aliases, Class[] types) {
        return internalCalcSubIndexes(subIndexes, aliases, types, false);
    }

    public String[] polyCalcSubIndexes(String[] subIndexes, String[] aliases, Class[] types) {
        return internalCalcSubIndexes(subIndexes, aliases, types, true);
    }

    public String[] internalCalcSubIndexes(String[] subIndexes, String[] aliases, Class[] types, boolean poly) {
        if (aliases == null && types == null) {
            return calcSubIndexes(subIndexes, aliases);
        }
        HashSet<String> aliasesSet = new HashSet<String>();
        if (aliases != null) {
            for (String alias : aliases) {
                ResourceMapping resourceMapping = mapping.getRootMappingByAlias(alias);
                if (resourceMapping == null) {
                    throw new IllegalArgumentException("No root mapping found for alias [" + alias + "]");
                }
                aliasesSet.add(resourceMapping.getAlias());
                if (poly) {
                    aliasesSet.addAll(Arrays.asList(resourceMapping.getExtendingAliases()));
                }
            }
        }
        if (types != null) {
            for (Class type : types) {
                ResourceMapping resourceMapping = mapping.getRootMappingByClass(type);
                if (resourceMapping == null) {
                    throw new IllegalArgumentException("No root mapping found for class [" + type + "]");
                }
                aliasesSet.add(resourceMapping.getAlias());
                if (poly) {
                    aliasesSet.addAll(Arrays.asList(resourceMapping.getExtendingAliases()));
                }
            }
        }
        return calcSubIndexes(subIndexes, aliasesSet.toArray(new String[aliasesSet.size()]));
    }

    public String[] calcSubIndexes(String[] subIndexes, String[] aliases) {
        if (aliases == null) {
            if (subIndexes == null) {
                return getSubIndexes();
            }
            // filter out any duplicates
            HashSet<String> ret = new HashSet<String>();
            ret.addAll(Arrays.asList(subIndexes));
            return ret.toArray(new String[ret.size()]);
        }
        HashSet<String> ret = new HashSet<String>();
        for (String aliase : aliases) {
            List<String> subIndexesList = subIndexesByAlias.get(aliase);
            if (subIndexesList == null) {
                throw new IllegalArgumentException("No sub-index is mapped to alias [" + aliase + "]");
            }
            for (String subIndex : subIndexesList) {
                ret.add(subIndex);
            }
        }
        if (subIndexes != null) {
            ret.addAll(Arrays.asList(subIndexes));
        }
        return ret.toArray(new String[ret.size()]);
    }

    public Directory openDirectory(String subIndex) throws SearchEngineException {
        return openDirectory(defaultSubContext, subIndex);
    }

    public Directory openDirectory(String subContext, String subIndex) throws SearchEngineException {
        Map<String, Directory> subContextDirs = dirs.get(subContext);
        if (subContextDirs == null) {
            subContextDirs = new ConcurrentHashMap<String, Directory>();
            dirs.put(subContext, subContextDirs);
        }
        Directory dir = subContextDirs.get(subIndex);
        if (dir != null) {
            return dir;
        }
        synchronized (subContextDirs) {
            dir = subContextDirs.get(subIndex);
            if (dir != null) {
                return dir;
            }
            dir = directoryStore.open(subContext, subIndex);
            Object lockFactoryType = settings.getSettingAsObject(LuceneEnvironment.LockFactory.TYPE);
            if (lockFactoryType != null) {
                String path = settings.getSetting(LuceneEnvironment.LockFactory.PATH);
                if (path != null) {
                    path = StringUtils.replace(path, "#subindex#", subIndex);
                    path = StringUtils.replace(path, "#subContext#", subContext);
                }
                LockFactory lockFactory;
                if (lockFactoryType instanceof String && LuceneEnvironment.LockFactory.Type.NATIVE_FS.equalsIgnoreCase((String) lockFactoryType)) {
                    String lockDir = path;
                    if (lockDir == null) {
                        if (directoryStore instanceof FSDirectoryStore) {
                            lockDir = ((FSDirectoryStore) directoryStore).buildPath(subContext, subIndex);
                        } else {
                            lockDir = connectionString + "/" + subContext + "/" + subIndex;
                            if (lockDir.startsWith(FSDirectoryStore.PROTOCOL)) {
                                lockDir = lockDir.substring(FSDirectoryStore.PROTOCOL.length());
                            }
                        }
                    }
                    try {
                        lockFactory = new NativeFSLockFactory(lockDir);
                    } catch (IOException e) {
                        throw new SearchEngineException("Failed to create native fs lock factory with lock dir [" + lockDir + "]", e);
                    }
                    if (log.isDebugEnabled()) {
                        log.debug("Using native fs lock for sub index [" + subIndex + "] and lock directory [" + lockDir + "]");
                    }
                } else if (lockFactoryType instanceof String && LuceneEnvironment.LockFactory.Type.SIMPLE_FS.equalsIgnoreCase((String) lockFactoryType)) {
                    String lockDir = path;
                    if (lockDir == null) {
                        if (directoryStore instanceof FSDirectoryStore) {
                            lockDir = ((FSDirectoryStore) directoryStore).buildPath(subContext, subIndex);
                        } else {
                            lockDir = connectionString + "/" + subContext + "/" + subIndex;
                            if (lockDir.startsWith(FSDirectoryStore.PROTOCOL)) {
                                lockDir = lockDir.substring(FSDirectoryStore.PROTOCOL.length());
                            }
                        }
                    }
                    try {
                        lockFactory = new SimpleFSLockFactory(lockDir);
                    } catch (IOException e) {
                        throw new SearchEngineException("Failed to create simple fs lock factory with lock dir [" + lockDir + "]", e);
                    }
                    if (log.isDebugEnabled()) {
                        log.debug("Using simple fs lock for sub index [" + subIndex + "] and lock directory [" + lockDir + "]");
                    }

                } else if (lockFactoryType instanceof String && LuceneEnvironment.LockFactory.Type.SINGLE_INSTANCE.equalsIgnoreCase((String) lockFactoryType)) {
                    lockFactory = new SingleInstanceLockFactory();
                } else if (lockFactoryType instanceof String && LuceneEnvironment.LockFactory.Type.NO_LOCKING.equalsIgnoreCase((String) lockFactoryType)) {
                    lockFactory = new NoLockFactory();
                } else {
                    Object temp;
                    if (lockFactoryType instanceof String) {
                        try {
                            temp = ClassUtils.forName((String) lockFactoryType, settings.getClassLoader()).newInstance();
                        } catch (Exception e) {
                            throw new SearchEngineException("Failed to create lock type [" + lockFactoryType + "]", e);
                        }
                    } else {
                        temp = lockFactoryType;
                    }

                    if (temp instanceof LockFactory) {
                        lockFactory = (LockFactory) temp;
                    } else if (temp instanceof LockFactoryProvider) {
                        lockFactory = ((LockFactoryProvider) temp).createLockFactory(path, subContext, subIndex, settings);
                    } else {
                        throw new SearchEngineException("No specific type of lock factory");
                    }

                    if (lockFactory instanceof CompassConfigurable) {
                        ((CompassConfigurable) lockFactory).configure(settings);
                    }
                }
                dir.setLockFactory(lockFactory);
            }
            if (directoryWrapperProviders != null) {
                for (DirectoryWrapperProvider directoryWrapperProvider : directoryWrapperProviders) {
                    dir = directoryWrapperProvider.wrap(subIndex, dir);
                }
            }
            if (!closed) {
                dir = localCacheManager.createLocalCache(subContext, subIndex, dir);
            }
            subContextDirs.put(subIndex, dir);
        }
        return dir;
    }

    public synchronized boolean indexExists() throws SearchEngineException {
        for (String subIndex : subIndexes) {
            if (!indexExists(subIndex)) {
                return false;
            }
        }
        return true;
    }

    public synchronized boolean indexExists(String subIndex) throws SearchEngineException {
        return indexExists(defaultSubContext, subIndex);
    }

    public synchronized boolean indexExists(String subContext, String subIndex) throws SearchEngineException {
        boolean closeDir = !directoryExists(subContext, subIndex);
        Directory dir = openDirectory(subContext, subIndex);
        Boolean retVal = directoryStore.indexExists(unwrapDir(dir));
        if (retVal != null) {
            return retVal;
        }
        try {
            retVal = IndexReader.indexExists(dir);
        } catch (IOException e) {
            return false;
        }
        if (closeDir) {
            closeDirectory(dir, subContext, subIndex);
        }
        return retVal;
    }

    public synchronized void createIndex() throws SearchEngineException {
        for (String subIndex : subIndexes) {
            createIndex(subIndex);
        }
    }

    public synchronized void createIndex(String subIndex) throws SearchEngineException {
        createIndex(defaultSubContext, subIndex);
    }

    public synchronized void createIndex(String subContext, String subIndex) throws SearchEngineException {
        Directory dir = openDirectory(subContext, subIndex);
        try {
            IndexWriter indexWriter = new IndexWriter(dir, new StandardAnalyzer(), true, IndexWriter.MaxFieldLength.UNLIMITED);
            indexWriter.close();
        } catch (IOException e) {
            throw new SearchEngineException("Failed to create index for sub index [" + subIndex + "]", e);
        }
    }

    public synchronized void deleteIndex() throws SearchEngineException {
        for (String subIndex : subIndexes) {
            deleteIndex(subIndex);
        }
    }

    public synchronized void deleteIndex(String subIndex) throws SearchEngineException {
        deleteIndex(defaultSubContext, subIndex);
    }

    public synchronized void deleteIndex(String subContext, String subIndex) throws SearchEngineException {
        Directory dir = openDirectory(subContext, subIndex);
        directoryStore.deleteIndex(unwrapDir(dir), subContext, subIndex);
        closeDirectory(dir, subContext, subIndex);
    }

    public synchronized boolean verifyIndex() throws SearchEngineException {
        boolean createdIndex = false;
        for (String subIndex : subIndexes) {
            if (verifyIndex(subIndex)) {
                createdIndex = true;
            }
        }
        return createdIndex;
    }

    public synchronized boolean verifyIndex(String subIndex) throws SearchEngineException {
        return verifyIndex(defaultSubContext, subIndex);
    }

    public synchronized boolean verifyIndex(String subContext, String subIndex) throws SearchEngineException {
        if (!indexExists(subContext, subIndex)) {
            createIndex(subContext, subIndex);
            return true;
        }
        return false;
    }

    public synchronized void cleanIndex(String subIndex) throws SearchEngineException {
        cleanIndex(defaultSubContext, subIndex);
    }

    public synchronized void cleanIndex(String subContext, String subIndex) throws SearchEngineException {
        Directory dir = directoryStore.open(subContext, subIndex);

        Directory unwrapDir = unwrapDir(dir);
        directoryStore.cleanIndex(unwrapDir, subContext, subIndex);

        closeDirectory(dir, subContext, subIndex);
        createIndex(subContext, subIndex);
    }


    public synchronized boolean isLocked() throws SearchEngineException {
        for (String subIndex : getSubIndexes()) {
            if (isLocked(subIndex)) {
                return true;
            }
        }
        return false;
    }

    public synchronized boolean isLocked(String subIndex) throws SearchEngineException {
        return isLocked(defaultSubContext, subIndex);
    }

    public synchronized boolean isLocked(String subContext, String subIndex) throws SearchEngineException {
        try {
            return IndexReader.isLocked(openDirectory(subContext, subIndex));
        } catch (IOException e) {
            throw new SearchEngineException("Failed to check if index is locked for sub context [" + subContext + "] and sub index [" + subIndex + "]", e);
        }
    }

    public synchronized void releaseLocks() throws SearchEngineException {
        for (String subIndex : subIndexes) {
            releaseLock(subIndex);
        }
    }

    public synchronized void releaseLock(String subIndex) throws SearchEngineException {
        releaseLock(defaultSubContext, subIndex);
    }

    public synchronized void releaseLock(String subContext, String subIndex) throws SearchEngineException {
        try {
            IndexWriter.unlock(openDirectory(subContext, subIndex));
        } catch (IOException e) {
            throw new SearchEngineException("Failed to unlock index for sub context [" + subContext + "] and sub index [" + subIndex + "]", e);
        }
    }

    public void copyFrom(String subIndex, LuceneSearchEngineStore searchEngineStore) throws SearchEngineException {
        copyFrom(defaultSubContext, subIndex, searchEngineStore);
    }

    public void copyFrom(String subContext, String subIndex, LuceneSearchEngineStore searchEngineStore) throws SearchEngineException {
        // clear any possible wrappers
        Directory dir = openDirectory(subContext, subIndex);
        Directory unwrappedDir = unwrapDir(dir);
        if (dir instanceof DirectoryWrapper) {
            try {
                ((DirectoryWrapper) dir).clearWrapper();
            } catch (IOException e) {
                throw new SearchEngineException("Failed to clear wrapper for sub index [" + subIndex + "]", e);
            }
        }
        CopyFromHolder holder = directoryStore.beforeCopyFrom(subContext, subIndex, unwrappedDir);
        final byte[] buffer = new byte[32768];
        try {
            Directory dest = openDirectory(subContext, subIndex);
            // no need to pass the sub context to the given search engine store, it has its own sub context
            Directory src = searchEngineStore.openDirectory(subIndex);
            LuceneUtils.copy(src, dest, buffer);
            // in case the index does not container anything, create an empty index
            if (!IndexReader.indexExists(dest)) {
                if (log.isDebugEnabled()) {
                    log.debug("Copy From sub context [" + subContext + "] and sub index [" + subIndex + "] does not contain data, creating empty index");
                }
                IndexWriter writer = new IndexWriter(dest, new StandardAnalyzer(), true);
                writer.close();
            }
        } catch (Exception e) {
            directoryStore.afterFailedCopyFrom(subContext, subIndex, holder);
            if (e instanceof SearchEngineException) {
                throw (SearchEngineException) e;
            }
            throw new SearchEngineException("Failed to copy from " + searchEngineStore, e);
        }
        directoryStore.afterSuccessfulCopyFrom(subContext, subIndex, holder);
    }

    public void registerEventListeners(SearchEngine searchEngine, SearchEngineEventManager eventManager) {
        directoryStore.registerEventListeners(searchEngine, eventManager);
    }

    public boolean requiresAsyncTransactionalContext() {
        return directoryStore.requiresAsyncTransactionalContext();
    }

    public boolean supportsConcurrentOperations() {
        return supportsConcurrentOperations;
    }

    public boolean supportsConcurrentCommits() {
        return supportsConcurrentCommits;
    }

    public boolean isUseCompoundFile() {
        return useCompoundFile;
    }

    public String suggestedIndexDeletionPolicy() {
        return directoryStore.suggestedIndexDeletionPolicy();
    }

    public String getDefaultSubContext() {
        return this.defaultSubContext;
    }

    private boolean directoryExists(String subContext, String subIndex) throws SearchEngineException {
        Map<String, Directory> subContextDirs = dirs.get(subContext);
        return subContextDirs != null && subContextDirs.containsKey(subIndex);
    }

    private void closeDirectory(Directory dir, String subContext, String subIndex) throws SearchEngineException {
        directoryStore.closeDirectory(dir, subContext, subIndex);
        Map<String, Directory> subContextDirs = dirs.get(subContext);
        if (subContextDirs != null) {
            subContextDirs.remove(subIndex);
        }
    }

    private Directory unwrapDir(Directory dir) {
        while (dir instanceof DirectoryWrapper) {
            dir = ((DirectoryWrapper) dir).getWrappedDirectory();
        }
        return dir;
    }

    public String toString() {
        return "store [" + connectionString + "][" + defaultSubContext + "] sub-indexes [" + StringUtils.arrayToCommaDelimitedString(subIndexes) + "]";
    }
}
