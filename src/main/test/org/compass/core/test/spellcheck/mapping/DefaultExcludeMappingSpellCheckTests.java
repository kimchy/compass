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

package org.compass.core.test.spellcheck.mapping;

import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.config.CompassSettings;
import org.compass.core.lucene.LuceneEnvironment;
import org.compass.core.mapping.SpellCheck;
import org.compass.core.test.AbstractTestCase;

/**
 * @author kimchy
 */
public class DefaultExcludeMappingSpellCheckTests extends AbstractTestCase {

    protected String[] getMappings() {
        return new String[]{"spellcheck/mapping/mapping.cpm.xml"};
    }

    protected void addSettings(CompassSettings settings) {
        settings.setBooleanSetting(LuceneEnvironment.SpellCheck.ENABLE, true);
        settings.setBooleanSetting(LuceneEnvironment.SpellCheck.SCHEDULE, false);
        settings.setSetting(LuceneEnvironment.SpellCheck.DEFAULT_MODE, SpellCheck.EXCLUDE.toString());
    }

    public void testSimplePropertiesSpellCheck() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        A a = new A();
        a.id = 1;
        a.value1 = "orange";
        a.value2 = "white";
        a.value3 = "black";
        session.save("a1", a);

        tr.commit();
        session.close();

        getCompass().getSpellCheckManager().concurrentRebuild();

        String[] suggestions = getCompass().getSpellCheckManager().suggestBuilder("orang").suggest().getSuggestions();
        assertEquals(1, suggestions.length);
        assertEquals("orange", suggestions[0]);

        suggestions = getCompass().getSpellCheckManager().suggestBuilder("blac").suggest().getSuggestions();
        assertEquals(1, suggestions.length);
        assertEquals("black", suggestions[0]);

        suggestions = getCompass().getSpellCheckManager().suggestBuilder("whit").suggest().getSuggestions();
        assertEquals(0, suggestions.length);
    }
}