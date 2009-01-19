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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import com.tangosol.io.ExternalizableLite;

/**
 * A file entry holds the meta data of a file, without its content.
 *
 * @author kimchy
 */
public class FileHeaderValue implements ExternalizableLite {

    private long lastModified;

    private long size;

    // just here for serialization
    public FileHeaderValue() {
    }

    public FileHeaderValue(long lastModified, long size) {
        this.lastModified = lastModified;
        this.size = size;
    }

    public long getLastModified() {
        return this.lastModified;
    }

    public long getSize() {
        return this.size;
    }

    public void touch() {
        // we are using currentTime here, which should be sync between nodes (though some
        // minor difference won't matter that much with Lucene).
        this.lastModified = System.currentTimeMillis();
    }

    public void readExternal(DataInput in) throws IOException {
        size = in.readLong();
        lastModified = in.readLong();
    }

    public void writeExternal(DataOutput out) throws IOException {
        out.writeLong(size);
        out.writeLong(lastModified);
    }
}