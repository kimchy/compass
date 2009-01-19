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

package org.compass.core.test.config.json;

import java.util.Map;

import junit.framework.TestCase;
import org.compass.core.config.CompassConfiguration;
import org.compass.core.config.CompassEnvironment;
import org.compass.core.config.CompassSettings;

/**
 * @author kimchy
 */
public class JsonConfigurationTests extends TestCase {

    public void testSimpleConnection() {
        CompassConfiguration conf = new CompassConfiguration().configure("/org/compass/core/test/config/json/test1.json");

        CompassSettings settings = conf.getSettings();
        assertEquals("test-index", settings.getSetting(CompassEnvironment.CONNECTION));
        Map<String, CompassSettings> eventSettings = settings.getSettingGroups(CompassEnvironment.Event.PREFIX_PRE_CREATE);
        assertEquals("test.MyEvent1", eventSettings.get("event1").getSetting(CompassEnvironment.Event.TYPE));
        assertEquals("test.MyEvent2", eventSettings.get("event2").getSetting(CompassEnvironment.Event.TYPE));
    }
}
