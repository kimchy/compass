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
import java.util.Iterator;
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

    private Map aliasesBySubIndex = new HashMap();

    private Map subIndexesByAlias = new HashMap();

    private String[] subIndexes;

    protected LuceneStoreTemplate template;

    protected String connectionString;

    protected String subContext;

    private LuceneSettings luceneSettings;

    private DirectoryWrapperProvider[] directoryWrapperProviders;

    private LocalDirectoryCacheManager localDirectoryCacheManager;

    // holds the directories cache per sub index
    private HashMap dirs = new HashMap();

    public AbstractLuceneSearchEngineStore(String connectionString, String subContext) {
        this.connectionString = connectionString;
        this.subContext = subContext;
    }

    public void configure(LuceneSearchEngineFactory searchEngineFactory, CompassSettings settings, CompassMapping mapping) {
        template = new LuceneStoreTemplate(this);

        this.luceneSettings = searchEngineFactory.getLuceneSettings();

        HashSet subIndexesSet = new HashSet();
        ResourceMapping[] rootMappings = mapping.getRootMappings();
        for (int i = 0; i < rootMappings.length; i++) {
            ResourceMapping resourceMapping = rootMappings[i];
            String alias = resourceMapping.getAlias();
            String[] tempSubIndexes = resourceMapping.getSubIndexHash().getSubIndexes();
            for (int j = 0; j < tempSubIndexes.length; j++) {
                String subIndex = tempSubIndexes[j];
                subIndexesSet.add(subIndex.intern());

                ArrayList list = (ArrayList) subIndexesByAlias.get(alias);
                if (list == null) {
                    list = new ArrayList();
                    subIndexesByAlias.put(alias, list);
                }
                list.add(subIndex);

                list = (ArrayList) aliasesBySubIndex.get(subIndex);
                if (aliasesBySubIndex.get(subIndex) == null) {
                    list = new ArrayList();
                    aliasesBySubIndex.put(subIndex, list);
                }
                list.add(alias);
            }
        }
        subIndexes = (String[]) subIndexesSet.toArray(new String[subIndexesSet.size()]);

        // set up directory wrapper providers
        Map dwSettingGroups = settings.getSettingGroups(LuceneEnvironment.DirectoryWrapper.PREFIX);
        if (dwSettingGroups.size() > 0) {
            ArrayList dws = new ArrayList();
            for (Iterator it = dwSettingGroups.entrySet().iterator(); it.hasNext();) {
                Map.Entry entry = (Map.Entry) it.next();
                String dwName = (String) entry.getKey();
                if (log.isInfoEnabled()) {
                    log.info("Building directory wrapper [" + dwName + "]");
                }
                CompassSettings dwSettings = (CompassSettings) entry.getValue();
                String dwType = dwSettings.getSetting(LuceneEnvironment.DirectoryWrapper.TYPE);
                if (dwType == null) {
                    throw new ConfigurationException("Directory wrapper [" + dwName + "] has no type associated with it");
                }
                DirectoryWrapperProvider dw;
                try {
                    dw = (DirectoryWrapperProvider) ClassUtils.forName(dwType).newInstance();
                } catch (Exception e) {
                    throw new ConfigurationException("Failed to create directory wrapper [" + dwName + "]", e);
                }
                if (dw instanceof CompassConfigurable) {
                    ((CompassConfigurable) dw).configure(dwSettings);
                }
                dws.add(dw);
            }
            directoryWrapperProviders = (DirectoryWrapperProvider[]) dws.toArray(new DirectoryWrapperProvider[dws.size()]);
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
        for (Iterator it = dirs.values().iterator(); it.hasNext();) {
            Directory dir = (Directory) it.next();
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
        return ((ArrayList) aliasesBySubIndex.get(subIndex)).size();
    }

    public Directory getDirectoryBySubIndex(String subIndex, boolean create) throws SearchEngineException {
        Directory dir = (Directory) dirs.get(subIndex);
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
                    temp = ClassUtils.forName(lockFactoryType).newInstance();
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
            for (int i = 0; i < directoryWrapperProviders.length; i++) {
                dir = directoryWrapperProviders[i].wrap(subIndex, dir);
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
            Boolean retVal = (Boolean) template.executeForSubIndex(subIndex, false,
                    new LuceneStoreCallback() {
                        public Object doWithStore(Directory dir) throws IOException {
                            return indexExists(dir);
                        }
                    });
            return retVal.booleanValue();
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
        for (int i = 0; i < subIndexes.length; i++) {
            createIndex(subIndexes[i]);
        }
    }

    public boolean verifyIndex() throws SearchEngineException {
        boolean createdIndex = false;
        for (int i = 0; i < subIndexes.length; i++) {
            if (verifyIndex(subIndexes[i])) {
                createdIndex = true;
            }
        }
        return createdIndex;
    }

    public boolean indexExists() throws SearchEngineException {
        for (int i = 0; i < subIndexes.length; i++) {
            if (!indexExists(subIndexes[i])) {
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
        HashSet ret = new HashSet();
        for (int i = 0; i < aliases.length; i++) {
            ArrayList subIndexesList = (ArrayList) subIndexesByAlias.get(aliases[i]);
            if (subIndexesList == null) {
                throw new IllegalArgumentException("No sub-index is mapped to alias [" + aliases[i] + "]");
            }
            for (int j = 0; j < subIndexesList.size(); j++) {
                ret.add(subIndexesList.get(j));
            }
        }
        if (subIndexes != null) {
            ret.addAll(Arrays.asList(subIndexes));
        }
        return (String[]) ret.toArray(new String[ret.size()]);
    }

    public boolean isLocked(String subIndex) throws SearchEngineException {
        Boolean retVal = (Boolean) template.executeForSubIndex(subIndex, false,
                new LuceneStoreCallback() {
                    public Object doWithStore(Directory dir) throws IOException {
                        return Boolean.valueOf(IndexReader.isLocked(dir));
                    }
                });
        return retVal.booleanValue();
    }

    public void registerEventListeners(SearchEngine searchEngine, SearchEngineEventManager eventManager) {
        // do nothing here
    }

    protected class CopyFromHolder {

        boolean createOriginalDirectory = false;

        Object data;
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
