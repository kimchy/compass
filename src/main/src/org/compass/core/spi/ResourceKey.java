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

package org.compass.core.spi;

import org.compass.core.Property;
import org.compass.core.Resource;
import org.compass.core.engine.subindex.SubIndexHash;
import org.compass.core.engine.utils.ResourceHelper;
import org.compass.core.mapping.ResourceMapping;

/**
 * A simple resource id key helper.
 *
 * @author kimchy
 */
public final class ResourceKey {

    private String alias;

    private String subIndex;

    private Property[] ids;

    private int hashCode = Integer.MIN_VALUE;

    private ResourceMapping resourceMapping;

    public ResourceKey(ResourceMapping resourceMapping, Resource idResource) {
        this(resourceMapping, ResourceHelper.toIds(idResource, resourceMapping));
    }

    public ResourceKey(ResourceMapping resourceMapping, Property[] ids) {
        this.resourceMapping = resourceMapping;
        this.ids = ids;
        this.alias = resourceMapping.getAlias();
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
            hashCode = getHashCode();
        }
        return hashCode;
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

    public ResourceMapping getResourceMapping() {
        return this.resourceMapping;
    }

    private int getHashCode() {
        int result = alias.hashCode();
        for (int i = 0; i < ids.length; i++) {
            result = 29 * result + ids[i].getStringValue().hashCode();
        }
        return result;
    }
}
