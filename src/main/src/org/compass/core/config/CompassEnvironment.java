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

    /**
     * Settings for global rregistration and handling of property accessors.
     * <p/>
     * This is a group settings, you can set more than one property accessor setting.
     */
    public abstract class PropertyAccessor {

        /**
         * The prefix for property accessor group settings
         */
        public static final String PREFIX = "compass.propertyAccessor";

        /**
         * The fully qualified class name of the {@link PropertyAccessor}.
         */
        public static final String TYPE = "type";

        /**
         * The group name of the default group. It will be used when no
         * accessor is defined.
         */
        public static final String DEFAULT_GROUP = "default";
    }

    /**
     * Converter settings
     */
    public abstract class Converter {

        /**
         * The prefix used for converter groups.
         */
        public static final String PREFIX = "compass.converter";

        /**
         * The fully qualified class name of the {@link Converter} implementation.
         */
        public static final String TYPE = "type";

        /**
         * The class that the converter will be registered under, ADVANCE USAGE.
         */
        public static final String REGISTER_CLASS = "registerClass";

        /**
         * Default compass short hand types. It means that instead of defining a converter of
         * type <code>org.compass.converter.basic.DateConverter</code>, you can use the
         * shorthand name.
         */
        public abstract class DefaultTypes {

            public abstract class Simple {

                public static final String DATE = "date";

                public static final String CALENDAR = "calendar";

                public static final String LONG = "long";

                public static final String BOOLEAN = "boolean";

                public static final String BYTE = "byte";

                public static final String CHAR = "char";

                public static final String DOUBLE = "double";

                public static final String FLOAT = "float";

                public static final String INTEGER = "int";

                public static final String SHORT = "short";

                public static final String STRING = "string";

                public static final String STRINGBUFFER = "stringbuffer";

                public static final String URL = "url";

                public static final String BIGDECIMAL = "bigdecimal";

                public static final String BIGINTEGER = "biginteger";
            }

            public abstract class Extendend {

                public static final String FILE = "file";

                public static final String SQL_DATE = "sqldate";

                public static final String SQL_TIME = "sqltime";

                public static final String SQL_TIMESTAMP = "sqltimestamp";

                public static final String READER = "reader";

                public static final String PRIMITIVE_BYTE_ARRAY = "primitivebytearray";

                public static final String OBJECT_BYTE_ARRAY = "objectbytearray";

                public static final String INPUT_STREAM = "inputstream";

                public static final String LOCALE = "locale";
            }

        }

        /**
         * The default name types that compass default converters will be registered under.
         */
        public abstract class DefaultTypeNames {

            public abstract class Simple {

                public static final String DATE = "date";

                public static final String CALENDAR = "calendar";

                public static final String LONG = "long";

                public static final String BOOLEAN = "boolean";

                public static final String BYTE = "byte";

                public static final String CHAR = "char";

                public static final String DOUBLE = "double";

                public static final String FLOAT = "float";

                public static final String INTEGER = "int";

                public static final String SHORT = "short";

                public static final String STRING = "string";

                public static final String STRINGBUFFER = "stringbuffer";

                public static final String URL = "url";

                public static final String BIGDECIMAL = "bigdecimal";

                public static final String BIGINTEGER = "biginteger";
            }

            public abstract class Extendend {

                public static final String FILE = "file";

                public static final String SQL_DATE = "sqldate";

                public static final String SQL_TIME = "sqltime";

                public static final String SQL_TIMESTAMP = "sqltimestamp";

                public static final String READER = "reader";

                public static final String PRIMITIVE_BYTE_ARRAY = "primitivebytearray";

                public static final String OBJECT_BYTE_ARRAY = "objectbytearray";

                public static final String INPUT_STREAM = "inputstream";

                public static final String LOCALE = "locale";
            }

            public abstract class Mapping {

                public static final String RAW_RESOURCE_MAPPING = "rawResourceMapping";

                public static final String XML_OBJECT_MAPPING = "xmlObjectMapping";

                public static final String XML_PROPERTY_MAPPING = "xmlPropertyMapping";

                public static final String XML_ID_MAPPING = "xmlIdMapping";

                public static final String XML_CONTENT_MAPPING = "xmlContentMapping";

                public static final String CLASS_MAPPING = "classMapping";

                public static final String CLASS_PROPERTY_MAPPING = "classPropertyMapping";

                public static final String CLASS_ID_PROPERTY_MAPPING = "classIdPropertyMapping";

                public static final String COMPONENT_MAPPING = "component";

                public static final String REFERENCE_MAPPING = "referenceMapping";

                public static final String COLLECTION_MAPPING = "collectionMapping";

                public static final String ARRAY_MAPPING = "arrayMapping";

                public static final String CONSTANT_MAPPING = "constantMapping";

                public static final String PARENT_MAPPING = "parentMapping";
            }

            public abstract class Dynamic {

                public static final String JEXL = "jexl";

                public static final String VELOCITY = "velocity";

                public static final String JAKARTA_EL = "el";

                public static final String OGNL = "ognl";

                public static final String GROOVY = "groovy";
            }
        }

        /**
         * Settings applicable to xml content mapping converters
         */
        public abstract class XmlContent {

            public static final String TYPE = "xmlContentConverter.type";

            public static final String WRAPPER = "xmlContentConverter.wrapper";

            public static final String WRAPPER_SINGLETON = "singleton";
            public static final String WRAPPER_POOL = "pool";
            public static final String WRAPPER_PROTOTYPE = "prototype";

            /**
             * The minimum pool size. Applies to pooled xml content converters.
             */
            public static final String MIN_POOL_SIZE = "xmlContentConverter.minPoolSize";

            /**
             * The maximum pool size. Applies to pooled xml content converters.
             */
            public static final String MAX_POOL_SIZE = "xmlContentConverter.maxPoolSize";
        }

        /**
         * Formatted settings that apply to all the default date and number types.
         */
        public abstract class Format {

            /**
             * The format itself. For data format structure, please see
             * {@link java.text.SimpleDateFormat}. For number formats
             * please see {@link java.text.DecimalFormat}.
             */
            public static final String FORMAT = "format";

            /**
             * The locale used with the formatters.
             */
            public static final String LOCALE = "format.locale";

            /**
             * The minimum pool size. Formatters are pooled for better
             * performance.
             */
            public static final String MIN_POOL_SIZE = "format.minPoolSize";

            /**
             * The maximum pool size. Formatters are pooled for better
             * performance.
             */
            public static final String MAX_POOL_SIZE = "format.maxPoolSize";
        }
    }

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
         * <p/>
         * Can have <code>true</code> or <code>false</code> values, defaults to <code>false</code>.
         * <p/>
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
         * Set whether to cache the JTA UserTransaction object fetched from JNDI.
         * <p>Default is "true": UserTransaction lookup will only happen at startup,
         * reusing the same UserTransaction handle for all transactions of all threads.
         * This is the most efficient choice for all application servers that provide
         * a shared UserTransaction object (the typical case).
         * <p>Turn this flag off to enforce a fresh lookup of the UserTransaction
         * for every transaction. This is only necessary for application servers
         * that return a new UserTransaction for every transaction, keeping state
         * tied to the UserTransaction object itself rather than the current thread.
         */
        public static final String CACHE_USER_TRANSACTION = "compass.transaction.cacheUserTransaction";

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
     * Settings relating to the "all" property.
     */
    public abstract class All {

        /**
         * Controls the name of the "all" property.
         */
        public static final String NAME = "compass.property.all";

        /**
         * The default name for the "all" property.
         */
        public static final String DEFAULT_NAME = "all";

        /**
         * The term vector the will be used with the all property. Can be one out
         * of: <code>no</code>, <code>yes</code>, <code>positions</code>,
         * <code>offsets</code>, <code>positions_offsets</code>.
         */
        public static final String TERM_VECTOR = "compass.property.all.termVector";

    }

    /**
     * Settings relating to the "alias" property
     */
    public abstract class Alias {

        /**
         * The name of the "alias" proeprty.
         */
        public static final String NAME = "compass.property.alias";

        /**
         * The default name for the "alias" property.
         */
        public static final String DEFAULT_NAME = "alias";
    }

    public abstract class NamingStrategy {

        /**
         * The naming strategy that will be used to save internal resource
         * properties within a resource. A fully qualified class name of the naming
         * strategy.
         */
        public static final String TYPE = "compass.property.naming";

        /**
         * The fully qualified class name of the naming factory.
         */
        public static final String FACTORY_TYPE = "compass.property.naming.factory";
    }

    /**
     * Settings for cache management of objects / resources
     */
    public abstract class Cache {

        public abstract class FirstLevel {

            /**
             * Sets the first level cache class.
             */
            public static final String TYPE = "compass.cache.first";
        }
    }

    public abstract class Osem {

        /**
         * A setting for managed id index feature. When an internal managed id is
         * created, it's index setting will be created using this global setting (if
         * not defined locally in the mapping file).
         */
        public static final String MANAGED_ID_INDEX = "compass.osem.managedId.index";

        /**
         * Controls if the default support for un-marshalling within class mappings will
         * default to <code>true</code> or <code>false</code> (unless it is explicitly set
         * in the class mapping). Defaults to <code>true</code>.
         */
        public static final String SUPPORT_UNMARSHALL = "compass.osem.supportUnmarshall";
    }

}
