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

package org.compass.core.lucene.engine.store;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.compass.core.engine.SearchEngineException;

/**
 * A directory store implemented using memory. Uses Lucene {@link org.apache.lucene.store.RAMDirectory}.
 *
 * @author kimchy
 */
public class RAMDirectoryStore extends AbstractDirectoryStore {

    public static final String PROTOCOL = "ram://";

    private final Map<String, Map<String, RAMDirectory>> dirs = new ConcurrentHashMap<String, Map<String, RAMDirectory>>();

    public Directory open(String subContext, String subIndex) throws SearchEngineException {
        Map<String, RAMDirectory> subContextDirs = dirs.get(subContext);
        if (subContextDirs == null) {
            synchronized (dirs) {
                subContextDirs = dirs.get(subContext);
                if (subContextDirs == null) {
                    subContextDirs = new ConcurrentHashMap<String, RAMDirectory>();
                }
            }
        }
        RAMDirectory dir = subContextDirs.get(subIndex);
        if (dir == null) {
            synchronized (subContextDirs) {
                dir = subContextDirs.get(subIndex);
                if (dir == null) {
                    dir = new RAMDirectory();
                }
            }
        }
        return dir;
    }

    @Override
    public void cleanIndex(Directory dir, String subContext, String subIndex) throws SearchEngineException {
        deleteIndex(dir, subContext, subIndex);
    }

    @Override
    public void deleteIndex(Directory dir, String subContext, String subIndex) throws SearchEngineException {
        Map<String, RAMDirectory> subContextDirs = dirs.get(subContext);
        if (subContextDirs != null) {
            subContextDirs.remove(subIndex);
        }
    }

    @Override
    public String[] listSubIndexes(String subContext) throws SearchEngineException, UnsupportedOperationException {
        Map<String, RAMDirectory> subContextDirs = dirs.get(subContext);
        if (subContextDirs == null) {
            return null;
        }
        return subContextDirs.keySet().toArray(new String[0]);
    }

    public CopyFromHolder beforeCopyFrom(String subContext, String subIndex, Directory dir) throws SearchEngineException {
        try {
            String[] files = dir.list();
            for (String file : files) {
                dir.deleteFile(file);
            }
        } catch (IOException e) {
            throw new SearchEngineException("Faield to delete ram directory before copy", e);
        }
        return new CopyFromHolder();
    }
}
