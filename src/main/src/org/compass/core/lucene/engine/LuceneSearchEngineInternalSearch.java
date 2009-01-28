package org.compass.core.lucene.engine;

import java.io.IOException;
import java.util.List;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MultiSearcher;
import org.apache.lucene.search.Searchable;
import org.apache.lucene.search.Searcher;
import org.compass.core.engine.SearchEngineException;
import org.compass.core.engine.SearchEngineInternalSearch;
import org.compass.core.lucene.engine.manager.LuceneIndexHolder;
import org.compass.core.lucene.search.CacheableMultiReader;

/**
 * A Lucene specific search "internals", allowing for Lucene {@link IndexReader} and {@link Searcher}
 * access.
 *
 * @author kimchy
 */
public class LuceneSearchEngineInternalSearch implements SearchEngineInternalSearch, LuceneDelegatedClose {

    private LuceneSearchEngine searchEngine;

    private Searcher searcher;

    private IndexReader indexReader;

    private List<LuceneIndexHolder> indexHoldersToClose;

    private boolean closeReader;

    private boolean closeSearcher;

    private boolean closed;

    public LuceneSearchEngineInternalSearch(LuceneSearchEngine searchEngine, List<LuceneIndexHolder> indexHolders) {
        this.searchEngine = searchEngine;
        this.indexHoldersToClose = indexHolders;
    }

    public LuceneSearchEngineInternalSearch(LuceneSearchEngine searchEngine, MultiSearcher searcher, List<LuceneIndexHolder> indexHolders) {
        this.searchEngine = searchEngine;
        this.searcher = searcher;
        this.indexHoldersToClose = indexHolders;
        Searchable[] searchables = searcher.getSearchables();
        IndexReader[] readers = new IndexReader[searchables.length];
        for (int i = 0; i < searchables.length; i++) {
            readers[i] = ((IndexSearcher) searchables[i]).getIndexReader();
        }
        indexReader = new CacheableMultiReader(readers, false);
        this.closeReader = true;
        this.closeSearcher = true;
    }

    public LuceneSearchEngineInternalSearch(LuceneSearchEngine searchEngine, LuceneIndexHolder indexHolder, List<LuceneIndexHolder> indexHolders) {
        this.searchEngine = searchEngine;
        this.searcher = indexHolder.getIndexSearcher();
        this.indexReader = indexHolder.getIndexReader();
        this.indexHoldersToClose = indexHolders;
        this.closeReader = false;
        this.closeSearcher = false;
    }

    /**
     * Creates a new instance, with a searcher and index holders which will be used
     * to release when calling close.
     */
    public LuceneSearchEngineInternalSearch(LuceneSearchEngine searchEngine, IndexReader indexReader, Searcher searcher, List<LuceneIndexHolder> indexHolders) {
        this.searchEngine = searchEngine;
        this.indexReader = indexReader;
        this.searcher = searcher;
        this.indexHoldersToClose = indexHolders;
        this.closeReader = true;
        this.closeSearcher = true;
    }

    /**
     * Returns <code>true</code> if it represents an empty index scope.
     */
    public boolean isEmpty() {
        return searcher == null;
    }

    /**
     * Returns a Lucene {@link Searcher}.
     */
    public Searcher getSearcher() {
        return this.searcher;
    }

    /**
     * Returns a Lucene {@link IndexReader}.
     */
    public IndexReader getReader() throws SearchEngineException {
        return this.indexReader;
    }

    /**
     * [Intenral]: Closes without removing the delegate.
     */
    public void closeDelegate() throws SearchEngineException {
        close(false);
    }

    /**
     * Closes the internal search. Note, does not require to be called here since
     * will automatically be called on transaction commit / rollback.
     */
    public void close() throws SearchEngineException {
        close(true);
    }

    /**
     * Closes this instance of Lucene search "internals". This is an optional operation
     * since Compass will take care of closing it when commit/rollback is called on the
     * transaction.
     */
    private void close(boolean removeDelegate) throws SearchEngineException {
        if (closed) {
            return;
        }
        closed = true;

        if (removeDelegate) {
            searchEngine.removeDelegatedClose(this);
        }

        if (searcher != null && closeSearcher) {
            try {
                searcher.close();
            } catch (IOException e) {
                // ignore
            }
        }

        if (indexReader != null && closeReader) {
            try {
                indexReader.close();
            } catch (IOException e) {
                // ignore
            }
        }

        if (indexHoldersToClose != null) {
            for (LuceneIndexHolder indexHolder : indexHoldersToClose) {
                indexHolder.release();
            }
        }
    }

}
