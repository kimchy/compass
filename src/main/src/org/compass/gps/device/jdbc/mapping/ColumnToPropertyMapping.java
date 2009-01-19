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

package org.compass.gps.device.jdbc.mapping;

import org.compass.core.Property;
import org.compass.core.mapping.ExcludeFromAll;

/**
 * A general interface for a jdbc column mapping to a Compass
 * <code>Property</code>. Holds differnt aspects of the mapped
 * <code>Property</code> like the property name, the
 * <code>Property.Index</code>, <code>Property.Store</code>,
 * <code>Proeprty.TermVector</code> and the property boost level.
 * 
 * @author kimchy
 */
public interface ColumnToPropertyMapping extends ColumnMapping {

    /**
     * Returns the <code>Resource</code> property name.
     * 
     * @return the proeprty name.
     */
    String getPropertyName();
    
    /**
     * Sets the <code>Resource</code> property name
     *
     */
    void setPropertyName(String propertyName);

    /**
     * Returns the property index option.
     */
    Property.Index getPropertyIndex();

    /**
     * Returns the property store option.
     */
    Property.Store getPropertyStore();

    /**
     * Returns the property termVector option.
     */
    Property.TermVector getPropertyTermVector();

    /**
     * Returns the property boost level.
     */
    float getBoost();

    /**
     * The analyzer that will be used to analyzer this property.
     */
    String getAnalyzer();

    /**
     * Returns the converter lookup name that will be used to convert this property.
     */
    String getConverter();

    /**
     * Should this property be excluded from the all property.
     */
    ExcludeFromAll getExcludeFromAll();
}
