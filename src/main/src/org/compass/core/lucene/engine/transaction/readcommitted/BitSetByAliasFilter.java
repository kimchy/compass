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

package org.compass.core.lucene.engine.transaction.readcommitted;

import java.io.IOException;
import java.util.HashMap;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.search.Filter;
import org.apache.lucene.util.OpenBitSet;

/**
 * A Lucene filter which stored deletion (per alias), and filters them when
 * execution lucene operations.
 *
 * @author kimchy
 */
public class BitSetByAliasFilter extends Filter {

    public static class AllSetBitSet extends DocIdSet {

        private int size;

        public AllSetBitSet(int size) {
            this.size = size;
        }

        public DocIdSetIterator iterator() {
            return new AllDocIdSetIterator();
        }

        private class AllDocIdSetIterator extends DocIdSetIterator {

            private int currentDoc = -1;

            public int doc() {
                return currentDoc;
            }

            public boolean next() throws IOException {
                return ++currentDoc < size;
            }

            public boolean skipTo(int target) throws IOException {
                currentDoc += target;
                return currentDoc < size;
            }
        }
    }

    private HashMap<IndexReader, DocIdSet> deletedBitSets = new HashMap<IndexReader, DocIdSet>();

    private HashMap<IndexReader, DocIdSet> allBitSets = new HashMap<IndexReader, DocIdSet>();

    private boolean hasDeletes = false;

    public BitSetByAliasFilter() {
    }

    public void clear() {
        deletedBitSets.clear();
        allBitSets.clear();
        hasDeletes = false;
    }

    public boolean hasDeletes() {
        return hasDeletes;
    }

    public void markDelete(IndexReader indexReader, int docNum, int maxDoc) {
        OpenBitSet bitSet = (OpenBitSet) deletedBitSets.get(indexReader);
        if (bitSet == null) {
            // TODO we can implement our own DocIdSet for marked deleted ones
            bitSet = new OpenBitSet(maxDoc);
            bitSet.set(0, maxDoc);
            deletedBitSets.put(indexReader, bitSet);
            allBitSets.remove(indexReader);
        }
        bitSet.fastClear(docNum);
        hasDeletes = true;
    }

    public DocIdSet getDocIdSet(IndexReader reader) throws IOException {
        DocIdSet bitSet = deletedBitSets.get(reader);
        if (bitSet != null) {
            return bitSet;
        }
        bitSet = allBitSets.get(reader);
        if (bitSet == null) {
            bitSet = new AllSetBitSet(reader.maxDoc());
            allBitSets.put(reader, bitSet);
        }
        return bitSet;
    }
}