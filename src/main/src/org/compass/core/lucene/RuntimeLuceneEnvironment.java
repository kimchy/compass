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
 * @see org.compass.core.CompassOperations#getSettings()
 */
public abstract class RuntimeLuceneEnvironment {

    public static abstract class Transaction {

        /**
         * Transaction log settings
         */
        public static final class TransLog {

            /**
             * @see org.compass.core.lucene.LuceneEnvironment.Transaction.TransLog#TYPE
             */
            public static final String TYPE = LuceneEnvironment.Transaction.TransLog.TYPE;

            /**
             * @see org.compass.core.lucene.LuceneEnvironment.Transaction.TransLog#PATH
             */
            public static final String PATH = LuceneEnvironment.Transaction.TransLog.PATH;

            /**
             * @see org.compass.core.lucene.LuceneEnvironment.Transaction.TransLog#WRITE_BUFFER_SIZE
             */
            public static final String WRITE_BUFFER_SIZE = LuceneEnvironment.Transaction.TransLog.WRITE_BUFFER_SIZE;

            /**
             * @see org.compass.core.lucene.LuceneEnvironment.Transaction.TransLog#READ_BUFFER_SIZE
             */
            public static final String READ_BUFFER_SIZE = LuceneEnvironment.Transaction.TransLog.READ_BUFFER_SIZE;
        }

    }
}
