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

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.compass.core.engine.SearchEngineException;

import java.util.HashMap;
import java.util.Map;

/**
 * @author kimchy
 */
public class RAMLuceneSearchEngineStore extends AbstractLuceneSearchEngineStore {

    private Map ramIndexes = new HashMap();

    public RAMLuceneSearchEngineStore(String path, String subContext) {
        super(path, subContext);
        // nothing to do with the path yet..
    }

    protected synchronized Directory doGetDirectoryForPath(String path, boolean create) throws SearchEngineException {
        RAMDirectory directory = (RAMDirectory) ramIndexes.get(path);
        if (directory == null && create) {
            directory = new RAMDirectory();
            ramIndexes.put(path, directory);
        }
        return directory;
    }

    public synchronized void deleteIndex() throws SearchEngineException {
        if (ramIndexes != null) {
            ramIndexes.clear();
        }
    }

    protected void doClose() {
        if (ramIndexes != null) {
            ramIndexes.clear();
            ramIndexes = null;
        }
    }
}
