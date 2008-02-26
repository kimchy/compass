/*
 * Copyright 2004-2006 the original author or authors.
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

package org.compass.core.test.spellcheck.simple;

import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.config.CompassSettings;
import org.compass.core.engine.spellcheck.SearchEngineSpellCheckManager;
import org.compass.core.lucene.LuceneEnvironment;
import org.compass.core.test.AbstractTestCase;

/**
 * @author kimcy
 */
public class SpellCheckTests extends AbstractTestCase {

    protected String[] getMappings() {
        return new String[]{"spellcheck/simple/mapping.cpm.xml"};
    }

    protected void addSettings(CompassSettings settings) {
        settings.setBooleanSetting(LuceneEnvironment.SpellCheck.ENABLE, true);
        settings.setBooleanSetting(LuceneEnvironment.SpellCheck.SCHEDULE, false);
    }

    public void testSimpleSpellCheck() {
        setUpData();
        SearchEngineSpellCheckManager spellCheckManager = getCompass().getSpellCheckManager();
        spellCheckManager.rebuild();

        String[] suggestions = spellCheckManager.suggestBuilder("fiv").suggest().getSuggestions();
        assertEquals(1, suggestions.length);
        assertEquals("five", suggestions[0]);
        assertTrue(spellCheckManager.suggestBuilder("five").suggest().isExists());
    }

    public void testSubIndexNarrowing() {
        setUpData();

        SearchEngineSpellCheckManager spellCheckManager = getCompass().getSpellCheckManager();
        spellCheckManager.rebuild();

        String[] suggestions = spellCheckManager.suggestBuilder("fiv").subIndexes("a1").suggest().getSuggestions();
        assertEquals(1, suggestions.length);
        assertEquals("five", suggestions[0]);
        assertTrue(spellCheckManager.suggestBuilder("five").subIndexes("a1").suggest().isExists());

        suggestions = spellCheckManager.suggestBuilder("fiv").subIndexes("a2").suggest().getSuggestions();
        assertEquals(0, suggestions.length);
        assertFalse(spellCheckManager.suggestBuilder("five").subIndexes("a2").suggest().isExists());
    }

    public void testAliasNarrowing() {
        setUpData();

        SearchEngineSpellCheckManager spellCheckManager = getCompass().getSpellCheckManager();
        spellCheckManager.rebuild();

        String[] suggestions = spellCheckManager.suggestBuilder("fiv").aliases("a1").suggest().getSuggestions();
        assertEquals(1, suggestions.length);
        assertEquals("five", suggestions[0]);
        assertTrue(spellCheckManager.suggestBuilder("five").aliases("a1").suggest().isExists());

        suggestions = spellCheckManager.suggestBuilder("fiv").aliases("a2").suggest().getSuggestions();
        assertEquals(0, suggestions.length);
        assertFalse(spellCheckManager.suggestBuilder("five").aliases("a2").suggest().isExists());
    }

    public void testMorePopular() {
        setUpData();
        SearchEngineSpellCheckManager spellCheckManager = getCompass().getSpellCheckManager();
        spellCheckManager.rebuild();

        String[] suggestions = spellCheckManager.suggestBuilder("fiv").morePopular(true).suggest().getSuggestions();
        assertEquals(1, suggestions.length);
        assertEquals("five", suggestions[0]);
    }

    public void testNoRebuild() {
        setUpData();
        SearchEngineSpellCheckManager spellCheckManager = getCompass().getSpellCheckManager();

        String[] suggestions = spellCheckManager.suggestBuilder("fiv").suggest().getSuggestions();
        assertEquals(0, suggestions.length);
        assertFalse(spellCheckManager.suggestBuilder("five").suggest().isExists());
    }

    public void testRebuildNeeded() {
        setUpData();
        SearchEngineSpellCheckManager spellCheckManager = getCompass().getSpellCheckManager();

        assertTrue(spellCheckManager.isRebuildNeeded());
        spellCheckManager.rebuild();
        assertFalse(spellCheckManager.isRebuildNeeded());
    }

    protected void setUpData() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        A a = new A();
        a.id = 1;
        a.value1 = "five";
        a.value2 = "sixteen";
        session.save("a1", a);

        a.value1 = "black";
        a.value2 = "white";
        session.save("a2", a);

        tr.commit();
        session.close();
    }
}
