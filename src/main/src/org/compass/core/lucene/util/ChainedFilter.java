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

package org.compass.core.lucene.util;

import java.io.IOException;
import java.util.BitSet;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Filter;
import org.compass.core.util.Parameter;

/**
 * <p/>
 * Allows multiple {@link Filter}s to be chained.
 * Logical operations such as <b>NOT</b> and <b>XOR</b>
 * are applied between filters. One operation can be used
 * for all filters, or a specific operation can be declared
 * for each filter.
 * </p>
 * <p/>
 * Order in which filters are called depends on
 * the position of the filter in the chain. It's probably
 * more efficient to place the most restrictive filters
 * /least computationally-intensive filters first.
 * </p>
 *
 * @author <a href="mailto:kelvint@apache.org">Kelvin Tan</a>
 */
public class ChainedFilter extends Filter {

	private static final long serialVersionUID = 9048275274970647058L;

	public static final class ChainedFilterType extends Parameter {

		private static final long serialVersionUID = 1983615628912707296L;

		private ChainedFilterType(String name) {
            super(name);
        }

        public static final ChainedFilterType OR = new ChainedFilterType("OR");
        public static final ChainedFilterType AND = new ChainedFilterType("AND");
        public static final ChainedFilterType ANDNOT = new ChainedFilterType("ANDNOT");
        public static final ChainedFilterType XOR = new ChainedFilterType("XOR");
    }

    /**
     * Logical operation when none is declared. Defaults to
     * {@link BitSet#or}.
     */
    public static ChainedFilterType DEFAULT  = ChainedFilterType.OR;

    /**
     * The filter chain
     */
    private Filter[] chain = null;

    private ChainedFilterType[] logicArray;

    private ChainedFilterType logic = null;

    /**
     * Ctor.
     *
     * @param chain The chain of filters
     */
    public ChainedFilter(Filter[] chain) {
        this.chain = chain;
    }

    /**
     * Ctor.
     *
     * @param chain      The chain of filters
     * @param logicArray Logical operations to apply between filters
     */
    public ChainedFilter(Filter[] chain, ChainedFilterType[] logicArray) {
        this.chain = chain;
        this.logicArray = logicArray;
    }

    /**
     * Ctor.
     *
     * @param chain The chain of filters
     * @param logic Logicial operation to apply to ALL filters
     */
    public ChainedFilter(Filter[] chain, ChainedFilterType logic) {
        this.chain = chain;
        this.logic = logic;
    }

    /**
     * {@link Filter#bits}.
     */
    public BitSet bits(IndexReader reader) throws IOException {
        if (logic != null)
            return bits(reader, logic);
        else if (logicArray != null)
            return bits(reader, logicArray);
        else
            return bits(reader, DEFAULT);
    }

    /**
     * Delegates to each filter in the chain.
     *
     * @param reader IndexReader
     * @param logic  Logical operation
     * @return BitSet
     */
    private BitSet bits(IndexReader reader, ChainedFilterType logic) throws IOException {
        BitSet result;
        int i = 0;

        /**
         * First AND operation takes place against a completely false
         * bitset and will always return zero results. Thanks to
         * Daniel Armbrust for pointing this out and suggesting workaround.
         */
        if (logic == ChainedFilterType.AND) {
            result = (BitSet) chain[i].bits(reader).clone();
            ++i;
        } else {
            result = new BitSet(reader.maxDoc());
        }

        for (; i < chain.length; i++) {
            doChain(result, reader, logic, chain[i]);
        }
        return result;
    }

    /**
     * Delegates to each filter in the chain.
     *
     * @param reader IndexReader
     * @param logic  Logical operation
     * @return BitSet
     */
    private BitSet bits(IndexReader reader, ChainedFilterType[] logic) throws IOException {
        if (logic.length != chain.length)
            throw new IllegalArgumentException("Invalid number of elements in logic array");
        BitSet result;
        int i = 0;

        /**
         * First AND operation takes place against a completely false
         * bitset and will always return zero results. Thanks to
         * Daniel Armbrust for pointing this out and suggesting workaround.
         */
        if (logic[0] == ChainedFilterType.AND) {
            result = (BitSet) chain[i].bits(reader).clone();
            ++i;
        } else {
            result = new BitSet(reader.maxDoc());
        }

        for (; i < chain.length; i++) {
            doChain(result, reader, logic[i], chain[i]);
        }
        return result;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("ChainedFilter: [");
        for (int i = 0; i < chain.length; i++) {
            sb.append(chain[i]);
            sb.append(' ');
        }
        sb.append(']');
        return sb.toString();
    }

    private void doChain(BitSet result, IndexReader reader,
                         ChainedFilterType logic, Filter filter) throws IOException {
        if (logic == ChainedFilterType.OR) {
            result.or(filter.bits(reader));
        } else if (logic == ChainedFilterType.AND) {
            result.and(filter.bits(reader));
        } else if (logic == ChainedFilterType.ANDNOT) {
            result.andNot(filter.bits(reader));
        } else if (logic == ChainedFilterType.XOR) {
            result.xor(filter.bits(reader));
        } else {
            doChain(result, reader, DEFAULT, filter);
        }
    }
}
