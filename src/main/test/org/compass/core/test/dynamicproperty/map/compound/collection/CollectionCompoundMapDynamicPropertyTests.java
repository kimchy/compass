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

package org.compass.core.test.dynamicproperty.map.compound.collection;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.compass.core.CompassSession;
import org.compass.core.test.AbstractTestCase;

/**
 * @author kimchy
 */
public class CollectionCompoundMapDynamicPropertyTests extends AbstractTestCase {

    @Override
    protected String[] getMappings() {
        return new String[]{"dynamicproperty/map/compound/collection/mapping.cpm.xml"};
    }

    public void testCollectionCompoundDynamicProperty() {
        CompassSession session = openSession();

        A a = new A();
        a.id = 1;
        a.values = new HashMap<DynaKey, List<DynaValue>>();
        a.values.put(new DynaKey("key1", "key2"), Arrays.asList(new DynaValue("value11", "value12"), new DynaValue("value21", "value22")));

        session.save(a);

        assertEquals(1, session.find("key1:value11").length());
        assertEquals(0, session.find("key2:value12").length());
        assertEquals(1, session.find("key1:value21").length());
        assertEquals(0, session.find("key2:value22").length());

        session.close();
    }
}