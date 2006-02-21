
@SearchableAnalyzers({
        @SearchableAnalyzer(name = LuceneEnvironment.Analyzer.SEARCH_GROUP, type = AnalyzerType.Simple, stopWords = {"fox"}),
        @SearchableAnalyzer(name = "simple", type = AnalyzerType.Simple)
        })
package org.compass.annotations.test.analyzer;

import org.compass.annotations.SearchableAnalyzer;
import org.compass.annotations.SearchableAnalyzers;
import org.compass.annotations.AnalyzerType;
import org.compass.core.lucene.LuceneEnvironment;
