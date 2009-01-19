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

import com.j_spaces.core.client.ClientUIDHandler;
import com.j_spaces.core.client.EntryInfo;
import com.j_spaces.core.client.MetaDataEntry;

/**
 * A file lock represent a lock held by an application.
 *
 * @author kimchy
 */
public class FileLock extends MetaDataEntry implements Externalizable {

    private static final String CLASS_NAME = FileLock.class.getName();

    public String indexName;

    public String lockName;

    public FileLock() {

    }

    public FileLock(String indexName, String lockName) {
        this.indexName = indexName;
        this.lockName = lockName;
        String uid = ClientUIDHandler.createUIDFromName(indexName + lockName, CLASS_NAME);
        __setEntryInfo(new EntryInfo(uid, 0));
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super._readExternal(in);
        indexName = in.readUTF();
        lockName = in.readUTF();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        super._writeExternal(out);
        out.writeUTF(indexName);
        out.writeUTF(lockName);
    }
}
