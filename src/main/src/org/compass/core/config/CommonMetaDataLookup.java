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

package org.compass.core.config;

import org.compass.core.converter.ConversionException;
import org.compass.core.metadata.Alias;
import org.compass.core.metadata.CompassMetaData;
import org.compass.core.metadata.MetaData;
import org.compass.core.metadata.MetaDataGroup;

/**
 * @author kimchy
 */
public class CommonMetaDataLookup {

    private CompassMetaData metaData;

    public CommonMetaDataLookup(CompassMetaData metaData) {
        this.metaData = metaData;
    }

    public Alias lookupAlias(String value) throws ConfigurationException {
        if (value == null) {
            return null;
        }

        if (!value.startsWith("${") || !value.endsWith("}")) {
            return null;
        }

        String val = value.substring(2, value.length() - 1);
        int dotIndex = val.indexOf(".");
        if (dotIndex == -1) {
            throw new ConfigurationException("Mailformed alias lookup value, must have a '.'");
        }

        String groupId = val.substring(0, dotIndex);
        MetaDataGroup group = metaData.getGroup(groupId);
        if (group == null) {
            throw new ConfigurationException("Couldn't find group [" + groupId + "] in lookup value [" + val + "]");
        }

        String aliasId = val.substring(dotIndex + 1, val.length());
        dotIndex = aliasId.indexOf(".");
        if (dotIndex != -1) {
            aliasId = aliasId.substring(0, dotIndex);
        }

        Alias alias = group.getAlias(aliasId);
        if (alias == null) {
            throw new ConfigurationException("Couldn't find alias for [" + val + "]");
        }

        return alias;
    }

    public String lookupAliasName(String value) throws ConfigurationException {
        Alias alias = lookupAlias(value);
        if (alias == null) {
            return value;
        }

        return alias.getName();
    }

    public MetaData lookupMetaData(String value) throws ConfigurationException {
        if (value == null) {
            return null;
        }

        if (!value.startsWith("${") || !value.endsWith("}")) {
            return null;
        }

        String val = value.substring(2, value.length() - 1);
        int dotIndex = val.indexOf(".");
        if (dotIndex == -1) {
            throw new ConfigurationException(
                    "Mailformed meta-data lookup value, must have a '.' with a meta-data-group as the prefix");
        }

        String groupId = val.substring(0, dotIndex);
        MetaDataGroup group = metaData.getGroup(groupId);
        if (group == null) {
            throw new ConfigurationException("Couldn't find group [" + groupId + "] in lookup value [" + val + "]");
        }

        String metaDataId = val.substring(dotIndex + 1, val.length());
        dotIndex = metaDataId.indexOf(".");
        if (dotIndex != -1) {
            metaDataId = metaDataId.substring(0, dotIndex);
        }

        MetaData metaData = group.getMetaData(metaDataId);
        if (metaData == null) {
            throw new ConfigurationException("Couldn't find meta-data for [" + val + "]");
        }

        return metaData;
    }

    public String lookupMetaDataName(String value) throws ConfigurationException {
        MetaData metaData = lookupMetaData(value);
        if (metaData == null) {
            return value;
        }
        return metaData.getName();
    }

    public String lookupMetaDataFormat(String value) throws ConfigurationException {
        MetaData metaData = lookupMetaData(value);
        if (metaData == null) {
            // this part return null here, and not the original value
            return null;
        }

        return metaData.getFormat();
    }

    public String lookupMetaDataValue(String value) throws ConfigurationException {
        MetaData metaData = lookupMetaData(value);
        if (metaData == null) {
            return value;
        }

        int dotIndex = value.lastIndexOf(".");
        if (dotIndex == -1) {
            throw new ConversionException(
                    "Must defined a [.] after the meta data for the id of the value with lookup [" + value + "]");
        }

        String valueId = value.substring(dotIndex + 1, value.length() - 1);
        return metaData.getValue(valueId);
    }

}
