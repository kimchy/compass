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
 * @author kimchy
 */
public class FileBucketKey implements FileKey, ExternalizableLite {

    private String indexName;

    private String fileName;

    private long bucketIndex;

    // just here for serialization
    public FileBucketKey() {
    }

    public FileBucketKey(String indexName, String fileName, long bucketIndex) {
        this.indexName = indexName;
        this.fileName = fileName;
        this.bucketIndex = bucketIndex;
    }

    public String getIndexName() {
        return indexName;
    }

    public String getFileName() {
        return fileName;
    }

    public long getBucketIndex() {
        return bucketIndex;
    }

    public byte getType() {
        return FILE_BUCKET;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (((FileKey) o).getType() != getType()) return false;

        FileBucketKey that = (FileBucketKey) o;

        if (bucketIndex != that.bucketIndex) return false;
        if (!fileName.equals(that.fileName)) return false;
        if (!indexName.equals(that.indexName)) return false;

        return true;
    }

    public int hashCode() {
        int result = getType();
        result = 31 * result + indexName.hashCode();
        result = 31 * result + fileName.hashCode();
        result = 31 * result + (int) (bucketIndex ^ (bucketIndex >>> 32));
        return result;
    }

    public void readExternal(DataInput in) throws IOException {
        indexName = in.readUTF();
        fileName = in.readUTF();
        bucketIndex = in.readLong();
    }

    public void writeExternal(DataOutput out) throws IOException {
        out.writeUTF(indexName);
        out.writeUTF(fileName);
        out.writeLong(bucketIndex);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("fileBucketKey: indexName[").append(indexName).append("]");
        sb.append(" fileName[").append(fileName).append("]");
        sb.append(" bucketIndex[").append(bucketIndex).append("]");
        return sb.toString();
    }
}
