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
}
