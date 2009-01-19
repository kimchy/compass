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

package org.compass.core.test.analyzerfilter;

import org.compass.core.CompassException;
import org.compass.core.CompassHits;
import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.config.CompassSettings;
import org.compass.core.lucene.LuceneEnvironment;
import org.compass.core.lucene.engine.analyzer.synonym.SynonymLookupProvider;
import org.compass.core.test.AbstractTestCase;

/**
 * @author kimchy
 */
public class AnalyzerFilterTests extends AbstractTestCase {

    private static final String TEXT = "the quick brown fox jumped over the lazy dogs";

    public static class TestLookupSynonymProvider implements SynonymLookupProvider {

        public void configure(CompassSettings settings) throws CompassException {
        }

        public String[] lookupSynonyms(String value) {
            if (value.equals("quick")) {
                return new String[]{"fast"};
            }
            return null;
        }
    }

    protected String[] getMappings() {
        return new String[]{"analyzerfilter/osem.cpm.xml"};
    }

    protected void addSettings(CompassSettings settings) {
        settings.setGroupSettings(LuceneEnvironment.AnalyzerFilter.PREFIX, "synm",
                new String[]{LuceneEnvironment.AnalyzerFilter.TYPE, LuceneEnvironment.AnalyzerFilter.Synonym.LOOKUP},
                new String[]{LuceneEnvironment.AnalyzerFilter.SYNONYM_TYPE, TestLookupSynonymProvider.class.getName()});

        settings.setGroupSettings(LuceneEnvironment.Analyzer.PREFIX, LuceneEnvironment.Analyzer.DEFAULT_GROUP,
                new String[] {LuceneEnvironment.Analyzer.FILTERS},
                new String[] {"synm"});
    }

    public void testSimpleLookupSynonym() throws Exception {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        Long id = new Long(1);
        A a = new A();
        a.setId(id);
        a.setValue(TEXT);
        session.save(a);

        CompassHits hits = session.find("value:quick");
        assertEquals(1, hits.getLength());
        // test for the all property as well
        hits = session.find("quick");
        assertEquals(1, hits.getLength());

        hits = session.find("value:fast");
        assertEquals(1, hits.getLength());
        // test for the all property as well
        hits = session.find("fast");
        assertEquals(1, hits.getLength());

        tr.commit();
        session.close();
    }
}
