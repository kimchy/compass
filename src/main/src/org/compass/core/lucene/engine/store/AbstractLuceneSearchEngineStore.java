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

package org.compass.core.lucene.engine.store;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

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
import org.compass.core.config.CompassSettings;
import org.compass.core.config.ConfigurationException;
import org.compass.core.engine.SearchEngine;
import org.compass.core.engine.SearchEngineException;
import org.compass.core.engine.event.SearchEngineEventManager;
import org.compass.core.lucene.LuceneEnvironment;
import org.compass.core.lucene.engine.LuceneSearchEngineFactory;
import org.compass.core.lucene.engine.LuceneSettings;
import org.compass.core.lucene.engine.store.localcache.LocalDirectoryCacheManager;
import org.compass.core.lucene.engine.store.wrapper.DirectoryWrapperProvider;
import org.compass.core.mapping.CompassMapping;
import org.compass.core.mapping.ResourceMapping;
import org.compass.core.util.ClassUtils;
import org.compass.core.util.StringUtils;

/**
 * @author kimchy
 */
public abstract class AbstractLuceneSearchEngineStore implements LuceneSearchEngineStore {

    protected Log log = LogFactory.getLog(getClass());

    private Map<String, List<String>> aliasesBySubIndex = new HashMap<String, List<String>>();

    private Map<String, List<String>> subIndexesByAlias = new HashMap<String, List<String>>();

    private String[] subIndexes;

    protected LuceneStoreTemplate template;

    protected String connectionString;

    protected String subContext;

    private LuceneSettings luceneSettings;

    private DirectoryWrapperProvider[] directoryWrapperProviders;

    private LocalDirectoryCacheManager localDirectoryCacheManager;

    // holds the directories cache per sub index
    private HashMap<String, Directory> dirs = new HashMap<String, Directory>();

    public AbstractLuceneSearchEngineStore(String connectionString, String subContext) {
        this.connectionString = connectionString;
        this.subContext = subContext;
    }

    public void configure(LuceneSearchEngineFactory searchEngineFactory, CompassSettings settings, CompassMapping mapping) {
        template = new LuceneStoreTemplate(this);

        this.luceneSettings = searchEngineFactory.getLuceneSettings();

        HashSet<String> subIndexesSet = new HashSet<String>();
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

        this.localDirectoryCacheManager = new LocalDirectoryCacheManager(searchEngineFactory);
        localDirectoryCacheManager.configure(settings);
    }

    public void close() {
        localDirectoryCacheManager.close();
        closeDirectories();
        doClose();
    }

    protected void closeDirectories() {
        for (Directory dir : dirs.values()) {
            try {
                dir.close();
            } catch (IOException e) {
                log.debug("Failed to close directory while shutting down, ignoring", e);
            }
        }
        dirs.clear();
    }

    protected void doClose() {

    }

    public void performScheduledTasks() {
        // do nothing
    }

    public void closeDirectory(String subIndex, Directory dir) throws SearchEngineException {
        // do nothing since we cache directories
    }

    public int getNumberOfAliasesBySubIndex(String subIndex) {
        return (aliasesBySubIndex.get(subIndex)).size();
    }

    public Directory getDirectoryBySubIndex(String subIndex, boolean create) throws SearchEngineException {
        Directory dir = dirs.get(subIndex);
        if (dir == null) {
            dir = openDirectoryBySubIndex(subIndex, create);
            dirs.put(subIndex, dir);
            return dir;
        }
        if (create) {
            // in case of create, we need to refresh the cache
            try {
                dir.close();
            } catch (IOException e) {
                log.warn("Failed to close directory", e);
            }
            dir = openDirectoryBySubIndex(subIndex, create);
            dirs.put(subIndex, dir);
        }
        return dir;
    }

    private Directory openDirectoryBySubIndex(String subIndex, boolean create) throws SearchEngineException {
        Directory dir = doOpenDirectoryBySubIndex(subIndex, create);
        if (dir == null) {
            return null;
        }
        String lockFactoryType = luceneSettings.getSettings().getSetting(LuceneEnvironment.LockFactory.TYPE);
        if (lockFactoryType != null) {
            String path = luceneSettings.getSettings().getSetting(LuceneEnvironment.LockFactory.PATH);
            if (path != null) {
                path = StringUtils.replace(path, "#subindex#", subIndex);
            }
            LockFactory lockFactory;
            if (LuceneEnvironment.LockFactory.Type.NATIVE_FS.equalsIgnoreCase(lockFactoryType)) {
                String lockDir = path;
                if (lockDir == null) {
                    lockDir = connectionString + "/" + subIndex;
                }
                try {
                    lockFactory = new NativeFSLockFactory(lockDir);
                } catch (IOException e) {
                    throw new SearchEngineException("Failed to create native fs lock factory with lock dir [" + lockDir + "]", e);
                }
                if (log.isDebugEnabled()) {
                    log.debug("Using native fs lock for sub index [" + subIndex + "] and lock directory [" + lockDir + "]");
                }
            } else if (LuceneEnvironment.LockFactory.Type.SIMPLE_FS.equalsIgnoreCase(lockFactoryType)) {
                String lockDir = path;
                if (lockDir == null) {
                    lockDir = connectionString + "/" + subIndex;
                }
                try {
                    lockFactory = new SimpleFSLockFactory(lockDir);
                } catch (IOException e) {
                    throw new SearchEngineException("Failed to create simple fs lock factory with lock dir [" + lockDir + "]", e);
                }
                if (log.isDebugEnabled()) {
                    log.debug("Using simple fs lock for sub index [" + subIndex + "] and lock directory [" + lockDir + "]");
                }

            } else if (LuceneEnvironment.LockFactory.Type.SINGLE_INSTANCE.equalsIgnoreCase(lockFactoryType)) {
                lockFactory = new SingleInstanceLockFactory();
            } else if (LuceneEnvironment.LockFactory.Type.NO_LOCKING.equalsIgnoreCase(lockFactoryType)) {
                lockFactory = new NoLockFactory();
            } else {
                Object temp;
                try {
                    temp = ClassUtils.forName(lockFactoryType, luceneSettings.getSettings().getClassLoader()).newInstance();
                } catch (Exception e) {
                    throw new SearchEngineException("Failed to create lock type [" + lockFactoryType + "]", e);
                }
                if (temp instanceof LockFactory) {
                    lockFactory = (LockFactory) temp;
                } else if (temp instanceof LockFactoryProvider) {
                    lockFactory = ((LockFactoryProvider) temp).createLockFactory(path, subIndex, luceneSettings.getSettings());
                } else {
                    throw new SearchEngineException("No specific type of lock factory");
                }

                if (lockFactory instanceof CompassConfigurable) {
                    ((CompassConfigurable) lockFactory).configure(luceneSettings.getSettings());
                }
            }
            dir.setLockFactory(lockFactory);
        }
        if (directoryWrapperProviders != null) {
            for (DirectoryWrapperProvider directoryWrapperProvider : directoryWrapperProviders) {
                dir = directoryWrapperProvider.wrap(subIndex, dir);
            }
        }
        return localDirectoryCacheManager.createLocalCache(subIndex, dir);
    }

    protected abstract Directory doOpenDirectoryBySubIndex(String subIndex, boolean create) throws SearchEngineException;

    private void createIndex(final String subIndex) throws SearchEngineException {
        template.executeForSubIndex(subIndex, true, new LuceneStoreCallback() {
            public Object doWithStore(Directory dir) throws IOException {
                IndexWriter indexWriter = new IndexWriter(dir, new StandardAnalyzer(), true);
                indexWriter.close();
                return null;
            }
        });
    }

    protected boolean indexExists(final String subIndex) throws SearchEngineException {
        try {
            return (Boolean) template.executeForSubIndex(subIndex, false,
                    new LuceneStoreCallback() {
                        public Object doWithStore(Directory dir) throws IOException {
                            return indexExists(dir);
                        }
                    });
        } catch (SearchEngineException e) {
            return false;
        }
    }

    protected Boolean indexExists(Directory dir) throws IOException {
        if (!IndexReader.indexExists(dir)) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    protected boolean verifyIndex(final String subIndex) throws SearchEngineException {
        if (!indexExists(subIndex)) {
            createIndex(subIndex);
            return true;
        }
        return false;
    }

    public void createIndex() throws SearchEngineException {
        for (String subIndexe : subIndexes) {
            createIndex(subIndexe);
        }
    }

    public boolean verifyIndex() throws SearchEngineException {
        boolean createdIndex = false;
        for (String subIndexe : subIndexes) {
            if (verifyIndex(subIndexe)) {
                createdIndex = true;
            }
        }
        return createdIndex;
    }

    public boolean indexExists() throws SearchEngineException {
        for (String subIndexe : subIndexes) {
            if (!indexExists(subIndexe)) {
                return false;
            }
        }
        return true;
    }

    public void deleteIndex() throws SearchEngineException {
        closeDirectories();
        doDeleteIndex();
    }

    protected abstract void doDeleteIndex() throws SearchEngineException;

    public String[] getSubIndexes() {
        return subIndexes;
    }

    public String[] calcSubIndexes(String[] subIndexes, String[] aliases) {
        if (aliases == null) {
            if (subIndexes == null) {
                return getSubIndexes();
            }
            return subIndexes;
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

    public boolean isLocked(String subIndex) throws SearchEngineException {
        return (Boolean) template.executeForSubIndex(subIndex, false,
                new LuceneStoreCallback() {
                    public Object doWithStore(Directory dir) throws IOException {
                        return IndexReader.isLocked(dir);
                    }
                });
    }

    public void registerEventListeners(SearchEngine searchEngine, SearchEngineEventManager eventManager) {
        // do nothing here
    }

    public static class CopyFromHolder {

        public boolean createOriginalDirectory = false;

        public Object data;
    }

    public void copyFrom(final LuceneSearchEngineStore searchEngineStore) throws SearchEngineException {
        // clear any possible wrappers
        for (int i = 0; i < getSubIndexes().length; i++) {
            final String subIndex = getSubIndexes()[i];
            template.executeForSubIndex(subIndex, false, new LuceneStoreCallback() {
                public Object doWithStore(Directory dir) throws IOException {
                    if (dir instanceof DirectoryWrapper) {
                        ((DirectoryWrapper) dir).clearWrapper();
                    }
                    return null;
                }
            });
        }
        CopyFromHolder holder = doBeforeCopyFrom();
        final byte[] buffer = new byte[32768];
        final LuceneStoreTemplate srcTemplate = new LuceneStoreTemplate(searchEngineStore);
        try {
            for (int i = 0; i < getSubIndexes().length; i++) {
                final String subIndex = getSubIndexes()[i];
                template.executeForSubIndex(subIndex, holder.createOriginalDirectory, new LuceneStoreCallback() {
                    public Object doWithStore(final Directory dest) {
                        srcTemplate.executeForSubIndex(subIndex, false, new LuceneStoreCallback() {
                            public Object doWithStore(Directory src) throws IOException {
                                LuceneUtils.copy(src, searchEngineStore.getLuceneSettings().isUseCompoundFile(),
                                        dest, luceneSettings.isUseCompoundFile(), buffer);
                                return null;
                            }
                        });
                        return null;
                    }
                });
            }
        } catch (Exception e) {
            doAfterFailedCopyFrom(holder);
            if (e instanceof SearchEngineException) {
                throw (SearchEngineException) e;
            }
            throw new SearchEngineException("Failed to copy from " + searchEngineStore, e);
        }
        doAfterSuccessfulCopyFrom(holder);
    }

    protected CopyFromHolder doBeforeCopyFrom() throws SearchEngineException {
        CopyFromHolder holder = new CopyFromHolder();
        holder.createOriginalDirectory = false;
        return holder;
    }

    protected void doAfterSuccessfulCopyFrom(CopyFromHolder holder) throws SearchEngineException {

    }

    protected void doAfterFailedCopyFrom(CopyFromHolder holder) throws SearchEngineException {

    }

    public LuceneSettings getLuceneSettings() {
        return this.luceneSettings;
    }

    public String toString() {
        return "store [" + connectionString + "][" + subContext + "] sub-indexes [" + StringUtils.arrayToCommaDelimitedString(subIndexes) + "]";
    }
}
