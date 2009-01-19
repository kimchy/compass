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

import org.compass.core.Compass;
import org.compass.core.config.CompassEnvironment;
import org.compass.core.config.CompassSettings;
import org.compass.core.engine.spellcheck.SearchEngineSpellCheckManager;

/**
 * @author kimchy
 */
public class FSSpellCheckTests extends SpellCheckTests {

    protected void addSettings(CompassSettings settings) {
        super.addSettings(settings);
        settings.setSetting(CompassEnvironment.CONNECTION, "file://target/test-index");
    }

    public void testRebuildNeeded2() throws Exception {
        setUpData();
        SearchEngineSpellCheckManager spellCheckManager = getCompass().getSpellCheckManager();

        assertTrue(spellCheckManager.isRebuildNeeded());
        spellCheckManager.rebuild();
        assertFalse(spellCheckManager.isRebuildNeeded());

        // we test this with FS since it is shared between compass instances (unlike RAM).
        Compass compass = buildCompass();
        assertFalse(compass.getSpellCheckManager().isRebuildNeeded());
        compass.close();
    }

}