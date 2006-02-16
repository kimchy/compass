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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.LuceneUtils;
import org.apache.lucene.store.Directory;
import org.compass.core.config.CompassEnvironment;
import org.compass.core.config.CompassSettings;
import org.compass.core.engine.SearchEngine;
import org.compass.core.engine.SearchEngineException;
import org.compass.core.engine.event.SearchEngineEventManager;
import org.compass.core.lucene.engine.LuceneSearchEngineFactory;
import org.compass.core.lucene.engine.LuceneSettings;
import org.compass.core.mapping.CompassMapping;
import org.compass.core.mapping.ResourceMapping;

/**
 * @author kimchy
 */
public abstract class AbstractLuceneSearchEngineStore implements LuceneSearchEngineStore {

    protected Log log = LogFactory.getLog(getClass());

    private Map aliasBySubIndex = new HashMap();

    private Map subIndexByAlias = new HashMap();

    private String[] subIndexes;

    protected LuceneStoreTemplate template;

    private String connectionString;

    private LuceneSettings luceneSettings;

    public void configure(LuceneSearchEngineFactory searchEngineFactory, CompassSettings settings, CompassMapping mapping) {
        connectionString = settings.getSetting(CompassEnvironment.CONNECTION);
        template = new LuceneStoreTemplate(this);

        this.luceneSettings = searchEngineFactory.getLuceneSettings();

        HashSet subIndexesSet = new HashSet();
        ResourceMapping[] rootMappings = mapping.getRootMappings();
        for (int i = 0; i < rootMappings.length; i++) {
            ResourceMapping resourceMapping = rootMappings[i];
            String subIndex = resourceMapping.getSubIndex();
            String alias = resourceMapping.getAlias();
            aliasBySubIndex.put(alias, subIndex);
            subIndexesSet.add(subIndex);
            ArrayList list = (ArrayList) subIndexByAlias.get(subIndex);
            if (subIndexByAlias.get(subIndex) == null) {
                list = new ArrayList();
                subIndexByAlias.put(subIndex, list);
            }
            list.add(alias);
        }
        subIndexes = (String[]) subIndexesSet.toArray(new String[subIndexesSet.size()]);
    }

    public void close() {
        doClose();
    }

    protected void doClose() {

    }

    public void performScheduledTasks() {
        // do nothing
    }

    public void closeDirectory(Directory dir) throws SearchEngineException {
        try {
            if (dir != null) {
                dir.close();
            }
        } catch (IOException e) {
            throw new SearchEngineException("Failed to close directory store", e);
        }
    }

    public String getSubIndexForAlias(String alias) {
        return (String) aliasBySubIndex.get(alias);
    }

    public int getNumberOfAliasesByAlias(String alias) {
        return getNumberOfAliasesBySubIndex(getSubIndexForAlias(alias));
    }

    public int getNumberOfAliasesBySubIndex(String subIndex) {
        return ((ArrayList) subIndexByAlias.get(subIndex)).size();
    }

    public Directory getDirectoryByAlias(String alias, boolean create) throws SearchEngineException {
        return doGetDirectoryForPath(getSubIndexForAlias(alias), create);
    }

    public Directory getDirectoryBySubIndex(String subIndex, boolean create) throws SearchEngineException {
        return doGetDirectoryForPath(subIndex, create);
    }

    protected abstract Directory doGetDirectoryForPath(String path, boolean create) throws SearchEngineException;

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
            String subIndex = getSubIndexForAlias(aliases[i]);
            if (subIndex == null) {
                throw new IllegalArgumentException("No sub-index is mapped to alias [" + aliases[i] + "]");
            }
            ret.add(subIndex);
        }
        if (subIndexes != null) {
            for (int i = 0; i < subIndexes.length; i++) {
                ret.add(subIndexes[i]);
            }
        }
        return (String[]) ret.toArray(new String[ret.size()]);
    }

    public void registerEventListeners(SearchEngine searchEngine, SearchEngineEventManager eventManager) {
        // do nothing here
    }

    protected class CopyFromHolder {

        boolean createOriginalDirectory = false;

        Object data;
    }

    public void copyFrom(final LuceneSearchEngineStore searchEngineStore) throws SearchEngineException {
        CopyFromHolder holder = doBeforeCopyFrom();
        final byte[] buffer = new byte[32768];
        try {
            for (int i = 0; i < getSubIndexes().length; i++) {
                final String subIndex = getSubIndexes()[i];
                template.executeForSubIndex(subIndex, holder.createOriginalDirectory, new LuceneStoreCallback() {
                    public Object doWithStore(final Directory dest) {
                        final Directory src = searchEngineStore.getDirectoryBySubIndex(subIndex, false);
                        template.execute(src, new LuceneStoreCallback() {
                            public Object doWithStore(Directory dir) throws IOException {
                                LuceneUtils.copy(src, searchEngineStore.getLuceneSettings().isUseCompoundFile(),
                                        dest, luceneSettings.isUseCompoundFile(), buffer,
                                        luceneSettings.getTransactionCommitTimeout());
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
        return "STORE[" + connectionString + "]";
    }
}
