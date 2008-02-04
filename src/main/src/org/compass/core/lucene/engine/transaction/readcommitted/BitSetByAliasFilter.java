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
import java.util.BitSet;
import java.util.HashMap;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Filter;

/**
 * A Lucene filter which stored deletion (per alias), and filters them when
 * execution lucene operations.
 *
 * @author kimchy
 */
public class BitSetByAliasFilter extends Filter {

    public static class AllSetBitSet extends BitSet {

        public AllSetBitSet() {
        }

        public int cardinality() {
            throw new UnsupportedOperationException();
        }

        public int hashCode() {
            return System.identityHashCode(this);
        }

        public int length() {
            throw new UnsupportedOperationException();
        }

        public int size() {
            throw new UnsupportedOperationException();
        }

        public void clear() {
            throw new UnsupportedOperationException();
        }

        public boolean isEmpty() {
            return false;
        }

        public int nextClearBit(int fromIndex) {
            throw new UnsupportedOperationException();
        }

        public int nextSetBit(int fromIndex) {
            throw new UnsupportedOperationException();
        }

        public void clear(int bitIndex) {
            throw new UnsupportedOperationException();
        }

        public void set(int bitIndex) {
            // do nothing
        }

        public boolean get(int bitIndex) {
            return true;
        }

        public void clear(int fromIndex, int toIndex) {
            throw new UnsupportedOperationException();
        }

        public void flip(int fromIndex, int toIndex) {
            throw new UnsupportedOperationException();
        }

        public void set(int fromIndex, int toIndex) {
            // do nothing
        }

        public void set(int fromIndex, int toIndex, boolean value) {
            // do nothing
        }

        public void set(int bitIndex, boolean value) {
            // do nothing
        }

        public Object clone() {
            return super.clone();    //To change body of overridden methods use File | Settings | File Templates.
        }

        public boolean equals(Object obj) {
            throw new UnsupportedOperationException();
        }

        public String toString() {
            return "AllBitSet";
        }

        public BitSet get(int fromIndex, int toIndex) {
            throw new UnsupportedOperationException();
        }

        public void and(BitSet set) {
            throw new UnsupportedOperationException();
        }

        public void andNot(BitSet set) {
            throw new UnsupportedOperationException();
        }

        public void or(BitSet set) {
            throw new UnsupportedOperationException();
        }

        public void xor(BitSet set) {
            throw new UnsupportedOperationException();
        }

        public boolean intersects(BitSet set) {
            throw new UnsupportedOperationException();
        }
    }

    private static final AllSetBitSet allSetBitSet = new AllSetBitSet();

    private HashMap<IndexReader, BitSet> bitSets = new HashMap<IndexReader, BitSet>();

    private boolean hasDeletes = false;

    public BitSetByAliasFilter() {
    }

    public void clear() {
        bitSets.clear();
        hasDeletes = false;
    }

    public boolean hasDeletes() {
        return hasDeletes;
    }

    public void markDelete(IndexReader indexReader, int docNum, int maxDoc) {
        BitSet bitSet = bitSets.get(indexReader);
        if (bitSet == null) {
            bitSet = new BitSet(maxDoc);
            bitSet.set(0, maxDoc, true);
            bitSets.put(indexReader, bitSet);
        }
        bitSet.set(docNum, false);
        hasDeletes = true;
    }

    public BitSet bits(IndexReader reader) throws IOException {
        BitSet bitSet = bitSets.get(reader);
        if (bitSet != null) {
            return bitSet;
        }
        return allSetBitSet;
    }
}