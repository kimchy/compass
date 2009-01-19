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

package org.compass.core.spi;

import java.io.Serializable;

import org.compass.core.CompassException;
import org.compass.core.Property;
import org.compass.core.Resource;
import org.compass.core.engine.SearchEngineFactory;
import org.compass.core.engine.subindex.SubIndexHash;
import org.compass.core.engine.utils.ResourceHelper;
import org.compass.core.mapping.ResourceMapping;

/**
 * A simple resource id key helper.
 *
 * @author kimchy
 */
public final class ResourceKey implements Serializable {

    private static final char SEPARATOR = '#';

    private String alias;

    private String subIndex;

    private Property[] ids;

    private transient int hashCode = Integer.MIN_VALUE;

    private transient ResourceMapping resourceMapping;

    public ResourceKey(ResourceMapping resourceMapping, Resource idResource) {
        this(resourceMapping, ResourceHelper.toIds(idResource, resourceMapping));
    }

    public ResourceKey(ResourceMapping resourceMapping, Property[] ids) {
        this.resourceMapping = resourceMapping;
        this.ids = ids;
        this.alias = resourceMapping.getAlias();
    }


    public String getAlias() {
        return alias;
    }

    public String getSubIndex() {
        if (subIndex == null) {
            SubIndexHash subIndexHash = getResourceMapping().getSubIndexHash();
            subIndex = subIndexHash.mapSubIndex(getAlias(), getIds());
        }
        return subIndex;
    }

    public Property[] getIds() {
        return ids;
    }

    public String buildUID() throws CompassException {
        StringBuilder sb = new StringBuilder();
        sb.append(getAlias()).append(SEPARATOR);
        for (Property idProp : getIds()) {
            String idValue = idProp.getStringValue();
            if (idValue == null) {
                throw new CompassException("Missing id [" + idProp.getName() + "] for alias [" + getAlias() + "]");
            }
            sb.append(idValue);
            sb.append(SEPARATOR);
        }
        return sb.toString();
    }

    public String getUIDPath() {
        return this.resourceMapping.getUIDPath();
    }

    public ResourceMapping getResourceMapping() {
        return this.resourceMapping;
    }

    public void attach(SearchEngineFactory searchEngineFactory) {
        this.resourceMapping = searchEngineFactory.getMapping().getRootMappingByAlias(getAlias());
    }

    public boolean equals(Object other) {
        if (this == other)
            return true;

//      We will make sure that it never happens
//        if (!(other instanceof ResourceKey))
//            return false;

        final ResourceKey key = (ResourceKey) other;
        if (!key.alias.equals(alias)) {
            return false;
        }

        for (int i = 0; i < ids.length; i++) {
            if (!key.ids[i].getStringValue().equals(ids[i].getStringValue())) {
                return false;
            }
        }

        return true;
    }

    public int hashCode() {
        if (hashCode == Integer.MIN_VALUE) {
            hashCode = computeHashCode();
        }
        return hashCode;
    }
    
    private int computeHashCode() {
        int result = alias.hashCode();
        for (Property id : ids) {
            result = 29 * result + id.getStringValue().hashCode();
        }
        return result;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("alias [").append(getAlias()).append("]");
        sb.append(" uid [").append(buildUID()).append("]");
        return sb.toString();
    }
}
