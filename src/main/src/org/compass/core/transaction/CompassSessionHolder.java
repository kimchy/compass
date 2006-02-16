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

package org.compass.core.transaction;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.compass.core.CompassSession;

public class CompassSessionHolder {

    private static final Object DEFAULT_KEY = new Object();

    private final Map sessionMap = Collections.synchronizedMap(new HashMap(1));

    public CompassSessionHolder() {
    }

    public CompassSessionHolder(CompassSession session) {
        addSession(session);
    }

    public CompassSessionHolder(Object key, CompassSession session) {
        addSession(key, session);
    }

    public CompassSession getSession() {
        return getSession(DEFAULT_KEY);
    }

    public CompassSession getSession(Object key) {
        return (CompassSession) sessionMap.get(key);
    }

    public void addSession(CompassSession session) {
        sessionMap.put(DEFAULT_KEY, session);
    }

    public void addSession(Object key, CompassSession session) {
        sessionMap.put(key, session);
    }

    public CompassSession removeSession() {
        return removeSession(DEFAULT_KEY);
    }

    public CompassSession removeSession(Object key) {
        return (CompassSession) sessionMap.remove(key);
    }

    public boolean containSession(CompassSession session) {
        return sessionMap.containsValue(session);
    }

    public boolean isEmpty() {
        return sessionMap.isEmpty();
    }

    public boolean doesNotHoldNonDefaultSession() {
        synchronized (this.sessionMap) {
            return this.sessionMap.isEmpty()
                    || (this.sessionMap.size() == 1 && this.sessionMap.containsKey(DEFAULT_KEY));
        }
    }

}
