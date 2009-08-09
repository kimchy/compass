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

package org.compass.core.converter;

import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.compass.core.CompassException;
import org.compass.core.config.CompassConfigurable;
import org.compass.core.config.CompassEnvironment;
import org.compass.core.config.CompassSettings;
import org.compass.core.config.ConfigurationException;
import org.compass.core.converter.basic.*;
import org.compass.core.converter.basic.atomic.AtomicBooleanConverter;
import org.compass.core.converter.basic.atomic.AtomicIntConverter;
import org.compass.core.converter.basic.atomic.AtomicLongConverter;
import org.compass.core.converter.dynamic.GroovyDynamicConverter;
import org.compass.core.converter.dynamic.JakartaElDynamicConverter;
import org.compass.core.converter.dynamic.JexlDynamicConverter;
import org.compass.core.converter.dynamic.MVELDynamicConverter;
import org.compass.core.converter.dynamic.OgnlDynamicConverter;
import org.compass.core.converter.dynamic.VelocityDynamicConverter;
import org.compass.core.converter.extended.DateTimeConverter;
import org.compass.core.converter.extended.FileConverter;
import org.compass.core.converter.extended.InputStreamConverter;
import org.compass.core.converter.extended.LocaleConverter;
import org.compass.core.converter.extended.ObjectByteArrayConverter;
import org.compass.core.converter.extended.PrimitiveByteArrayConverter;
import org.compass.core.converter.extended.ReaderConverter;
import org.compass.core.converter.extended.SqlDateConverter;
import org.compass.core.converter.extended.SqlTimeConverter;
import org.compass.core.converter.extended.SqlTimestampConverter;
import org.compass.core.converter.mapping.json.JsonArrayMappingConverter;
import org.compass.core.converter.mapping.json.JsonContentMappingConverter;
import org.compass.core.converter.mapping.json.JsonIdMappingConverter;
import org.compass.core.converter.mapping.json.JsonPropertyMappingConverter;
import org.compass.core.converter.mapping.json.PlainJsonObjectMappingConverter;
import org.compass.core.converter.mapping.json.RootJsonObjectMappingConverter;
import org.compass.core.converter.mapping.osem.ArrayMappingConverter;
import org.compass.core.converter.mapping.osem.ClassDynamicPropertyMappingConverter;
import org.compass.core.converter.mapping.osem.ClassMappingConverter;
import org.compass.core.converter.mapping.osem.ClassPropertyMappingConverter;
import org.compass.core.converter.mapping.osem.CollectionMappingConverter;
import org.compass.core.converter.mapping.osem.ComponentMappingConverter;
import org.compass.core.converter.mapping.osem.ConstantMappingConverter;
import org.compass.core.converter.mapping.osem.ParentMappingConverter;
import org.compass.core.converter.mapping.osem.PlainCascadeMappingConverter;
import org.compass.core.converter.mapping.osem.ReferenceMappingConverter;
import org.compass.core.converter.mapping.rsem.RawResourceMappingConverter;
import org.compass.core.converter.mapping.xsem.XmlContentMappingConverter;
import org.compass.core.converter.mapping.xsem.XmlIdMappingConverter;
import org.compass.core.converter.mapping.xsem.XmlObjectMappingConverter;
import org.compass.core.converter.mapping.xsem.XmlPropertyMappingConverter;
import org.compass.core.mapping.json.JsonArrayMapping;
import org.compass.core.mapping.json.JsonContentMapping;
import org.compass.core.mapping.json.JsonIdMapping;
import org.compass.core.mapping.json.JsonPropertyMapping;
import org.compass.core.mapping.json.PlainJsonObjectMapping;
import org.compass.core.mapping.json.RootJsonObjectMapping;
import org.compass.core.mapping.osem.ArrayMapping;
import org.compass.core.mapping.osem.ClassDynamicPropertyMapping;
import org.compass.core.mapping.osem.ClassIdPropertyMapping;
import org.compass.core.mapping.osem.ClassMapping;
import org.compass.core.mapping.osem.ClassPropertyMapping;
import org.compass.core.mapping.osem.CollectionMapping;
import org.compass.core.mapping.osem.ComponentMapping;
import org.compass.core.mapping.osem.ConstantMetaDataMapping;
import org.compass.core.mapping.osem.DynamicMetaDataMapping;
import org.compass.core.mapping.osem.ParentMapping;
import org.compass.core.mapping.osem.PlainCascadeMapping;
import org.compass.core.mapping.osem.ReferenceMapping;
import org.compass.core.mapping.rsem.RawResourceMapping;
import org.compass.core.mapping.xsem.XmlContentMapping;
import org.compass.core.mapping.xsem.XmlIdMapping;
import org.compass.core.mapping.xsem.XmlObjectMapping;
import org.compass.core.mapping.xsem.XmlPropertyMapping;
import org.compass.core.util.ClassUtils;

/**
 * Acts as a <code>Converter</code> registry based on all the converters
 * supplied in the module.
 *
 * @author kimchy
 */
public class DefaultConverterLookup implements ConverterLookup {

    private static final Log log = LogFactory.getLog(DefaultConverterLookup.class);

    // not synchronized since the assumption is that no changes are made after
    // theh constructor
    private final Map<String, Converter> convertersByClass = new HashMap<String, Converter>();

    private final Map<Class, Converter> cachedConvertersByClassType = new ConcurrentHashMap<Class, Converter>();

    private final Map<String, Converter> convertersByName = new HashMap<String, Converter>();

    private final Map<String, Class> defaultConveterTypes = new HashMap<String, Class>();

    private CompassSettings settings;

    public DefaultConverterLookup() {
        defaultConveterTypes.put(CompassEnvironment.Converter.DefaultTypes.Simple.BIGDECIMAL, BigDecimalConverter.class);
        defaultConveterTypes.put(CompassEnvironment.Converter.DefaultTypes.Simple.BIGINTEGER, BigIntegerConverter.class);
        defaultConveterTypes.put(CompassEnvironment.Converter.DefaultTypes.Simple.BOOLEAN, BooleanConverter.class);
        defaultConveterTypes.put(CompassEnvironment.Converter.DefaultTypes.Simple.ATOMIC_BOOLEAN, AtomicBooleanConverter.class);
        defaultConveterTypes.put(CompassEnvironment.Converter.DefaultTypes.Simple.BYTE, ByteConverter.class);
        defaultConveterTypes.put(CompassEnvironment.Converter.DefaultTypes.Simple.CALENDAR, CalendarConverter.class);
        defaultConveterTypes.put(CompassEnvironment.Converter.DefaultTypes.Simple.CHAR, CharConverter.class);
        defaultConveterTypes.put(CompassEnvironment.Converter.DefaultTypes.Simple.DATE, DateConverter.class);
        defaultConveterTypes.put(CompassEnvironment.Converter.DefaultTypes.Simple.DOUBLE, DoubleConverter.class);
        defaultConveterTypes.put(CompassEnvironment.Converter.DefaultTypes.Simple.FLOAT, FloatConverter.class);
        defaultConveterTypes.put(CompassEnvironment.Converter.DefaultTypes.Simple.INTEGER, IntConverter.class);
        defaultConveterTypes.put(CompassEnvironment.Converter.DefaultTypes.Simple.ATOMIC_INTEGER, AtomicIntConverter.class);
        defaultConveterTypes.put(CompassEnvironment.Converter.DefaultTypes.Simple.LONG, LongConverter.class);
        defaultConveterTypes.put(CompassEnvironment.Converter.DefaultTypes.Simple.ATOMIC_LONG, AtomicLongConverter.class);
        defaultConveterTypes.put(CompassEnvironment.Converter.DefaultTypes.Simple.SHORT, ShortConverter.class);
        defaultConveterTypes.put(CompassEnvironment.Converter.DefaultTypes.Simple.STRING, StringConverter.class);
        defaultConveterTypes.put(CompassEnvironment.Converter.DefaultTypes.Simple.STRINGBUFFER, StringBufferConverter.class);
        defaultConveterTypes.put(CompassEnvironment.Converter.DefaultTypes.Simple.STRINGBUILDER, StringBuilderConverter.class);
        defaultConveterTypes.put(CompassEnvironment.Converter.DefaultTypes.Simple.ENUM, EnumConverter.class);
        defaultConveterTypes.put(CompassEnvironment.Converter.DefaultTypes.Simple.UUID, UUIDConverter.class);
        defaultConveterTypes.put(CompassEnvironment.Converter.DefaultTypes.Simple.URL, URLConverter.class);
        defaultConveterTypes.put(CompassEnvironment.Converter.DefaultTypes.Extendend.FILE, FileConverter.class);
        defaultConveterTypes.put(CompassEnvironment.Converter.DefaultTypes.Extendend.INPUT_STREAM, InputStreamConverter.class);
        defaultConveterTypes.put(CompassEnvironment.Converter.DefaultTypes.Extendend.LOCALE, LocaleConverter.class);
        defaultConveterTypes.put(CompassEnvironment.Converter.DefaultTypes.Extendend.PRIMITIVE_BYTE_ARRAY, PrimitiveByteArrayConverter.class);
        defaultConveterTypes.put(CompassEnvironment.Converter.DefaultTypes.Extendend.OBJECT_BYTE_ARRAY, ObjectByteArrayConverter.class);
        defaultConveterTypes.put(CompassEnvironment.Converter.DefaultTypes.Extendend.READER, ReaderConverter.class);
        defaultConveterTypes.put(CompassEnvironment.Converter.DefaultTypes.Extendend.SQL_DATE, SqlDateConverter.class);
        defaultConveterTypes.put(CompassEnvironment.Converter.DefaultTypes.Extendend.SQL_TIME, SqlTimeConverter.class);
        defaultConveterTypes.put(CompassEnvironment.Converter.DefaultTypes.Extendend.SQL_TIMESTAMP, SqlTimestampConverter.class);
    }

    public CompassSettings getSettings() {
        return settings;
    }

    public void configure(CompassSettings settings) throws CompassException {
        this.settings = settings;
        Map<String, CompassSettings> converterGroups = settings.getSettingGroups(CompassEnvironment.Converter.PREFIX);
        // add basic types
        addDefaultConverter(converterGroups, CompassEnvironment.Converter.DefaultTypeNames.Simple.BIGDECIMAL,
                BigDecimal.class, new BigDecimalConverter());
        addDefaultConverter(converterGroups, CompassEnvironment.Converter.DefaultTypeNames.Simple.BIGINTEGER,
                BigInteger.class, new BigIntegerConverter());
        addDefaultConverter(converterGroups, CompassEnvironment.Converter.DefaultTypeNames.Simple.BOOLEAN,
                new Class[]{Boolean.class, boolean.class}, new BooleanConverter());
        addDefaultConverter(converterGroups, CompassEnvironment.Converter.DefaultTypeNames.Simple.ATOMIC_BOOLEAN,
                new Class[]{AtomicBoolean.class}, new AtomicBooleanConverter());
        addDefaultConverter(converterGroups, CompassEnvironment.Converter.DefaultTypeNames.Simple.BYTE,
                new Class[]{Byte.class, byte.class}, new ByteConverter());
        addDefaultConverter(converterGroups, CompassEnvironment.Converter.DefaultTypeNames.Simple.CHAR,
                new Class[]{Character.class, char.class}, new CharConverter());
        addDefaultConverter(converterGroups, CompassEnvironment.Converter.DefaultTypeNames.Simple.DATE,
                Date.class, new DateConverter());
        addDefaultConverter(converterGroups, CompassEnvironment.Converter.DefaultTypeNames.Simple.CALENDAR,
                Calendar.class, new CalendarConverter());
        addDefaultConverter(converterGroups, CompassEnvironment.Converter.DefaultTypeNames.Simple.DOUBLE,
                new Class[]{Double.class, double.class}, new DoubleConverter());
        addDefaultConverter(converterGroups, CompassEnvironment.Converter.DefaultTypeNames.Simple.FLOAT,
                new Class[]{Float.class, float.class}, new FloatConverter());
        addDefaultConverter(converterGroups, CompassEnvironment.Converter.DefaultTypeNames.Simple.INTEGER,
                new Class[]{Integer.class, int.class}, new IntConverter());
        addDefaultConverter(converterGroups, CompassEnvironment.Converter.DefaultTypeNames.Simple.ATOMIC_INTEGER,
                new Class[]{AtomicInteger.class}, new AtomicIntConverter());
        addDefaultConverter(converterGroups, CompassEnvironment.Converter.DefaultTypeNames.Simple.LONG,
                new Class[]{Long.class, long.class}, new LongConverter());
        addDefaultConverter(converterGroups, CompassEnvironment.Converter.DefaultTypeNames.Simple.ATOMIC_LONG,
                new Class[]{AtomicLong.class}, new AtomicLongConverter());
        addDefaultConverter(converterGroups, CompassEnvironment.Converter.DefaultTypeNames.Simple.SHORT,
                new Class[]{Short.class, short.class}, new ShortConverter());
        addDefaultConverter(converterGroups, CompassEnvironment.Converter.DefaultTypeNames.Simple.STRING,
                String.class, new StringConverter());
        addDefaultConverter(converterGroups, CompassEnvironment.Converter.DefaultTypeNames.Simple.STRINGBUFFER,
                StringBuffer.class, new StringBufferConverter());
        addDefaultConverter(converterGroups, CompassEnvironment.Converter.DefaultTypeNames.Simple.STRINGBUILDER,
                StringBuilder.class, new StringBuilderConverter());
        addDefaultConverter(converterGroups, CompassEnvironment.Converter.DefaultTypeNames.Simple.ENUM,
                Enum.class, new EnumConverter());
        addDefaultConverter(converterGroups, CompassEnvironment.Converter.DefaultTypeNames.Simple.UUID,
                UUID.class, new UUIDConverter());
        addDefaultConverter(converterGroups, CompassEnvironment.Converter.DefaultTypeNames.Simple.URL,
                URL.class, new URLConverter());
        addDefaultConverter(converterGroups, CompassEnvironment.Converter.DefaultTypeNames.Simple.URI,
                URI.class, new URIConverter());
        // add extended types
        addDefaultConverter(converterGroups, CompassEnvironment.Converter.DefaultTypeNames.Extendend.FILE,
                File.class, new FileConverter());
        addDefaultConverter(converterGroups, CompassEnvironment.Converter.DefaultTypeNames.Extendend.INPUT_STREAM,
                InputStream.class, new InputStreamConverter());
        addDefaultConverter(converterGroups, CompassEnvironment.Converter.DefaultTypeNames.Extendend.LOCALE,
                Locale.class, new LocaleConverter());
        addDefaultConverter(converterGroups, CompassEnvironment.Converter.DefaultTypeNames.Extendend.PRIMITIVE_BYTE_ARRAY,
                byte[].class, new PrimitiveByteArrayConverter());
        addDefaultConverter(converterGroups, CompassEnvironment.Converter.DefaultTypeNames.Extendend.OBJECT_BYTE_ARRAY,
                Byte[].class, new ObjectByteArrayConverter());
        addDefaultConverter(converterGroups, CompassEnvironment.Converter.DefaultTypeNames.Extendend.READER,
                Reader.class, new ReaderConverter());
        addDefaultConverter(converterGroups, CompassEnvironment.Converter.DefaultTypeNames.Extendend.SQL_DATE,
                java.sql.Date.class, new SqlDateConverter());
        addDefaultConverter(converterGroups, CompassEnvironment.Converter.DefaultTypeNames.Extendend.SQL_TIME,
                java.sql.Time.class, new SqlTimeConverter());
        addDefaultConverter(converterGroups, CompassEnvironment.Converter.DefaultTypeNames.Extendend.SQL_TIMESTAMP,
                java.sql.Timestamp.class, new SqlTimestampConverter());
        try {
            addDefaultConverter(converterGroups, CompassEnvironment.Converter.DefaultTypeNames.Extendend.JODA_DATETIME,
                    ClassUtils.forName("org.joda.time.DateTime", settings.getClassLoader()), new DateTimeConverter());
            log.debug("JODA found in the class path, registering DataTime converter");
        } catch (Throwable t) {
            // do nothing
        }
        
        // dynamic converters
        try {
            addDefaultConverter(converterGroups, CompassEnvironment.Converter.DefaultTypeNames.Dynamic.MVEL,
                    DynamicMetaDataMapping.class, new MVELDynamicConverter());
            log.debug("Dynamic converter - MVEL found in the class path, registering it");
        } catch (Throwable e) {
            // do nothing
        }
        try {
            addDefaultConverter(converterGroups, CompassEnvironment.Converter.DefaultTypeNames.Dynamic.JEXL,
                    DynamicMetaDataMapping.class, new JexlDynamicConverter());
            log.debug("Dynamic converter - JEXL found in the class path, registering it");
        } catch (Throwable e) {
            // do nothing
        }
        try {
            addDefaultConverter(converterGroups, CompassEnvironment.Converter.DefaultTypeNames.Dynamic.VELOCITY,
                    DynamicMetaDataMapping.class, new VelocityDynamicConverter());
            log.debug("Dynamic converter - Velocity found in the class path, registering it");
        } catch (Throwable e) {
            // do nothing
        }
        try {
            addDefaultConverter(converterGroups, CompassEnvironment.Converter.DefaultTypeNames.Dynamic.JAKARTA_EL,
                    DynamicMetaDataMapping.class, new JakartaElDynamicConverter());
            log.debug("Dynamic converter - Jakarta EL found in the class path, registering it");
        } catch (Throwable e) {
            // do nothing
        }
        try {
            addDefaultConverter(converterGroups, CompassEnvironment.Converter.DefaultTypeNames.Dynamic.OGNL,
                    DynamicMetaDataMapping.class, new OgnlDynamicConverter());
            log.debug("Dynamic converter - OGNL found in the class path, registering it");
        } catch (Throwable e) {
            // do nothing
        }
        try {
            addDefaultConverter(converterGroups, CompassEnvironment.Converter.DefaultTypeNames.Dynamic.GROOVY,
                    DynamicMetaDataMapping.class, new GroovyDynamicConverter());
            log.debug("Dynamic converter - GROOVY found in the class path, registering it");
        } catch (Throwable e) {
            // do nothing
        }

        // add mapping converters
        addDefaultConverter(converterGroups, CompassEnvironment.Converter.DefaultTypeNames.Mapping.RAW_RESOURCE_MAPPING,
                RawResourceMapping.class, new RawResourceMappingConverter());
        addDefaultConverter(converterGroups, CompassEnvironment.Converter.DefaultTypeNames.Mapping.CLASS_MAPPING,
                ClassMapping.class, new ClassMappingConverter());
        addDefaultConverter(converterGroups, CompassEnvironment.Converter.DefaultTypeNames.Mapping.CLASS_PROPERTY_MAPPING,
                ClassPropertyMapping.class, new ClassPropertyMappingConverter());
        addDefaultConverter(converterGroups, CompassEnvironment.Converter.DefaultTypeNames.Mapping.CLASS_DYNAMIC_PROPERTY_MAPPING,
                ClassDynamicPropertyMapping.class, new ClassDynamicPropertyMappingConverter());
        addDefaultConverter(converterGroups, CompassEnvironment.Converter.DefaultTypeNames.Mapping.CLASS_ID_PROPERTY_MAPPING,
                ClassIdPropertyMapping.class, new ClassPropertyMappingConverter());
        addDefaultConverter(converterGroups, CompassEnvironment.Converter.DefaultTypeNames.Mapping.COMPONENT_MAPPING,
                ComponentMapping.class, new ComponentMappingConverter());
        addDefaultConverter(converterGroups, CompassEnvironment.Converter.DefaultTypeNames.Mapping.COLLECTION_MAPPING,
                CollectionMapping.class, new CollectionMappingConverter());
        addDefaultConverter(converterGroups, CompassEnvironment.Converter.DefaultTypeNames.Mapping.ARRAY_MAPPING,
                ArrayMapping.class, new ArrayMappingConverter());
        addDefaultConverter(converterGroups, CompassEnvironment.Converter.DefaultTypeNames.Mapping.REFERENCE_MAPPING,
                ReferenceMapping.class, new ReferenceMappingConverter());
        addDefaultConverter(converterGroups, CompassEnvironment.Converter.DefaultTypeNames.Mapping.CONSTANT_MAPPING,
                ConstantMetaDataMapping.class, new ConstantMappingConverter());
        addDefaultConverter(converterGroups, CompassEnvironment.Converter.DefaultTypeNames.Mapping.PARENT_MAPPING,
                ParentMapping.class, new ParentMappingConverter());
        addDefaultConverter(converterGroups, CompassEnvironment.Converter.DefaultTypeNames.Mapping.CASCADE_MAPPING,
                PlainCascadeMapping.class, new PlainCascadeMappingConverter());
        addDefaultConverter(converterGroups, CompassEnvironment.Converter.DefaultTypeNames.Mapping.XML_OBJECT_MAPPING,
                XmlObjectMapping.class, new XmlObjectMappingConverter());
        addDefaultConverter(converterGroups, CompassEnvironment.Converter.DefaultTypeNames.Mapping.XML_PROPERTY_MAPPING,
                XmlPropertyMapping.class, new XmlPropertyMappingConverter());
        addDefaultConverter(converterGroups, CompassEnvironment.Converter.DefaultTypeNames.Mapping.XML_ID_MAPPING,
                XmlIdMapping.class, new XmlIdMappingConverter());
        addDefaultConverter(converterGroups, CompassEnvironment.Converter.DefaultTypeNames.Mapping.XML_CONTENT_MAPPING,
                XmlContentMapping.class, new XmlContentMappingConverter());
        addDefaultConverter(converterGroups, CompassEnvironment.Converter.DefaultTypeNames.Mapping.JSON_ROOT_OBJECT_MAPPING,
                RootJsonObjectMapping.class, new RootJsonObjectMappingConverter());
        addDefaultConverter(converterGroups, CompassEnvironment.Converter.DefaultTypeNames.Mapping.JSON_OBJECT_MAPPING,
                PlainJsonObjectMapping.class, new PlainJsonObjectMappingConverter());
        addDefaultConverter(converterGroups, CompassEnvironment.Converter.DefaultTypeNames.Mapping.JSON_PROPERTY_MAPPING,
                JsonPropertyMapping.class, new JsonPropertyMappingConverter());
        addDefaultConverter(converterGroups, CompassEnvironment.Converter.DefaultTypeNames.Mapping.JSON_ID_MAPPING,
                JsonIdMapping.class, new JsonIdMappingConverter());
        addDefaultConverter(converterGroups, CompassEnvironment.Converter.DefaultTypeNames.Mapping.JSON_ARRAY_MAPPING,
                JsonArrayMapping.class, new JsonArrayMappingConverter());
        addDefaultConverter(converterGroups, CompassEnvironment.Converter.DefaultTypeNames.Mapping.JSON_CONTENT_MAPPING,
                JsonContentMapping.class, new JsonContentMappingConverter());

        // now configure all the none default converters
        for (String converterName : converterGroups.keySet()) {
            CompassSettings converterSettings = converterGroups.get(converterName);
            if (log.isDebugEnabled()) {
                log.debug("Conveter [" + converterName + "] building...");
            }
            Converter converter;
            Object obj = converterSettings.getSettingAsObject(CompassEnvironment.Converter.TYPE);
            if (obj == null) {
                throw new ConfigurationException("Must define a class type / object instance for converter [" + converterName + "]");
            }
            if (obj instanceof String) {
                String converterClassType = (String) obj;
                try {
                    Class converterClass = defaultConveterTypes.get(converterClassType);
                    if (converterClass == null) {
                        converterClass = ClassUtils.forName(converterClassType, settings.getClassLoader());
                    }
                    if (log.isDebugEnabled()) {
                        log.debug("Converter [" + converterName + "] is of type [" + converterClass.getName() + "]");
                    }
                    converter = (Converter) converterClass.newInstance();
                } catch (Exception e) {
                    throw new ConfigurationException("Failed to create converter type [" + converterClassType +
                            " for converter [" + converterName + "]", e);
                }
            } else {
                converter = (Converter) obj;
            }
            if (converter instanceof CompassConfigurable) {
                if (log.isDebugEnabled()) {
                    log.debug("Conveter [" + converterName + "] implements CompassConfigurable, configuring...");
                }
                ((CompassConfigurable) converter).configure(converterSettings);
            }
            convertersByName.put(converterName, converter);
            String registerClass = converterSettings.getSetting(CompassEnvironment.Converter.REGISTER_CLASS);
            if (registerClass != null) {
                try {
                    if (log.isDebugEnabled()) {
                        log.debug("Converter [" + converterName + "] registered under register type [" +
                                registerClass + "]");
                    }
                    cachedConvertersByClassType.put(ClassUtils.forName(registerClass, settings.getClassLoader()), converter);
                    convertersByClass.put(registerClass, converter);
                } catch (Exception e) {
                    throw new ConfigurationException("Failed to create register class [" + registerClass + "] " +
                            " for converter [" + converterName + "]", e);
                }
            }
        }
    }

    private void addDefaultConverter(Map<String, CompassSettings> converterGroups, String name, Class type, Converter converter) {
        addDefaultConverter(converterGroups, name, new Class[]{type}, converter);
    }

    private void addDefaultConverter(Map<String, CompassSettings> converterGroups, String name, Class[] types, Converter converter) {
        CompassSettings converterSettings = converterGroups.remove(name);
        if (converterSettings == null) {
            converterSettings = new CompassSettings(settings.getClassLoader());
            converterSettings.setGlobalSettings(settings);
        }
        String converterType = converterSettings.getSetting(CompassEnvironment.Converter.TYPE);
        if (converterType != null) {
            try {
                if (log.isDebugEnabled()) {
                    log.debug("Converter [" + name + "] (default) configured with a non default type [" + converterType + "]");
                }
                converter = (Converter) ClassUtils.forName(converterType, settings.getClassLoader()).newInstance();
            } catch (Exception e) {
                throw new ConfigurationException("Failed to create converter type [" + converterType + "] for " +
                        "converter name [" + name + "]");
            }
        }
        if (converter instanceof CompassConfigurable) {
            ((CompassConfigurable) converter).configure(converterSettings);
        }
        convertersByName.put(name, converter);
        for (Class type : types) {
            convertersByClass.put(type.getName(), converter);
            cachedConvertersByClassType.put(type, converter);
        }
    }

    public void registerConverter(String converterName, Converter converter) {
        if (log.isDebugEnabled()) {
            log.debug("Converter [" + converterName + "] registered");
        }
        convertersByName.put(converterName, converter);
    }

    public void registerConverter(String converterName, Converter converter, Class registerType) {
        if (log.isDebugEnabled()) {
            log.debug("Converter [" + converterName + "] registered with type [" + registerType + "]");
        }
        convertersByName.put(converterName, converter);
        convertersByClass.put(registerType.getName(), converter);
        cachedConvertersByClassType.put(registerType, converter);
    }

    public Converter lookupConverter(String name) {
        Converter converter = convertersByName.get(name);
        if (converter == null) {
            converter = convertersByClass.get(name);
        }
        if (converter == null) {
            throw new IllegalArgumentException("Failed to find converter by name [" + name + "]");
        }
        return converter;
    }

    /**
     * Looks up a converter based on the type. If there is a direct hit, than it
     * is returned, else it checks for a converter based on the interfaces, and
     * than recursive on the super class.
     */
    public Converter lookupConverter(Class type) {
        // not the most thread safe caching, but good enough for us
        // so we don't need to create a thread safe collection.
        Converter c = cachedConvertersByClassType.get(type);
        if (c != null) {
            return c;
        }
        c = actualConverterLookup(type);
        if (c == null) {
            return c;
        }
        cachedConvertersByClassType.put(type, c);
        return c;
    }

    private Converter actualConverterLookup(Class type) {
        Converter c = convertersByClass.get(type.getName());
        if (c != null) {
            return c;
        }
        for (Class anInterface : type.getInterfaces()) {
            c = convertersByClass.get(anInterface.getName());
            if (c != null) {
                return c;
            }
        }
        Class superClass = type.getSuperclass();
        if (superClass == null) {
            return null;
        }
        c = lookupConverter(superClass);
        if (c != null) {
            return c;
        }
        return null;
    }
}
