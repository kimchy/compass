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

package org.compass.core.config;

/**
 * Compass environment settings constants class. Additional environemnt setting
 * can be found at: {@link org.compass.core.lucene.LuceneEnvironment}.
 * 
 * @author kimchy
 */
public class CompassEnvironment {

    /**
     * Determines the connection string (like file path)
     */
    public static final String CONNECTION = "compass.engine.connection";

    /**
     * Expert. The sub context of the connection.
     */
    public static final String CONNECTION_SUB_CONTEXT = "compass.engine.connection.subContext";

    /**
     * The name of the compass instance. If Jndi is enabled, will also be the name
     * under which compass will register.
     */
    public static final String NAME = "compass.name";


    /* JNDI Settings */
    public abstract class Jndi {

        /**
         * If jndi registration is enabled
         */
        public static final String ENABLE = "compass.jndi.enable";

        /**
         * JNDI initial context class, <code>Context.INITIAL_CONTEXT_FACTORY</code>
         */
        public static final String CLASS = "compass.jndi.class";

        /**
         * JNDI provider URL, <code>Context.PROVIDER_URL</code>
         */
        public static final String URL = "compass.jndi.url";

        /**
         * prefix for arbitrary JNDI <code>InitialContext</code> properties
         */
        public static final String PREFIX = "compass.jndi";

    }
    
    /* Transaction Settings */
    
    public abstract class Transaction {

        /**
         * Sets the transaction factory to be used (<code>LocalTransactionFactory</code>
         * for example).
         */
        public static final String FACTORY = "compass.transaction.factory";

        /**
         * For transaction factories that uses synchronization, commits the transaction in the
         * <code>beforeCompletion</code> stage. Relevant transaction factories are JTA and Spring.
         * <p>
         * Can have <code>true</code> or <code>false</code> values, defaults to <code>false</code>.
         * <p>
         * <b>Must</b> be set when using a jdbc based index, and <b>must not</b> be used in other cases!.
         */
        public static final String COMMIT_BEFORE_COMPLETION = "compass.transaction.commitBeforeCompletion";

        /**
         * <code>TransactionManagerLookup</code> implementor to use for obtaining
         * the <code>TransactionManager</code>
         */
        public static final String MANAGER_LOOKUP = "compass.transaction.managerLookup";

        /**
         * JNDI name of JTA <code>UserTransaction</code> object
         */
        public static final String USER_TRANSACTION = "compass.transaction.userTransactionName";

        /**
         * The transaction isolation, can be one of the 4 constants values.
         */
        public static final String ISOLATION = "compass.transaction.isolation";

        /**
         * The transaction isolation class name that will be used as the
         * transaction. Overrides the TRANSACTION_ISOLATION if set.
         */
        public static final String ISOLATION_CLASS = "compass.transaction.isolation.class";

        /**
         * Used as the value for the TRANSACTION_ISOLATION setting. Configures the
         * transaction isolation level to have no support for transactions.
         */
        public static final String ISOLATION_NONE = "none";

        /**
         * Used as the value for the TRANSACTION_ISOLATION setting. Configures the
         * transaction isolation level to have a read uncommitted support for
         * transactions. Dirty reads, non-repeatable reads and phantom reads can
         * occur.
         */
        public static final String ISOLATION_READ_UNCOMMITTED = "read_uncommitted";

        /**
         * Used as the value for the TRANSACTION_ISOLATION setting. Configures the
         * transaction isolation level to have a read committed support for
         * transactions. Dirty reads are prevented, non-repeatable reads and phantom
         * reads can occur.
         */
        public static final String ISOLATION_READ_COMMITTED = "read_committed";

        /**
         * Used as the value for the TRANSACTION_ISOLATION setting. Configures the
         * transaction isolation level to have a repeatable read support for
         * transactions. Dirty reads and non-repeatable reads are prevented, phantom
         * reads can occur.
         */
        public static final String ISOLATION_REPEATABLE_READ = "repeatable_read";

        /**
         * Used as the value for the TRANSACTION_ISOLATION setting. Configures the
         * transaction isolation level to have a serializable support for
         * transactions. Dirty reads, non-repeatable reads and phantom reads are
         * prevented.
         */
        public static final String ISOLATION_SERIALIZABLE = "serializable";

        /**
         * Used as the value for the TRANSACTION_ISOLATION setting. Configures the
         * transaction isolation level to have a batch insert support for
         * transactions. Highly fast transaction for inserting new data into the
         * index (like rebuilding an index).
         */
        public static final String ISOLATION_BATCH_INSERT = "batch_insert";

    }

    /**
     * The name of the "all" property.
     */
    public static final String ALL_PROPERTY = "compass.property.all";

    /**
     * The default name for the "all" property.
     */
    public static final String DEFAULT_ALL_PROPERTY = "all";

    /**
     * The term vector the will be used with the all property. Can be one out
     * of: <code>no</code>, <code>yes</code>, <code>positions</code>,
     * <code>offsets</code>, <code>positions_offsets</code>.
     */
    public static final String ALL_PROPERTY_TERM_VECTOR = "compass.property.all.termVector";

    /**
     * The name of the "alias" proeprty.
     */
    public static final String ALIAS_PROPERTY = "compass.property.alias";

    /**
     * The default name for the "alias" property.
     */
    public static final String DEFAULT_ALIAS_PROPERTY = "alias";

    /**
     * The naming strategy that will be used to save internal resource
     * properties within a resource. A fully qualified class name of the naming
     * strategy.
     */
    public static final String NAMING_STRATEGY = "compass.property.naming";

    /**
     * The fully qualified class name of the naming factory.
     */
    public static final String NAMING_STRATEGY_FACTORY = "compass.property.naming.factory";

    /**
     * Sets the first level cache class.
     */
    public static final String FIRST_LEVEL_CACHE = "compass.cache.first";

    /**
     * A setting for managed id index feature. When an internal managed id is
     * created, it's index setting will be created using this global setting (if
     * not defined locally in the mapping file).
     */
    public static final String MANAGED_ID_INDEX = "compass.managedId.index";
}
