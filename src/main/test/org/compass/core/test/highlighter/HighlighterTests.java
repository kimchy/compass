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

package org.compass.core.test.highlighter;

import org.compass.core.CompassDetachedHits;
import org.compass.core.CompassHighlighter;
import org.compass.core.CompassHits;
import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.Resource;
import org.compass.core.config.CompassSettings;
import org.compass.core.engine.SearchEngineException;
import org.compass.core.lucene.LuceneEnvironment;
import org.compass.core.test.AbstractTestCase;

/**
 * @author kimchy
 */
public class HighlighterTests extends AbstractTestCase {

    private static String texts[] = {
            "Hello this is a piece of text that is very long and contains too much preamble and the meat is really here which says kennedy has been shot",
            "This piece of text refers to Kennedy at the beginning then has a longer piece of text that is very long in the middle and finally ends with another reference to Kennedy",
            "JFK has been shot", "John Kennedy has been shot", "This text has a typo in referring to Keneddy"};

    protected String[] getMappings() {
        return new String[]{"highlighter/highlighter.cpm.xml"};
    }

    protected void addSettings(CompassSettings settings) {
        super.addSettings(settings);
        settings.setGroupSettings(LuceneEnvironment.Highlighter.PREFIX, "smallFragmenter",
                new String[]{LuceneEnvironment.Highlighter.Fragmenter.SIMPLE_SIZE}, new String[]{"20"});
    }

    public void testSimpleHighlighting() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        setUpData(session);

        CompassHits hits = session.find("Kennedy");
        String fragment = hits.highlighter(0).fragment("text");
        assertEquals("John <b>Kennedy</b> has been shot", fragment);

        fragment = hits.highlighter(0).fragment("text", texts[3]);
        assertEquals("John <b>Kennedy</b> has been shot", fragment);

        // test automatic storing of highlighted text
        assertEquals(fragment, hits.highlightedText(0).getHighlightedText());
        CompassDetachedHits detachedHits = hits.detach();
        assertEquals(fragment, detachedHits.highlightedText(0).getHighlightedText());

        fragment = hits.highlighter(1).setHighlighter("smallFragmenter").setMaxNumFragments(3).fragmentsWithSeparator(
                "text");
        assertEquals("This piece of text refers to <b>Kennedy</b>... to <b>Kennedy</b>", fragment);
        assertEquals(fragment, hits.highlightedText(1).getHighlightedText());
        detachedHits = hits.detach();
        assertEquals(fragment, detachedHits.highlightedText(1).getHighlightedText());

        String fragments[] = hits.highlighter(1).setHighlighter("smallFragmenter").setMaxNumFragments(3).fragments(
                "text");
        assertEquals(2, fragments.length);
        assertEquals("This piece of text refers to <b>Kennedy</b>", fragments[0]);
        assertEquals(" to <b>Kennedy</b>", fragments[1]);


        tr.commit();
    }

    public void testNoTermVectorException() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        setUpData(session);

        CompassHits hits = session.find("Kennedy");

        String fragment = hits.highlighter(1).setTextTokenizer(CompassHighlighter.TextTokenizer.ANALYZER)
                .setHighlighter("smallFragmenter").setMaxNumFragments(3).fragmentsWithSeparator("text");
        assertEquals("This piece of text refers to <b>Kennedy</b>... to <b>Kennedy</b>", fragment);

        try {
            hits.highlighter(0).setTextTokenizer(CompassHighlighter.TextTokenizer.TERM_VECTOR).fragment("text");
            fail();
        } catch (SearchEngineException e) {

        }

        tr.commit();
    }

    public void testWithTermVectorException() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        setUpData(session, "a1");

        CompassHits hits = session.find("Kennedy");

        String fragment = hits.highlighter(1).setTextTokenizer(CompassHighlighter.TextTokenizer.ANALYZER)
                .setHighlighter("smallFragmenter").setMaxNumFragments(3).fragmentsWithSeparator("text");
        assertEquals("This piece of text refers to <b>Kennedy</b>... to <b>Kennedy</b>", fragment);

        fragment = hits.highlighter(1).setTextTokenizer(CompassHighlighter.TextTokenizer.TERM_VECTOR).setHighlighter(
                "smallFragmenter").setMaxNumFragments(3).fragmentsWithSeparator("text");
        assertEquals("This piece of text refers to <b>Kennedy</b>... to <b>Kennedy</b>", fragment);

        tr.commit();
    }

    private void setUpData(CompassSession session) {
        setUpData(session, "a");
    }

    private void setUpData(CompassSession session, String alias) {
        for (int i = 0; i < texts.length; i++) {
            Resource resource = session.createResource(alias);
            resource.addProperty("id", "" + i);
            resource.addProperty("text", texts[i]);
            session.save(resource);
        }
    }

}
