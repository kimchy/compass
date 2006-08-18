package org.compass.core.lucene.engine;

import java.io.IOException;
import java.util.List;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MultiSearcher;
import org.apache.lucene.search.Searchable;
import org.apache.lucene.search.Searcher;
import org.compass.core.engine.SearchEngineException;
import org.compass.core.engine.SearchEngineInternalSearch;
import org.compass.core.lucene.engine.manager.LuceneSearchEngineIndexManager;

/**
 * A Lucene specific search "internals", allowing for Lucene {@link IndexReader} and {@link Searcher}
 * access.
 *
 * @author kimchy
 */
public class LuceneSearchEngineInternalSearch implements SearchEngineInternalSearch, LuceneDelegatedClose {

    private MultiSearcher searcher;
    protected MultiReader reader;

    private boolean closed;
    private List indexHolders;

    /**
     * Creates a new instance, with a searcher and index holders which will be used
     * to release when calling close.
     *
     * @param searcher     The searcher, which is also used to construct the reader
     * @param indexHolders Holders to be released when calling close.
     */
    public LuceneSearchEngineInternalSearch(MultiSearcher searcher, List indexHolders) {
        this.searcher = searcher;
        this.indexHolders = indexHolders;
    }

    /**
     * Returns <code>true</code> if it represents an empty index scope.
     */
    public boolean isEmpty() {
        return searcher == null || searcher.getSearchables().length == 0;
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
        if (reader != null) {
            return this.reader;
        }

        Searchable[] searchables = searcher.getSearchables();
        IndexReader[] readers = new IndexReader[searchables.length];
        for (int i = 0; i < searchables.length; i++) {
            readers[i] = ((IndexSearcher) searchables[i]).getIndexReader();
        }
        try {
            reader = new MultiReader(readers);
        } catch (IOException e) {
            throw new SearchEngineException("Failed to open readers for highlighting", e);
        }
        return this.reader;
    }

    /**
     * Closes this instance of Lucene search "internals". This is an optional operation
     * since Compass will take care of closing it when commit/rollback is called on the
     * transaction.
     */
    public void close() throws SearchEngineException {
        if (closed) {
            return;
        }
        closed = true;

        if (indexHolders != null) {
            for (int i = 0; i < indexHolders.size(); i++) {
                LuceneSearchEngineIndexManager.LuceneIndexHolder indexHolder =
                        (LuceneSearchEngineIndexManager.LuceneIndexHolder) indexHolders.get(i);
                indexHolder.release();
            }
        }
    }

}
