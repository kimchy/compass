
@SearchAnalyzers({
        @SearchAnalyzer(name = LuceneEnvironment.Analyzer.SEARCH_GROUP, type = AnalyzerType.Simple, stopWords = {"fox"}),
        @SearchAnalyzer(name = "simple", type = AnalyzerType.Simple)
        })
package org.compass.annotations.test.analyzer;

import org.compass.annotations.SearchAnalyzer;
import org.compass.annotations.SearchAnalyzers;
import org.compass.annotations.AnalyzerType;
import org.compass.core.lucene.LuceneEnvironment;
