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
public class FileBucketValue implements ExternalizableLite {

    private byte[] data;

    // just here for serialization
    public FileBucketValue() {
    }

    public FileBucketValue(byte[] data) {
        this.data = data;
    }

    public byte[] getData() {
        return data;
    }

    public void readExternal(DataInput in) throws IOException {
        int size = in.readInt();
        data = new byte[size];
        in.readFully(data, 0, size);
    }

    public void writeExternal(DataOutput out) throws IOException {
        out.writeInt(data.length);
        out.write(data);
    }
}
