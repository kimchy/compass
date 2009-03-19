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

package org.compass.needle.terracotta.transaction.processor;

/**
 * Settings that apply to {@link TerracottaTransactionProcessorFactory}.
 *
 * @author kimchy
 */
public class TerracottaTransactionProcessorEnvironment {

    /**
     * The name of the processor.
     */
    public static final String NAME = "tc";

    /**
     * Allows to control if order between concurrent transactions within a single JVM or across JVMs on
     * the sub index level will have to be ordered or not. If they are ordered, a lock will be obtained
     * once a dirty operation is perfomed against the sub index, and it will only be released once the
     * transaction commits / rolls back. Default value is <code>true</code>.
     */
    public static final String MAINTAIN_ORDER = "compass.transaction.processor.tc.maintainOrder"; 


    /**
     * <code>true</code> if this terracotta processor will also act as worker and processor
     * transactional jobs, <code>false</code> if this is just a client node that only submits
     * transactions to be processed by worker nodes. Defaults to <code>true</code>.
     */
    public static final String PROCESS = "compass.transaction.processor.tc.process";

    /**
     * A comma separated list of sub indexs that the processor node (worker) will process against the index.
     * Defaults to all sub indexes.
     */
    public static final String SUB_INDEXES = "compass.transaction.processor.tc.subIndexes";

    /**
     * A comma separated list of aliases that the processor node (worker) will process against the index.
     * Defaults to all aliases.
     */
    public static final String ALIASES = "compass.transaction.processor.tc.aliases";

    /**
     * Once a transaction is identified as needed to be processed, this
     * setting controls the number of additional transactions the processor will try to get in a non
     * blocking fashion.
     *
     * <p>Defaults to <code>5</code>.
     */
    public static final String NON_BLOCKING_BATCH_JOBS_SIZE = "compass.transaction.processor.tc.nonBlockingBatchJobSize";

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
    public static final String BATCH_JOBS_SIZE = "compass.transaction.processor.tc.batchJobSize";

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
    public static final String BATCH_JOBS_TIMEOUT = "compass.transaction.processor.tc.batchJobTimeout";
}
