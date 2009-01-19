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

package org.compass.needle.gigaspaces.store;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import net.jini.core.entry.Entry;

/**
 * A file entry holds the meta data of a file, without its content.
 *
 * @author kimchy
 */
public class FileEntry implements Entry, Externalizable {

    public String indexName;

    public String fileName;

    public Long lastModified;

    public Long size;

    public FileEntry() {

    }

    public FileEntry(String indexName, String fileName) {
        this.indexName = indexName;
        this.fileName = fileName;
    }

    public FileEntry(String indexName, String fileName, long size) {
        this.indexName = indexName;
        this.fileName = fileName;
        this.size = size;
        this.lastModified = System.currentTimeMillis();
    }

    public String getIndexName() {
        return this.indexName;
    }

    public String getFileName() {
        return this.fileName;
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

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeUTF(indexName);
        if (fileName == null) {
            out.writeBoolean(false);
        } else {
            out.writeBoolean(true);
            out.writeUTF(fileName);
        }
        if (size == null && lastModified == null) {
            out.writeBoolean(false);
        } else {
            out.writeBoolean(true);
            out.writeLong(size);
            out.writeLong(lastModified);
        }
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        indexName = in.readUTF();
        if (in.readBoolean()) {
            fileName = in.readUTF();
        }
        if (in.readBoolean()) {
            size = in.readLong();
            lastModified = in.readLong();
        }
    }

    public static String[] __getSpaceIndexedFields() {
        // fileName is our routing index
        return new String[]{"fileName"};
    }
}
