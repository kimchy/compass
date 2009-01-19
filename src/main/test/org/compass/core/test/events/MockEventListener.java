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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.compass.core.events.FilterOperation;
import org.compass.core.events.PreCreateEventListener;
import org.compass.core.events.PreDeleteEventListener;
import org.compass.core.events.PreSaveEventListener;

/**
 * @author kimchy
 */
public class MockEventListener implements PreCreateEventListener, PreDeleteEventListener, PreSaveEventListener {

    public static Map<String, List<MockHolder>> events = new HashMap<String, List<MockHolder>>();

    public static FilterOperation filter = FilterOperation.NO;

    public FilterOperation onPreCreate(String alias, Object obj) {
        addEvent("preCreate", alias, obj);
        return filter;
    }

    public FilterOperation onPreDelete(String alias, Object obj) {
        addEvent("preDelete", alias, obj);
        return filter;
    }

    public FilterOperation onPreSave(String alias, Object obj) {
        addEvent("preSave", alias, obj);
        return filter;
    }

    private void addEvent(String type, String alias, Object obj) {
        List<MockHolder> holders = events.get(type);
        if (holders == null) {
            holders = new ArrayList<MockHolder>();
            events.put(type, holders);
        }
        holders.add(new MockHolder(alias, obj));
    }

    public static class MockHolder {
        public String alias;
        public Object obj;

        public MockHolder(String alias, Object obj) {
            this.alias = alias;
            this.obj = obj;
        }
    }
}
