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

package org.compass.core.mapping.xsem;

import org.compass.core.Property;
import org.compass.core.converter.mapping.ResourcePropertyConverter;
import org.compass.core.mapping.ExcludeFromAll;
import org.compass.core.mapping.Mapping;
import org.compass.core.mapping.ResourcePropertyMapping;
import org.compass.core.mapping.support.AbstractResourcePropertyMapping;

/**
 * @author kimchy
 */
public class XmlContentMapping extends AbstractResourcePropertyMapping implements ResourcePropertyMapping {

    public Mapping copy() {
        XmlContentMapping copy = new XmlContentMapping();
        copy(copy);
        return copy;
    }
    
    /**
     * Xml content mapping is always {@link Property.Index#NO}.
     */
    public Property.Index getIndex() {
        return Property.Index.NO;
    }

    /**
     * Xml content mapping is always excluded from all
     */
    public ExcludeFromAll getExcludeFromAll() {
        return ExcludeFromAll.YES;
    }

    /**
     * Xml content mapping is alwasy {@link org.compass.core.Property.TermVector#NO}.
     */
    public Property.TermVector getTermVector() {
        return Property.TermVector.NO;
    }

    /**
     * Xml content mapping is always <code>true</code>.
     */
    public Boolean isOmitNorms() {
        return true;
    }

    /**
     * Xml content mapping is always <code>true</code>.
     */
    public Boolean isOmitTf() {
        return true;
    }

    public ResourcePropertyConverter getResourcePropertyConverter() {
        return null;
    }
}
