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

package org.compass.core.lucene.engine;

import org.compass.core.engine.SearchEngineException;

/**
 * Some operations that return results from {@link LuceneSearchEngine} do not have
 * to be closed, and they will be closed automatically when the transaction commits/rollsback.
 * 
 * @author kimchy
 */
public interface LuceneDelegatedClose {

    /**
     * Closes the delegate. Note, there is no need to remove the delegate using
     * {@link LuceneSearchEngine#removeDelegatedClose(LuceneDelegatedClose)}.
     */
    void closeDelegate() throws SearchEngineException;
}
