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

package org.compass.annotations.config.binding;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;

import org.compass.annotations.Cascade;
import org.compass.annotations.Index;
import org.compass.annotations.ManagedId;
import org.compass.annotations.ManagedIdIndex;
import org.compass.annotations.OmitNorms;
import org.compass.annotations.OmitTf;
import org.compass.annotations.Reverse;
import org.compass.annotations.Store;
import org.compass.annotations.TermVector;
import org.compass.core.Property;
import org.compass.core.mapping.ExcludeFromAll;
import org.compass.core.mapping.ReverseType;

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
                    Type actualType = actualTypeArguments[0];
                    if (actualType instanceof Class) {
                        return (Class) actualTypeArguments[0];
                    } else if (actualType instanceof ParameterizedType) {
                        return (Class) ((ParameterizedType) actualType).getRawType();
                    }
                }
            }
        }
        return null;
    }

    public static org.compass.core.mapping.osem.ManagedId convert(ManagedId managedId) throws IllegalArgumentException {
        if (managedId == ManagedId.NA) {
            return null;
        } else if (managedId == ManagedId.AUTO) {
            return org.compass.core.mapping.osem.ManagedId.AUTO;
        } else if (managedId == ManagedId.TRUE) {
            return org.compass.core.mapping.osem.ManagedId.TRUE;
        } else if (managedId == ManagedId.FALSE) {
            return org.compass.core.mapping.osem.ManagedId.FALSE;
        } else if (managedId == ManagedId.NO) {
            return org.compass.core.mapping.osem.ManagedId.NO;
        } else if (managedId == ManagedId.NO_STORE) {
            return org.compass.core.mapping.osem.ManagedId.NO_STORE;
        }
        throw new IllegalArgumentException("Failed to convert managedId [" + managedId + "]");
    }

    public static Property.TermVector convert(TermVector termVector) throws IllegalArgumentException {
        if (termVector == TermVector.NA) {
            return null;
        } else if (termVector == TermVector.NO) {
            return Property.TermVector.NO;
        } else if (termVector == TermVector.YES) {
            return Property.TermVector.YES;
        } else if (termVector == TermVector.WITH_POSITIONS) {
            return Property.TermVector.WITH_POSITIONS;
        } else if (termVector == TermVector.WITH_OFFSETS) {
            return Property.TermVector.WITH_OFFSETS;
        } else if (termVector == TermVector.WITH_POSITIONS_OFFSETS) {
            return Property.TermVector.WITH_POSITIONS_OFFSETS;
        }
        throw new IllegalArgumentException("Failed to convert termVectory [" + termVector + "]");
    }

    public static ReverseType convert(Reverse reverse) throws IllegalArgumentException {
        if (reverse == Reverse.NO) {
            return ReverseType.NO;
        } else if (reverse == Reverse.READER) {
            return ReverseType.READER;
        } else if (reverse == Reverse.STRING) {
            return ReverseType.STRING;
        }
        throw new IllegalArgumentException("Failed to convert reverse [" + reverse + "]");
    }

    public static ExcludeFromAll convert(org.compass.annotations.ExcludeFromAll excludeFromAll) throws IllegalArgumentException {
        if (excludeFromAll == org.compass.annotations.ExcludeFromAll.NO) {
            return ExcludeFromAll.NO;
        } else if (excludeFromAll == org.compass.annotations.ExcludeFromAll.NO_ANALYZED) {
            return ExcludeFromAll.NO_ANALYZED;
        } else if (excludeFromAll == org.compass.annotations.ExcludeFromAll.YES) {
            return ExcludeFromAll.YES;
        }
        throw new IllegalArgumentException("Failed to convert exclude from all [" + excludeFromAll + "]");
    }

    public static Boolean convert(OmitNorms omitNorms) {
        if (omitNorms == OmitNorms.NA) {
            return null;
        } else if (omitNorms == OmitNorms.YES) {
            return true;
        } else if (omitNorms == OmitNorms.NO) {
            return false;
        }
        throw new IllegalArgumentException("Failed to convert omitNorms [" + omitNorms + "]");
    }

    public static Boolean convert(OmitTf omitTf) {
        if (omitTf == OmitTf.NA) {
            return null;
        } else if (omitTf == OmitTf.YES) {
            return true;
        } else if (omitTf == OmitTf.NO) {
            return false;
        }
        throw new IllegalArgumentException("Failed to convert omitTf [" + omitTf + "]");
    }

    public static Property.Store convert(Store store) throws IllegalArgumentException {
        if (store == Store.NA) {
            return null;
        } else if (store == Store.NO) {
            return Property.Store.NO;
        } else if (store == Store.YES) {
            return Property.Store.YES;
        } else if (store == Store.COMPRESS) {
            return Property.Store.COMPRESS;
        }
        throw new IllegalArgumentException("Failed to convert store [" + store + "]");
    }

    public static Property.Index convert(Index index) throws IllegalArgumentException {
        if (index == Index.NA) {
            return null;
        } else if (index == Index.NO) {
            return Property.Index.NO;
        } else if (index == Index.ANALYZED) {
            return Property.Index.ANALYZED;
        } else if (index == Index.NOT_ANALYZED) {
            return Property.Index.NOT_ANALYZED;
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
        } else if (index == ManagedIdIndex.NOT_ANALYZED) {
            return Property.Index.NOT_ANALYZED;
        } else if (index == ManagedIdIndex.UN_TOKENIZED) {
            return Property.Index.UN_TOKENIZED;
        }
        throw new IllegalArgumentException("Failed to convert index [" + index + "]");
    }

    public static org.compass.core.mapping.Cascade convert(Cascade cascade) throws IllegalArgumentException {
        if (cascade == Cascade.ALL) {
            return org.compass.core.mapping.Cascade.ALL;
        } else if (cascade == Cascade.CREATE) {
            return org.compass.core.mapping.Cascade.CREATE;
        } else if (cascade == Cascade.DELETE) {
            return org.compass.core.mapping.Cascade.DELETE;
        } else if (cascade == Cascade.SAVE) {
            return org.compass.core.mapping.Cascade.SAVE;
        }
        throw new IllegalArgumentException("Failed to convert cascade [" + cascade + "]");
    }
}
