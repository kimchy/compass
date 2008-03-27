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

package org.compass.needle.terracotta;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.store.Directory;
import org.compass.core.CompassException;
import org.compass.core.config.CompassConfigurable;
import org.compass.core.config.CompassSettings;
import org.compass.core.engine.SearchEngineException;
import org.compass.core.lucene.engine.store.AbstractDirectoryStore;
import org.compass.core.lucene.engine.store.CopyFromHolder;

/**
 * A Compass direcoty store that will use the {@link org.compass.needle.terracotta.TerracottaDirectory}.
 *
 * @author kimchy
 */
public class TerracottaDirectoryStore extends AbstractDirectoryStore implements CompassConfigurable {

    public static final String BUFFER_SIZE_PROP = "compass.engine.store.tc.bufferSize";

    private final Map<String, Map<String, TerracottaDirectory>> dirs = new HashMap<String, Map<String, TerracottaDirectory>>();

    private int bufferSize;

    public void configure(CompassSettings settings) throws CompassException {
        bufferSize = settings.getSettingAsInt(BUFFER_SIZE_PROP, TerracottaDirectory.DEFAULT_BUFFER_SIZE);
    }

    public Directory open(String subContext, String subIndex) throws SearchEngineException {
        synchronized (dirs) {
            Map<String, TerracottaDirectory> subIndexDirs = dirs.get(subContext);
            if (subIndexDirs == null) {
                subIndexDirs = new HashMap<String, TerracottaDirectory>();
                dirs.put(subContext, subIndexDirs);
            }
            TerracottaDirectory dir = new TerracottaDirectory(bufferSize);
            subIndexDirs.put(subIndex, dir);
            return dir;
        }
    }

    public void cleanIndex(Directory dir, String subContext, String subIndex) throws SearchEngineException {
        deleteIndex(dir, subContext, subIndex);
    }

    public void deleteIndex(Directory dir, String subContext, String subIndex) throws SearchEngineException {
        synchronized (dirs) {
            Map<String, TerracottaDirectory> subIndexDirs = dirs.get(subContext);
            if (subIndexDirs != null) {
                subIndexDirs.remove(subIndex);
            }
        }
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
