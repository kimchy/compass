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

package org.compass.core.lucene.search;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiReader;

/**
 * An extension to Lucene {@link org.apache.lucene.index.MultiReader} that can be cached.
 *
 * @author kimchy
 */
public class CacheableMultiReader extends MultiReader {

    public CacheableMultiReader(IndexReader[] subReaders) {
        super(subReaders);
    }

    public CacheableMultiReader(IndexReader[] subReaders, boolean closeSubReaders) {
        super(subReaders, closeSubReaders);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof CacheableMultiReader)) {
            return false;
        }
        CacheableMultiReader other = (CacheableMultiReader) obj;
        int length = this.subReaders.length;
        if (length != other.subReaders.length) {
            return false;
        }
        for (int index = 0; index < length; index++) {
            if (!this.subReaders[index].equals(other.subReaders[index])) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = 0;
        for (Object reader : this.subReaders) {
            result = 31 * result + reader.hashCode();
        }
        return result;
    }
}
