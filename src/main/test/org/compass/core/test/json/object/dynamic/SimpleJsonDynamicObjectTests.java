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

package org.compass.core.test.json.object.dynamic;

import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.Resource;
import org.compass.core.json.RawAliasedJsonObject;
import org.compass.core.test.AbstractTestCase;

/**
 * @author kimchy
 */
public class SimpleJsonDynamicObjectTests extends AbstractTestCase {

    protected String[] getMappings() {
        return new String[]{"json/object/dynamic/mapping.cpm.xml"};
    }

    public void testSimpleDynamicJsonObject() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        RawAliasedJsonObject jsonObject = new RawAliasedJsonObject("a", "{id : 1, value : \"test\", obj : { objValue1 : \"4\", arr : [1, 2]}}");
        session.save(jsonObject);

        Resource resource = session.loadResource("a", 1);
        assertEquals("test", resource.getValue("value"));
        assertEquals("4", resource.getValue("objValue1"));
        assertEquals(2, resource.getProperties("arr").length);
        assertEquals("1", resource.getValues("arr")[0]);
        assertEquals("2", resource.getValues("arr")[1]);

        tr.commit();
        session.close();
    }

    public void testNoJsonObjectMappingDynamicJsonObject() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        RawAliasedJsonObject jsonObject = new RawAliasedJsonObject("b", "{id : 1, value : \"test\", obj : { objValue1 : \"4\", arr : [1, 2]}}");
        session.save(jsonObject);

        Resource resource = session.loadResource("b", 1);
        assertEquals("test", resource.getValue("value"));
        assertEquals("4", resource.getValue("objValue1"));
        assertEquals(2, resource.getProperties("arr").length);
        assertEquals("1", resource.getValues("arr")[0]);
        assertEquals("2", resource.getValues("arr")[1]);

        tr.commit();
        session.close();
    }

    public void testNoJsonObjectMappingDynamicJsonObjectWithDynamicNamingFull() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        RawAliasedJsonObject jsonObject = new RawAliasedJsonObject("c", "{id : 1, value : \"test\", obj : { objValue1 : \"4\", arr : [1, 2]}}");
        session.save(jsonObject);

        Resource resource = session.loadResource("c", 1);
        assertEquals("test", resource.getValue("value"));
        assertEquals("4", resource.getValue("obj.objValue1"));
        assertEquals(2, resource.getProperties("obj.arr").length);
        assertEquals("1", resource.getValues("obj.arr")[0]);
        assertEquals("2", resource.getValues("obj.arr")[1]);

        tr.commit();
        session.close();
    }
}