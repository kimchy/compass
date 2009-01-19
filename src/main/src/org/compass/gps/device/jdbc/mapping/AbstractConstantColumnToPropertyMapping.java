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
import org.compass.core.Property.Index;
import org.compass.core.Property.Store;
import org.compass.core.Property.TermVector;

/**
 * A helper base class which has all the property options as constants and
 * immutables except for the property name.
 * <p>
 * The <code>PropertyIndex</code> is <code>Property.Index.UN_TOKENIZED</code>.
 * the <code>PropertyStore</code> is <code>Property.Store.YES</code>, the
 * <code>PropertyTermVector</code> is <code>Property.TermVector.NO</code>
 * and the <code>Boost</code> is <code>1.0f</code>.
 * 
 * @author kimchy
 */
public abstract class AbstractConstantColumnToPropertyMapping extends AbstractColumnToPropertyMapping {

    public AbstractConstantColumnToPropertyMapping() {

    }

    public AbstractConstantColumnToPropertyMapping(String columnName, String propertyName) {
        super(columnName, propertyName);
    }

    public AbstractConstantColumnToPropertyMapping(int columnIndex, String propertyName) {
        super(columnIndex, propertyName);
    }

    public Index getPropertyIndex() {
        return Property.Index.NOT_ANALYZED;
    }

    public Store getPropertyStore() {
        return Property.Store.YES;
    }

    public TermVector getPropertyTermVector() {
        return Property.TermVector.NO;
    }

    public float getBoost() {
        return 1.0f;
    }
}
