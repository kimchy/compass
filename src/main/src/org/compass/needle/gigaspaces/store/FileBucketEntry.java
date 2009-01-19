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
 * A file bucket entry holds a bucket (part) of the file content.
 *
 * @author kimchy
 */
public class FileBucketEntry implements Entry, Externalizable {

    public String indexName;

    public String fileName;

    public Long bucketIndex;

    public byte[] data;

    public FileBucketEntry() {

    }

    public FileBucketEntry(String indexName, String fileName) {
        this.indexName = indexName;
        this.fileName = fileName;
    }

    public FileBucketEntry(String indexName, String fileName, long bucketIndex, byte[] data) {
        this.indexName = indexName;
        this.fileName = fileName;
        this.bucketIndex = bucketIndex;
        this.data = data;
    }

    public String getIndexName() {
        return this.indexName;
    }

    public String getFileName() {
        return this.fileName;
    }

    public byte[] getData() {
        return this.data;
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeUTF(indexName);
        if (fileName == null) {
            out.writeBoolean(false);
        } else {
            out.writeBoolean(true);
            out.writeUTF(fileName);
        }
        if (bucketIndex == null) {
            out.writeBoolean(false);
        } else {
            out.writeBoolean(true);
            out.writeLong(bucketIndex);
        }
        if (data == null) {
            out.writeInt(0);
        } else {
            out.writeInt(data.length);
            out.write(data);
        }
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        indexName = in.readUTF();
        if (in.readBoolean()) {
            fileName = in.readUTF();
        }
        if (in.readBoolean()) {
            bucketIndex = in.readLong();
        }
        int size = in.readInt();
        if (size > 0) {
            data = new byte[size];
            int index = 0;
            while (true) {
                int bytesRead = in.read(data, index, size);
                if (bytesRead == size) {
                    break;
                }
                index += bytesRead;
                size -= bytesRead;
            }
        }
    }

    public static String[] __getSpaceIndexedFields() {
        // bucketIndex is our routing index
        // TODO: we might want to have fileName + bucketIndex as the routing index
        return new String[]{"bucketIndex", "fileName"};
    }
}
