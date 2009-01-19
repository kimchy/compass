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

package org.compass.core.mapping.osem;

import org.compass.core.engine.naming.PropertyPath;
import org.compass.core.mapping.Mapping;
import org.compass.core.mapping.internal.InternalOverrideByNameMapping;

/**
 * @author kimchy
 */
public abstract class AbstractCollectionMapping extends AbstractAccessorMapping implements InternalOverrideByNameMapping {

    public static enum CollectionType {


        NOT_REQUIRED,
        UNKNOWN,
        SET,
        LIST,
        ENUM_SET,
        SORTED_SET,
        LINKED_HASH_SET;

        public static String toString(CollectionType collectionType) {
            if (collectionType == CollectionType.NOT_REQUIRED) {
                return "na";
            } else if (collectionType == CollectionType.UNKNOWN) {
                return "unknown";
            } else if (collectionType == CollectionType.SET) {
                return "set";
            } else if (collectionType == CollectionType.LIST) {
                return "list";
            } else if (collectionType == CollectionType.SORTED_SET) {
                return "sortset";
            } else if (collectionType == CollectionType.LINKED_HASH_SET) {
                return "linkedset";
            } else if (collectionType == CollectionType.ENUM_SET) {
                return "eset";
            }
            throw new IllegalArgumentException("Can't find collection type for [" + collectionType + "]");
        }

        public static CollectionType fromString(String collectionType) {
            if ("na".equalsIgnoreCase(collectionType)) {
                return CollectionType.NOT_REQUIRED;
            } else if ("unknown".equalsIgnoreCase(collectionType)) {
                return CollectionType.UNKNOWN;
            } else if ("set".equalsIgnoreCase(collectionType)) {
                return CollectionType.SET;
            } else if ("list".equalsIgnoreCase(collectionType)) {
                return CollectionType.LIST;
            } else if ("sortset".equalsIgnoreCase(collectionType)) {
                return CollectionType.SORTED_SET;
            } else if ("linkedset".equalsIgnoreCase(collectionType)) {
                return CollectionType.LINKED_HASH_SET;
            } else if ("eset".equalsIgnoreCase(collectionType)) {
                return CollectionType.ENUM_SET;
            }
            throw new IllegalArgumentException("Can't find collection type for [" + collectionType + "]");
        }
    }

    private PropertyPath collectionTypePath;

    private PropertyPath colSizePath;

    private Mapping elementMapping;

    private boolean overrideByName;

    private CollectionType collectionType;

    public void copy(AbstractCollectionMapping copy) {
        super.copy(copy);
        copy.setElementMapping(getElementMapping());
        copy.setCollectionTypePath(getCollectionTypePath());
        copy.setColSizePath(getColSizePath());
        copy.setOverrideByName(isOverrideByName());
        copy.setCollectionType(getCollectionType());
    }

    public boolean canBeCollectionWrapped() {
        return false;
    }

    public Mapping getElementMapping() {
        return elementMapping;
    }

    public void setElementMapping(Mapping elementMapping) {
        this.elementMapping = elementMapping;
    }

    public PropertyPath getCollectionTypePath() {
        return collectionTypePath;
    }

    public void setCollectionTypePath(PropertyPath collectionTypePath) {
        this.collectionTypePath = collectionTypePath;
    }

    public PropertyPath getColSizePath() {
        return colSizePath;
    }

    public void setColSizePath(PropertyPath colSizePath) {
        this.colSizePath = colSizePath;
    }

    public boolean isOverrideByName() {
        return overrideByName;
    }

    public void setOverrideByName(boolean overrideByName) {
        this.overrideByName = overrideByName;
    }

    public CollectionType getCollectionType() {
        return collectionType;
    }

    public void setCollectionType(CollectionType collectionType) {
        this.collectionType = collectionType;
    }
}
