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
// TODO This should be removed and we should use HitCollector
// TODO We are using ArrayList with Integer to record deletion, we should have a growable int[] for better performance
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

    public static class IntArray {

        private static final int DEFAULT_SIZE = 10;

        public int[] array;

        public int length;

        public IntArray() {
            array = new int[DEFAULT_SIZE];
            length = 0;
        }

        public void add(int val) {
            if (length == array.length) {
                int[] tempArray = new int[array.length + DEFAULT_SIZE];
                System.arraycopy(array, 0, tempArray, 0, array.length);
                array = tempArray;
            }
            array[length++] = val;
        }
    }

    private static final long serialVersionUID = 3618980083415921974L;

    private static final AllSetBitSet allSetBitSet = new AllSetBitSet();

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

    public IntArray getDeletesBySubIndex(String subIndex) {
        return (IntArray) deleteBySubIndex.get(subIndex);
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
        IntArray aliasDeletions = (IntArray) deleteBySubIndex.get(subIndex);
        if (aliasDeletions == null) {
            aliasDeletions = new IntArray();
            deleteBySubIndex.put(subIndex, aliasDeletions);
        }
        aliasDeletions.add(docNum);
        hasDeletes = true;
    }

    public BitSet bits(IndexReader reader) throws IOException {
        if (!(reader instanceof HasSubIndexReader)) {
            return allSetBitSet;
        }
        HasSubIndexReader hasSubIndexReader = (HasSubIndexReader) reader;
        BitSet bitSet = (BitSet) bitSets.get(hasSubIndexReader.getSubIndex());
        if (bitSet != null) {
            return bitSet;
        }
        return allSetBitSet;
    }
}
