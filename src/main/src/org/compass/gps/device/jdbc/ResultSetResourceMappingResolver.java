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

package org.compass.gps.device.jdbc;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Iterator;
import javax.sql.DataSource;

import org.compass.core.CompassException;
import org.compass.core.Property;
import org.compass.core.config.InputStreamMappingResolver;
import org.compass.core.mapping.ExcludeFromAll;
import org.compass.gps.device.jdbc.mapping.AutoGenerateMapping;
import org.compass.gps.device.jdbc.mapping.ColumnToPropertyMapping;
import org.compass.gps.device.jdbc.mapping.DataColumnToPropertyMapping;
import org.compass.gps.device.jdbc.mapping.IdColumnToPropertyMapping;
import org.compass.gps.device.jdbc.mapping.ResultSetToResourceMapping;

/**
 * A Compass::Core <code>MappingResolver</code>, which can generate compass
 * mappings (<code>Resource</code> mapping)
 * {@link org.compass.gps.device.jdbc.mapping.ResultSetToResourceMapping}.
 * <p/>
 * Other relevant mapping settings can be set as well, such as subIndex, all,
 * allMetaData, and the allTermVector.
 * <p/>
 * The required property is the
 * {@link org.compass.gps.device.jdbc.mapping.ResultSetToResourceMapping}, and
 * the <code>DataSource</code>.
 * <p/>
 * Using the mapping builder helps automatically generate compass mapping files (<code>Resource</code>
 * mappings) based on the configuration of the
 * {@link org.compass.gps.device.jdbc.mapping.ResultSetToResourceMapping} or one
 * of it's sub classes (like
 * {@link org.compass.gps.device.jdbc.mapping.TableToResourceMapping}).
 *
 * @author kimchy
 */
public class ResultSetResourceMappingResolver implements InputStreamMappingResolver {

    private ResultSetToResourceMapping mapping;

    private DataSource dataSource;

    private String subIndex;

    private boolean all = true;

    private String allMetaData;

    private Property.TermVector allTermVector;

    /**
     * Creates a new mapping builder. Must set the
     * {@link  #setMapping(ResultSetToResourceMapping)}, and the
     * {@link #setDataSource(DataSource)}.
     */
    public ResultSetResourceMappingResolver() {

    }

    /**
     * Creates a new mapping builder, using the mapping and the data source.
     *
     * @param mapping The mapping that will be used to generate compass mapping
     *                definition.
     */
    public ResultSetResourceMappingResolver(ResultSetToResourceMapping mapping, DataSource dataSource) {
        this.mapping = mapping;
        this.dataSource = dataSource;
    }

    public String getName() {
        return "" + subIndex + ".cpm.xml";
    }

    /**
     * Generates the compass mapping definitions.
     */
    public InputStream getMappingAsInputStream() throws CompassException {
        StringBuffer sb = new StringBuffer();
        sb.append("<?xml version=\"1.0\"?>");
        sb.append("<!DOCTYPE compass-core-mapping PUBLIC ");
        sb.append("    \"-//Compass/Compass Core Mapping DTD 2.2//EN\"");
        sb.append("    \"http://www.compass-project.org/dtd/compass-core-mapping-2.2.dtd\">");
        sb.append("<compass-core-mapping>");
        sb.append("    <resource alias=\"").append(mapping.getAlias()).append("\"");
        if (subIndex != null) {
            sb.append(" sub-index=\"").append(subIndex).append("\"");
        }
        sb.append(" >");

        sb.append("<all enable=\"").append(all).append("\" ");
        if (allMetaData != null) {
            sb.append(" name=\"").append(allMetaData).append("\"");
        }
        if (allTermVector != null) {
            sb.append(" term-vector=\"").append(Property.TermVector.toString(allTermVector)).append("\"");
        }
        sb.append(" />");

        if (mapping.idMappingsSize() == 0) {
            if (mapping instanceof AutoGenerateMapping) {
                ((AutoGenerateMapping) mapping).generateMappings(this.dataSource);
            }
            if (mapping.idMappingsSize() == 0) {
                throw new JdbcGpsDeviceException(
                        "Can not generate resource mapping with no id to column mappings defined");
            }
        }
        for (Iterator it = mapping.idMappingsIt(); it.hasNext();) {
            IdColumnToPropertyMapping idMapping = (IdColumnToPropertyMapping) it.next();
            generateResourceIdMapping("resource-id", idMapping, sb);
        }
        for (Iterator it = mapping.dataMappingsIt(); it.hasNext();) {
            DataColumnToPropertyMapping dataMapping = (DataColumnToPropertyMapping) it.next();
            generateResourceProperyMapping("resource-property", dataMapping, sb);
        }
        sb.append("    </resource>");
        sb.append("</compass-core-mapping>");
        return new ByteArrayInputStream(sb.toString().getBytes());
    }

    private void generateResourceIdMapping(String tag, ColumnToPropertyMapping mapping, StringBuffer sb) {
        sb.append("<").append(tag).append(" ");
        sb.append("name=\"").append(mapping.getPropertyName()).append("\"").append(" ");
        if (mapping.getAnalyzer() != null) {
            sb.append("analyzer=\"").append(mapping.getAnalyzer()).append("\"").append(" ");
        }
        if (mapping.getConverter() != null) {
            sb.append("converter=\"").append(mapping.getConverter()).append("\"").append(" ");
        }
        if (mapping.getBoost() > 0.0f) {
            sb.append("boost=\"").append(mapping.getBoost()).append("\"").append(" ");
        }
        sb.append(" />");
    }

    private void generateResourceProperyMapping(String tag, ColumnToPropertyMapping mapping, StringBuffer sb) {
        sb.append("<").append(tag).append(" ");
        sb.append("name=\"").append(mapping.getPropertyName()).append("\"").append(" ");
        sb.append("index=\"").append(Property.Index.toString(mapping.getPropertyIndex())).append("\"").append(" ");
        sb.append("store=\"").append(Property.Store.toString(mapping.getPropertyStore())).append("\"").append(" ");
        sb.append("term-vector=\"").append(Property.TermVector.toString(mapping.getPropertyTermVector())).append("\"").append(" ");
        sb.append("exclude-from-all=\"").append(ExcludeFromAll.toString(mapping.getExcludeFromAll())).append("\"").append(" ");
        if (mapping.getAnalyzer() != null) {
            sb.append("analyzer=\"").append(mapping.getAnalyzer()).append("\"").append(" ");
        }
        if (mapping.getConverter() != null) {
            sb.append("converter=\"").append(mapping.getConverter()).append("\"").append(" ");
        }
        if (mapping.getBoost() > 0.0f) {
            sb.append("boost=\"").append(mapping.getBoost()).append("\"").append(" ");
        }
        sb.append(" />");
    }

    /**
     * Returns the mapping that will be used to generate the compass mapping
     * definitions.
     */
    public ResultSetToResourceMapping getMapping() {
        return mapping;
    }

    /**
     * Sets the mapping that will be used to generate the compass mapping
     * definitions.
     */
    public void setMapping(ResultSetToResourceMapping mapping) {
        this.mapping = mapping;
    }

    /**
     * Returns the subIndex that the <code>Resource</code> (alias) will be
     * mapped to. Optional - null value means no subIndex part in the compass
     * mapping definition.
     */
    public String getSubIndex() {
        return subIndex;
    }

    /**
     * Sets the subIndex that the <code>Resource</code> (alias) will be mapped
     * to. Optional - null value means no subIndex part in the compass mapping
     * definition.
     */
    public void setSubIndex(String subIndex) {
        this.subIndex = subIndex;
    }

    /**
     * Returns the all option of the compass mapping (if the all property will
     * be saved or not). Optional, defaults to <code>true</code>.
     */
    public boolean isAll() {
        return all;
    }

    /**
     * Sets the all option of the compass mapping (if the all property will be
     * saved or not). Optional, defaults to <code>true</code>.
     */
    public void setAll(boolean all) {
        this.all = all;
    }

    /**
     * Returns the all meta data name that will be used in the compass mapping.
     * Optional, defaults to not be added to the compass mapping definitions.
     */
    public String getAllMetaData() {
        return allMetaData;
    }

    /**
     * Sets the all meta data name that will be used in the compass mapping.
     * Optional, defaults to not be added to the compass mapping definitions.
     */
    public void setAllMetaData(String allMetaData) {
        this.allMetaData = allMetaData;
    }

    /**
     * Returns the term vector setting for the all property. Optional, defaults
     * to not be added to the compass mapping definitions.
     */
    public Property.TermVector getAllTermVector() {
        return allTermVector;
    }

    /**
     * Sets the term vector setting for the all property. Optional, defaults to
     * not be added to the compass mapping definitions.
     */
    public void setAllTermVector(Property.TermVector allTermVector) {
        this.allTermVector = allTermVector;
    }

    /**
     * Sets the term vector setting (as a parsable string) for the all property.
     * Optional, defaults to not be added to the compass mapping definitions.
     */
    public void setAllTermVectorString(String allTermVector) {
        this.allTermVector = Property.TermVector.fromString(allTermVector);
    }

    /**
     * Returns the jdbc data source.
     */
    public DataSource getDataSource() {
        return dataSource;
    }

    /**
     * Sets the jdbc data source.
     */
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }
}
