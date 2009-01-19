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

/**
 * Maps a data column to Compass <code>Resource Property</code>.
 * <p>
 * The <code>PropertyIndex</code> defaults to
 * <code>Property.Index.TOKENIZED</code>. the <code>PropertyStore</code>
 * defaults to <code>Property.Store.YES</code>, the
 * <code>PropertyTermVector</code> defaults to
 * <code>Property.TermVector.NO</code> and the <code>Boost</code> defaults
 * to <code>1.0f</code>.
 * 
 * @author kimchy
 */
public class DataColumnToPropertyMapping extends AbstractColumnToPropertyMapping {

    private Property.Index propertyIndex = Property.Index.ANALYZED;

    private Property.Store propertyStore = Property.Store.YES;

    private Property.TermVector propertyTermVector = Property.TermVector.NO;

    private float boost;

    /**
     * Creates an empty data column to property mapping. Must set at least the
     * colum index or colum name, and the property name.
     * <p>
     * The <code>PropertyIndex</code> defaults to
     * <code>Property.Index.TOKENIZED</code>. the <code>PropertyStore</code>
     * defaults to <code>Property.Store.YES</code>, the
     * <code>PropertyTermVector</code> defaults to
     * <code>Property.TermVector.NO</code> and the <code>Boost</code>
     * defaults to <code>1.0f</code>.
     */
    public DataColumnToPropertyMapping() {

    }

    /**
     * Creates a new data column to propery mapping given the column index and
     * the property name.
     * <p>
     * The <code>PropertyIndex</code> defaults to
     * <code>Property.Index.TOKENIZED</code>. the <code>PropertyStore</code>
     * defaults to <code>Property.Store.YES</code>, the
     * <code>PropertyTermVector</code> defaults to
     * <code>Property.TermVector.NO</code> and the <code>Boost</code>
     * defaults to <code>1.0f</code>.
     * 
     * @param columnIndex
     *            The data column index that will be used to look up the column
     *            value.
     * @param propertyName
     *            The Compass <code>Resource Property</code> name.
     */
    public DataColumnToPropertyMapping(int columnIndex, String propertyName) {
        this(columnIndex, propertyName, Property.Index.TOKENIZED, Property.Store.YES, Property.TermVector.NO);
    }

    /**
     * Creates a new data column to propery mapping given the column name and
     * the property name.
     * <p>
     * The <code>PropertyIndex</code> defaults to
     * <code>Property.Index.TOKENIZED</code>. the <code>PropertyStore</code>
     * defaults to <code>Property.Store.YES</code>, the
     * <code>PropertyTermVector</code> defaults to
     * <code>Property.TermVector.NO</code> and the <code>Boost</code>
     * defaults to <code>1.0f</code>.
     * 
     * @param columnName
     *            The data column name that will be used to look up the column
     *            value.
     * @param propertyName
     *            The Compass <code>Resource Property</code> name.
     */
    public DataColumnToPropertyMapping(String columnName, String propertyName) {
        this(columnName, propertyName, Property.Index.ANALYZED, Property.Store.YES, Property.TermVector.NO);
    }

    /**
     * Creates a new data column to propery mapping given the column index,
     * property name, <code>PropertyIndex</code>, and
     * <code>PropertyStore</code>.
     * <p>
     * The <code>PropertyTermVector</code> defaults to
     * <code>Property.TermVector.NO</code> and the <code>Boost</code>
     * defaults to <code>1.0f</code>.
     * 
     * @param columnIndex
     *            The data column index that will be used to look up the column
     *            value.
     * @param propertyName
     *            The Compass <code>Resource Property</code> name.
     * @param propertyIndex
     * @param propertyStore
     */
    public DataColumnToPropertyMapping(int columnIndex, String propertyName, Property.Index propertyIndex,
            Property.Store propertyStore) {
        this(columnIndex, propertyName, propertyIndex, propertyStore, Property.TermVector.NO);
    }

    /**
     * Creates a new data column to propery mapping given the column name,
     * property name, <code>PropertyIndex</code>, and
     * <code>PropertyStore</code>.
     * <p>
     * The <code>PropertyTermVector</code> defaults to
     * <code>Property.TermVector.NO</code> and the <code>Boost</code>
     * defaults to <code>1.0f</code>.
     * 
     * @param columnName
     *            The data column name that will be used to look up the column
     *            value.
     * @param propertyName
     *            The Compass <code>Resource Property</code> name.
     * @param propertyIndex
     * @param propertyStore
     */
    public DataColumnToPropertyMapping(String columnName, String propertyName, Property.Index propertyIndex,
            Property.Store propertyStore) {
        this(columnName, propertyName, propertyIndex, propertyStore, Property.TermVector.NO);
    }

    /**
     * Creates a new data column to propery mapping given the column index,
     * property name, <code>PropertyIndex</code>, and
     * <code>PropertyStore</code>.
     * 
     * @param columnIndex
     *            The data column index that will be used to look up the column
     *            value.
     * @param propertyName
     *            The Compass <code>Resource Property</code> name.
     * @param propertyIndex
     * @param propertyStore
     * @param propertyTermVector
     */
    public DataColumnToPropertyMapping(int columnIndex, String propertyName, Property.Index propertyIndex,
            Property.Store propertyStore, Property.TermVector propertyTermVector) {
        super(columnIndex, propertyName);
        this.propertyIndex = propertyIndex;
        this.propertyStore = propertyStore;
        this.propertyTermVector = propertyTermVector;
    }

    /**
     * Creates a new data column to propery mapping given the column name,
     * property name, <code>PropertyIndex</code>, and
     * <code>PropertyStore</code>.
     * 
     * @param columnName
     *            The data column name that will be used to look up the column
     *            value.
     * @param propertyName
     *            The Compass <code>Resource Property</code> name.
     * @param propertyIndex
     * @param propertyStore
     * @param propertyTermVector
     */
    public DataColumnToPropertyMapping(String columnName, String propertyName, Property.Index propertyIndex,
            Property.Store propertyStore, Property.TermVector propertyTermVector) {
        super(columnName, propertyName);
        this.propertyIndex = propertyIndex;
        this.propertyStore = propertyStore;
        this.propertyTermVector = propertyTermVector;
    }

    public Property.Index getPropertyIndex() {
        return propertyIndex;
    }

    public void setPropertyIndex(Property.Index propertyIndex) {
        this.propertyIndex = propertyIndex;
    }

    public void setPropertyIndexString(String propertyIndex) {
        this.propertyIndex = Property.Index.fromString(propertyIndex);
    }

    public Property.Store getPropertyStore() {
        return propertyStore;
    }

    public void setPropertyStore(Property.Store propertyStore) {
        this.propertyStore = propertyStore;
    }

    public void setPropertyStoreString(String propertyStore) {
        this.propertyStore = Property.Store.fromString(propertyStore);
    }

    public Property.TermVector getPropertyTermVector() {
        return propertyTermVector;
    }

    public void setPropertyTermVector(Property.TermVector propertyTermVector) {
        this.propertyTermVector = propertyTermVector;
    }

    public void setPropertyTermVectorString(String propertyTermVector) {
        this.propertyTermVector = Property.TermVector.fromString(propertyTermVector);
    }

    public float getBoost() {
        return boost;
    }

    public void setBoost(float boost) {
        this.boost = boost;
    }
}
