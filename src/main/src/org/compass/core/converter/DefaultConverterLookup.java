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

package org.compass.core.converter;

import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import org.compass.core.converter.basic.BigDecimalConverter;
import org.compass.core.converter.basic.BigIntegerConverter;
import org.compass.core.converter.basic.BooleanConverter;
import org.compass.core.converter.basic.ByteConverter;
import org.compass.core.converter.basic.CalendarConverter;
import org.compass.core.converter.basic.CharConverter;
import org.compass.core.converter.basic.DateConverter;
import org.compass.core.converter.basic.DoubleConverter;
import org.compass.core.converter.basic.FloatConverter;
import org.compass.core.converter.basic.IntConverter;
import org.compass.core.converter.basic.LongConverter;
import org.compass.core.converter.basic.ShortConverter;
import org.compass.core.converter.basic.StringBufferConverter;
import org.compass.core.converter.basic.StringConverter;
import org.compass.core.converter.basic.URLConverter;
import org.compass.core.converter.extended.FileConverter;
import org.compass.core.converter.extended.InputStreamConverter;
import org.compass.core.converter.extended.LocaleConverter;
import org.compass.core.converter.extended.ObjectByteArrayConverter;
import org.compass.core.converter.extended.PrimitiveByteArrayConverter;
import org.compass.core.converter.extended.ReaderConverter;
import org.compass.core.converter.extended.SqlDateConverter;
import org.compass.core.converter.extended.SqlTimeConverter;
import org.compass.core.converter.extended.SqlTimestampConverter;
import org.compass.core.converter.mapping.osem.ArrayMappingConverter;
import org.compass.core.converter.mapping.osem.ClassMappingConverter;
import org.compass.core.converter.mapping.osem.ClassPropertyMappingConverter;
import org.compass.core.converter.mapping.osem.CollectionMappingConverter;
import org.compass.core.converter.mapping.osem.ComponentMappingConverter;
import org.compass.core.converter.mapping.osem.ConstantMappingConverter;
import org.compass.core.converter.mapping.osem.ParentMappingConverter;
import org.compass.core.converter.mapping.osem.ReferenceMappingConverter;
import org.compass.core.mapping.osem.ArrayMapping;
import org.compass.core.mapping.osem.ClassMapping;
import org.compass.core.mapping.osem.ClassPropertyIdMapping;
import org.compass.core.mapping.osem.ClassPropertyMapping;
import org.compass.core.mapping.osem.CollectionMapping;
import org.compass.core.mapping.osem.ComponentMapping;
import org.compass.core.mapping.osem.ConstantMetaDataMapping;
import org.compass.core.mapping.osem.ParentMapping;
import org.compass.core.mapping.osem.ReferenceMapping;

/**
 * Acts as a <code>Converter</code> registry based on all the converters
 * supplied in the module.
 *
 * @author kimchy
 */
public class DefaultConverterLookup implements ConverterLookup {

    // not synchronized since the assumption is that no changes are made after
    // theh constructor
    private final HashMap converters = new HashMap();

    private final HashMap cachedConvertersByClassType = new HashMap();

    private final HashMap convertersByName = new HashMap();

    public DefaultConverterLookup() {
        // mapping converters
        addConverter(ClassMapping.class, new ClassMappingConverter());
        addConverter(ClassPropertyMapping.class, new ClassPropertyMappingConverter());
        addConverter(ClassPropertyIdMapping.class, new ClassPropertyMappingConverter());
        addConverter(ComponentMapping.class, new ComponentMappingConverter());
        addConverter(CollectionMapping.class, new CollectionMappingConverter());
        addConverter(ArrayMapping.class, new ArrayMappingConverter());
        addConverter(ReferenceMapping.class, new ReferenceMappingConverter());
        addConverter(ConstantMetaDataMapping.class, new ConstantMappingConverter());
        addConverter(ParentMapping.class, new ParentMappingConverter());

        // simple types converters
        addConverter(BigDecimal.class, new BigDecimalConverter());
        addConverter(BigInteger.class, new BigIntegerConverter());
        addConverter(Boolean.class, new BooleanConverter());
        addConverter(boolean.class, new BooleanConverter());
        addConverter(Byte.class, new ByteConverter());
        addConverter(byte.class, new ByteConverter());
        addConverter(Character.class, new CharConverter());
        addConverter(char.class, new CharConverter());
        addConverter(Date.class, new DateConverter());
        addConverter(Calendar.class, new CalendarConverter());
        addConverter(Double.class, new DoubleConverter());
        addConverter(double.class, new DoubleConverter());
        addConverter(Float.class, new FloatConverter());
        addConverter(float.class, new FloatConverter());
        addConverter(Integer.class, new IntConverter());
        addConverter(int.class, new IntConverter());
        addConverter(Long.class, new LongConverter());
        addConverter(long.class, new LongConverter());
        addConverter(Short.class, new ShortConverter());
        addConverter(short.class, new ShortConverter());
        addConverter(String.class, new StringConverter());
        addConverter(StringBuffer.class, new StringBufferConverter());
        addConverter(URL.class, new URLConverter());
        // extended
        addConverter(File.class, new FileConverter());
        addConverter(java.sql.Date.class, new SqlDateConverter());
        addConverter(Time.class, new SqlTimeConverter());
        addConverter(Timestamp.class, new SqlTimestampConverter());
        addConverter(Reader.class, new ReaderConverter());
        addConverter(byte[].class, new PrimitiveByteArrayConverter());
        addConverter(Byte[].class, new ObjectByteArrayConverter());
        addConverter(InputStream.class, new InputStreamConverter());
        addConverter(Locale.class, new LocaleConverter());
    }

    public void registerConverter(String converterName, Converter converter) {
        convertersByName.put(converterName, converter);
    }

    public Converter lookupConverter(String name) {
        Converter converter = (Converter) convertersByName.get(name);
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
        Converter c = (Converter) cachedConvertersByClassType.get(type);
        if (c != null) {
            return c;
        }
        synchronized (cachedConvertersByClassType) {
            c = actualConverterLookup(type);
            cachedConvertersByClassType.put(type, c);
            return c;
        }
    }

    private Converter actualConverterLookup(Class type) {
        Converter c = (Converter) converters.get(type.getName());
        if (c != null) {
            return c;
        }
        Class[] interfaces = type.getInterfaces();
        for (int i = 0; i < interfaces.length; i++) {
            c = (Converter) converters.get(interfaces[i].getName());
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

    public void addConverter(Class type, Converter converter) {
        converters.put(type.getName(), converter);
        cachedConvertersByClassType.put(type, converter);
    }
}
