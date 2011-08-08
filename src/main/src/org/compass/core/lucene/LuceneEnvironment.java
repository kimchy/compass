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

import org.apache.lucene.store.jdbc.JdbcFileEntrySettings;
import org.apache.lucene.store.jdbc.index.JdbcBufferedIndexInput;
import org.apache.lucene.store.jdbc.index.JdbcBufferedIndexOutput;
import org.apache.lucene.store.jdbc.index.RAMAndFileJdbcIndexOutput;

/**
 * @author kimchy
 */
public class LuceneEnvironment {

    /**
     * The default search that will be used for non prefixed query values.
     * Defaults to the value of the "all" property.
     */
    public static final String DEFAULT_SEARCH = "compass.engine.defaultsearch";

    /**
     * A set of configuration settings for similarity.
     */
    public static abstract class Similarity {

        /**
         * The prefix for the similarity settings.
         */
        public static final String PREFIX = "compass.engine.similarity";

        public static final String DEFAULT_SIMILARITY_TYPE = PREFIX + ".default.type";

        public static final String INDEX_SIMILARITY_TYPE = PREFIX + ".index.type";

        public static final String SEARCH_SIMILARITY_TYPE = PREFIX + ".search.type";
    }

    /**
     * A set of configuration settings for analyzers.
     */
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
         * ANALYZER_STANDARD, ANALYZER_SIMPLE, ANALYZER_STOP, a fully
         * qualified class of the analyzer ({@link Analyzer} or an instnace of it.
         *
         * <p>It is part of the anaylzer group, and should be constructed using the
         * {@link org.compass.core.config.CompassSettings#setGroupSettings(String,String,String[],Object[])},
         * with the {@link #PREFIX} as the prefix, the analyzer group
         * name, and the type as one of the values.
         */
        public static final String TYPE = "type";

        /**
         * The fully qualified name of the anayzer factory or an instnace of it. Must implement the
         * {@link org.compass.core.lucene.engine.analyzer.LuceneAnalyzerFactory}
         * inteface.
         *
         * <p>It is part of the anaylzer group, and should be constructed using the
         * {@link org.compass.core.config.CompassSettings#setGroupSettings(String,String,String[],Object[])},
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
         * {@link org.compass.core.config.CompassSettings#setGroupSettings(String,String,String[],Object[])},
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

            /**
             * Lucene Keyword analyzer.
             *
             * @see org.apache.lucene.analysis.KeywordAnalyzer
             */
            public static final String KEYWORD = "keyword";
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
         * {@link org.compass.core.lucene.engine.analyzer.LuceneAnalyzerTokenFilterProvider} implementation
         * or an actual instance of it.
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
         * The fully qualified name of the highlighter factory or an actual instance. Must implement
         * the
         * {@link org.compass.core.lucene.engine.highlighter.LuceneHighlighterFactory}
         * inteface. <p/> It is part of the highlighter group, and should be
         * constructed using the
         * {@link org.compass.core.config.CompassSettings#setGroupSettings(String,String,String[],Object[])},
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
         * specific lock (in Compass time format). Defaults to <code>10s</code>.
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
         * reflected in other transactions). Defaults to <code>true</code>.
         */
        public static final String CLEAR_CACHE_ON_COMMIT = "compass.transaction.clearCacheOnCommit";

        /**
         * Allows to control transaction processors within Compass. Several transaction processors can
         * be defined in Compass using the {@link #PREFIX} and then the name. Default ones include
         * {@link org.compass.core.lucene.LuceneEnvironment.Transaction.Processor.ReadCommitted ReadCommitted},
         * {@link org.compass.core.lucene.LuceneEnvironment.Transaction.Processor.Lucene Lucene},
         * {@link org.compass.core.lucene.LuceneEnvironment.Transaction.Processor.Async Async}.
         */
        public static final class Processor {

            /**
             * The prefix used for the transaction processor group setting.
             */
            public static final String PREFIX = "compass.transaction.processor.";

            /**
             * The type of the transaction processor (can be set in runtime). The name can be
             * the either default built in ones (<code>read_committed</code>, <code>lucene</code>,
             * <code>serializable</code>, <code>async</code>) or custom ones registered under a custom name.
             */
            public static final String TYPE = "compass.transaction.processor";

            /**
             * The config type group setting key of the transaction processor. Can be class name or
             * actual intance of {@link org.compass.core.lucene.engine.transaction.TransactionProcessorFactory}.
             */
            public static final String CONFIG_TYPE = "type";

            /**
             * Settings for <code>read_committed</code> tranasction processr.
             *
             * @see org.compass.core.lucene.engine.transaction.readcommitted.ReadCommittedTransactionProcessorFactory
             * @see org.compass.core.lucene.engine.transaction.readcommitted.ReadCommittedTransactionProcessor
             */
            public static final class ReadCommitted {

                /**
                 * The name of the read committed transaction processor.
                 */
                public static final String NAME = "read_committed";

                /**
                 * Should dirty operations be perfomed asynchronously. Defaults to <code>true</code>.
                 */
                public static final String CONCURRENT_OPERATIONS = "compass.transaction.processor.read_committed.concurrentOperations";

                /**
                 * The number of threads used to process dirty operations asynchronously. Defaults to <code>5</code>.
                 *
                 * <p>Note, this is the number of threads used per transaction and threads are pooled.
                 */
                public static final String CONCURRENCY_LEVEL = "compass.transaction.processor.read_committed.concurrencyLevel";

                /**
                 * Controls how hashing will be done for parallel processing. Eitehr <code>uid</code> or <code>subindex</code>.
                 * Defuats to <code>uid</code>.
                 */
                public static final String HASHING = "compass.transaction.processor.read_committed.hashing";

                /**
                 * The backlog size of the current ongoing dirty operations. If full, will block the dirty operation
                 * until it is emptied by the running async processor threads. Defaults to <code>100</code>.
                 */
                public static final String BACKLOG = "compass.transaction.processor.read_committed.backlog";

                /**
                 * The time for a dirty operation to wait if the backlog is full until the dirty operation can be added.
                 * Defaults to <code>10</code> seconds. Supports time based configuration and default value is in millis.
                 */
                public static final String ADD_TIMEOUT = "compass.transaction.processor.read_committed.addTimeout";

                /**
                 * Transaction log settings.
                 */
                public static final class TransLog {

                    /**
                     * The connection type for the read committed transactional log. Can be either <code>ram://</code>
                     * or <code>file://</code>.
                     */
                    public static final String CONNECTION = "compass.transaction.processor.read_committed.translog.connection";

                    /**
                     * Should the transactional index be optimized before it is added to the actual index. Defaults to
                     * <code>true</code>.
                     */
                    public static final String OPTIMIZE_TRANS_LOG = "compass.transaction.processor.read_committed.translog.optimize";
                }
            }

            /**
             * Setting for the <code>mt</code> transaction processor.
             *
             * @see org.compass.core.lucene.engine.transaction.mt.MTTransactionProcessorFactory
             * @see org.compass.core.lucene.engine.transaction.mt.MTTransactionProcessor
             */
            public static final class MT {
                /**
                 * The name of the mt transaction processor.
                 */
                public static final String NAME = "mt";
            }

            /**
             * Settings for <code>lucene</code> transaction procssor.
             *
             * @see org.compass.core.lucene.engine.transaction.lucene.LuceneTransactionProcessorFactory
             * @see org.compass.core.lucene.engine.transaction.lucene.LuceneTransactionProcessor
             */
            public static final class Lucene {

                /**
                 * The name of the lucene transaction processor.
                 */
                public static final String NAME = "lucene";

                /**
                 * Should dirty operations be perfomed asynchronously. Defaults to <code>true</code>.
                 */
                public static final String CONCURRENT_OPERATIONS = "compass.transaction.processor.lucene.concurrentOperations";

                /**
                 * The number of threads used to process dirty operations asynchronously. Defaults to <code>5</code>.
                 *
                 * <p>Note, this is the number of threads used per transaction and threads are pooled.
                 */
                public static final String CONCURRENCY_LEVEL = "compass.transaction.processor.lucene.concurrencyLevel";

                /**
                 * Controls how hashing will be done for parallel processing. Eitehr <code>uid</code> or <code>subindex</code>.
                 * Defuats to <code>uid</code>.
                 */
                public static final String HASHING = "compass.transaction.processor.lucene.hashing";

                /**
                 * The backlog size of the current ongoing dirty operations. If full, will block the dirty operation
                 * until it is emptied by the running async processor threads. Defaults to <code>100</code>.
                 */
                public static final String BACKLOG = "compass.transaction.processor.lucene.backlog";

                /**
                 * The time for a dirty operation to wait if the backlog is full until the dirty operation can be added.
                 * Defaults to <code>10</code> seconds. Supports time based configuration and default value is in millis.
                 */
                public static final String ADD_TIMEOUT = "compass.transaction.processor.lucene.addTimeout";
            }

            /**
             * Search transaction processor allows to perform only search opeations. Very lightweight.
             */
            public static final class Search {

                /**
                 * The name of the search transaction processor.
                 */
                public static final String NAME = "search";
            }

            /**
             * Settings for async transaction processor.
             *
             * @see org.compass.core.lucene.engine.transaction.async.AsyncTransactionProcessorFactory
             * @see org.compass.core.lucene.engine.transaction.async.AsyncTransactionProcessor
             */
            public static final class Async {

                /**
                 * The name of the lucene async transaction processor.
                 */
                public static final String NAME = "async";

                /**
                 * Allows to control if order between concurrent transactions within a single JVM or across JVMs on
                 * the sub index level will have to be ordered or not. If they are ordered, a lock will be obtained
                 * once a dirty operation is perfomed against the sub index, and it will only be released once the
                 * transaction commits / rolls back. Default value is <code>true</code>.
                 */
                public static final String MAINTAIN_ORDER = "compass.transaction.processor.async.maintainOrder";

                /**
                 * The bounded size of the backlog for async transactions to process (note, a transaciton includes
                 * one or more destructive operations. Once the backlog is full, transactions will block until new
                 * tranasctions can be inserted to it. The default backlog size is <code>10</code>.
                 */
                public static final String BACKLOG = "compass.transaction.processor.async.backlog";

                /**
                 * The timeout value to wait if the backlog is full until it is cleared. Defaults to
                 * <code>10</code> seconds. Accepts Compass time format settings.
                 */
                public static final String ADD_TIMEOUT = "compass.transaction.processor.async.addTimeout";

                /**
                 * Once a transaction is identified as needed to be processed asynchronously, it can try and wait
                 * for more transactions to happen in order to process all of them in one go. This settings controls
                 * how many additional transactions will be accumalated by blocking for them. The blocking time
                 * is controlled using {@link #BATCH_JOBS_TIMEOUT}.
                 *
                 * <p>While there is an additional job within the timeout, transactions will be accumelated until
                 * the configured size. If there is none within the timeout, the processor will break and won't
                 * wait for more in order to process the jobs.
                 *
                 * <p>Defaults to <code>5</code>.
                 */
                public static final String BATCH_JOBS_SIZE = "compass.transaction.processor.async.batchJobSize";

                /**
                 * Once a transaction is identified as needed to be processed asynchronously, it can try and wait
                 * for more transactions to happen in order to process all of them in one go. This settings controls
                 * how long to wait for each additional transaction.
                 *
                 * <p>While there is an additional job within the timeout, transactions will be accumelated until
                 * the configured size. If there is none within the timeout, the processor will break and won't
                 * wait for more in order to process the jobs.
                 *
                 * <p>Defaults to 100 milliseconds.
                 */
                public static final String BATCH_JOBS_TIMEOUT = "compass.transaction.processor.async.batchJobTimeout";

                /**
                 * Once a transaction is identified as needed to be processed asynchronously, and after it has waited
                 * for additional transactions (see {@link #BATCH_JOBS_SIZE} and {@link #BATCH_JOBS_TIMEOUT}, this
                 * setting controls the number of additional transactions the processor will try to get in a non
                 * blocking fashion.
                 *
                 * <p>Defaults to <code>5</code>.
                 */
                public static final String NON_BLOCKING_BATCH_JOBS_SIZE = "compass.transaction.processor.async.nonBlockingBatchJobSize";

                /**
                 * The number of threads that will be used to process the transactions. Defaults to <code>5</code>.
                 */
                public static final String CONCURRENCY_LEVEL = "compass.transaction.processor.async.concurrencyLevel";

                /**
                 * When Compass is closed, should it wait for all the unprocessed transactions to be processed. Defaults
                 * to <code>true</code>.
                 */
                public static final String PROCESS_BEFORE_CLOSE = "compass.transaction.processor.async.processBeforeClose";

                /**
                 * Controls how hashing will be done for parallel processing. Eitehr <code>uid</code> or <code>subindex</code>.
                 * Defuats to <code>uid</code>.
                 */
                public static final String HASHING = "compass.transaction.processor.async.hashing";
            }
        }
    }

    /* Optimizer Settings */

    public static abstract class Optimizer {

        /**
         * The fully qualified class name of the optimizer or an actual instance. Must implement
         * {@link org.compass.core.lucene.engine.optimizer.LuceneSearchEngineOptimizer}.
         */
        public static final String TYPE = "compass.engine.optimizer.type";

        /**
         * If the optimizer should be scheduled (can be "true" or "false"). <p/>
         * Defaults to <code>true</code>
         */
        public static final String SCHEDULE = "compass.engine.optimizer.schedule";

        /**
         * Determines the how often the optimizer will kick in (in seconds).
         * Default is 10 seconds. Can be float number.
         */
        public static final String SCHEDULE_PERIOD = "compass.engine.optimizer.schedule.period";

        /**
         * Controls the maximum number of segments during the optimization process (to remain). Defaults
         * to <code>10</code>.
         */
        public static final String MAX_NUMBER_OF_SEGMENTS = "compass.engine.optimizer.maxNumberOfSegments";
    }

    public static abstract class SpellCheck {

        public static final String PREFIX = "compass.engine.spellcheck.";

        /**
         * Should the spell check module be enabled or not. Defaults to <code>false</code>.
         */
        public static final String ENABLE = PREFIX + "enable";

        /**
         * A globabl set of comma separated properties that will be included for each sub index.
         */
        public static final String GLOBAL_INCLUDE_PROPERTIES = PREFIX + "globablIncludeProperties";

        /**
         * A globabl set of comma separated properties that will be exluded for each sub index.
         */
        public static final String GLOBAL_EXCLUDE_PROPERTY = PREFIX + "globalExcludeProperties";

        /**
         * The default property for the spell check.
         */
        public static final String DEFAULT_PROPERTY = PREFIX + "defaultProperty";

        /**
         * The default mode for inclduing/excluding of proeprties from the spell check index. Only applies on resource
         * mappings (class/resource/xml-object) that have their spell-check default value (which is NA).
         *
         * <p>If not set, will use just the all proeprty for mappings it can apply to. If set to <code>INCLUDE</code>
         * will include by default all the given proeprties unless there are specific ones that have <code>EXLCUDE</code>
         * mappings. If set to <code>EXCLUDE</codE> will exclude all proeprties by default unless the given proeprties
         * are marked with <code>INCLUDE</code>.
         */
        public static final String DEFAULT_MODE = PREFIX + "defaultMode";

        /**
         * The default accuracy that will be used. Defaults to <code>0.5</code>.
         */
        public static final String ACCURACY = PREFIX + "accuracy";

        /**
         * The default number of suggestions.
         */
        public static final String NUMBER_OF_SUGGESTIONS = PREFIX + "numberOfSuggestions";

        /**
         * Sets the dictionary threshold, which controls the minimum
         * number of documents (of the total) where a term should appear. Defaults to <code>0.0f</code>.
         */
        public static final String DICTIONARY_THRESHOLD = PREFIX + "dictionaryThreshold";

        /**
         * Set to <code>true</code> in order to have a scheduled task that rebuilds the spell index
         * if needed.
         */
        public static final String SCHEDULE = PREFIX + "schedule";


        /**
         * The initial delay of the scheduled rebuild. In seconds. Defaults to 10 seconds.
         */
        public static final String SCHEDULE_INITIAL_DELAY = PREFIX + "scheduleInitialDelay";

        /**
         * Set <b>in seconds</b> the interval at which a check and a possible rebuild of the spell check
         * index will occur. Defaults to <b>10</b> minutes.
         */
        public static final String SCHEDULE_INTERVAL = PREFIX + "scheduleInterval";

        /**
         * The FQN of the spell check class.
         */
        public static final String CLASS = PREFIX + "class";
    }

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
         * Expert: Set the interval between indexed terms.  Large values cause less
         * memory to be used by IndexReader, but slow random-access to terms.  Small
         * values cause more memory to be used by an IndexReader, and speed
         * random-access to terms.
         *
         * This parameter determines the amount of computation required per query
         * term, regardless of the number of documents that contain that term.  In
         * particular, it is the maximum number of other terms that must be
         * scanned before a term is located and its frequency and position information
         * may be processed.  In a large index with user-entered query terms, query
         * processing time is likely to be dominated not by term lookup but rather
         * by the processing of frequency and positional data.  In a small index
         * or when many uncommon query terms are generated (e.g., by wildcard
         * queries) term lookup may become a dominant cost.
         *
         * In particular, <code>numUniqueTerms/interval</code> terms are read into
         * memory by an IndexReader, and, on average, <code>interval/2</code> terms
         * must be scanned for each random term access.
         *
         * @see org.apache.lucene.index.IndexWriter#DEFAULT_TERM_INDEX_INTERVAL
         */
        public static final String TERM_INDEX_INTERVAL = "compass.engine.termIndexInterval";

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

        /**
         * Setting to turn on usage of a compound file. When on, multiple files for
         * each segment are merged into a single file once the segment creation is
         * finished. This is done regardless of what directory is in use. <p/>
         * Default value id <code>true</code>
         */
        public static final String USE_COMPOUND_FILE = "compass.engine.useCompoundFile";

        /**
         * Should concurrent operations be performed during a transaction against the search engine
         * index store. Defualts to the directory used {@link org.compass.core.lucene.engine.store.DirectoryStore#supportsConcurrentOperations()}.
         */
        public static final String USE_CONCURRENT_OPERATIONS = "compass.engine.useConcurrentOperations";

        /**
         * Should concurrent commits be performed during a transaction against the search engine
         * index store. Defaults to the directory store used {@link org.compass.core.lucene.engine.store.DirectoryStore#supportsConcurrentCommits()}.
         */
        public static final String USE_CONCURRENT_COMMITS = "compass.engine.useConcurrentCommits";

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
         * Sets if invalidation of cache will happen in the background or not. By default set to <code>true</code>.
         */
        public static final String CACHE_ASYNC_INVALIDATION = "compass.engine.cacheAsyncInvalidation";

        /**
         * The default cache interval invalidation.
         *
         * @see #CACHE_INTERVAL_INVALIDATION
         */
        public static final long DEFAULT_CACHE_INTERVAL_INVALIDATION = 5000;

        /**
         * The index manager schedule interval where different actions related to index manager will happen (such
         * as global cache interval checks). Accepts time setting (10millis for 10 milliseconds, 10s for 10 seconds, and so on).
         * If set to <code>-1</code>, not scheduling will happen. Defaults to 60 seconds.
         */
        public static final String INDEX_MANAGER_SCHEDULE_INTERVAL = "compass.engine.indexManagerScheduleInterval";

        /**
         * Defaults to <code>false</code>. If set to <code>true</code>, will cause index manager operation (including
         * replace index) to wait for all other Compass instances to invalidate their cache. The wait time will be
         * the same as the {@link #INDEX_MANAGER_SCHEDULE_INTERVAL}.
         */
        public static final String WAIT_FOR_CACHE_INVALIDATION_ON_INDEX_OPERATION = "compass.engine.waitForCacheInvalidationOnIndexOperation";

        /**
         * Tracks opened index writers. Will make sure to rollback any open index writer when Compass closes.
         */
        public static final String TRACK_OPENED_INDEX_WRITERS = "compass.engine.trackOpenedIndexWriters";
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
             * The class (or the actual instance) for the data source provider. Responsible for creating data sources.
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

            public static final String EXPIRATION_TIME_IN_SECONDS = PREFIX + ".expirationTimeInSeconds";
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
         * The type of the query parser. A fully qualified class name or an actual instance, must
         * implement {@link org.compass.core.lucene.engine.queryparser.LuceneQueryParser}.
         */
        public static final String TYPE = "type";

        /**
         * The default query parser group that must be set.
         */
        public static final String DEFAULT_GROUP = "default";

        /**
         * The spell check group.
         */
        public static final String SPELLCHECK_GROUP = "spellcheck";

        /**
         * The default parsers implementation allows to set if leading wildcards
         * are allowed or not. Boolen value defaults to <code>true</code>.
         */
        public static final String DEFAULT_PARSER_ALLOW_LEADING_WILDCARD = "allowLeadingWildcard";

        /**
         * The default parsers implementation allows to use contanst score prefix query. Constnat score
         * prefix query allows for faster prefix queries but lack in highlighting support.
         * Boolen value. Defaults to <code>true</code>.
         */
        public static final String DEFAULT_PARSER_ALLOW_CONSTANT_SCORE_PREFIX_QUERY = "allowConstantScorePrefixQuery";

        /**
         * The minimum fuzzy similarity for the query parser. Default to <code>0.5f</code>.
         */
        public static final String DEFAULT_PARSER_FUZZY_MIN_SIMILARITY = "fuzzyMinSimilarity";

        /**
         * Get the prefix length for fuzzy queries. Default to <code>0</code>.
         */
        public static final String DEFAULT_PARSER_FUZZY_PERFIX_LENGTH = "fuzzyPrefixLength";

        /**
         * The default operator when parsing query strings. Defaults to <code>AND</code>. Can be either
         * <code>AND</code> or <code>OR</code>.
         */
        public static final String DEFAULT_PARSER_DEFAULT_OPERATOR = "defaultOperator";
    }

    public static abstract class LocalCache {

        public static final String DISABLE_LOCAL_CACHE = "compass.engine.disableLocalCache";

        public static final String PREFIX = "compass.engine.localCache";

        public static final String CONNECTION = "connection";

        public static final String DEFAULT_NAME = "__default__";
    }

    public static abstract class Query {

        /**
         * Sets Lucene <b>static</b> setting of the number of clauses a boolean query can hold.
         * Defaults to <code>1024</code>.
         *
         * @see org.apache.lucene.search.BooleanQuery#setMaxClauseCount(int)
         */
        public static final String MAX_CLAUSE_COUNT = "compass.query.maxClauseCount";
    }
}
