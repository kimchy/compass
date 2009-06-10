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

package org.compass.core.lucene.engine.spellcheck.queryparser;

import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryParser.CompassMultiFieldQueryParser;
import org.apache.lucene.queryParser.CompassQueryParser;
import org.compass.core.lucene.engine.queryparser.DefaultLuceneQueryParser;

/**
 * @author kimchy
 */
public class SpellCheckLuceneQueryParser extends DefaultLuceneQueryParser {

    protected CompassQueryParser createQueryParser(String property, Analyzer analyzer, boolean forceAnalyzer) {
        if (getSearchEngineFactory().getSpellCheckManager() != null) {
            return new SpellCheckQueryParser(property, analyzer, getMapping(), getSearchEngineFactory(), forceAnalyzer);
        }
        return super.createQueryParser(property, analyzer, forceAnalyzer);
    }

    protected CompassMultiFieldQueryParser createMultiQueryParser(String[] properties, Map<String, Float> boosts, Analyzer analyzer, boolean forceAnalyzer) {
        if (getSearchEngineFactory().getSpellCheckManager() != null) {
            return new SpellCheckMultiFieldQueryParser(properties, boosts, analyzer, getMapping(), getSearchEngineFactory(), forceAnalyzer);
        }
        return super.createMultiQueryParser(properties, boosts, analyzer, forceAnalyzer);
    }
}
