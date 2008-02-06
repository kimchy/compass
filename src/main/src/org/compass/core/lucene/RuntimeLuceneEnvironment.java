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

package org.compass.core.lucene;

/**
 * A set of settings constants that applies on the Compass session level.
 *
 * @author kimchy
 */
public abstract class RuntimeLuceneEnvironment {

    public static abstract class Transaction {

        /**
         * Transaction log settings
         */
        public static final class ReadCommittedTransLog {

            /**
             * The connection type for the read committed transactional log. Can be either <code>ram://</code>
             * or <code>file://</code>.
             */
            public static final String CONNECTION = "compass.transaction.readcommitted.translog.connection";

            /**
             * Should the transactional index be optimized before it is added to the actual index. Defaults to
             * <code>true</code>.
             */
            public static final String OPTIMIZE_TRANS_LOG = "compass.transaction.readcommitted.translog.optimize";
        }
    }

    /**
     * Controls Lucene {@link org.apache.lucene.index.MergePolicy} configuration.
     */
    public static abstract class MergePolicy {

        /**
         * The prefix setting for merge policy.
         */
        public static final String PREFIX = "compass.engine.merge.policy";

        /**
         * The type of the {@link org.compass.core.lucene.engine.merge.policy.MergePolicyProvider} that
         * will be created. Can be one of the constant names of specific types (inner classes) or the
         * FQN of a merge policy provider.
         */
        public static final String TYPE = PREFIX + ".type";

        /**
         * Allows to cofnigure {@link org.apache.lucene.index.LogByteSizeMergePolicy}.
         */
        public abstract class LogByteSize {

            /**
             * The name of the merge policy to be used with the merge policy type.
             */
            public static final String NAME = "logbytesize";

            /**
             * @see {@link org.apache.lucene.index.LogByteSizeMergePolicy#setMaxMergeMB(double)}.
             */
            public static final String MAX_MERGE_MB = PREFIX + ".maxMergeMB";

            /**
             * @see {@link org.apache.lucene.index.LogByteSizeMergePolicy#setMinMergeMB(double)}.
             */
            public static final String MIN_MERGE_MB = PREFIX + ".minMergeMB";
        }

        /**
         * Allows to configure {@link org.apache.lucene.index.LogDocMergePolicy}.
         */
        public abstract class LogDoc {

            /**
             * The name of the merge policy to be used with the merge policy type.
             */
            public static final String NAME = "logdoc";

            /**
             * @see {@link org.apache.lucene.index.LogDocMergePolicy#setMaxMergeDocs(int)}.
             */
            public static final String MAX_MERGE_DOCS = PREFIX + ".maxMergeDocs";

            /**
             * @see {@link org.apache.lucene.index.LogDocMergePolicy#setMinMergeDocs(int)}.
             */
            public static final String MIN_MERGE_DOCS = PREFIX + ".minMergeDocs";
        }
    }
}
