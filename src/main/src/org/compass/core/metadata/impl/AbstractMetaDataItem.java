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

package org.compass.core.metadata.impl;

import org.compass.core.metadata.MetaDataGroup;

/**
 * @author kimchy
 */
public abstract class AbstractMetaDataItem {

    private String id;

    private String name;

    private String uri;

    private String displayName;

    private String description;

    private MetaDataGroup group;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public void setId(String value) {
        this.id = value;
    }

    public MetaDataGroup getGroup() {
        return group;
    }

    public void setGroup(MetaDataGroup group) {
        this.group = group;
    }

    protected void copy(AbstractMetaDataItem item) {
        item.setName(getName());
        item.setUri(getUri());
        item.setDisplayName(getDisplayName());
        item.setDescription(getDescription());
        item.setGroup(getGroup());
        item.setId(getId());
    }

    public String toString() {
        return getGroup().getId() + "/" + getId();
    }

}
