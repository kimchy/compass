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

package org.compass.annotations.config.binding;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;

import org.compass.annotations.*;
import org.compass.core.Property;
import org.compass.core.mapping.ResourcePropertyMapping;
import org.compass.core.mapping.osem.ClassPropertyMapping;

/**
 * @author kimchy
 */
public abstract class AnnotationsBindingUtils {

    public static String getCollectionParameterClassName(Class<?> clazz, Type type) {
        Class retVal = getCollectionParameterClass(clazz, type);
        if (retVal == null) {
            return null;
        }
        return retVal.getName();
    }

    public static Class getCollectionParameterClass(Class<?> clazz, Type type) {
        if (Collection.class.isAssignableFrom(clazz)) {
            if (type instanceof ParameterizedType) {
                ParameterizedType paramType = (ParameterizedType) type;
                Type[] actualTypeArguments = paramType.getActualTypeArguments();
                if (actualTypeArguments != null && actualTypeArguments.length == 1) {
                    return (Class) actualTypeArguments[0];
                }
            }
        }
        return null;
    }

    public static ClassPropertyMapping.ManagedId convert(ManagedId managedId) throws IllegalArgumentException {
        if (managedId == ManagedId.AUTO) {
            return ClassPropertyMapping.ManagedId.AUTO;
        } else if (managedId == ManagedId.TRUE) {
            return ClassPropertyMapping.ManagedId.TRUE;
        } else if (managedId == ManagedId.FALSE) {
            return ClassPropertyMapping.ManagedId.FALSE;
        }
        throw new IllegalArgumentException("Failed to convert managedId [" + managedId + "]");
    }

    public static Property.TermVector convert(TermVector termVector) throws IllegalArgumentException {
        if (termVector == TermVector.NO) {
            return Property.TermVector.NO;
        } else if (termVector == TermVector.YES) {
            return Property.TermVector.YES;
        } else if (termVector == TermVector.WITH_POSITIONS) {
            return Property.TermVector.WITH_POSITIONS;
        } else if (termVector == TermVector.WITH_OFFSETS) {
            return Property.TermVector.WITH_OFFSETS;
        } else if (termVector == TermVector.WITH_POSITIONS_OFFESTS) {
            return Property.TermVector.WITH_POSITIONS_OFFSETS;
        }
        throw new IllegalArgumentException("Failed to convert termVectory [" + termVector + "]");
    }

    public static ResourcePropertyMapping.ReverseType convert(Reverse reverse) throws IllegalArgumentException {
        if (reverse == Reverse.NO) {
            return ResourcePropertyMapping.ReverseType.NO;
        } else if (reverse == Reverse.READER) {
            return ResourcePropertyMapping.ReverseType.READER;
        } else if (reverse == Reverse.STRING) {
            return ResourcePropertyMapping.ReverseType.STRING;
        }
        throw new IllegalArgumentException("Failed to convert reverse [" + reverse + "]");
    }

    public static Property.Store convert(Store store) throws IllegalArgumentException {
        if (store == Store.NO) {
            return Property.Store.NO;
        } else if (store == Store.YES) {
            return Property.Store.YES;
        } else if (store == Store.COMPRESS) {
            return Property.Store.COMPRESS;
        }
        throw new IllegalArgumentException("Failed to convert store [" + store + "]");
    }

    public static Property.Index convert(Index index) throws IllegalArgumentException {
        if (index == Index.NO) {
            return Property.Index.NO;
        } else if (index == Index.TOKENIZED) {
            return Property.Index.TOKENIZED;
        } else if (index == Index.UN_TOKENIZED) {
            return Property.Index.UN_TOKENIZED;
        }
        throw new IllegalArgumentException("Failed to convert index [" + index + "]");
    }

    public static Property.Index convert(ManagedIdIndex index) throws IllegalArgumentException {
        if (index == ManagedIdIndex.NA) {
            return null;
        } else if (index == ManagedIdIndex.NO) {
            return Property.Index.NO;
        } else if (index == ManagedIdIndex.TOKENIZED) {
            return Property.Index.TOKENIZED;
        } else if (index == ManagedIdIndex.UN_TOKENIZED) {
            return Property.Index.UN_TOKENIZED;
        }
        throw new IllegalArgumentException("Failed to convert index [" + index + "]");
    }

}
