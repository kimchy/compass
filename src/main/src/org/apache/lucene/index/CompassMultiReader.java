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

import org.apache.lucene.store.Directory;

/**
 * A specific Lucene <code>MultiReader</code> which holds the sub index that
 * it is associated with.
 * 
 * @author kimchy
 */
public class CompassMultiReader extends MultiReader implements HasSubIndexReader {

    private String subIndex;

    public CompassMultiReader(String subIndex, Directory directory, SegmentInfos sis, boolean closeDirectory,
            IndexReader[] subReaders) {
        super(directory, sis, closeDirectory, subReaders);
        this.subIndex = subIndex;
    }

    public String getSubIndex() {
        return subIndex;
    }
}
