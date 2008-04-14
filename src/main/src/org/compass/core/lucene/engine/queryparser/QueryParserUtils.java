package org.compass.core.lucene.engine.queryparser;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.payloads.BoostingTermQuery;
import org.apache.lucene.search.spans.SpanTermQuery;
import org.compass.core.engine.SearchEngineException;
import org.compass.core.engine.SearchEngineFactory;
import org.compass.core.lucene.engine.all.AllBoostingTermQuery;
import org.compass.core.mapping.ResourcePropertyLookup;

/**
 * @author kimchy
 */
public abstract class QueryParserUtils {

    private QueryParserUtils() {
    }

    /**
     * Returns a poly alias query.
     */
    public static Query createPolyAliasQuery(SearchEngineFactory searchEngineFactory, String value) {
        BooleanQuery query = new BooleanQuery();
        query.add(new TermQuery(new Term(searchEngineFactory.getAliasProperty(), value)), BooleanClause.Occur.SHOULD);
        query.add(new TermQuery(new Term(searchEngineFactory.getExtendedAliasProperty(), value)), BooleanClause.Occur.SHOULD);
        return query;
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

        BooleanQuery aliasQuery = new BooleanQuery();
        aliasQuery.add(new TermQuery(new Term(searchEngineFactory.getAliasProperty(), alias)), BooleanClause.Occur.SHOULD);
        aliasQuery.add(new TermQuery(new Term(searchEngineFactory.getExtendedAliasProperty(), alias)), BooleanClause.Occur.SHOULD);
        aliasQuery.setMinimumNumberShouldMatch(1);

        booleanQuery.add(aliasQuery, BooleanClause.Occur.MUST);

        return booleanQuery;
    }

    public static interface QueryTermVisitor {

        Term replaceTerm(Term term) throws SearchEngineException;
    }

    public static Query visit(Query query, QueryTermVisitor visitor) throws SearchEngineException {
        if (query instanceof TermQuery) {
            Query q = new TermQuery(visitor.replaceTerm(((TermQuery) query).getTerm()));
            q.setBoost(query.getBoost());
            return q;
        }
        if (query instanceof BoostingTermQuery) {
            Query q = new BoostingTermQuery(visitor.replaceTerm(((SpanTermQuery) query).getTerm()));
            q.setBoost(query.getBoost());
            return q;
        }
        if (query instanceof AllBoostingTermQuery) {
            Query q = new AllBoostingTermQuery(visitor.replaceTerm(((SpanTermQuery) query).getTerm()));
            q.setBoost(query.getBoost());
            return q;
        }
        if (query instanceof SpanTermQuery) {
            Query q = new SpanTermQuery(visitor.replaceTerm(((SpanTermQuery) query).getTerm()));
            q.setBoost(query.getBoost());
            return q;
        }
        if (query instanceof PhraseQuery) {
            PhraseQuery original = (PhraseQuery) query;
            PhraseQuery replaced = new PhraseQuery();
            replaced.setBoost(original.getBoost());
            replaced.setSlop(original.getSlop());
            Term[] terms = original.getTerms();
            int[] positions = original.getPositions();
            for (int i = 0; i < terms.length; i++) {
                replaced.add(visitor.replaceTerm(terms[i]), positions[i]);
            }
            return replaced;
        }
        if (query instanceof BooleanQuery) {
            BooleanQuery original = (BooleanQuery) query;
            BooleanQuery replaced = new BooleanQuery(original.isCoordDisabled());
            replaced.setBoost(original.getBoost());
            replaced.setMinimumNumberShouldMatch(original.getMinimumNumberShouldMatch());
            for (BooleanClause clause : original.getClauses()) {
                replaced.add(visit(clause.getQuery(), visitor), clause.getOccur());
            }
            return replaced;
        }
        return query;
    }
}
