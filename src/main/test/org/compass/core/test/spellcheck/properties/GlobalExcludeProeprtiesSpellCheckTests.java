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

package org.compass.core.test.spellcheck.properties;

import org.compass.core.config.CompassSettings;
import org.compass.core.lucene.LuceneEnvironment;
import org.compass.core.mapping.SpellCheck;

/**
 * @author kimchy
 */
public class GlobalExcludeProeprtiesSpellCheckTests extends AbstractGlobalPropertiesSpellCheckTests {

    protected void addSettings(CompassSettings settings) {
        super.addSettings(settings);
        settings.setSetting(LuceneEnvironment.SpellCheck.GLOBAL_EXCLUDE_PROPERTY, "value3");
        settings.setSetting(LuceneEnvironment.SpellCheck.DEFAULT_MODE, SpellCheck.INCLUDE.toString());
    }

}