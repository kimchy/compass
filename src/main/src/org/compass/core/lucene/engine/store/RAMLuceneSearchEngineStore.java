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

import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.compass.core.engine.SearchEngineException;

/**
 * @author kimchy
 */
public class RAMLuceneSearchEngineStore extends AbstractLuceneSearchEngineStore {

    private Map<String, RAMDirectory> ramIndexes = new HashMap<String, RAMDirectory>();

    public RAMLuceneSearchEngineStore(String path, String subContext) {
        super(path, subContext);
        // nothing to do with the path yet..
    }

    protected synchronized Directory doOpenDirectoryBySubIndex(String subIndex, boolean create) throws SearchEngineException {
        RAMDirectory directory = ramIndexes.get(subIndex);
        if (directory == null && create) {
            directory = new RAMDirectory();
            ramIndexes.put(subIndex, directory);
        }
        return directory;
    }

    protected void doCleanIndex(String subIndex) throws SearchEngineException {
        ramIndexes.remove(subIndex);
        ramIndexes.put(subIndex, new RAMDirectory());
    }

    protected synchronized void doDeleteIndex() throws SearchEngineException {
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
