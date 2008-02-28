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

package org.compass.core.lucene.engine.spellcheck;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.LuceneSubIndexInfo;
import org.apache.lucene.index.MultiReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MultiSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Searchable;
import org.apache.lucene.search.spell.CompassSpellChecker;
import org.apache.lucene.search.spell.HighFrequencyDictionary;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.LockObtainFailedException;
import org.compass.core.CompassException;
import org.compass.core.CompassQuery;
import org.compass.core.config.CompassEnvironment;
import org.compass.core.config.CompassSettings;
import org.compass.core.engine.SearchEngineException;
import org.compass.core.engine.spellcheck.SearchEngineSpellCheckSuggestBuilder;
import org.compass.core.impl.DefaultCompassQuery;
import org.compass.core.lucene.LuceneEnvironment;
import org.compass.core.lucene.engine.LuceneSearchEngineFactory;
import org.compass.core.lucene.engine.LuceneSearchEngineInternalSearch;
import org.compass.core.lucene.engine.LuceneSearchEngineQuery;
import org.compass.core.lucene.engine.queryparser.QueryParserUtils;
import org.compass.core.lucene.engine.store.DefaultLuceneSearchEngineStore;
import org.compass.core.lucene.engine.store.LuceneSearchEngineStore;
import org.compass.core.mapping.CompassMapping;
import org.compass.core.transaction.InternalCompassTransaction;
import org.compass.core.transaction.context.TransactionContextCallback;
import org.compass.core.transaction.context.TransactionalRunnable;

/**
 * The default implementation of the search engine spell check manager. Uses Lucene (modified) spell check
 * support. Only activated if the spell jar exists.
 *
 * @author kimchy
 */
public class DefaultLuceneSpellCheckManager implements InternalLuceneSearchEngineSpellCheckManager {

    private static final Log log = LogFactory.getLog(DefaultLuceneSpellCheckManager.class);

    private LuceneSearchEngineFactory searchEngineFactory;

    private LuceneSearchEngineStore indexStore;

    private LuceneSearchEngineStore spellCheckStore;

    private String spellIndexSubContext = "spellcheck";

    private CompassSettings spellCheckSettings;

    private Map<String, IndexReader> readerMap = new HashMap<String, IndexReader>();

    private Map<String, IndexSearcher> searcherMap = new HashMap<String, IndexSearcher>();

    private Map<String, Long> versionMap = new ConcurrentHashMap<String, Long>();

    private Map<String, Object> indexLocks = new HashMap<String, Object>();

    private String defaultProperty;

    private float defaultAccuracy = 0.5f;

    private float defaultDictionaryThreshold;

    private volatile boolean started = false;

    private boolean closeStore;

    private ScheduledFuture refreshCacheFuture;

    private ScheduledFuture rebuildFuture;

    public void configure(LuceneSearchEngineFactory searchEngineFactory, CompassSettings settings, CompassMapping mapping) {
        this.searchEngineFactory = searchEngineFactory;
        this.indexStore = searchEngineFactory.getLuceneIndexManager().getStore();
        this.spellCheckSettings = settings.copy();
        for (Object key1 : settings.getProperties().keySet()) {
            String key = (String) key1;
            String value = settings.getSetting(key);
            if (key.startsWith(LuceneEnvironment.SpellCheck.PREFIX)) {
                key = "compass." + key.substring(LuceneEnvironment.SpellCheck.PREFIX.length());
                spellCheckSettings.setSetting(key, value);
            }
        }
        spellCheckSettings.setIntSetting(LuceneEnvironment.SearchEngineIndex.MERGE_FACTOR, spellCheckSettings.getSettingAsInt(LuceneEnvironment.SearchEngineIndex.MERGE_FACTOR, 3000));

        if (spellCheckSettings.getSetting(CompassEnvironment.CONNECTION).equals(settings.getSetting(CompassEnvironment.CONNECTION))) {
            spellCheckStore = searchEngineFactory.getLuceneIndexManager().getStore();
            closeStore = false;
            if (log.isDebugEnabled()) {
                log.debug("Spell index uses Compass store [" + spellCheckSettings.getSetting(CompassEnvironment.CONNECTION) + "]");
            }
        } else {
            spellCheckStore = new DefaultLuceneSearchEngineStore();
            spellCheckStore.configure(searchEngineFactory, spellCheckSettings, mapping);
            closeStore = true;
            if (log.isDebugEnabled()) {
                log.debug("Spell index uses specialized store [" + spellCheckSettings.getSetting(CompassEnvironment.CONNECTION) + "]");
            }
        }

        this.defaultProperty = spellCheckSettings.getSetting(LuceneEnvironment.SpellCheck.PROPERTY, CompassEnvironment.All.DEFAULT_NAME);
        this.defaultAccuracy = spellCheckSettings.getSettingAsFloat(LuceneEnvironment.SpellCheck.ACCURACY, 0.5f);
        this.defaultDictionaryThreshold = spellCheckSettings.getSettingAsFloat(LuceneEnvironment.SpellCheck.DICTIONARY_THRESHOLD, 0.0f);

        for (final String subIndex : indexStore.getSubIndexes()) {
            versionMap.put(subIndex, -1l);
            indexLocks.put(subIndex, new Object());
        }        
    }

    public void start() {
        if (started) {
            return;
        }
        started = true;

        for (final String subIndex : indexStore.getSubIndexes()) {
            searchEngineFactory.getTransactionContext().execute(new TransactionContextCallback<Object>() {
                public Object doInTransaction(InternalCompassTransaction tr) throws CompassException {
                    Directory dir = spellCheckStore.openDirectory(spellIndexSubContext, subIndex);
                    close(subIndex);
                    try {
                        if (!IndexReader.indexExists(dir)) {
                            IndexWriter writer = new IndexWriter(dir, new WhitespaceAnalyzer(), true);
                            writer.close();
                        }
                    } catch (IOException e) {
                        throw new SearchEngineException("Failed to verify spell index for sub index [" + subIndex + "]", e);
                    }
                    refresh(subIndex);
                    return null;
                }
            });
        }
        
        // schedule a refresh task
        long cacheRefreshInterval = spellCheckSettings.getSettingAsLong(LuceneEnvironment.SearchEngineIndex.CACHE_INTERVAL_INVALIDATION, 5000);
        refreshCacheFuture = searchEngineFactory.getExecutorManager().scheduleWithFixedDelay(new TransactionalRunnable(searchEngineFactory.getTransactionContext(), new Runnable() {
            public void run() {
                refresh();
            }
        }), cacheRefreshInterval, cacheRefreshInterval, TimeUnit.MILLISECONDS);

        if (spellCheckSettings.getSettingAsBoolean(LuceneEnvironment.SpellCheck.SCHEDULE, true)) {
            rebuildFuture = searchEngineFactory.getExecutorManager().scheduleWithFixedDelay(new Runnable() {
                public void run() {
                    rebuild();
                }
            }, 0, spellCheckSettings.getSettingAsLong(LuceneEnvironment.SpellCheck.SCHEDULE_INTERVAL, 10) * 60, TimeUnit.SECONDS);
        }
    }

    public void stop() {
        if (!started) {
            return;
        }
        started = false;
        if (refreshCacheFuture != null) {
            refreshCacheFuture.cancel(true);
        }
        if (rebuildFuture != null) {
            rebuildFuture.cancel(true);
        }
    }

    public void close() {
        stop();
        for (String subIndex : indexStore.getSubIndexes()) {
            close(subIndex);
        }
        if (closeStore) {
            spellCheckStore.close();
        }
    }

    private void close(String subIndex) {
        synchronized (indexLocks.get(subIndex)) {
            IndexSearcher searcher = searcherMap.remove(subIndex);
            if (searcher != null) {
                try {
                    searcher.close();
                } catch (IOException e) {
                    // ignore
                }
            }
            IndexReader reader = readerMap.remove(subIndex);
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
    }

    public String getDefaultProperty() {
        return this.defaultProperty;
    }

    public float getDefaultAccuracy() {
        return defaultAccuracy;
    }

    public CompassMapping getMapping() {
        return searchEngineFactory.getMapping();
    }

    public void concurrentRefresh() throws SearchEngineException {
        checkIfStarted();
        ArrayList<Callable<Object>> rebuildTasks = new ArrayList<Callable<Object>>();
        for (String subIndex : indexStore.getSubIndexes()) {
            rebuildTasks.add(new RefreshTask(subIndex));
        }
        searchEngineFactory.getExecutorManager().invokeAllWithLimitBailOnException(rebuildTasks, Integer.MAX_VALUE);
    }
    
    public void refresh() throws SearchEngineException {
        checkIfStarted();
        for (String subIndex : indexStore.getSubIndexes()) {
            refresh(subIndex);
        }
    }

    public void refresh(String subIndex) throws SearchEngineException {
        checkIfStarted();
        synchronized (indexLocks.get(subIndex)) {
            IndexReader reader = readerMap.get(subIndex);
            if (reader != null) {
                try {
                    if (reader.isCurrent()) {
                        return;
                    }
                } catch (IOException e) {
                    throw new SearchEngineException("Failed to check if spell index is current for sub index [" + subIndex + "]", e);
                }
            }
            try {
                reader = IndexReader.open(spellCheckStore.openDirectory(spellIndexSubContext, subIndex));
                readerMap.put(subIndex, reader);
                searcherMap.put(subIndex, new IndexSearcher(reader));
            } catch (IOException e) {
                throw new SearchEngineException("Failed to open spell index searcher for sub index [" + subIndex + "]", e);
            }
        }
    }

    public boolean isRebuildNeeded() throws SearchEngineException {
        checkIfStarted();
        boolean rebulidRequired = false;
        for (String subIndex : indexStore.getSubIndexes()) {
            rebulidRequired |= isRebuildNeeded(subIndex);
        }
        return rebulidRequired;
    }

    public boolean isRebuildNeeded(final String subIndex) throws SearchEngineException {
        checkIfStarted();
        long version = versionMap.get(subIndex);
        long indexVersion = searchEngineFactory.getTransactionContext().execute(new TransactionContextCallback<Long>() {
            public Long doInTransaction(InternalCompassTransaction tr) throws CompassException {
                try {
                    return LuceneSubIndexInfo.getIndexInfo(subIndex, indexStore).version();
                } catch (IOException e) {
                    throw new SearchEngineException("Failed to read index version for sub index [" + subIndex + "]");
                }
            }
        });
        return version != indexVersion;
    }

    public void concurrentRebuild() throws SearchEngineException {
        checkIfStarted();
        ArrayList<Callable<Object>> rebuildTasks = new ArrayList<Callable<Object>>();
        for (String subIndex : indexStore.getSubIndexes()) {
            rebuildTasks.add(new RebuildTask(subIndex));
        }
        searchEngineFactory.getExecutorManager().invokeAllWithLimitBailOnException(rebuildTasks, Integer.MAX_VALUE);
    }

    public void rebuild() throws SearchEngineException {
        checkIfStarted();
        for (String subIndex : indexStore.getSubIndexes()) {
            rebuild(subIndex);
        }
    }

    public synchronized void rebuild(final String subIndex) throws SearchEngineException {
        checkIfStarted();
        long version = versionMap.get(subIndex);
        long indexVersion = searchEngineFactory.getTransactionContext().execute(new TransactionContextCallback<Long>() {
            public Long doInTransaction(InternalCompassTransaction tr) throws CompassException {
                try {
                    return LuceneSubIndexInfo.getIndexInfo(subIndex, indexStore).version();
                } catch (IOException e) {
                    throw new SearchEngineException("Failed to read index version for sub index [" + subIndex + "]");
                }
            }
        });
        if (version == indexVersion) {
            if (log.isDebugEnabled()) {
                log.debug("No need to rebuild spell check index, sub index [" + subIndex + "] has not changed");
            }
            return;
        }

        if (log.isDebugEnabled()) {
            log.debug("Rebuilding spell index for sub index [" + subIndex + "]");
        }
        searchEngineFactory.getTransactionContext().execute(new TransactionContextCallback<Object>() {
            public Object doInTransaction(InternalCompassTransaction tr) throws CompassException {
                Directory dir = spellCheckStore.openDirectory(spellIndexSubContext, subIndex);
                CompassSpellChecker spellChecker;
                try {
                    spellChecker = new CompassSpellChecker(dir, true);
                    spellChecker.clearIndex();
                } catch (IOException e) {
                    throw new SearchEngineException("Failed to create spell checker for sub index [" + subIndex + "]", e);
                }
                IndexWriter writer = null;
                try {
                    LuceneSearchEngineInternalSearch search = (LuceneSearchEngineInternalSearch) tr.getSearchEngine().internalSearch(new String[]{subIndex}, null);
                    if (search.getSearcher() != null) {
                        writer = searchEngineFactory.getLuceneIndexManager().openIndexWriter(spellCheckSettings, dir,
                                true, true, null, new WhitespaceAnalyzer());
                        spellChecker.indexDictionary(writer, new HighFrequencyDictionary(search.getReader(), defaultProperty, defaultDictionaryThreshold));
                    } else {
                        if (log.isDebugEnabled()) {
                            log.debug("No data found in sub index [" + subIndex + "], skipping building spell index");
                        }
                    }
                } catch (LockObtainFailedException e) {
                    log.debug("Failed to obtain lock, assuming indexing of spell index is in process for sub index [" + subIndex + "]");
                    return null;
                } catch (IOException e) {
                    throw new SearchEngineException("Failed to index spell index for sub index [" + subIndex + "]", e);
                } finally {
                    if (writer != null) {
                        try {
                            writer.close();
                        } catch (IOException e) {
                            // do nothing
                        }
                    }
                }
                // refresh the readers and searchers
                closeAndRefresh(subIndex);

                if (log.isDebugEnabled()) {
                    log.debug("Finished rebuilding spell index for sub index [" + subIndex + "]");
                }
                return null;
            }
        });
        // mark the last version we indexed
        versionMap.put(subIndex, indexVersion);
    }

    public void deleteIndex() throws SearchEngineException {
        for (String subIndex : indexStore.getSubIndexes()) {
            deleteIndex(subIndex);
        }
    }

    public void deleteIndex(String subIndex) throws SearchEngineException {
        close(subIndex);
        spellCheckStore.deleteIndex(spellIndexSubContext, subIndex);
    }

    public SearchEngineSpellCheckSuggestBuilder suggestBuilder(String word) {
        checkIfStarted();
        return new DefaultLuceneSearchEngineSpellCheckSuggestBuilder(word, this);
    }

    public CompassQuery suggest(CompassQuery query) {
        checkIfStarted();


        DefaultCompassQuery defaultCompassQuery = (DefaultCompassQuery) query;
        LuceneSearchEngineQuery searchEngineQuery = (LuceneSearchEngineQuery) defaultCompassQuery.getSearchEngineQuery();
        final CompassSpellChecker spellChecker = createSpellChecker(searchEngineQuery.getSubIndexes(), searchEngineQuery.getAliases());

        if (spellChecker == null) {
            return query;
        }

        CompassQuery suggested;
        try {
            suggested = (CompassQuery) query.clone();
        } catch (CloneNotSupportedException e) {
            throw new SearchEngineException("Failed to clone query", e);
        }
        final AtomicBoolean suggestedQuery = new AtomicBoolean(false);
        try {
            Query replacedQ = QueryParserUtils.visit(searchEngineQuery.getQuery(), new QueryParserUtils.QueryTermVisitor() {
                public Term replaceTerm(Term term) throws SearchEngineException {
                    try {
                        if (spellChecker.exist(term.text())) {
                            return term;
                        }
                        String[] similarWords = spellChecker.suggestSimilar(term.text(), 1);
                        if (similarWords.length == 0) {
                            return term;
                        }
                        suggestedQuery.set(true);
                        return term.createTerm(similarWords[0]);
                    } catch (IOException e) {
                        throw new SearchEngineException("Failed to suggest for query", e);
                    }
                }
            });
            LuceneSearchEngineQuery suggestedSEQ = (LuceneSearchEngineQuery) ((DefaultCompassQuery) suggested).getSearchEngineQuery();
            suggestedSEQ.setQuery(replacedQ);
            suggestedSEQ.setSuggested(suggestedQuery.get());
            return suggested;
        } finally {
            spellChecker.close();
        }
    }

    public <T> T execute(final String[] subIndexes, final String[] aliases, final SpellCheckerCallback<T> callback) {
        checkIfStarted();
        return searchEngineFactory.getTransactionContext().execute(new TransactionContextCallback<T>() {
            public T doInTransaction(InternalCompassTransaction tr) throws CompassException {
                CompassSpellChecker spellChecker = createSpellChecker(subIndexes, aliases);
                if (spellChecker == null) {
                    return callback.execute(null, null);
                }
                try {
                    LuceneSearchEngineInternalSearch search = (LuceneSearchEngineInternalSearch) tr.getSearchEngine().internalSearch(subIndexes, aliases);
                    return callback.execute(spellChecker, search.getReader());
                } finally {
                    spellChecker.close();
                }
            }
        });
    }

    public CompassSpellChecker createSpellChecker(final String[] subIndexes, final String[] aliases) {
        checkIfStarted();
        String[] calcSubIndexes = indexStore.calcSubIndexes(subIndexes, aliases);
        ArrayList<Searchable> searchers = new ArrayList<Searchable>(calcSubIndexes.length);
        ArrayList<IndexReader> readers = new ArrayList<IndexReader>(calcSubIndexes.length);
        for (String subIndex : calcSubIndexes) {
            synchronized (indexLocks.get(subIndex)) {
                IndexSearcher searcher = searcherMap.get(subIndex);
                if (searcher != null) {
                    searchers.add(searcher);
                    readers.add(searcher.getIndexReader());
                }
            }
        }
        MultiSearcher searcher;
        try {
            searcher = new MultiSearcher(searchers.toArray(new Searchable[searchers.size()]));
        } catch (IOException e) {
            throw new SearchEngineException("Failed to open searcher for spell check", e);
        }

        if (searchers.isEmpty()) {
            return null;
        }

        MultiReader reader = new MultiReader(readers.toArray(new IndexReader[readers.size()]), false);
        return new CompassSpellChecker(searcher, reader);
    }

    private void closeAndRefresh(String subIndex) {
        synchronized (indexLocks.get(subIndex)) {
            close(subIndex);
            refresh(subIndex);
        }
    }
    
    private void checkIfStarted() throws java.lang.IllegalStateException {
        if (!started) {
            throw new IllegalStateException("Spell check manager must be started to perform this operation");
        }
    }

    private class RebuildTask implements Callable<Object> {

        private String subIndex;

        public RebuildTask(String subIndex) {
            this.subIndex = subIndex;
        }

        public Object call() throws Exception {
            rebuild(subIndex);
            return null;
        }
    }

    private class RefreshTask implements Callable<Object> {

        private String subIndex;

        public RefreshTask(String subIndex) {
            this.subIndex = subIndex;
        }

        public Object call() throws Exception {
            refresh(subIndex);
            return null;
        }
    }
}
