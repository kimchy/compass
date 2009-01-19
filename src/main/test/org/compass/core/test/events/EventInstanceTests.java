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

package org.compass.core.test.events;

import org.compass.core.config.CompassEnvironment;
import org.compass.core.config.CompassSettings;

/**
 * Same as event tests, simple tests with an actual instance of the event listener.
 *
 * @author kimchy
 */
public class EventInstanceTests extends EventTests {

    protected void addSettings(CompassSettings settings) {
        settings.setGroupSettings(CompassEnvironment.Event.PREFIX_PRE_CREATE, "1",
                new String[]{CompassEnvironment.Event.TYPE},
                new Object[]{new MockEventListener()});
        settings.setGroupSettings(CompassEnvironment.Event.PREFIX_PRE_DELETE, "1",
                new String[]{CompassEnvironment.Event.TYPE},
                new Object[]{new MockEventListener()});
        settings.setGroupSettings(CompassEnvironment.Event.PREFIX_PRE_SAVE, "1",
                new String[]{CompassEnvironment.Event.TYPE},
                new Object[]{new MockEventListener()});
    }
}