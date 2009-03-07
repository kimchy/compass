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

package org.compass.core.lucene.engine.spellcheck;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
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
import org.apache.lucene.store.IndexInput;
import org.apache.lucene.store.IndexOutput;
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
import org.compass.core.lucene.search.CacheableMultiReader;
import org.compass.core.mapping.CompassMapping;
import org.compass.core.mapping.ResourceMapping;
import org.compass.core.mapping.ResourcePropertyMapping;
import org.compass.core.mapping.SpellCheck;
import org.compass.core.transaction.InternalCompassTransaction;
import org.compass.core.transaction.context.TransactionContextCallback;
import org.compass.core.transaction.context.TransactionContextCallbackWithTr;
import org.compass.core.transaction.context.TransactionalRunnable;
import org.compass.core.util.StringUtils;

/**
 * The default implementation of the search engine spell check manager. Uses Lucene (modified) spell check
 * support. Only activated if the spell jar exists.
 *
 * @author kimchy
 */
public class DefaultLuceneSpellCheckManager implements InternalLuceneSearchEngineSpellCheckManager {

    public static String SPELL_CHECK_VERSION_FILENAME = "spellcheck.version";

    private static final Log log = LogFactory.getLog(DefaultLuceneSpellCheckManager.class);

    private LuceneSearchEngineFactory searchEngineFactory;

    private LuceneSearchEngineStore indexStore;

    private LuceneSearchEngineStore spellCheckStore;

    private String spellIndexSubContext = "spellcheck";

    private CompassSettings spellCheckSettings;

    private Map<String, IndexReader> readerMap = new HashMap<String, IndexReader>();

    private Map<String, IndexSearcher> searcherMap = new HashMap<String, IndexSearcher>();

    private Map<String, Object> indexLocks = new HashMap<String, Object>();

    private String defaultProperty;

    private Map<String, Set<String>> properties = new HashMap<String, Set<String>>();

    private float defaultAccuracy = 0.5f;

    private int defaultNumberOfSuggestions = 1;

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
        spellCheckSettings.setIntSetting(LuceneEnvironment.SearchEngineIndex.MERGE_FACTOR, spellCheckSettings.getSettingAsInt(LuceneEnvironment.SearchEngineIndex.MERGE_FACTOR, 300));
        spellCheckSettings.setIntSetting(LuceneEnvironment.SearchEngineIndex.RAM_BUFFER_SIZE, spellCheckSettings.getSettingAsInt(LuceneEnvironment.SearchEngineIndex.RAM_BUFFER_SIZE, 50));

        if (spellCheckSettings.getSetting(CompassEnvironment.CONNECTION).equals(settings.getSetting(CompassEnvironment.CONNECTION))) {
            spellCheckStore = searchEngineFactory.getLuceneIndexManager().getStore();
            closeStore = false;
            if (log.isDebugEnabled()) {
                log.debug("Spell index uses Compass store [" + spellCheckSettings.getSetting(CompassEnvironment.CONNECTION) + "]");
            }
        } else {
            spellCheckStore = new DefaultLuceneSearchEngineStore(searchEngineFactory, spellCheckSettings, mapping);
            closeStore = true;
            if (log.isDebugEnabled()) {
                log.debug("Spell index uses specialized store [" + spellCheckSettings.getSetting(CompassEnvironment.CONNECTION) + "]");
            }
        }

        String[] sharedProps = new String[0];
        String sSharedProps = spellCheckSettings.getSetting(LuceneEnvironment.SpellCheck.GLOBAL_INCLUDE_PROPERTIES);
        if (sSharedProps != null) {
            sharedProps = StringUtils.tokenizeToStringArray(sSharedProps, ",");
        }
        String[] sharedExcludeProps = new String[0];
        String sSharedExcludeProps = spellCheckSettings.getSetting(LuceneEnvironment.SpellCheck.GLOBAL_EXCLUDE_PROPERTY);
        if (sSharedExcludeProps != null) {
            sharedExcludeProps = StringUtils.tokenizeToStringArray(sSharedExcludeProps, ",");
        }

        for (String subIndex : indexStore.getSubIndexes()) {
            Set<String> subIndexProps = properties.get(subIndex);
            if (subIndexProps == null) {
                subIndexProps = new HashSet<String>();
                properties.put(subIndex, subIndexProps);
            }

            subIndexProps.addAll(Arrays.asList(sharedProps));

            for (String alias : spellCheckStore.getAliasesBySubIndex(subIndex)) {
                ResourceMapping resourceMapping = searchEngineFactory.getMapping().getMappingByAlias(alias);
                for (ResourcePropertyMapping resourcePropertyMapping : resourceMapping.getResourcePropertyMappings()) {
                    if (resourcePropertyMapping.isInternal()) {
                        continue;
                    }
                    if (resourceMapping.getSpellCheck() == SpellCheck.INCLUDE && resourcePropertyMapping.getSpellCheck() != SpellCheck.EXCLUDE) {
                        subIndexProps.add(resourcePropertyMapping.getPath().getPath());
                    }
                    if (resourceMapping.getSpellCheck() == SpellCheck.EXCLUDE && resourcePropertyMapping.getSpellCheck() == SpellCheck.INCLUDE) {
                        subIndexProps.add(resourcePropertyMapping.getPath().getPath());
                    }
                }
                if (resourceMapping.getAllMapping().getSpellCheck() == SpellCheck.INCLUDE) {
                    subIndexProps.add(resourceMapping.getAllMapping().getProperty());
                }

                if (subIndexProps.size() == 0 && resourceMapping.getSpellCheck() == SpellCheck.NA &&
                        resourceMapping.getAllMapping().getSpellCheck() != SpellCheck.EXCLUDE) {
                    subIndexProps.add(settings.getSetting(CompassEnvironment.All.NAME, CompassEnvironment.All.DEFAULT_NAME));
                }
            }

            for (String excludeProperty : sharedExcludeProps) {
                subIndexProps.remove(excludeProperty);
            }


            if (log.isDebugEnabled()) {
                log.debug("Sub index [" + subIndex + "] includes the following properties " + subIndexProps);
            }
        }
        defaultProperty = settings.getSetting(LuceneEnvironment.SpellCheck.DEFAULT_PROPERTY, settings.getSetting(CompassEnvironment.All.NAME, CompassEnvironment.All.DEFAULT_NAME));

        this.defaultAccuracy = spellCheckSettings.getSettingAsFloat(LuceneEnvironment.SpellCheck.ACCURACY, 0.5f);
        this.defaultNumberOfSuggestions = spellCheckSettings.getSettingAsInt(LuceneEnvironment.SpellCheck.NUMBER_OF_SUGGESTIONS, 1);
        this.defaultDictionaryThreshold = spellCheckSettings.getSettingAsFloat(LuceneEnvironment.SpellCheck.DICTIONARY_THRESHOLD, 0.0f);

        for (final String subIndex : indexStore.getSubIndexes()) {
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
                public Object doInTransaction() throws CompassException {
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
            }, spellCheckSettings.getSettingAsLong(LuceneEnvironment.SpellCheck.SCHEDULE_INITIAL_DELAY, 10), spellCheckSettings.getSettingAsLong(LuceneEnvironment.SpellCheck.SCHEDULE_INTERVAL, 10) * 60, TimeUnit.SECONDS);
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

    public int getDefaultNumberOfSuggestions() {
        return defaultNumberOfSuggestions;
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
        return searchEngineFactory.getTransactionContext().execute(new TransactionContextCallback<Boolean>() {
            public Boolean doInTransaction() throws CompassException {
                try {
                    long spellCheckVersion = readSpellCheckIndexVersion(subIndex);
                    long indexVerion = LuceneSubIndexInfo.getIndexInfo(subIndex, indexStore).version();
                    return indexVerion != spellCheckVersion;
                } catch (IOException e) {
                    throw new SearchEngineException("Failed to read index version for sub index [" + subIndex + "]");
                }
            }
        });
    }

    public boolean concurrentRebuild() throws SearchEngineException {
        checkIfStarted();
        ArrayList<Callable<Boolean>> rebuildTasks = new ArrayList<Callable<Boolean>>();
        for (String subIndex : indexStore.getSubIndexes()) {
            rebuildTasks.add(new RebuildTask(subIndex));
        }
        List<Future<Boolean>> rebuildResults = searchEngineFactory.getExecutorManager().invokeAllWithLimitBailOnException(rebuildTasks, Integer.MAX_VALUE);
        boolean rebuilt = false;
        for (Future<Boolean> rebuildResult : rebuildResults) {
            try {
                rebuilt |= rebuildResult.get();
            } catch (Exception e) {
                // will not happen
            }
        }
        return rebuilt;
    }

    public boolean rebuild() throws SearchEngineException {
        checkIfStarted();
        boolean rebuilt = false;
        for (String subIndex : indexStore.getSubIndexes()) {
            rebuilt |= rebuild(subIndex);
        }
        return rebuilt;
    }

    public synchronized boolean rebuild(final String subIndex) throws SearchEngineException {
        checkIfStarted();
        return searchEngineFactory.getTransactionContext().execute(new TransactionContextCallbackWithTr<Boolean>() {
            public Boolean doInTransaction(InternalCompassTransaction tr) throws CompassException {
                long version = readSpellCheckIndexVersion(subIndex);
                long indexVersion;
                try {
                    indexVersion = LuceneSubIndexInfo.getIndexInfo(subIndex, indexStore).version();
                } catch (IOException e) {
                    throw new SearchEngineException("Failed to read actual index version for sub index [" + subIndex + "]", e);
                }
                if (version == indexVersion) {
                    if (log.isDebugEnabled()) {
                        log.debug("No need to rebuild spell check index, sub index [" + subIndex + "] has not changed");
                    }
                    return false;
                }

                if (log.isDebugEnabled()) {
                    log.debug("Rebuilding spell index for sub index [" + subIndex + "]");
                }
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
                        writer = searchEngineFactory.getLuceneIndexManager().getIndexWritersManager().openIndexWriter(spellCheckSettings, dir, true, null, new WhitespaceAnalyzer());
                        for (String property : properties.get(subIndex)) {
                            spellChecker.indexDictionary(writer, new HighFrequencyDictionary(search.getReader(), property, defaultDictionaryThreshold));
                        }
                        writer.optimize();
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
                            log.warn("Failed to close specll check index writer for sub index [" + subIndex + "]", e);
                        }
                    }
                }
                // refresh the readers and searchers
                closeAndRefresh(subIndex);
                writeSpellCheckIndexVersion(subIndex, indexVersion);

                if (log.isDebugEnabled()) {
                    log.debug("Finished rebuilding spell index for sub index [" + subIndex + "]");
                }
                return true;
            }
        });
    }

    public void deleteIndex() throws SearchEngineException {
        // no need to check if started
        for (String subIndex : indexStore.getSubIndexes()) {
            deleteIndex(subIndex);
        }
    }

    public void deleteIndex(String subIndex) throws SearchEngineException {
        // no need to check if started
        close(subIndex);
        spellCheckStore.deleteIndex(spellIndexSubContext, subIndex);
    }

    public SearchEngineSpellCheckSuggestBuilder suggestBuilder(String word) {
        return new DefaultLuceneSearchEngineSpellCheckSuggestBuilder(word, this);
    }

    public CompassQuery suggest(CompassQuery query) {
        DefaultCompassQuery defaultCompassQuery = (DefaultCompassQuery) query;
        LuceneSearchEngineQuery searchEngineQuery = (LuceneSearchEngineQuery) defaultCompassQuery.getSearchEngineQuery();
        final CompassSpellChecker spellChecker = createSpellChecker(searchEngineQuery.getSubIndexes(), searchEngineQuery.getAliases());

        if (spellChecker == null) {
            return query;
        }
        spellChecker.setAccuracy(getDefaultAccuracy());
        final AtomicBoolean suggestedQuery = new AtomicBoolean(false);
        try {
            Query replacedQ = QueryParserUtils.visit(searchEngineQuery.getQuery(), new QueryParserUtils.QueryTermVisitor() {
                public Term replaceTerm(Term term) throws SearchEngineException {
                    try {
                        if (spellChecker.exist(term.text())) {
                            return term;
                        }
                        String[] similarWords = spellChecker.suggestSimilar(term.text(), getDefaultNumberOfSuggestions());
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

            if (!suggestedQuery.get()) {
                return query;
            }

            try {
                CompassQuery suggested = (CompassQuery) query.clone();
                LuceneSearchEngineQuery suggestedSEQ = (LuceneSearchEngineQuery) ((DefaultCompassQuery) suggested).getSearchEngineQuery();
                suggestedSEQ.setQuery(replacedQ);
                suggestedSEQ.setSuggested(suggestedQuery.get());
                return suggested;
            } catch (CloneNotSupportedException e) {
                throw new SearchEngineException("Failed to clone query", e);
            }

        } finally {
            spellChecker.close();
        }
    }

    public <T> T execute(final String[] subIndexes, final String[] aliases, final SpellCheckerCallback<T> callback) {
        return searchEngineFactory.getTransactionContext().execute(new TransactionContextCallbackWithTr<T>() {
            public T doInTransaction(InternalCompassTransaction tr) throws CompassException {
                CompassSpellChecker spellChecker = createSpellChecker(subIndexes, aliases);
                if (spellChecker == null) {
                    return callback.execute(null, null);
                }
                LuceneSearchEngineInternalSearch search = null;
                try {
                    search = (LuceneSearchEngineInternalSearch) tr.getSearchEngine().internalSearch(subIndexes, aliases);
                    return callback.execute(spellChecker, search.getReader());
                } finally {
                    if (search != null) {
                        search.close();
                    }
                    spellChecker.close();
                }
            }
        });
    }

    public CompassSpellChecker createSpellChecker(final String[] subIndexes, final String[] aliases) {
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

        if (searchers.isEmpty()) {
            return null;
        }

        MultiSearcher searcher;
        try {
            searcher = searchEngineFactory.getLuceneIndexManager().openMultiSearcher(searchers.toArray(new Searchable[searchers.size()]));
        } catch (IOException e) {
            throw new SearchEngineException("Failed to open searcher for spell check", e);
        }
        MultiReader reader = new CacheableMultiReader(readers.toArray(new IndexReader[readers.size()]), false);

        return new CompassSpellChecker(searcher, reader);
    }

    private void writeSpellCheckIndexVersion(String subIndex, long version) {
        Directory dir = spellCheckStore.openDirectory(spellIndexSubContext, subIndex);
        try {
            if (dir.fileExists(SPELL_CHECK_VERSION_FILENAME)) {
                dir.deleteFile(SPELL_CHECK_VERSION_FILENAME);
            }
            IndexOutput indexOutput = dir.createOutput(SPELL_CHECK_VERSION_FILENAME);
            indexOutput.writeLong(version);
            indexOutput.close();
        } catch (IOException e) {
            throw new SearchEngineException("Failed to write spell check index version for sub index [" + subIndex + "]", e);
        }
    }

    private long readSpellCheckIndexVersion(String subIndex) {
        Directory dir = spellCheckStore.openDirectory(spellIndexSubContext, subIndex);
        IndexInput input = null;
        try {
            if (!dir.fileExists(SPELL_CHECK_VERSION_FILENAME)) {
                return -1;
            }
            input = dir.openInput(SPELL_CHECK_VERSION_FILENAME);
            return input.readLong();
        } catch (IOException e) {
            throw new SearchEngineException("Failed to read spell check index version for sub index [" + subIndex + "]", e);
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
            } catch (IOException e) {
                // ignore
            }
        }
    }

    private void closeAndRefresh() {
        for (String subIndex : indexStore.getSubIndexes()) {
            closeAndRefresh(subIndex);
        }
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

    private class RebuildTask implements Callable<Boolean> {

        private String subIndex;

        public RebuildTask(String subIndex) {
            this.subIndex = subIndex;
        }

        public Boolean call() throws Exception {
            return rebuild(subIndex);
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
