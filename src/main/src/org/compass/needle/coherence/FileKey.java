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

package org.compass.needle.coherence;

/**
 * A generic interface for all the different type of keys stored within a Cohernece Cache (Map).
 *
 * @author kimchy
 */
public interface FileKey {

    public static final byte FILE_HEADER = 0;

    public static final byte FILE_BUCKET = 1;

    public static final byte FILE_LOCK = 2;

    /**
     * Returns the index name.
     */
    String getIndexName();

    /**
     * Returns the file name (within the index name).
     */
    String getFileName();

    /**
     * Returns the type of the key.
     *
     * @see #FILE_HEADER
     * @see #FILE_BUCKET
     * @see #FILE_LOCK
     */
    byte getType();
}
