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

package org.compass.core.lucene.engine.analyzer.synonym;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.compass.core.CompassException;
import org.compass.core.config.CompassSettings;
import org.compass.core.engine.SearchEngineException;
import org.compass.core.lucene.LuceneEnvironment;
import org.compass.core.lucene.engine.analyzer.LuceneAnalyzerTokenFilterProvider;
import org.compass.core.util.ClassUtils;

/**
 * A synonym analyzer token filter provider. Uses the {@link SynonymFilter}
 * to return synonyms. The {@link SynonymFilter} in turn uses the {@link SynonymLookupProvider}
 * which should be provided in order to lookup synonyms for a given value.
 *
 * @author kimchy
 * @see SynonymFilter
 * @see SynonymLookupProvider
 */
public class SynonymAnalyzerTokenFilterProvider implements LuceneAnalyzerTokenFilterProvider {

    private SynonymLookupProvider synonymLookupProvider;

    public void configure(CompassSettings settings) throws CompassException {
        String lookupProviderClassName = settings.getSetting(LuceneEnvironment.AnalyzerFilter.Synonym.LOOKUP);
        if (lookupProviderClassName == null) {
            throw new SearchEngineException("Failed to locate synonym lookup provider, verify that you set the [" +
                    LuceneEnvironment.AnalyzerFilter.Synonym.LOOKUP + "] setting for the group");
        }
        try {
            synonymLookupProvider = (SynonymLookupProvider) ClassUtils.forName(lookupProviderClassName, settings.getClassLoader()).newInstance();
        } catch (Exception e) {
            throw new SearchEngineException("Failed to create lookup synonym provider [" + lookupProviderClassName + "]", e);
        }
        synonymLookupProvider.configure(settings);
    }

    public TokenFilter createTokenFilter(TokenStream tokenStream) {
        return new SynonymFilter(tokenStream, synonymLookupProvider);
    }

}
