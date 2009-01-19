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

package org.compass.core.test.converter;

import org.compass.core.config.CompassEnvironment;
import org.compass.core.config.CompassSettings;

/**
 * Same tests as {@link org.compass.core.test.converter.ConverterTests} just tests it
 * with injection of an actual instance of {@link org.compass.core.test.converter.SampleConverter}.
 *
 * @author kimchy
 */
public class ConverterObjectTests extends ConverterTests {

    protected void addSettings(CompassSettings settings) {
        settings.setGroupSettings(CompassEnvironment.Converter.PREFIX,
                "sample",
                new String[]{CompassEnvironment.Converter.TYPE, "seperator"},
                new Object[]{new SampleConverter(), "XXX1"});
    }
}
