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

import java.io.IOException;
import java.util.Random;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.compass.core.CompassException;
import org.compass.core.config.CompassSettings;
import org.compass.core.engine.SearchEngineException;
import org.compass.core.lucene.LuceneEnvironment;
import org.compass.core.lucene.util.LuceneUtils;

/**
 * @author kimchy
 */
public class FSTransLog implements TransLog {

    private FSDirectory dir;

    // TODO need to find a better way to generate trans id
    private static Random transId = new Random();

    private static final String DEFAULT_LOCATION = System.getProperty("java.io.tmpdir") + "/compass/translog";

    public FSTransLog() {
    }

    public void configure(CompassSettings settings) throws CompassException {
        String location = settings.getSetting(LuceneEnvironment.Transaction.TransLog.PATH, DEFAULT_LOCATION);
        try {
            location = location + "/" + transId.nextLong();
            dir = FSDirectory.getDirectory(location, true);
        } catch (IOException e) {
            throw new SearchEngineException("Failed to create tran log location [" + location + "]");
        }
    }

    public Directory getDirectory() {
        return this.dir;
    }

    public boolean shouldUpdateTransSegments() {
        return false;
    }

    public void close() {
        dir.close();
        LuceneUtils.deleteDir(dir.getFile());
        dir = null;
    }
}
