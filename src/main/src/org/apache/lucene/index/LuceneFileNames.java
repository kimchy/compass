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

package org.apache.lucene.index;

import java.util.HashSet;
import java.util.Set;

import org.compass.core.lucene.engine.manager.IndexHoldersCache;
import org.compass.core.lucene.engine.spellcheck.DefaultLuceneSpellCheckManager;

/**
 * A set of utility methods for index file names.
 *
 * @author kimchy
 */
public class LuceneFileNames {

    private static final Set<String> staticFiles;

    static {
        staticFiles = new HashSet<String>();
        staticFiles.add("segments.gen");
        staticFiles.add(IndexHoldersCache.CLEAR_CACHE_NAME);
        staticFiles.add(DefaultLuceneSpellCheckManager.SPELL_CHECK_VERSION_FILENAME);
    }

    /**
     * Returns if this file name is a static file. A static file is a file that is updated
     * and changed by Lucene.
     */
    public static boolean isStaticFile(String name) {
        return staticFiles.contains(name);
    }

    /**
     * Returns if the name is a segment file or not.
     */
    public static boolean isSegmentsFile(String name) {
        return name.startsWith(IndexFileNames.SEGMENTS) || name.equals(IndexFileNames.SEGMENTS_GEN);
    }

}
