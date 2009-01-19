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

package org.compass.core.test.spellcheck.simple;

import org.compass.core.CompassHits;
import org.compass.core.CompassQuery;
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
        assertTrue(spellCheckManager.rebuild());

        String[] suggestions = spellCheckManager.suggestBuilder("fiv").suggest().getSuggestions();
        assertEquals(1, suggestions.length);
        assertEquals("five", suggestions[0]);
        assertTrue(spellCheckManager.suggestBuilder("five").suggest().isExists());
    }

    public void testSimpleSpellCheckConcurrentRebuild() {
        setUpData();
        SearchEngineSpellCheckManager spellCheckManager = getCompass().getSpellCheckManager();
        assertTrue(spellCheckManager.concurrentRebuild());

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

    public void testNoRebuildQuery() {
        setUpData();

        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        CompassQuery query = session.queryBuilder().queryString("fiv").toQuery();
        CompassQuery suggeted = query.getSuggestedQuery();

        assertEquals(false, suggeted.isSuggested());
        assertEquals("fiv", suggeted.toString());

        assertFalse(query.isSuggested());
        assertEquals("fiv", query.toString());

        tr.commit();
        session.close();
    }

    public void testNoRebuildHits() {
        setUpData();

        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        CompassHits hits = session.queryBuilder().queryString("fiv").toQuery().hits();
        CompassQuery query = hits.getQuery();
        CompassQuery suggeted = hits.getSuggestedQuery();

        assertEquals(false, suggeted.isSuggested());
        assertEquals("fiv", suggeted.toString());

        assertFalse(query.isSuggested());
        assertEquals("fiv", query.toString());

        tr.commit();
        session.close();
    }

    public void testRebuildNeeded() throws Exception {
        setUpData();
        SearchEngineSpellCheckManager spellCheckManager = getCompass().getSpellCheckManager();

        assertTrue(spellCheckManager.isRebuildNeeded());
        spellCheckManager.rebuild();
        assertFalse(spellCheckManager.isRebuildNeeded());
    }

    public void testSuggestQueryString() {
        setUpData();
        SearchEngineSpellCheckManager spellCheckManager = getCompass().getSpellCheckManager();
        spellCheckManager.rebuild();

        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        assertEquals("fiv", session.queryBuilder().queryString("fiv").toQuery().toString());
        assertFalse("fiv", session.queryBuilder().queryString("fiv").toQuery().isSuggested());
        assertEquals(0, session.queryBuilder().queryString("fiv").toQuery().hits().length());
        assertEquals("five", session.queryBuilder().queryString("fiv").useSpellCheck().toQuery().toString());
        assertTrue(session.queryBuilder().queryString("fiv").useSpellCheck().toQuery().isSuggested());
        assertEquals(1, session.queryBuilder().queryString("fiv").useSpellCheck().toQuery().hits().length());

        assertFalse(session.queryBuilder().queryString("five").useSpellCheck().toQuery().isSuggested());

        assertEquals("+fiv +blak", session.queryBuilder().queryString("fiv blak").toQuery().toString());
        assertEquals(0, session.queryBuilder().queryString("fiv blak").toQuery().hits().length());
        assertEquals("+five +black", session.queryBuilder().queryString("fiv blak").useSpellCheck().toQuery().toString());
        assertEquals(0, session.queryBuilder().queryString("fiv blak").useSpellCheck().toQuery().hits().length());

        tr.commit();
        session.close();
    }

    public void testSuggestedQuery() {
        setUpData();
        SearchEngineSpellCheckManager spellCheckManager = getCompass().getSpellCheckManager();
        spellCheckManager.rebuild();

        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        CompassQuery query = session.queryBuilder().queryString("fiv").toQuery();
        CompassQuery suggeted = query.getSuggestedQuery();
        assertEquals(true, suggeted.isSuggested());
        assertEquals("five", suggeted.toString());

        assertFalse(query.isSuggested());
        assertEquals("fiv", query.toString());

        query = session.queryBuilder().queryString("fiv blak").toQuery();
        suggeted = query.getSuggestedQuery();
        assertEquals(true, suggeted.isSuggested());
        assertEquals("+five +black", suggeted.toString());

        assertFalse(query.isSuggested());
        assertEquals("+fiv +blak", query.toString());

        tr.commit();
        session.close();
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
