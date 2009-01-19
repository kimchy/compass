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

package org.compass.core.lucene.engine.analyzer;

import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;

/**
 * A simple analyzer wrapper, that adds a set of token filters created by the corresponding
 * {@link LuceneAnalyzerTokenFilterProvider}s.
 *
 * @author kimchy
 */
public class LuceneAnalyzerFilterWrapper extends Analyzer {

    private Analyzer analyzer;

    private LuceneAnalyzerTokenFilterProvider[] filteresProviders;

    public LuceneAnalyzerFilterWrapper(Analyzer analyzer, LuceneAnalyzerTokenFilterProvider[] filteresProviders) {
        this.analyzer = analyzer;
        this.filteresProviders = filteresProviders;
    }

    public TokenStream tokenStream(String fieldName, Reader reader) {
        TokenStream result = analyzer.tokenStream(fieldName, reader);
        for (LuceneAnalyzerTokenFilterProvider filteresProvider : filteresProviders) {
            result = filteresProvider.createTokenFilter(result);
        }
        return result;
    }
}
