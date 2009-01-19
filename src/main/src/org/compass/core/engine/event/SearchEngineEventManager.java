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

package org.compass.core.engine.event;

import java.util.ArrayList;

import org.compass.core.engine.SearchEngineException;

/**
 * @author kimchy
 */
public class SearchEngineEventManager implements SearchEngineLifecycleEventListener {

    private ArrayList<SearchEngineLifecycleEventListener> lifecycleListeners;

    public void registerLifecycleListener(SearchEngineLifecycleEventListener lifecycleEventListener) {
        if (lifecycleListeners == null) {
            lifecycleListeners = new ArrayList<SearchEngineLifecycleEventListener>();
        }
        lifecycleListeners.add(lifecycleEventListener);
    }

    public void removeLifecycleListener(SearchEngineLifecycleEventListener lifecycleEventListener) {
        if (lifecycleListeners == null) {
            return;
        }
        lifecycleListeners.remove(lifecycleEventListener);
    }

    public void beforeBeginTransaction() throws SearchEngineException {
        if (lifecycleListeners == null) {
            return;
        }
        for (SearchEngineLifecycleEventListener lifecycleListener : lifecycleListeners) {
            lifecycleListener.beforeBeginTransaction();
        }
    }

    public void afterBeginTransaction() throws SearchEngineException {
        if (lifecycleListeners == null) {
            return;
        }
        for (SearchEngineLifecycleEventListener lifecycleListener : lifecycleListeners) {
            lifecycleListener.afterBeginTransaction();
        }
    }

    public void afterPrepare() throws SearchEngineException {
        if (lifecycleListeners == null) {
            return;
        }
        for (SearchEngineLifecycleEventListener lifecycleListener : lifecycleListeners) {
            lifecycleListener.afterPrepare();
        }
    }

    public void afterCommit(boolean onePhase) throws SearchEngineException {
        if (lifecycleListeners == null) {
            return;
        }
        for (SearchEngineLifecycleEventListener lifecycleListener : lifecycleListeners) {
            lifecycleListener.afterCommit(onePhase);
        }
    }

    public void afterRollback() throws SearchEngineException {
        if (lifecycleListeners == null) {
            return;
        }
        for (SearchEngineLifecycleEventListener lifecycleListener : lifecycleListeners) {
            lifecycleListener.afterRollback();
        }
    }

    public void close() throws SearchEngineException {
        if (lifecycleListeners == null) {
            return;
        }
        for (SearchEngineLifecycleEventListener lifecycleListener : lifecycleListeners) {
            lifecycleListener.close();
        }
    }
}
