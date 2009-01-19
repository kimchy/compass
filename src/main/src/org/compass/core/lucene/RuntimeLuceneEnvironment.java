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

package org.compass.core.lucene;

/**
 * A set of settings constants that applies on the Compass session level.
 *
 * @author kimchy
 */
public abstract class RuntimeLuceneEnvironment {

    /**
     * Specific environment settings for the <code>batch_insert</code> settings.
     */
    public static abstract class SearchEngineIndex {

        /**
         * <p>Determines the largest segment (measured by
         * document count) that may be merged with other segments.
         * Small values (e.g., less than 10,000) are best for
         * interactive indexing, as this limits the length of
         * pauses while indexing to a few seconds.  Larger values
         * are best for batched indexing and speedier
         * searches.</p>
         *
         * <p>The default value is {@link Integer#MAX_VALUE}.</p>
         */
        public static final String MAX_MERGE_DOCS = "compass.engine.maxMergeDocs";

        /**
         * Determines how often segment indices are merged by addDocument().  With
         * smaller values, less RAM is used while indexing, and searches on
         * unoptimized indices are faster, but indexing speed is slower.  With larger
         * values, more RAM is used during indexing, and while searches on unoptimized
         * indices are slower, indexing is faster.  Thus larger values (> 10) are best
         * for batch index creation, and smaller values (< 10) for indices that are
         * interactively maintained.
         *
         * <p>Defaults to <code>10</code>.
         */
        public static final String MERGE_FACTOR = "compass.engine.mergeFactor";

        /**
         * Determines the minimal number of documents required
         * before the buffered in-memory documents are flushed as
         * a new Segment.  Large values generally gives faster
         * indexing.
         *
         * <p>When this is set, the writer will flush every
         * maxBufferedDocs added documents.  Pass in {@link
         * org.apache.lucene.index.IndexWriter#DISABLE_AUTO_FLUSH} to prevent triggering a flush due
         * to number of buffered documents.  Note that if flushing
         * by RAM usage is also enabled, then the flush will be
         * triggered by whichever comes first.
         *
         * <p>Disabled by default (writer flushes by RAM usage).
         */
        public static final String MAX_BUFFERED_DOCS = "compass.engine.maxBufferedDocs";

        /**
         * <p>Determines the minimal number of delete terms required before the buffered
         * in-memory delete terms are applied and flushed. If there are documents
         * buffered in memory at the time, they are merged and a new segment is
         * created.</p>
         *
         * <p>Disabled by default (writer flushes by RAM usage).</p>
         */
        public static final String MAX_BUFFERED_DELETED_TERMS = "compass.engine.maxBufferedDeletedTerms";

        /**
         * Determines the amount of RAM that may be used for
         * buffering added documents before they are flushed as a
         * new Segment.  Generally for faster indexing performance
         * it's best to flush by RAM usage instead of document
         * count and use as large a RAM buffer as you can.
         *
         * <p>When this is set, the writer will flush whenever
         * buffered documents use this much RAM.  Pass in {@link
         * org.apache.lucene.index.IndexWriter#DISABLE_AUTO_FLUSH} to prevent triggering a flush due
         * to RAM usage.  Note that if flushing by document count
         * is also enabled, then the flush will be triggered by
         * whichever comes first.</p>
         *
         * <p> The default value is {@link org.apache.lucene.index.IndexWriter#DEFAULT_RAM_BUFFER_SIZE_MB}.</p>
         */
        public static final String RAM_BUFFER_SIZE = "compass.engine.ramBufferSize";
    }


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

    /**
     * Controls Lucene {@link org.compass.core.lucene.LuceneEnvironment.MergeScheduler} configuration.
     */
    public static abstract class MergeScheduler {

        /**
         * The prefix setting for merge scheduler.
         */
        public static final String PREFIX = "compass.engine.merge.scheduler";

        /**
         * The type of the {@link org.compass.core.lucene.engine.merge.scheduler.MergeSchedulerProvider} that
         * will be created. Can be one of the constant names of specific types (inner classes) or the
         * FQN of a merge scheduler provider.
         */
        public static final String TYPE = PREFIX + ".type";

        /**
         * Allows to cofnigure {@link org.apache.lucene.index.SerialMergeScheduler}.
         */
        public abstract class Serial {

            /**
             * The name of the serial merge scheduler to be used as the merge scheduler type.
             */
            public static final String NAME = "serial";
        }

        /**
         * Allows to configure {@link org.apache.lucene.index.ConcurrentMergeScheduler}.
         */
        public abstract class Concurrent {

            /**
             * The name of the concurrent merge scheduler to be used as the merge scheduler type.
             */
            public static final String NAME = "concurrent";

            /**
             * The maximum thread count that can be created for merges.
             */
            public static final String MAX_THREAD_COUNT = "maxThreadCount";

            /**
             * The thread priority of merge threads.
             */
            public static final String THREAD_PRIORITY = "threadPriority";
        }

        /**
         * Allows to configure Compass {@link org.apache.lucene.index.ExecutorMergeScheduler}.
         */
        public abstract class Executor {

            /**
             * The name of the executor merge scheduler to be used as the merge scheduler type.
             */
            public static final String NAME = "executor";

            /**
             * The maximum concurrent merges that are allowed to be executed.
             */
            public static final String MAX_CONCURRENT_MERGE = "maxConcurrentMerge";

        }
    }
}
