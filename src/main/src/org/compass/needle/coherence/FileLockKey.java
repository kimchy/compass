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
public class FileLockKey implements FileKey, ExternalizableLite {

    private String indexName;

    private String lockName;

    // just here for serialization
    public FileLockKey() {
    }

    public FileLockKey(String indexName, String lockName) {
        this.indexName = indexName;
        this.lockName = lockName;
    }

    public String getIndexName() {
        return this.indexName;
    }

    public String getLockName() {
        return this.lockName;
    }

    public String getFileName() {
        return getLockName();
    }

    public byte getType() {
        return FILE_LOCK;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (((FileKey) o).getType() != getType()) return false;

        FileLockKey that = (FileLockKey) o;

        if (!lockName.equals(that.lockName)) return false;
        if (!indexName.equals(that.indexName)) return false;

        return true;
    }

    public int hashCode() {
        int result = getType();
        result = 17 * result + indexName.hashCode();
        result = 17 * result + lockName.hashCode();
        return result;
    }

    public void readExternal(DataInput in) throws IOException {
        indexName = in.readUTF();
        lockName = in.readUTF();
    }

    public void writeExternal(DataOutput out) throws IOException {
        out.writeUTF(indexName);
        out.writeUTF(lockName);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("fileLcokKey: indexName[").append(indexName).append("]");
        sb.append(" lockName[").append(lockName).append("]");
        return sb.toString();
    }
}
