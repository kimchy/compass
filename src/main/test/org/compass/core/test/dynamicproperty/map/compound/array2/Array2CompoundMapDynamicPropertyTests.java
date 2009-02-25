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

package org.compass.core.test.dynamicproperty.map.compound.array2;

import java.util.HashMap;

import org.compass.core.CompassSession;
import org.compass.core.test.AbstractTestCase;

/**
 * @author kimchy
 */
public class Array2CompoundMapDynamicPropertyTests extends AbstractTestCase {

    @Override
    protected String[] getMappings() {
        return new String[]{"dynamicproperty/map/compound/array2/mapping.cpm.xml"};
    }

    public void testArray2CompoundDynamicProperty() {
        CompassSession session = openSession();

        A a = new A();
        a.id = 1;
        a.values = new HashMap<DynaKey, DynaValue[]>();
        a.values.put(new DynaKey("key1", "key2"), new DynaValue[]{
                new DynaValue(new String[]{"value111", "value112"}, new String[]{"value121", "value122"}),
                new DynaValue(new String[]{"value211", "value212"}, new String[]{"value221", "value222"})});

        session.save(a);

        assertEquals(1, session.find("key1:value111").length());
        assertEquals(1, session.find("key1:value111").length());
        assertEquals(0, session.find("key1:value121").length());
        assertEquals(0, session.find("key1:value122").length());
        assertEquals(1, session.find("key1:value211").length());
        assertEquals(1, session.find("key1:value212").length());
        assertEquals(0, session.find("key1:value221").length());
        assertEquals(0, session.find("key1:value222").length());

        assertEquals(0, session.find("key2:value111").length());
        assertEquals(0, session.find("key2:value111").length());
        assertEquals(0, session.find("key2:value121").length());
        assertEquals(0, session.find("key2:value122").length());
        assertEquals(0, session.find("key2:value211").length());
        assertEquals(0, session.find("key2:value212").length());
        assertEquals(0, session.find("key2:value221").length());
        assertEquals(0, session.find("key2:value222").length());

        session.close();
    }
}