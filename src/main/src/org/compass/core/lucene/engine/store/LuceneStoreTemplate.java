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

package org.compass.core.lucene.engine.store;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.store.Directory;
import org.compass.core.engine.SearchEngineException;

public class LuceneStoreTemplate {

    private static Log log = LogFactory.getLog(LuceneStoreTemplate.class);

    private LuceneSearchEngineStore searchEngineStore;

    public LuceneStoreTemplate(LuceneSearchEngineStore searchEngineStore) {
        this.searchEngineStore = searchEngineStore;
    }

    public Object executeForSubIndex(String subIndex, boolean create, LuceneSearchEngineStore.LuceneStoreCallback callback)
            throws SearchEngineException {
        Directory dir = searchEngineStore.getDirectoryBySubIndex(subIndex, create);
        return execute(dir, callback);
    }

    public Object executeForAlias(String alias, boolean create, LuceneSearchEngineStore.LuceneStoreCallback callback)
            throws SearchEngineException {
        Directory dir = searchEngineStore.getDirectoryByAlias(alias, create);
        return execute(dir, callback);
    }

    public Object execute(Directory dir, LuceneSearchEngineStore.LuceneStoreCallback callback) throws SearchEngineException {
        Exception ex = null;
        try {
            return callback.doWithStore(dir);
        } catch (Exception e) {
            ex = e;
        }
        try {
            if (dir != null) {
                dir.close();
            }
        } catch (Exception e) {
            if (ex == null) {
                ex = e;
            } else {
                log.warn("Caught an exception trying to close the lucene directory "
                        + "with other exception pending, logging and ignoring", e);
            }
        }
        if (ex != null) {
            if (ex instanceof SearchEngineException) {
                throw (SearchEngineException) ex;
            }
            throw new SearchEngineException("Failed while executing a lucene directory based operation", ex);
        }
        return null;
    }
}
