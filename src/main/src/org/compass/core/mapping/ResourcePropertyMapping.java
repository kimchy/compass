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

package org.compass.core.mapping;

import org.compass.core.Property;
import org.compass.core.converter.mapping.ResourcePropertyConverter;

/**
 * @author kimchy
 */
public interface ResourcePropertyMapping extends Mapping {

    /**
     * Returns the original name of the resource property, before any prefix aditions or any
     * other post processing manipulation.
     */
    String getOriginalName();

    /**
     * Returns the anayzer name that is associated with the property.
     * Can be <code>null</code> (i.e. not set).
     */
    String getAnalyzer();

    /**
     * Returns the root resource mapping alias name this resource property mapping belongs to.
     */
    String getRootAlias();

    /**
     * Returns <code>true</code> if this mapping is an internal one (<code>$/</code> notation).
     */
    boolean isInternal();

    /**
     * Returns the boost level.
     *
     * @see Property#setBoost(float)
     */
    float getBoost();

    /**
     * Should the reosurce property omit norms or not.
     *
     * @see Property#setOmitNorms(boolean)
     */
    Boolean isOmitNorms();

    /**
     * Expert:
     *
     * If set, omit tf from postings of this indexed field.
     *
     * @see Property#setOmitTf(boolean)
     */
    Boolean isOmitTf();

    ExcludeFromAll getExcludeFromAll();

    SpellCheck getSpellCheck();

    Property.Store getStore();

    Property.Index getIndex();

    Property.TermVector getTermVector();

    ReverseType getReverse();

    String getNullValue();

    boolean hasNullValue();

    ResourcePropertyConverter getResourcePropertyConverter();
}
