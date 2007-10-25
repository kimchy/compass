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

import org.apache.lucene.store.jdbc.JdbcFileEntrySettings;
import org.apache.lucene.store.jdbc.index.JdbcBufferedIndexInput;
import org.apache.lucene.store.jdbc.index.JdbcBufferedIndexOutput;
import org.apache.lucene.store.jdbc.index.RAMAndFileJdbcIndexOutput;

/**
 * @author kimchy
 */
public class LuceneEnvironment {

    /**
     * The analyzer that will be used for the all property. Optional and will
     * default to the default analyzer.
     */
    public static final String ALL_ANALYZER = "compass.engine.all.analyzer";

    /**
     * The default search that will be used for non prefixed query values.
     * Defaults to the value of the "all" property.
     */
    public static final String DEFAULT_SEARCH = "compass.engine.defaultsearch";

    public static abstract class Analyzer {

        /**
         * The prefix used for analyzer groups.
         */
        public static final String PREFIX = "compass.engine.analyzer";

        /**
         * The default anayzer group that must be set.
         */
        public static final String DEFAULT_GROUP = "default";

        /**
         * An optional analyzer group name that can be set, will be used when
         * searching.
         */
        public static final String SEARCH_GROUP = "search";

        /**
         * The name of the analyzer to use, can be ANALYZER_WHITESPACE,
         * ANALYZER_STANDARD, ANALYZER_SIMPLE, ANALYZER_STOP, or a fully
         * qualified class of the analyzer.
         *
         * <p>It is part of the anaylzer group, and should be constructed using the
         * {@link org.compass.core.config.CompassSettings#setGroupSettings(String,String,String[],String[])},
         * with the {@link #PREFIX} as the prefix, the analyzer group
         * name, and the type as one of the values.
         */
        public static final String TYPE = "type";

        /**
         * The fully qualified name of the anayzer factory. Must implement the
         * {@link org.compass.core.lucene.engine.analyzer.LuceneAnalyzerFactory}
         * inteface.
         *
         * <p>It is part of the anaylzer group, and should be constructed using the
         * {@link org.compass.core.config.CompassSettings#setGroupSettings(String,String,String[],String[])},
         * with the {@link #PREFIX} as the prefix, the analyzer group
         * name, and the type as one of the values.
         */
        public static final String FACTORY = "factory";

        /**
         * A comma separated list of stop words to use with the chosen analyzer.
         * If the string starts with <code>+</code>, the list of stop-words
         * will be added to the default set of stop words defined for the
         * analyzer. Only supported for the default analyzers that comes with
         * Compass.
         *
         * <p>It is part of the anaylzer group, and should be
         * constructed using the
         * {@link org.compass.core.config.CompassSettings#setGroupSettings(String,String,String[],String[])},
         * with the {@link #PREFIX} as the prefix, the analyzer group
         * name, and the stopwords as one of the values.
         */
        public static final String STOPWORDS = "stopwords";

        /**
         * A comma separated list of filter names to be applied to the analyzer. The names
         * match {@link org.compass.core.lucene.engine.analyzer.LuceneAnalyzerTokenFilterProvider}s
         * configured using the {@link AnalyzerFilter} configuration settings.
         */
        public static final String FILTERS = "filters";

        public abstract class CoreTypes {

            /**
             * An analyzer which tokenizes a text according to whitespaces.
             *
             * @see org.apache.lucene.analysis.WhitespaceAnalyzer
             */
            public static final String WHITESPACE = "whitespace";

            /**
             * The standard lucene analyzer.
             *
             * @see org.apache.lucene.analysis.standard.StandardAnalyzer
             */
            public static final String STANDARD = "standard";

            /**
             * Simple Lucene Analyzer.
             *
             * @see org.apache.lucene.analysis.SimpleAnalyzer
             */
            public static final String SIMPLE = "simple";

            /**
             * Lucene Stop analyzer.
             *
             * @see org.apache.lucene.analysis.StopAnalyzer
             */
            public static final String STOP = "stop";

        }

        public abstract class Snowball {

            /**
             *
             */
            public static final String SNOWBALL = "snowball";

            /**
             *
             */
            public static final String NAME_TYPE = "name";

            /**
             *
             */
            public static final String NAME_DANISH = "Danish";

            /**
             *
             */
            public static final String NAME_DUTCH = "Dutch";

            /**
             *
             */
            public static final String NAME_ENGLISH = "English";

            /**
             *
             */
            public static final String NAME_FINNISH = "Finnish";

            /**
             *
             */
            public static final String NAME_FRENCH = "French";

            /**
             *
             */
            public static final String NAME_GERMAN = "German";

            /**
             *
             */
            public static final String NAME_GERMAN2 = "German2";

            /**
             *
             */
            public static final String NAME_ITALIAN = "Italian";

            /**
             *
             */
            public static final String NAME_KP = "Kp";

            /**
             *
             */
            public static final String NAME_LOVINS = "Lovins";

            /**
             *
             */
            public static final String NAME_NORWEGIAN = "Norwegian";

            /**
             *
             */
            public static final String NAME_PORTER = "Porter";

            /**
             *
             */
            public static final String NAME_PORTUGUESE = "Portuguese";

            /**
             *
             */
            public static final String NAME_RUSSIAN = "Russian";

            /**
             *
             */
            public static final String NAME_SPANISH = "Spanish";

            /**
             *
             */
            public static final String NAME_SWEDISH = "Swedish";
        }

        public abstract class ExtendedTypes {

            /**
             *
             */
            public static final String BRAZILIAN = "brazilian";

            /**
             *
             */
            public static final String CJK = "cjk";

            /**
             *
             */
            public static final String CHINESE = "chinese";

            /**
             *
             */
            public static final String CZECH = "czech";

            /**
             *
             */
            public static final String GERMAN = "german";

            /**
             *
             */
            public static final String GREEK = "greek";

            /**
             *
             */
            public static final String FRENCH = "french";

            /**
             *
             */
            public static final String DUTCH = "dutch";

            /**
             *
             */
            public static final String RUSSIAN = "russian";
        }
    }

    public static abstract class AnalyzerFilter {

        /**
         * The prefix used for analyzer filter groups.
         */
        public static final String PREFIX = "compass.engine.analyzerfilter";

        /**
         * The fully qualified class name of the
         * {@link org.compass.core.lucene.engine.analyzer.LuceneAnalyzerTokenFilterProvider} implementation.
         */
        public static final String TYPE = "type";

        /**
         * The synonym type, used to set the {@link #TYPE} to
         * {@link org.compass.core.lucene.engine.analyzer.synonym.SynonymAnalyzerTokenFilterProvider}.
         */
        public static final String SYNONYM_TYPE = "synonym";

        public static abstract class Synonym {

            /**
             * The fully qualified class of the synonym lookup provider
             * ({@link org.compass.core.lucene.engine.analyzer.synonym.SynonymLookupProvider} implementation.
             */
            public static final String LOOKUP = "lookup";
        }
    }

    /**
     * Settings for Lucene highlighter.
     *
     * @author kimchy
     */
    public static abstract class Highlighter {
        /**
         * The prefix used for highlighter groups.
         */
        public static final String PREFIX = "compass.engine.highlighter";

        /**
         * The default highlighter group that must be set.
         */
        public static final String DEFAULT_GROUP = "default";

        /**
         * The text tokenizer type that will be used.
         */
        public static final String TEXT_TOKENIZER = "textTokenizer";

        /**
         * Low level. A boolean setting (<code>true</code>, or
         * <code>false</code>). If the query will be rewritten befored it is
         * used by the highlighter.
         */
        public static final String REWRITE_QUERY = "rewriteQuery";

        /**
         * Low level. A boolean setting (<code>true</code> or
         * <code>false</code>). If the idf value will be used during the
         * highlighting process. Used by formatters that a) score selected
         * fragments better b) use graded highlights eg chaning intensity of
         * font color. Automatically assigned for the provided formatters.
         */
        public static final String COMPUTE_IDF = "computeIdf";

        /**
         * Sets the maximum number of fragments that will be returned. Defaults
         * to <code>3</code>.
         */
        public static final String MAX_NUM_FRAGMENTS = "maxNumFragments";

        /**
         * Sets the separator string between fragments if using the combined
         * fragments highlight option. Defaults to <code>...</code>.
         */
        public static final String SEPARATOR = "separator";

        /**
         * Maximum bytes to analyze. Default to <code>50*1024</code> bytes.
         */
        public static final String MAX_BYTES_TO_ANALYZE = "maxBytesToAnalyze";

        /**
         * The fully qualified name of the highlighter factory. Must implement
         * the
         * {@link org.compass.core.lucene.engine.highlighter.LuceneHighlighterFactory}
         * inteface. <p/> It is part of the highlighter group, and should be
         * constructed using the
         * {@link org.compass.core.config.CompassSettings#setGroupSettings(String,String,String[],String[])},
         * with the {@link #PREFIX} as the prefix, the highlighter
         * group name, and the factory as one of the values.
         */
        public static final String FACTORY = "factory";

        /**
         * Settings for Lucene highlighter fragmenter.
         *
         * @author kimchy
         */
        public abstract class Fragmenter {

            /**
             * If set, sets the class name of the Lucene <code>Fragmenter</code>, or the actual type.
             */
            public static final String TYPE = "fragmenter.type";

            /**
             * A simple Lucene <code>Fragmenter</code>. Breaks text up into same-size fragments with no
             * concerns over spotting sentence boundaries.
             */
            public static final String TYPE_SIMPLE = "simple";

            /**
             * A null Lucene <code>Fragmenter</code>. Does not fragment the text.
             */
            public static final String TYPE_NULL = "null";

            /**
             * If not setting the {@link #TYPE} (and thus
             * using Lucene <code>SimpleFragmenter</code>), sets the size of
             * the fragment. Defaults to <code>100</code>.
             */
            public static final String SIMPLE_SIZE = "fragmenter.simple.size";

        }

        public abstract class Encoder {

            /**
             * If set, sets the type of the Lucene <code>Encoder</code>, or
             * it's fully qualifed name.
             */
            public static final String TYPE = "encoder.type";

            /**
             * Performs no encoding of the text.
             */
            public static final String DEFAULT = "default";

            /**
             * Simple encoder that encodes html tags.
             */
            public static final String HTML = "html";
        }

        /**
         * Settings for Lucene highlighter formatter.
         *
         * @author kimchy
         */
        public abstract class Formatter {

            /**
             * If set, sets the type of the Lucene <code>Formatter</code> or
             * it's fully qualified name. Defaults to {@link #SIMPLE}.
             */
            public static final String TYPE = "formatter.type";

            /**
             * A simple wrapper formatter. Wraps the highlight with pre and post
             * string (can be html or xml tags). They can be set using
             * {@link #SIMPLE_PRE_HIGHLIGHT} and
             * {@link #SIMPLE_POST_HIGHLIGHT}.
             */
            public static final String SIMPLE = "simple";

            /**
             * In case the highlighter uses the {@link #SIMPLE},
             * controlls the text that is appened before the highlighted text.
             * Defatuls to <code>&lt;b&gt;</code>.
             */
            public static final String SIMPLE_PRE_HIGHLIGHT = "formatter.simple.pre";

            /**
             * In case the highlighter uses the {@link #SIMPLE},
             * controlls the text that is appened after the highlighted text.
             * Defatuls to <code>&lt;/b&gt;</code>.
             */
            public static final String SIMPLE_POST_HIGHLIGHT = "formatter.simple.post";

            /**
             * Wraps an html span tag around the highlighted text. The
             * background and foreground colors can be controlled and will have
             * different color intensity depending on the score.
             */
            public static final String HTML_SPAN_GRADIENT = "htmlSpanGradient";

            /**
             * The score (and above) displayed as maxColor.
             */
            public static final String HTML_SPAN_GRADIENT_MAX_SCORE = "formatter.htmlSpanGradient.maxScore";

            /**
             * The hex color used for representing IDF scores of zero eg #FFFFFF
             * (white) or null if no foreground color required.
             */
            public static final String HTML_SPAN_GRADIENT_MIN_FOREGROUND_COLOR = "formatter.htmlSpanGradient.minForegroundColor";

            /**
             * The largest hex color used for representing IDF scores eg #000000
             * (black) or null if no foreground color required.
             */
            public static final String HTML_SPAN_GRADIENT_MAX_FOREGROUND_COLOR = "formatter.htmlSpanGradient.maxForegroundColor";

            /**
             * The hex color used for representing IDF scores of zero eg #FFFFFF
             * (white) or null if no background color required.
             */
            public static final String HTML_SPAN_GRADIENT_MIN_BACKGROUND_COLOR = "formatter.htmlSpanGradient.minBackgroundColor";

            /**
             * The largest hex color used for representing IDF scores eg #000000
             * (black) or null if no background color required.
             */
            public static final String HTML_SPAN_GRADIENT_MAX_BACKGROUND_COLOR = "formatter.htmlSpanGradient.maxBackgroundColor";
        }
    }

    /* Transaction Locking Settings */

    public static abstract class Transaction {

        /**
         * The amount of time a transaction will wait in order to obtain it's
         * specific lock (in seconds). Defaults to 10 seconds.
         */
        public static final String LOCK_TIMEOUT = "compass.transaction.lockTimeout";

        /**
         * The interval that the transaction will check to see if it can obtain
         * the lock (in milliseconds). <p/> The default value is 100
         * milliseconds
         */
        public static final String LOCK_POLL_INTERVAL = "compass.transaction.lockPollInterval";

        /**
         * Should the cache be cleared on commit. Note, that setting it to <code>false</code>
         * might mean that the transaction isolation level will not function properly (for example,
         * with read_committed, it will mean that data that is committed will take time to be
         * reflected in the index). Defaults to <code>true</code>.
         */
        public static final String CLEAR_CACHE_ON_COMMIT = "compass.transaction.clearCacheOnCommit";

        /**
         * Should concurrent commit be enabled (if allowed by the actual Lucene store). Default to
         * <code>true</code> if allows by store.
         */
        public static final String ENABLE_CONCURRENT_COMMIT = "compass.transaction.enableConcurrentCommit";

        /**
         * If concurrent commits are enabled, what is the maximum number of threads that will be used to
         * perform the concurrent commmit operation. Default to 10 threads (or less if there are less sub indexes).
         */
        public static final String MAX_CONCURRENT_COMMIT_THREADS = "compass.transaction.maxConcurrentCommitThreads";

        /**
         * If concurrent commits are enabled, the threshold above which concurrent commit will happen. Below
         * the threshold, serialized commits with no thread pool will occur. Default to 1.
         */
        public static final String CONCURRENT_COMMIT_THRESHOLD = "compass.transaction.concurrentCommitThreshold";

        /**
         * Transaction log settings
         */
        public static final class TransLog {

            /**
             * The fully qualified type of the {@link org.apache.lucene.index.TransLog}
             * implementation.
             */
            public static final String TYPE = "compass.transaction.translog.type";

            /**
             * Applies to tran logs that require a location like {@link org.apache.lucene.index.FSTransLog}.
             */
            public static final String PATH = "compass.transaction.translog.path";

            /**
             * <p>Applies to tran logs that require a write buffer size like {@link org.apache.lucene.index.FSTransLog}.
             * Defaults to 2048 (size is in bytes).
             *
             * <p>The bigger the value, the faster indexing will be perfomed though in expense of memory (the write
             * buffers are pooled, so the memory overhead will not be noitceable as is the case with read buffer.
             */
            public static final String WRITE_BUFFER_SIZE = "compass.transaction.translog.writeBufferSize";

            /**
             * <p>Applies to tran logs that require a read buffer size like {@link org.apache.lucene.index.FSTransLog}.
             * Defaults to 64 (size is in bytes).
             *
             * <p>The smaller the value, bigger transactions can be performed, though performance might suffer.
             */
            public static final String READ_BUFFER_SIZE = "compass.transaction.translog.readBufferSize";
        }
    }

    /* Optimizer Settings */

    public static abstract class Optimizer {

        /**
         * The fully qualified class name of the optimizer.
         */
        public static final String TYPE = "compass.engine.optimizer.type";

        /**
         * If the optimizer should be scheduled (can be "true" or "false"). <p/>
         * Defaults to <code>true</code>
         */
        public static final String SCHEDULE = "compass.engine.optimizer.schedule";

        /**
         * Determines the how often the optimizer will kick in (in seconds).
         * <p/> Default is 10 seconds. Can be float number.
         */
        public static final String SCHEDULE_PERIOD = "compass.engine.optimizer.schedule.period";

        /**
         * Determines if the optimizer schedule thread is a daemon or not
         * (values are true or false) <p/> Defaults to <code>false</code>
         */
        public static final String SCHEDULE_DEAMON = "compass.engine.optimizer.schedule.daemon";

        public abstract class Aggressive {

            /**
             * Determines how often the aggressive optimizer will optimize the
             * index. <p/> Defaults to 10.
             */
            public static final String MERGE_FACTOR = "compass.engine.optimizer.aggressive.mergeFactor";

        }

        /**
         * Set of environment settings for the adaptive optimizer.
         *
         * @author kimchy
         */
        public abstract class Adaptive {

            /**
             * Determines how often the adaptive optimizer will optimize the
             * index. <p/> Defaults to 10.
             */
            public static final String MERGE_FACTOR = "compass.engine.optimizer.adaptive.mergeFactor";
        }

    }

    /**
     * Specific environment settings for the <code>batch_insert</code> settings.
     */
    public static abstract class SearchEngineIndex {

        /**
         * Determines the largest number of documents ever merged by addDocument().
         * Small values (e.g., less than 10,000) are best for interactive indexing,
         * as this limits the length of pauses while indexing to a few seconds.
         * Larger values are best for batched indexing and speedier searches. <p/>
         * The default value is {@link Integer#MAX_VALUE}. <p/> Applies for Lucene
         * Batch transaction support.
         */
        public static final String MAX_MERGE_DOCS = "compass.engine.maxMergeDocs";

        /**
         * Determines how often segment indices are merged by addDocument(). With
         * smaller values, less RAM is used while indexing, and searches on
         * unoptimized indices are faster, but indexing speed is slower. With larger
         * values, more RAM is used during indexing, and while searches on
         * unoptimized indices are slower, indexing is faster. Thus larger values (>
         * 10) are best for batch index creation, and smaller values ( < 10) for
         * indices that are interactively maintained. <p/> This must never be less
         * than 2. The default value is 10. <p/> Applies for Lucene Batch
         * transaction support.
         */
        public static final String MERGE_FACTOR = "compass.engine.mergeFactor";

        /**
         * Determines the minimal number of documents required before the buffered
         * in-memory documents are merging and a new Segment is created. Since
         * Documents are merged in a {@link org.apache.lucene.store.RAMDirectory},
         * large value gives faster indexing. At the same time, mergeFactor limits
         * the number of files open in a FSDirectory.
         * <p/>
         * The default value is 10.
         * <p/>
         * Applies for Lucene Batch transaction support.
         */
        public static final String MAX_BUFFERED_DOCS = "compass.engine.maxBufferedDocs";

        /**
         * Setting to turn on usage of a compound file. When on, multiple files for
         * each segment are merged into a single file once the segment creation is
         * finished. This is done regardless of what directory is in use. <p/>
         * Default value id <code>true</code>
         */
        public static final String USE_COMPOUND_FILE = "compass.engine.useCompoundFile";

        /**
         * The maximum number of terms that will be indexed for a single field in a
         * document. This limits the amount of memory required for indexing, so that
         * collections with very large files will not crash the indexing process by
         * running out of memory. <p/>Note that this effectively truncates large
         * documents, excluding from the index terms that occur further in the
         * document. If you know your source documents are large, be sure to set
         * this value high enough to accomodate the expected size. If you set it to
         * Integer.MAX_VALUE, then the only limit is your memory, but you should
         * anticipate an OutOfMemoryError. <p/>By default, no more than 10,000 terms
         * will be indexed for a field.
         */
        public static final String MAX_FIELD_LENGTH = "compass.engine.maxFieldLength";

        /**
         * Sets how often (in milliseconds) the index manager will check if the index
         * cache needs to be invalidated. Defaults to <code>5000</code>. Setting it to
         * <code>0</code> means that the cache will check if it needs to be invalidated all the time. Setting
         * it to <code>-1</code> means that the cache will never check if it needs to
         * be invalidated, note, that it is perfectly fine if a single instance is
         * manipulating the index. It works, since the cache is invalidated when a
         * transaction is committed and a dirty operation has occured.
         */
        public static final String CACHE_INTERVAL_INVALIDATION = "compass.engine.cacheIntervalInvalidation";

        /**
         * The index manager schedule interval (in seconds) where different actions related to index manager will happen (such
         * as global cache interval checks. If set to <code>-1</code>, not scheduling will happen.
         */
        public static final String INDEX_MANAGER_SCHEDULE_INTERVAL = "compass.engine.indexManagerScheduleInterval";

        /**
         * Defaults to <code>false</code>. If set to <code>true</code>, will cause index manager operation (including
         * replace index) to wait for all other Compass instances to invalidate their cache. The wait time will be
         * the same as the {@link #INDEX_MANAGER_SCHEDULE_INTERVAL}.
         */
        public static final String WAIT_FOR_CACHE_INVALIDATION_ON_INDEX_OPERATION = "compass.engine.waitForCacheInvalidationOnIndexOperation";
    }

    /**
     * Settings applicable when storing the index within a database.
     */
    public static abstract class JdbcStore {

        /**
         * The dialect (database) that is used when storing the index in the database
         */
        public static final String DIALECT = "compass.engine.store.jdbc.dialect";

        /**
         * Some of the entries in the database are marked as deleted, and not actually gets to be
         * deleted from the database. The settings controls the delta time of when they should be deleted.
         * They will be deleted if they were marked for deleted "delta" time ago
         * (base on database time, if possible by dialect).
         */
        public static final String DELETE_MARK_DELETED_DELTA = "compass.engine.store.jdbc.deleteMarkDeletedDelta";

        /**
         * The class name of the Jdbc lock to be used.
         */
        public static final String LOCK_TYPE = "compass.engine.store.jdbc.lockType";

        /**
         * If the connection is managed or not. Basically, if set to <code>false</code>, compass
         * will commit and rollback the transaction. If set to <code>true</code>, compass will
         * not perform it. Defaults to <code>false</code>. Should be set to <code>true</code> if
         * using external transaction managers (like JTA or Spring <code>PlatformTransactionManager</literal>),
         * and <code>false</code> if using <code>LocalTransactionFactory</code>.
         */
        public static final String MANAGED = "compass.engine.store.jdbc.managed";

        /**
         * If set to <code>true</code>, no database schema level operations will be performed (drop and create
         * tables). When deleting the data in the index, the content will be deleted, but the table will not
         * be dropped. Default to <code>false</code>.
         */
        public static final String DISABLE_SCHEMA_OPERATIONS = "compass.engine.store.jdbc.disableSchemaOperations";

        public abstract class Connection {
            /**
             * The jdbc driver class
             */
            public static final String DRIVER_CLASS = "compass.engine.store.jdbc.connection.driverClass";

            /**
             * the jdbc connection user name
             */
            public static final String USERNAME = "compass.engine.store.jdbc.connection.username";

            /**
             * The jdbc connection password
             */
            public static final String PASSWORD = "compass.engine.store.jdbc.connection.password";

            /**
             * Sets the auto commit for the <code>Connection</code> created by the <code>DataSource</code>.
             * Defaults to <code>false</code>. Can be either <code>false</code>, <code>true</code> or
             * <code>external</code> (let outer configuration management to set it).
             */
            public static final String AUTO_COMMIT = "compass.engine.store.jdbc.connection.autoCommit";
        }

        public abstract class DataSourceProvider {

            /**
             * The class for the data source provider. Responsible for creating data sources.
             */
            public static final String CLASS = "compass.engine.store.jdbc.connection.provider.class";

            public abstract class Dbcp {

                private static final String PREFIX = "compass.engine.store.jdbc.connection.provider.dbcp.";

                /**
                 * The default TransactionIsolation state of connections created by this pool.
                 */
                public static final String DEFAULT_TRANSACTION_ISOLATION = PREFIX + "defaultTransactionIsolation";

                /**
                 * The initial number of connections that are created when the pool is started.
                 */
                public static final String INITIAL_SIZE = PREFIX + "initialSize";

                /**
                 * The maximum number of active connections that can be allocated from this pool at the same time,
                 * or zero for no limit.
                 */
                public static final String MAX_ACTIVE = PREFIX + "maxActive";

                /**
                 * The maximum number of active connections that can remain idle in the pool,
                 * without extra ones being released, or zero for no limit.
                 */
                public static final String MAX_IDLE = PREFIX + "maxIdle";

                /**
                 * The minimum number of active connections that can remain idle in the pool,
                 * without extra ones being created, or 0 to create none.
                 */
                public static final String MIN_IDLE = PREFIX + "minIdle";

                /**
                 * The maximum number of milliseconds that the pool will wait (when there are no available connections)
                 * for a connection to be returned before throwing an exception, or -1 to wait indefinitely.
                 */
                public static final String MAX_WAIT = PREFIX + "maxWait";

                /**
                 * The maximum number of open statements that can be allocated from the statement pool at the same time,
                 * or zero for no limit.
                 */
                public static final String MAX_OPEN_PREPARED_STATEMENTS = PREFIX + "maxOpenPreparedStatements";

                /**
                 * Sets if the pool will cache prepared statements.
                 */
                public static final String POOL_PREPARED_STATEMENTS = PREFIX + "poolPreparedStatements";
            }

        }

        public abstract class DDL {

            /**
             * The name of the column name. Defaults to name_.
             */
            public static final String NAME_NAME = "compass.engine.store.jdbc.ddl.name.name";

            /**
             * The length of the name column. Defaults to 50.
             */
            public static final String NAME_LENGTH = "compass.engine.store.jdbc.ddl.name.length";

            /**
             * The name of the value colum. Defaults to value_.
             */
            public static final String VALUE_NAME = "compass.engine.store.jdbc.ddl.value.name";

            /**
             * The length (in K) of the value column (for databases that require it). Defaults to 500 * 1024 K.
             */
            public static final String VALUE_LENGTH = "compass.engine.store.jdbc.ddl.value.length";

            /**
             * The name of the size column. Defaults to size_.
             */
            public static final String SIZE_NAME = "compass.engine.store.jdbc.ddl.size.name";

            /**
             * The name of the last modified column. Defaults to lf_.
             */
            public static final String LAST_MODIFIED_NAME = "compass.engine.store.jdbc.ddl.lastModified.name";

            /**
             * The name of the deleted column. Defaults to deleted_.
             */
            public static final String DELETED_NAME = "compass.engine.store.jdbc.ddl.deleted.name";
        }

        public abstract class FileEntry {

            public static final String PREFIX = "compass.engine.store.jdbc.fe";

            /**
             * The buffer size for implemenations of Lucene <code>IndexInput</code> where applicable.
             */
            public static final String INDEX_INPUT_BUFFER_SIZE = JdbcBufferedIndexInput.BUFFER_SIZE_SETTING;

            /**
             * The buffer size for implemenations of Lucene <code>IndexOutput</code> where applicable.
             */
            public static final String INDEX_OUTPUT_BUFFER_SIZE = JdbcBufferedIndexOutput.BUFFER_SIZE_SETTING;

            /**
             * The fully qualifed class of the <code>IndexInput</code> implementation.
             */
            public static final String INDEX_INPUT_TYPE = JdbcFileEntrySettings.INDEX_INPUT_TYPE_SETTING;

            /**
             * The fully qualifed class of the <code>IndexOutput</code> implementation.
             */
            public static final String INDEX_OUTPUT_TYPE = JdbcFileEntrySettings.INDEX_OUTPUT_TYPE_SETTING;

            /**
             * The fully qualifed class of the <code>FileEntryHandler</code> implementation.
             */
            public static final String FILE_ENTRY_HANDLER_TYPE = JdbcFileEntrySettings.FILE_ENTRY_HANDLER_TYPE;

            /**
             * The threshold value to be used for <code>RAMAndFileJdbcIndexOutput<code>.
             */
            public static final String INDEX_OUTPUT_THRESHOLD = RAMAndFileJdbcIndexOutput.INDEX_OUTPUT_THRESHOLD_SETTING;
        }
    }

    public static abstract class DirectoryWrapper {

        public static final String PREFIX = "compass.engine.store.wrapper";

        public static final String TYPE = "type";
    }

    /**
     * Lucene {@link org.apache.lucene.store.LockFactory} creation settings.
     */
    public static abstract class LockFactory {

        /**
         * The settings prefix for LockFactory
         */
        public static final String PREFIX = "compass.engine.store.lockFactory";

        /**
         * The type of the lock factory. Can either hold values stated at {@link Type} or
         * the fully qualified class name of the {@link org.apache.lucene.store.LockFactory}
         * implementation.
         */
        public static final String TYPE = PREFIX + ".type";

        /**
         * Certain implementation (such as {@link Type#SIMPLE_FS} or {@link Type#NATIVE_FS})
         * also accept an optional path where to store the index locking.
         */
        public static final String PATH = PREFIX + ".path";

        public static abstract class Type {

            /**
             * No locking is perfomed, generally should not be used. Maps to Lucene
             * {@link org.apache.lucene.store.NoLockFactory}.
             */
            public static final String NO_LOCKING = "nolock";

            /**
             * The default lock factory uses simple FS operations to write a lock file.
             * Maps to Lucene {@link org.apache.lucene.store.SimpleFSLockFactory}.
             */
            public static final String SIMPLE_FS = "simplefs";

            /**
             * A native FS lock factory (uses NIO). Maps to Lucene
             * {@link org.apache.lucene.store.NativeFSLockFactory}.
             */
            public static final String NATIVE_FS = "nativefs";

            /**
             * A single instance lock fatory (uses memory based ones). Maps to
             * Lucene {@link org.apache.lucene.store.SingleInstanceLockFactory}.
             */
            public static final String SINGLE_INSTANCE = "singleinstance";
        }
    }

    /**
     * Settings used to control Lucene {@link org.apache.lucene.index.IndexDeletionPolicy}
     * creation.
     */
    public static abstract class IndexDeletionPolicy {

        public static final String PREFIX = "compass.engine.store.indexDeletionPolicy";

        /**
         * The type of the index deleteion policy. Can eb one of the logical names that
         * comes built in with Compass, such as {@link org.compass.core.lucene.LuceneEnvironment.IndexDeletionPolicy.KeepLastCommit#NAME},
         * or the fully qualified class name of the actual implementation. In suce a case, the implementation
         * can also implement {@link org.compass.core.config.CompassConfigurable} and/or
         * {@link org.compass.core.lucene.engine.indexdeletionpolicy.DirectoryConfigurable} in order
         * to be further configured.
         */
        public static final String TYPE = PREFIX + ".type";

        /**
         * An index deletion policy that keeps only the last commit. Maps to
         * Lucene {@link org.apache.lucene.index.KeepOnlyLastCommitDeletionPolicy}.
         */
        public static abstract class KeepLastCommit {

            /**
             * The name to put under the
             */
            public static final String NAME = "keeplastcommit";
        }

        /**
         * An index deletion policy that keeps on the last N number of commits.
         * Maps to {@link org.compass.core.lucene.engine.indexdeletionpolicy.KeepLastNDeletionPolicy}.
         */
        public static abstract class KeepLastN {

            public static final String NAME = "keeplastn";

            public static final String NUM_TO_KEEP = PREFIX + ".numToKeep";
        }

        public static abstract class ExpirationTime {

            public static final String NAME = "expirationtime";

            public static final String EXPIRATION_TIME_IN_SECONDS = PREFIX + "expirationTimeInSeconds";
        }

        public static abstract class KeepAll {

            public static final String NAME = "keepall";
        }

        public static abstract class KeepNoneOnInit {

            public static final String NAME = "keepnoneoninit";
        }
    }

    /**
     * Settings for different query parser implementations.
     */
    public static abstract class QueryParser {

        /**
         * The prefix used for query parser groups.
         */
        public static final String PREFIX = "compass.engine.queryParser";

        /**
         * The type of the query parser. A fully qualified class name, must
         * implement {@link org.compass.core.lucene.engine.queryparser.LuceneQueryParser}.
         */
        public static final String TYPE = "type";

        /**
         * The default query parser group that must be set.
         */
        public static final String DEFAULT_GROUP = "default";

        /**
         * The default parsers implementation allows to set if leading wildcards
         * are allowed or not. Boolen value defaults to <code>false</code>. 
         */
        public static final String DEFAULT_PARSER_ALLOW_LEADING_WILDCARD = "allowLeadingWildcard";
    }

    public static abstract class LocalCache {

        public static final String DISABLE_LOCAL_CACHE = "compass.engine.disableLocalCache";

        public static final String PREFIX = "compass.engine.localCache";

        public static final String CONNECTION = "connection";

        public static final String DEFAULT_NAME = "__default__";
    }
}
