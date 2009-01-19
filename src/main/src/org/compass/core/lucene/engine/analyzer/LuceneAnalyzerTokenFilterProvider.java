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

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.compass.core.config.CompassConfigurable;

/**
 * A Lucene token filter provider. Can be used with Lucene analyzers in order to
 * further filter out (or add additional tokens) during the analysis process.
 *
 * @author kimchy
 * @see TokenFilter
 * @see org.compass.core.lucene.engine.analyzer.synonym.SynonymFilter
 * @see org.compass.core.lucene.engine.analyzer.synonym.SynonymAnalyzerTokenFilterProvider
 */
public interface LuceneAnalyzerTokenFilterProvider extends CompassConfigurable {

    /**
     * Creates a new token filter based on the token stream. Called every time an
     * analysis should occur, so it would be nice to create any global level
     * data during the configuration process.
     */
    TokenStream createTokenFilter(TokenStream tokenStream);
}
