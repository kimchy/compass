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

package org.compass.core.test.analyzer;

import org.compass.core.config.CompassSettings;
import org.compass.core.lucene.LuceneEnvironment;
import org.compass.core.test.AbstractTestCase;

/**
 * @author kimchy
 */
public abstract class AbstractAnalyzerTests extends AbstractTestCase {

    public static final String TEXT = "the quick brown fox jumped over the lazy dogs";

    protected void addSettings(CompassSettings settings) {
        settings.setGroupSettings(LuceneEnvironment.Analyzer.PREFIX, "simple",
                new String[] { LuceneEnvironment.Analyzer.TYPE },
                new String[] { LuceneEnvironment.Analyzer.CoreTypes.SIMPLE });
        settings.setGroupSettings(LuceneEnvironment.Analyzer.PREFIX, LuceneEnvironment.Analyzer.SEARCH_GROUP,
                new String[] { LuceneEnvironment.Analyzer.TYPE },
                new String[] { LuceneEnvironment.Analyzer.CoreTypes.SIMPLE });
    }

}
