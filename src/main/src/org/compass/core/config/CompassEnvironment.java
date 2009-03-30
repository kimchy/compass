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
     * Should Compass register a shut down hook. Default to <code>true</code>.
     */
    public static final String REGISTER_SHUTDOWN_HOOK = "compass.registerShutdownHook";

    /**
     * Allows to set event listeners for Compas.
     */
    public abstract class Event {

        /**
         * The type of the event listener. Either a fully qualified type of the event, or an actual instance.
         */
        public static final String TYPE = "type";

        /**
         * Allows to configure {@link org.compass.core.events.PreCreateEventListener}.
         */
        public static final String PREFIX_PRE_CREATE = "compass.event.preCreate";

        /**
         * Allows to configure {@link org.compass.core.events.PreCreateResourceEventListener}.
         */
        public static final String PREFIX_PRE_CREATE_RESOURCE = "compass.event.preCreateResource";

        /**
         * Allows to configure {@link org.compass.core.events.PreDeleteEventListener}.
         */
        public static final String PREFIX_PRE_DELETE = "compass.event.preDelete";

        /**
         * Allows to configure {@link org.compass.core.events.PreDeleteResourceEventListener}.
         */
        public static final String PREFIX_PRE_DELETE_RESOURCE = "compass.event.preDeleteResource";

        /**
         * Allows to configure {@link org.compass.core.events.PreDeleteQueryEventListener}.
         */
        public static final String PREFIX_PRE_DELETE_QUERY = "compass.event.preDeleteQuery";

        /**
         * Allows to configure {@link org.compass.core.events.PreSaveEventListener}.
         */
        public static final String PREFIX_PRE_SAVE = "compass.event.preSave";

        /**
         * Allows to configure {@link org.compass.core.events.PreSaveResourceEventListener}.
         */
        public static final String PREFIX_PRE_SAVE_RESOURCE = "compass.event.preSaveResource";

        /**
         * Allows to configure {@link org.compass.core.events.PostCreateEventListener}.
         */
        public static final String PREFIX_POST_CREATE = "compass.event.postCreate";

        /**
         * Allows to configure {@link org.compass.core.events.PostCreateResourceEventListener}.
         */
        public static final String PREFIX_POST_CREATE_RESOURCE = "compass.event.postCreateResource";

        /**
         * Allows to configure {@link org.compass.core.events.PostDeleteEventListener}.
         */
        public static final String PREFIX_POST_DELETE = "compass.event.postDelete";

        /**
         * Allows to configure {@link org.compass.core.events.PostDeleteResourceEventListener}.
         */
        public static final String PREFIX_POST_DELETE_RESOURCE = "compass.event.postDeleteResource";

        /**
         * Allows to configure {@link org.compass.core.events.PostDeleteQueryEventListener}.
         */
        public static final String PREFIX_POST_DELETE_QUERY = "compass.event.postDeleteQuery";

        /**
         * Allows to configure {@link org.compass.core.events.PostSaveEventListener}.
         */
        public static final String PREFIX_POST_SAVE = "compass.event.postSave";

        /**
         * Allows to configure {@link org.compass.core.events.PostSaveResourceEventListener}.
         */
        public static final String PREFIX_POST_SAVE_RESOURCE = "compass.event.postSaveResource";
    }

    /**
     * Settings for global registration and handling of property accessors.
     *
     * <p>This is a group settings, you can set more than one property accessor setting.
     */
    public abstract class PropertyAccessor {

        /**
         * The prefix for property accessor group settings
         */
        public static final String PREFIX = "compass.propertyAccessor";

        /**
         * The fully qualified class name of the {@link PropertyAccessor} or an actual instance.
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
         * The fully qualified class name of the {@link Converter} implementation or an actual instance.
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

                public static final String ATOMIC_LONG = "atomiclong";

                public static final String BOOLEAN = "boolean";

                public static final String ATOMIC_BOOLEAN = "atomicboolean";

                public static final String BYTE = "byte";

                public static final String CHAR = "char";

                public static final String DOUBLE = "double";

                public static final String FLOAT = "float";

                public static final String INTEGER = "int";

                public static final String ATOMIC_INTEGER = "atomicint";

                public static final String SHORT = "short";

                public static final String STRING = "string";

                public static final String STRINGBUFFER = "stringbuffer";

                public static final String URL = "url";

                public static final String BIGDECIMAL = "bigdecimal";

                public static final String BIGINTEGER = "biginteger";

                public static final String ENUM = "enum";

                public static final String STRINGBUILDER = "stringbuilder";
            }

            public abstract class Extendend {

                public static final String FILE = "file";

                public static final String SQL_DATE = "sqldate";

                public static final String SQL_TIME = "sqltime";

                public static final String SQL_TIMESTAMP = "sqltimestamp";

                public static final String READER = "reader";

                public static final String PRIMITIVE_BYTE_ARRAY = "primitivebytearray";

                public static final String OBJECT_BYTE_ARRAY = "objectbytearray";

                public static final String INPUT_STREAM = "binary";

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

                public static final String ATOMIC_LONG = "atomiclong";

                public static final String BOOLEAN = "boolean";

                public static final String ATOMIC_BOOLEAN = "atomicboolean";

                public static final String BYTE = "byte";

                public static final String CHAR = "char";

                public static final String DOUBLE = "double";

                public static final String FLOAT = "float";

                public static final String INTEGER = "int";

                public static final String ATOMIC_INTEGER = "atomicint";

                public static final String SHORT = "short";

                public static final String STRING = "string";

                public static final String STRINGBUFFER = "stringbuffer";

                public static final String STRINGBUILDER = "stringbuilder";

                public static final String ENUM = "enum";

                public static final String URL = "url";

                public static final String URI = "uri";

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

                public static final String INPUT_STREAM = "binary";

                public static final String LOCALE = "locale";

                public static final String JODA_DATETIME = "jodadatetime";
            }

            public abstract class Mapping {

                public static final String RAW_RESOURCE_MAPPING = "rawResourceMapping";

                public static final String XML_OBJECT_MAPPING = "xmlObjectMapping";

                public static final String XML_PROPERTY_MAPPING = "xmlPropertyMapping";

                public static final String XML_ID_MAPPING = "xmlIdMapping";

                public static final String XML_CONTENT_MAPPING = "xmlContentMapping";

                public static final String JSON_ROOT_OBJECT_MAPPING = "jsonRootObjectMapping";

                public static final String JSON_OBJECT_MAPPING = "jsonObjectMapping";

                public static final String JSON_ARRAY_MAPPING = "jsonArrayMapping";

                public static final String JSON_PROPERTY_MAPPING = "jsonPropertyMapping";

                public static final String JSON_ID_MAPPING = "jsonIdMapping";

                public static final String JSON_CONTENT_MAPPING = "jsonContentMapping";

                public static final String CLASS_MAPPING = "classMapping";

                public static final String CLASS_PROPERTY_MAPPING = "classPropertyMapping";

                public static final String CLASS_DYNAMIC_PROPERTY_MAPPING = "classDynamicPropertyMapping";

                public static final String CLASS_ID_PROPERTY_MAPPING = "classIdPropertyMapping";

                public static final String COMPONENT_MAPPING = "component";

                public static final String REFERENCE_MAPPING = "referenceMapping";

                public static final String COLLECTION_MAPPING = "collectionMapping";

                public static final String ARRAY_MAPPING = "arrayMapping";

                public static final String CONSTANT_MAPPING = "constantMapping";

                public static final String PARENT_MAPPING = "parentMapping";

                public static final String CASCADE_MAPPING = "cascadeMapping";
            }

            public abstract class Dynamic {

                public static final String JEXL = "jexl";

                public static final String MVEL = "mvel";

                public static final String VELOCITY = "velocity";

                public static final String JAKARTA_EL = "el";

                public static final String OGNL = "ognl";

                public static final String GROOVY = "groovy";
            }
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
         * When opening a session, Compass tries to automatically start a transaction
         * and join it. This might mean that transaction settings when running within
         * a managed environemnt won't take affect. The settings allows to disable
         * the auto joining of a session to a transaction.
         */
        public static final String DISABLE_AUTO_JOIN_SESSION = "compass.transaction.disableAutoJoinSession";

        /**
         * This settings allows to disable the default behaviour of the Local transaction factory to
         * bind the session / transaction to the local thread. This means that each call to <code>beginTransaction</code>
         * will create a new transaction and not join one if one is already in progress within the thread.
         */
        public static final String DISABLE_THREAD_BOUND_LOCAL_TRANSATION = "compass.transaction.disableThreadBoundLocalTransaction";

        /**
         * Configures the transaction timeout (JTA or Spring). The defualt is <code>-1</code> which does not set the
         * timout and uses the default one configured for the transaction manager.
         */
        public static final String TRANSACTION_TIMEOUT = "compass.transaction.timeout";
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
         * The default name for the "all" property. Defaults to <code>zzz-all</code>.
         */
        public static final String DEFAULT_NAME = "zzz-all";

        /**
         * The term vector the will be used with the all property. Can be one out
         * of: <code>no</code>, <code>yes</code>, <code>positions</code>,
         * <code>offsets</code>, <code>positions_offsets</code>.
         */
        public static final String TERM_VECTOR = "compass.property.all.termVector";

        /**
         * If all property will be globablly enabled or not. By default it is enabled.
         *
         * <p>Note, enabling and disabling all property can also be done on the mapping
         * itself. If it is explicitly defined on the mappings, then it will override this
         * setting.
         */
        public static final String ENABLED = "compass.property.all.enabled";

        /**
         * Should the all property exclude the alias from all. Defaults to <code>true</code>.
         *
         * <p>Note, this can be explicitly set on the mapping level, which will then be used
         * instead of this global setting.
         */
        public static final String EXCLUDE_ALIAS = "compass.property.all.excludeAlias";

        /**
         * Should the all property omit norms. Defaults to <code>false</code>.
         *
         * <p>Note, this can be explicitly set on the mapping level, which will then be used
         * instead of this global setting.
         */
        public static final String OMIT_NORMS = "compass.property.all.omitNorms";

        /**
         * Should the all property omit tf. Defaults to <code>false</code>.
         *
         * <p>Note, this can be explicitly set on the mapping level, which will then be used
         * instead of this global setting.
         */
        public static final String OMIT_TF = "compass.property.all.omitTf";

        /**
         * Should the all property honor boost settings on sepecific proeprties when searching.
         * Defaults to <code>true</code>.
         *
         * <p>If set to <code>true</code>, more data will be saved in the index in case of a
         * specific boost value, but searching experiance will be much improved.
         */
        public static final String BOOST_SUPPORT = "compass.property.all.boostSupport";

        /**
         * Should the all property include data from properties that do not have an explicit mapping.
         * Defaults to <code>true</code>.
         */
        public static final String INCLUDE_UNMAPPED_PROPERTIES = "compass.property.all.includeUnmappedProperties";
    }

    /**
     * Settings relating to the "alias" property
     */
    public abstract class Alias {

        /**
         * The name of the "alias" proeprty. Defaults to {@link #DEFAULT_NAME} which is
         * <code>alias</code>.
         */
        public static final String NAME = "compass.property.alias";

        /**
         * The default name for the "alias" property: <code>alias</code>.
         */
        public static final String DEFAULT_NAME = "alias";

        /**
         * The name of the property where extended aliases are stored in the resource.
         * Defaults to {@link #DEFAULT_EXTENDED_ALIAS_NAME} which is <code>extendedAlias</code>.
         */
        public static final String EXTENDED_ALIAS_NAME = "compass.property.extendedAlias";

        /**
         * The default name for the extended alias property: <code>extendedAlias</code>.
         */
        public static final String DEFAULT_EXTENDED_ALIAS_NAME = "extendedAlias";
    }

    /**
     * Global settings that affect the different mappings.
     */
    public abstract class Mapping {

        /**
         * The default value of store for mappings. If set, will be used for all the mappings
         * that have not explicitly set it. If not set, will be {@link org.compass.core.Property.Store#YES}.
         */
        public static final String GLOBAL_STORE = "compass.mapping.globalStore";

        /**
         * The default value of index for mappings. If set, will be used for all the mappings
         * that have not explicitly set it. If not set, will be {@link org.compass.core.Property.Index#ANALYZED}
         * for most properties unless the converer suggested otherwise (such as
         * {@link org.compass.core.Property.Index#NOT_ANALYZED} for numbers.
         */
        public static final String GLOBAL_INDEX = "comapss.mapping.globalIndex";

        /**
         * The default value of term vector for mappings. If set, will be used for all the mappings
         * that have not explicitly set it. If not set, will be {@link org.compass.core.Property.TermVector#NO}.
         */
        public static final String GLOBAL_TERM_VECTOR = "compass.mapping.globalTermVector";

        /**
         * The default value of omit norms for mappings. If set, will be used for all the mappings
         * that have not explicitly set it. If not set, will be <code>false</code>.
         */
        public static final String GLOBAL_OMIT_NORMS = "compass.mapping.globalOmitNorms";

        /**
         * The default value of omit tf for mappings. If set, will be used for all the mappings
         * that have not explicitly set it. If not set, will be <code>false</code>.
         */
        public static final String GLOBAL_OMIT_TF = "compass.mapping.globalOmitTf";

        /**
         * Allows to configure (can be confiugred multiple times) with a mapping to use. Can be either a classpath
         * path to the resoruce to the name of the class used.
         */
        public static final String MAPPING_PREFIX = "compass.mapping";

        /**
         * A prefix for definting scanning. The "logical name" of this scan should be defiend after the prefix,
         * and then at lease the {@link #SCAN_MAPPING_PACKAGE} must be defiend and optionally another setting
         * with the same logical name and {@link #SCAN_MAPPING_PATTERN}.
         */
        public static final String SCAN_MAPPING_PREFIX = "compass.mapping.scan";

        public static final String SCAN_MAPPING_PACKAGE = "package";

        public static final String SCAN_MAPPING_PATTERN = "pattern";
    }

    public abstract class NamingStrategy {

        /**
         * The naming strategy that will be used to save internal resource
         * properties within a resource. A fully qualified class name of
         * {@link org.compass.core.engine.naming.PropertyNamingStrategy} or an
         * actual instance of it.
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
         * not defined locally in the mapping file). Possible values can be taken from
         * {@link org.compass.core.Property.Index}, with the default value of <code>no</code>.
         * (Note, the other possible value is <code>un_tokenized</code>).
         */
        public static final String MANAGED_ID_INDEX = "compass.osem.managedId.index";

        /**
         * The default managed id for all property mappings based on {@link org.compass.core.mapping.osem.ManagedId}
         * configuration. Defaults to <code>NO_STORE</code>.
         *
         * <p>This can be overriden either by using class mapping level setting or property level mapping.
         */
        public static final String MANAGED_ID_DEFAULT = "compass.osem.managedId";

        /**
         * Controls if the default support for un-marshalling within class mappings will
         * default to <code>true</code> or <code>false</code> (unless it is explicitly set
         * in the class mapping). Defaults to <code>true</code>.
         */
        public static final String SUPPORT_UNMARSHALL = "compass.osem.supportUnmarshall";

        /**
         * Controls if reference mappings for collections are lazy by default or not. Defaults to
         * <code>false</code>.
         */
        public static final String LAZY_REFERNCE = "compass.osem.lazyReference";

        /**
         * Should duplucates (object with the same ids) be filtered out when they have already
         * been marshalled during the marshalling process of a root object. Defaults to
         * <code>false</code>.
         */
        public static final String FILTER_DUPLICATES = "compass.osem.filterDuplicates";
    }

    public abstract class Jsem {

        /**
         * Settings for Json Content converters.
         */
        public abstract class JsonContent {

            public static final String TYPE = "compass.jsem.contentConverter.type";
        }
    }

    public abstract class Xsem {

        public abstract class Namespace {

            public static final String PREFIX = "compass.xsem.namespace";

            public static final String URI = "uri";
        }

        /**
         * Settings applicable to xml content mapping converters
         */
        public abstract class XmlContent {

            public static final String PREFIX = "compass.xsem.contentConverter.";

            public static final String TYPE = PREFIX + "type";

            public static final String WRAPPER = PREFIX + "wrapper";

            public static final String WRAPPER_SINGLETON = "singleton";
            public static final String WRAPPER_POOL = "pool";
            public static final String WRAPPER_PROTOTYPE = "prototype";

            /**
             * The minimum pool size. Applies to pooled xml content converters.
             */
            public static final String MIN_POOL_SIZE = PREFIX + "minPoolSize";

            /**
             * The maximum pool size. Applies to pooled xml content converters.
             */
            public static final String MAX_POOL_SIZE = PREFIX + "maxPoolSize";

            /**
             * Specific settings for dom4j.
             */
            public abstract class Dom4j {
                /**
                 * Shorthand configuration to set {@link org.compass.core.config.CompassEnvironment.Xsem.XmlContent#TYPE}
                 * with {@link org.compass.core.xml.dom4j.converter.STAXReaderXmlContentConverter}.
                 */
                public static final String TYPE_STAX = "dom4j-stax";
                /**
                 * Shorthand configuration to set {@link org.compass.core.config.CompassEnvironment.Xsem.XmlContent#TYPE}
                 * with {@link org.compass.core.xml.dom4j.converter.SAXReaderXmlContentConverter}.
                 */
                public static final String TYPE_SAX = "dom4j-sax";
                /**
                 * Shorthand configuration to set {@link org.compass.core.config.CompassEnvironment.Xsem.XmlContent#TYPE}
                 * with {@link org.compass.core.xml.dom4j.converter.XPPReaderXmlContentConverter}.
                 */
                public static final String TYPE_XPP = "dom4j-xpp";
                /**
                 * Shorthand configuration to set {@link org.compass.core.config.CompassEnvironment.Xsem.XmlContent#TYPE}
                 * with {@link org.compass.core.xml.dom4j.converter.XPP3ReaderXmlContentConverter}.
                 */
                public static final String TYPE_XPP3 = "dom4j-xpp3";

                /**
                 * Controls the output format for dom4j. Values are <code>default</code> and <code>compact</code>.
                 * Defaults to <code>default</code>.
                 */
                public static final String OUTPUT_FORMAT = PREFIX + "dom4j.outputFormat";
            }

            /**
             * Specific settings for javax.
             */
            public abstract class Javax {
                /**
                 * Shorthand configuration to set {@link org.compass.core.config.CompassEnvironment.Xsem.XmlContent#TYPE}
                 * with {@link org.compass.core.xml.javax.converter.NodeXmlContentConverter}.
                 */
                public static final String TYPE_NODE = "javax-node";
                /**
                 * Shorthand configuration to set {@link org.compass.core.config.CompassEnvironment.Xsem.XmlContent#TYPE}
                 * with {@link org.compass.core.xml.javax.converter.StaxNodeXmlContentConverter}.
                 */
                public static final String TYPE_STAX = "javax-stax";
            }

            /**
             * Specific settings for JDOM.
             */
            public abstract class JDom {
                /**
                 * Shorthand configuration to set {@link org.compass.core.config.CompassEnvironment.Xsem.XmlContent#TYPE}
                 * with {@link org.compass.core.xml.jdom.converter.SAXBuilderXmlContentConverter}.
                 */
                public static final String TYPE_SAX = "jdom-sax";
                /**
                 * Shorthand configuration to set {@link org.compass.core.config.CompassEnvironment.Xsem.XmlContent#TYPE}
                 * with {@link org.compass.core.xml.jdom.converter.STAXBuilderXmlContentConverter}.
                 */
                public static final String TYPE_STAX = "jdom-stax";

                /**
                 * Controls the output format for dom4j. Values are <code>default</code> and <code>compact</code>.
                 * Defaults to <code>default</code>.
                 */
                public static final String OUTPUT_FORMAT = PREFIX + "jdom.outputFormat";
            }
        }
    }

    /**
     * Settings for cascading operations.
     */
    public abstract class Cascade {

        /**
         * Disable all cascading operations.
         */
        public static final String DISABLE = "compass.cascade.disable";

        /**
         * The fully qualified class name of a class implementing CompassCascadeFilter, which
         * allows filtering of create/insert/delete cascade operations.
         */
        public static final String FILTER_TYPE = "compass.cascade.filter.type";
    }

    public abstract class NullValue {

        /**
         * When setting the globabl null value, some mappings might require to disable
         * handling of null values for them. In such a case they can set the null value
         * for this specific mappings to this constant value (<code>$disable$</code>).
         */
        public static final String DISABLE_NULL_VALUE_FOR_MAPPING = "$disable$";

        /**
         * Allow to set a global null value that will be used. If this is set, the turning it off
         * for specific mappings can be done by assiging them the value of
         * {@link #DISABLE_NULL_VALUE_FOR_MAPPING}.
         */
        public static final String NULL_VALUE = "compass.nullvalue";
    }

    /**
     * A set of executor manager relevant settings.
     */
    public abstract class ExecutorManager {

        private static final String EXECUTOR_MANAGER_PREFIX = "compass.executorManager.";

        /**
         * The type of the executor manager used. Can also be the FQN of the implementation.
         * Defaults to the "concurrent" executor manager.
         */
        public static final String EXECUTOR_MANAGER_TYPE = EXECUTOR_MANAGER_PREFIX + "type";

        /**
         * A set of settings for the scheduled executor manager (based on java.util.concurrent).
         */
        public abstract class Scheduled {

            /**
             * The name (type) of the scheduled executor manager.
             */
            public static final String NAME = "scheduled";

            private static final String PREFIX = EXECUTOR_MANAGER_PREFIX + NAME + ".";

            /**
             * The core pool size that is used with the scheduled executor service. Defaults to <code>10</code>.
             */
            public static final String CORE_POOL_SIZE = PREFIX + "corePoolSize";
        }

        /**
         * A set of settings for the concurrent executor manager.
         */
        public abstract class Concurrent {

            /**
             * The name (type) of the concurrent executor manager.
             */
            public static final String NAME = "concurrent";

            private static final String PREFIX = EXECUTOR_MANAGER_PREFIX + NAME + ".";

            /**
             * The core pool size of the scheduled executor service. Defaults to <code>1</code>.
             */
            public static final String SCHEDULED_CORE_POOL_SIZE = PREFIX + "scheduledCorePoolSize";

            /**
             * The core pool size of the executor service, defaults to <code>10</code>.
             */
            public static final String CORE_POOL_SIZE = PREFIX + "corePoolSize";

            /**
             * The maximum pool size of the executor service. Defaults to <code>30</code>.
             */
            public static final String MAXIMUM_POOL_SIZE = PREFIX + "maximumPoolSize";

            /**
             * The keep alive time of the executor service (in <b>milliseconds</b>).
             * Defaults to <code>60000</code>, which is 60 seconds.
             */
            public static final String KEEP_ALIVE_TIME = PREFIX + "keepAliveTime";

        }

        /**
         * A set of settings of the work manager based executor manager.
         */
        public abstract class WorkManager {

            /**
             * The name (type) of the work manager executor manager.
             */
            public static final String NAME = "workManager";

            private static final String PREFIX = EXECUTOR_MANAGER_PREFIX + NAME + ".";

            /**
             * The JNDI to lookup the JNDI name from. Required.
             */
            public static final String JNDI_NAME = PREFIX + "jndiName";
        }

        /**
         * A set of settings of the commonj based executor manager.
         */
        public abstract class CommonJ {

            /**
             * The name (type) of the work manager executor manager.
             */
            public static final String NAME = "commonj";

            private static final String PREFIX = EXECUTOR_MANAGER_PREFIX + NAME + ".";

            /**
             * The JNDI to lookup the JNDI name from. Required.
             */
            public static final String WORK_MANAGER_JNDI_NAME = PREFIX + "workManagerJndiName";

            public static final String TIMER_MANAGER_JNDI_NAME = PREFIX + "timerManagerJndiName";
        }
    }

    /**
     * A set of settings that affect the automatic scanner.
     */
    public abstract static class Scanner {

        private static final String PREFIX = "compass.scanner.";

        public static final String READER = PREFIX + "reader";
    }

    /**
     * Settings allowing to contol the {@link org.compass.core.Compass#rebuild()} process.
     */
    public abstract static class Rebuild {

        /**
         * The time to sleep before closing a Compass instnace that was replaced by a rebuild
         * process. Defaults to 5 seconds. Set in <b>milliseconds</b>.
         */
        public static final String SLEEP_BEFORE_CLOSE = "compass.rebuild.sleepBeforeClose";

        public static final long DEFAULT_SLEEP_BEFORE_CLOSE = 5 * 1000;
    }

    /**
     * Settins controlling how reflection is performed when invoking methods (getter/setter),
     * fields, and constructors.
     */
    public abstract static class Reflection {

        /**
         * The type of reflection used. Defaults to {@link #ASM}.
         *
         * @see #ASM
         * @see #PLAIN
         */
        public static final String TYPE = "compass.reflection.type";

        /**
         * Plain reflection using java reflection API.
         */
        public static final String PLAIN = "plain";

        /**
         * Uses ASM based reflection.
         */
        public static final String ASM = "asm";
    }

    public static final String DEBUG = "compass.debug";
}
