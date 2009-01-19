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

package org.compass.annotations.test.spellcheck;

import org.compass.annotations.test.AbstractAnnotationsTestCase;
import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.config.CompassConfiguration;
import org.compass.core.config.CompassSettings;
import org.compass.core.engine.spellcheck.SearchEngineSpellCheckManager;
import org.compass.core.lucene.LuceneEnvironment;

/**
 * @author kimchy
 */
public class SpellCheckAnnotationTests extends AbstractAnnotationsTestCase {

    protected void addExtraConf(CompassConfiguration conf) {
        conf.addClass(A.class).addClass(B.class);
    }

    protected void addSettings(CompassSettings settings) {
        settings.setBooleanSetting(LuceneEnvironment.SpellCheck.ENABLE, true);
        settings.setBooleanSetting(LuceneEnvironment.SpellCheck.SCHEDULE, false);
    }

    public void testSearchInclude() {
        setUpData();

        SearchEngineSpellCheckManager spellCheckManager = getCompass().getSpellCheckManager();
        assertTrue(spellCheckManager.rebuild());

        String[] suggestions = spellCheckManager.suggestBuilder("whit").suggest().getSuggestions();
        assertEquals(1, suggestions.length);
        assertEquals("white", suggestions[0]);

        suggestions = spellCheckManager.suggestBuilder("whit").aliases("A").suggest().getSuggestions();
        assertEquals(1, suggestions.length);
        assertEquals("white", suggestions[0]);

        suggestions = spellCheckManager.suggestBuilder("blac").aliases("A").suggest().getSuggestions();
        assertEquals(0, suggestions.length);
    }

    public void testSearchExlcude() {
        setUpData();

        SearchEngineSpellCheckManager spellCheckManager = getCompass().getSpellCheckManager();
        assertTrue(spellCheckManager.rebuild());

        String[] suggestions = spellCheckManager.suggestBuilder("fiv").suggest().getSuggestions();
        assertEquals(1, suggestions.length);
        assertEquals("five", suggestions[0]);

        suggestions = spellCheckManager.suggestBuilder("fiv").aliases("B").suggest().getSuggestions();
        assertEquals(1, suggestions.length);
        assertEquals("five", suggestions[0]);

        suggestions = spellCheckManager.suggestBuilder("sixtee").aliases("B").suggest().getSuggestions();
        assertEquals(0, suggestions.length);
    }

    private void setUpData() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        A a = new A();
        a.id = 1;
        a.avalue1 = "white";
        a.avalue2 = "black";
        session.save(a);

        B b = new B();
        b.id = 1;
        b.bvalue1 = "five";
        b.bvalue2 = "sixteen";
        session.save(b);

        tr.commit();
        session.close();
    }
}
