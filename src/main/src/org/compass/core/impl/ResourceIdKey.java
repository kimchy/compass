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

package org.compass.core.impl;

import org.compass.core.Property;
import org.compass.core.Resource;
import org.compass.core.mapping.CompassMapping;
import org.compass.core.mapping.ResourceMapping;
import org.compass.core.util.ResourceHelper;

/**
 * A simple resource id key helper.
 *
 * @author kimchy
 */
public final class ResourceIdKey {

    private String alias;

    private Object[] values;

    private int hashCode = Integer.MIN_VALUE;

    public ResourceIdKey(CompassMapping compassMapping, Resource idResource) {
        this(idResource.getAlias(), ResourceHelper.toIds(idResource, compassMapping));
    }

    public ResourceIdKey(ResourceMapping resourceMapping, Object[] values) {
        this(resourceMapping.getAlias(), values);
    }

    public ResourceIdKey(String alias, Property[] ids) {
        values = new Object[ids.length];
        for (int i = 0; i < values.length; i++) {
            values[i] = ids[i].getStringValue();
        }
        this.alias = alias;
    }

    public ResourceIdKey(String alias, Object[] values) {
        this.alias = alias;
        this.values = values;
    }

    public boolean equals(Object other) {
        if (this == other)
            return true;

//      We will make sure that it never happens
//        if (!(other instanceof ResourceIdKey))
//            return false;

        final ResourceIdKey idKey = (ResourceIdKey) other;
        if (!idKey.getAlias().equals(alias)) {
            return false;
        }

        for (int i = 0; i < values.length; i++) {
            if (!idKey.getValues()[i].equals(values[i])) {
                return false;
            }
        }

        return true;
    }

    public int hashCode() {
        if (hashCode == Integer.MIN_VALUE) {
            hashCode = getHashCode();
        }
        return hashCode;
    }

    public String getAlias() {
        return alias;
    }

    public Object[] getValues() {
        return values;
    }

    private int getHashCode() {
        int result = alias.hashCode();
        for (int i = 0; i < values.length; i++) {
            result = 29 * result + values[i].hashCode();
        }
        return result;
    }
}
