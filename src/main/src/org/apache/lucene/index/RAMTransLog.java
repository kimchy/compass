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

package org.apache.lucene.index;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.compass.core.CompassException;
import org.compass.core.config.CompassSettings;

/**
 * A memory based transaction log. Fast, but long running transaciton or large
 * transactions might not fit into memory.
 *
 * @author kimchy
 */
public class RAMTransLog implements TransLog {

    private RAMDirectory dir;

    public RAMTransLog() {
        this.dir = new RAMDirectory();
    }

    public void configure(CompassSettings settings) throws CompassException {

    }

    public boolean shouldUpdateTransSegments() {
        return false;
    }

    public Directory getDirectory() {
        return this.dir;
    }

    public void onDocumentAdded() {
        
    }

    public void close() {
        dir.close();
        dir = null;
    }
}
