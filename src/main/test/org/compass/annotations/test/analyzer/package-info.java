
@SearchAnalyzers({
        @SearchAnalyzer(name = LuceneEnvironment.Analyzer.SEARCH_GROUP, type = AnalyzerType.Simple, stopWords = {"fox"})
        })
package org.compass.annotations.test.analyzer;

import org.compass.annotations.AnalyzerType;
import org.compass.annotations.SearchAnalyzer;
import org.compass.annotations.SearchAnalyzers;
import org.compass.core.lucene.LuceneEnvironment;
