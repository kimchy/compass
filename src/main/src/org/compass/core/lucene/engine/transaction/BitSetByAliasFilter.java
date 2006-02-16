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

package org.compass.core.lucene.engine.transaction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.lucene.index.HasSubIndexReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Filter;

/**
 * A Lucene filter which stored deletion (per alias), and filters them when
 * execution lucene operations.
 * 
 * @author kimchy
 */
public class BitSetByAliasFilter extends Filter {

    private static final long serialVersionUID = 3618980083415921974L;

    private HashMap bitSets = new HashMap();

    private boolean hasDeletes = false;

    private HashMap deleteBySubIndex = new HashMap();

    public BitSetByAliasFilter() {
    }

    public void clear() {
        bitSets.clear();
        deleteBySubIndex.clear();
        hasDeletes = false;
    }

    public boolean hasDeletes() {
        return hasDeletes;
    }

    public ArrayList getDeletesBySubIndex(String subIndex) {
        return (ArrayList) deleteBySubIndex.get(subIndex);
    }

    public Iterator subIndexDeletesIt() {
        return deleteBySubIndex.keySet().iterator();
    }

    public void markDeleteBySubIndex(String subIndex, int docNum, int maxDoc) {
        BitSet bitSet = (BitSet) bitSets.get(subIndex);
        if (bitSet == null) {
            bitSet = new BitSet(maxDoc);
            bitSet.set(0, maxDoc, true);
            bitSets.put(subIndex, bitSet);
        }
        bitSet.set(docNum, false);
        ArrayList aliasDeletions = (ArrayList) deleteBySubIndex.get(subIndex);
        if (aliasDeletions == null) {
            aliasDeletions = new ArrayList();
            deleteBySubIndex.put(subIndex, aliasDeletions);
        }
        aliasDeletions.add(new Integer(docNum));
        hasDeletes = true;
    }

    public BitSet bits(IndexReader reader) throws IOException {
        if (!(reader instanceof HasSubIndexReader)) {
            return allBits(reader);
        }
        HasSubIndexReader hasSubIndexReader = (HasSubIndexReader) reader;
        BitSet bitSet = (BitSet) bitSets.get(hasSubIndexReader.getSubIndex());
        if (bitSet != null) {
            return bitSet;
        }
        return allBits(reader);
    }

    private BitSet allBits(IndexReader reader) {
        int maxDoc = reader.maxDoc();
        BitSet bitSet = new BitSet(maxDoc);
        bitSet.set(0, maxDoc, true);
        return bitSet;
    }
}
