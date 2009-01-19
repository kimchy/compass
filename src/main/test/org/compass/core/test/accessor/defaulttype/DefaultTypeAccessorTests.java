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

package org.compass.core.test.accessor.defaulttype;

import org.compass.core.accessor.DirectPropertyAccessor;
import org.compass.core.config.CompassEnvironment;
import org.compass.core.config.CompassSettings;
import org.compass.core.mapping.osem.ClassMapping;
import org.compass.core.test.AbstractTestCase;

/**
 * @author kimchy
 */
public class DefaultTypeAccessorTests extends AbstractTestCase {

    protected String[] getMappings() {
        return new String[]{"accessor/defaulttype/mapping.cpm.xml"};
    }

    protected void addSettings(CompassSettings settings) {
        settings.setGroupSettings(CompassEnvironment.PropertyAccessor.PREFIX, CompassEnvironment.PropertyAccessor.DEFAULT_GROUP,
                new String[] {CompassEnvironment.PropertyAccessor.TYPE}, new String[] {DirectPropertyAccessor.class.getName()});
    }

    public void testDefaultAccessor() {
        ClassMapping classMapping = (ClassMapping) getCompass().getMapping().getRootMappingByClass(A.class);
        assertEquals(DirectPropertyAccessor.DirectGetter.class, classMapping.getClassPropertyMappings()[0].getGetter().getClass());
    }
}
