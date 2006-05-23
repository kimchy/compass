package org.compass.core.lucene.engine;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.compass.core.CompassToken;
import org.compass.core.Resource;
import org.compass.core.engine.SearchEngineAnalyzerHelper;
import org.compass.core.engine.SearchEngineException;
import org.compass.core.lucene.LuceneToken;
import org.compass.core.lucene.engine.analyzer.LuceneAnalyzerManager;

/**
 * @author kimchy
 */
public class LuceneSearchEngineAnalyzerHelper implements SearchEngineAnalyzerHelper {

    private LuceneAnalyzerManager analyzerManager;

    private Analyzer analyzer;

    public LuceneSearchEngineAnalyzerHelper(LuceneSearchEngine searchEngine) {
        this.analyzerManager = searchEngine.getSearchEngineFactory().getAnalyzerManager();
        this.analyzer = analyzerManager.getDefaultAnalyzer();
    }

    public SearchEngineAnalyzerHelper setAnalyzer(String analyzerName) {
        this.analyzer = analyzerManager.getAnalyzerMustExist(analyzerName);
        return this;
    }

    public SearchEngineAnalyzerHelper setAnalyzer(Resource resource) throws SearchEngineException {
        this.analyzer = analyzerManager.getAnalyzerByResource(resource);
        return this;
    }

    public SearchEngineAnalyzerHelper setAnalyzerByAlias(String alias) throws SearchEngineException {
        this.analyzer = analyzerManager.getAnalyzerByAliasMustExists(alias);
        return this;
    }

    public CompassToken analyzeSingle(String text) throws SearchEngineException {
        CompassToken[] tokens = analyze(text);
        if (tokens == null || tokens.length == 0) {
            return null;
        }
        return tokens[0];
    }

    public CompassToken[] analyze(String text) {
        return analyze(new StringReader(text));
    }

    public CompassToken[] analyze(String propertyName, String text) throws SearchEngineException {
        return analyze(propertyName, new StringReader(text));
    }

    public CompassToken[] analyze(Reader textReader) throws SearchEngineException {
        return analyze(null, textReader);
    }

    public CompassToken[] analyze(String propertyName, Reader textReader) throws SearchEngineException {
        try {
            TokenStream tokenStream = analyzer.tokenStream(propertyName, textReader);
            ArrayList tokenList = new ArrayList();
            while (true) {
                Token token = tokenStream.next();
                if (token == null) break;
                tokenList.add(new LuceneToken(token));
            }

            return (CompassToken[]) tokenList.toArray(new CompassToken[tokenList.size()]);
        } catch (IOException e) {
            throw new SearchEngineException("Failed to iterate token stream from analyzer [" + analyzer + "]");
        }
    }
}
