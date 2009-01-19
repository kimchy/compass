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

package org.compass.core.config.binding;

import org.compass.core.config.ConfigurationException;
import org.compass.core.mapping.MappingException;
import org.compass.core.metadata.Alias;
import org.compass.core.metadata.CompassMetaData;
import org.compass.core.metadata.MetaData;
import org.compass.core.metadata.MetaDataGroup;
import org.compass.core.metadata.impl.AbstractMetaDataItem;
import org.compass.core.metadata.impl.DefaultAlias;
import org.compass.core.metadata.impl.DefaultCompassMetaData;
import org.compass.core.metadata.impl.DefaultMetaData;
import org.compass.core.metadata.impl.DefaultMetaDataGroup;
import org.compass.core.util.config.ConfigurationHelper;

/**
 * @author kimchy
 */
public abstract class MetaDataBinding extends AbstractConfigurationHelperMappingBinding {

    public boolean doAddConfiguration(ConfigurationHelper doc) throws ConfigurationException, MappingException {
        if (!doc.getName().equals("compass-core-meta-data")) {
            return false;
        }

        ConfigurationHelper[] groups = doc.getChildren("meta-data-group");
        for (int i = 0; i < groups.length; i++) {
            DefaultMetaDataGroup group = new DefaultMetaDataGroup();
            bindGroup(groups[i], group, metaData);
            MetaDataGroup tmpGroup = metaData.getGroup(group.getName());
            if (tmpGroup != null) {
                throw new ConfigurationException("Group [" + group.getName() + "] found twice");
            }
            ((DefaultCompassMetaData) metaData).addGroup(group);
        }

        return true;
    }

    private void bindGroup(ConfigurationHelper groupConf, DefaultMetaDataGroup group,
                                      CompassMetaData compassMetaData) throws ConfigurationException {
        bindAbstractMetaDataItem(groupConf, group);

        ConfigurationHelper[] aliases = groupConf.getChildren("alias");
        for (int i = 0; i < aliases.length; i++) {
            DefaultAlias alias = new DefaultAlias();
            alias.setGroup(group);
            bindAlias(aliases[i], alias);
            Alias tmpAlias = compassMetaData.getAlias(alias.getId());
            if (tmpAlias != null) {
                throw new ConfigurationException("Alias [" + alias + "] found twice");
            }
            group.addAlias(alias);
        }

        ConfigurationHelper[] metadatas = groupConf.getChildren("meta-data");
        for (int i = 0; i < metadatas.length; i++) {
            DefaultMetaData metaData = new DefaultMetaData();
            metaData.setGroup(group);
            bindMetaData(metadatas[i], metaData);
            MetaData tmpMetaData = compassMetaData.getMetaData(metaData.getId());
            if (tmpMetaData != null) {
                throw new ConfigurationException("MetaData [" + metaData + "] found twice");
            }
            group.addMetaData(metaData);
        }
    }

    private void bindAlias(ConfigurationHelper aliasConf, DefaultAlias alias) throws ConfigurationException {
        bindAbstractMetaDataItem(aliasConf, alias);
    }

    private void bindMetaData(ConfigurationHelper metaDataConf, DefaultMetaData metaData)
            throws ConfigurationException {
        bindAbstractMetaDataItem(metaDataConf, metaData);
        ConfigurationHelper nameConf = metaDataConf.getChild("name");
        metaData.setFormat(nameConf.getAttribute("format", null));

        ConfigurationHelper[] values = metaDataConf.getChildren("value");
        for (int i = 0; i < values.length; i++) {
            String id = values[i].getAttribute("id");
            metaData.setValue(id, values[i].getValue().trim());
        }
    }

    private void bindAbstractMetaDataItem(ConfigurationHelper absConf, AbstractMetaDataItem item) {
        item.setId(absConf.getAttribute("id"));
        item.setDisplayName(absConf.getAttribute("displayName", item.getName()));
        ConfigurationHelper descriptionConf = absConf.getChild("description", false);
        if (descriptionConf != null) {
            item.setDescription(descriptionConf.getValue().trim());
        } else {
            item.setDescription(item.getName());
        }

        ConfigurationHelper uriConf = absConf.getChild("uri", false);
        if (uriConf != null) {
            item.setUri(uriConf.getValue().trim());
        } else {
            item.setUri(item.getName());
        }

        ConfigurationHelper nameConf = absConf.getChild("name", false);
        if (nameConf != null) {
            item.setName(nameConf.getValue().trim());
        }
    }

}
