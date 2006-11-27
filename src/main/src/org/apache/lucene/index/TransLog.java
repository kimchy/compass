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

import org.apache.lucene.store.Directory;
import org.compass.core.config.CompassConfigurable;

/**
 * Controls the {@link org.apache.lucene.index.TransIndex} transaction log.
 *
 * @author kimchy
 */
public interface TransLog extends CompassConfigurable {

    /**
     * Returns the Lucene directory the transaction log will be written to
     */
    Directory getDirectory();

    /**
     * Returns true if the transaction segments should be written for each
     * change made to it.
     */
    boolean shouldUpdateTransSegments();

    /**
     * Closes the transaction log.
     */
    void close() throws IOException;

    /**
     * A callback notifiying the trasaction log that a document was added
     * (can be used for flushing for example).
     */
    void onDocumentAdded() throws IOException;
}
