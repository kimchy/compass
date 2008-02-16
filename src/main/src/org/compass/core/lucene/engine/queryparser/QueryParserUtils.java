package org.compass.core.lucene.engine.queryparser;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.compass.core.engine.SearchEngineFactory;
import org.compass.core.mapping.ResourcePropertyLookup;

/**
 * @author kimchy
 */
public abstract class QueryParserUtils {

    private QueryParserUtils() {
    }

    /**
     * If enabled and allowed, wraps the given query with a boolean query that adds a must match on the alias).
     */
    public static Query andAliasQueryIfNeeded(Query query, ResourcePropertyLookup lookup, boolean addAliasQueryWithDotPath,
                                              SearchEngineFactory searchEngineFactory) {
        if (query == null) {
            return query;
        }
        if (!addAliasQueryWithDotPath) {
            return query;
        }
        if (lookup == null) {
            return query;
        }
        String alias = lookup.getDotPathAlias();
        if (alias == null) {
            return query;
        }
        BooleanQuery booleanQuery = new BooleanQuery();
        booleanQuery.add(query, BooleanClause.Occur.MUST);
        booleanQuery.add(new TermQuery(new Term(searchEngineFactory.getAliasProperty(), alias)), BooleanClause.Occur.MUST);
        booleanQuery.add(new TermQuery(new Term(searchEngineFactory.getExtendedAliasProperty(), alias)), BooleanClause.Occur.SHOULD);
        return booleanQuery;
    }
}
