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

package org.compass.core.test.dynamicproperty.map.collection;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.compass.core.CompassSession;
import org.compass.core.test.AbstractTestCase;

/**
 * @author kimchy
 */
public class CollectionMapDynamicPropertyTests extends AbstractTestCase {

    @Override
    protected String[] getMappings() {
        return new String[]{"dynamicproperty/map/collection/mapping.cpm.xml"};
    }

    public void testCollectionDynamicProperty() {
        CompassSession session = openSession();

        A a = new A();
        a.id = 1;
        a.values = new HashMap<String, List<String>>();
        a.values.put("tag1", Arrays.asList("value1", "value2"));

        session.save(a);

        assertEquals(1, session.find("tag1:value1").length());
        assertEquals(1, session.find("tag1:value2").length());

        session.close();
    }
}