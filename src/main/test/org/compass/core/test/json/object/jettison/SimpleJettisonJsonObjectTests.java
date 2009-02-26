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

package org.compass.core.test.json.object.jettison;

import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.Resource;
import org.compass.core.config.CompassEnvironment;
import org.compass.core.config.CompassSettings;
import org.compass.core.json.JsonObject;
import org.compass.core.json.RawAliasedJsonObject;
import org.compass.core.json.jettison.JettisonJSONObject;
import org.compass.core.json.jettison.converter.JettisonContentConverter;
import org.compass.core.test.AbstractTestCase;

/**
 * @author kimchy
 */
public class SimpleJettisonJsonObjectTests extends AbstractTestCase {

    protected String[] getMappings() {
        return new String[]{"json/object/jettison/mapping.cpm.xml"};
    }

    protected void addSettings(CompassSettings settings) {
        settings.setClassSetting(CompassEnvironment.Jsem.JsonContent.TYPE, JettisonContentConverter.class);
    }

    public void testDotPath() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        RawAliasedJsonObject jsonObject = new RawAliasedJsonObject("a", "{id : 1, value : \"test\", obj : { objValue1 : \"4\", arr : [1, 2]}}");
        session.save(jsonObject);

        assertEquals(1, session.find("a.value:test").length());
        assertEquals(1, session.find("a.obj.objValue1:4").length());
        assertEquals(1, session.find("a.obj.arr:1").length());
        assertEquals(1, session.find("a.obj.arr:2").length());

        JsonObject jettisonJsonObject = (JsonObject) session.load("a", 1);
        assertTrue(jettisonJsonObject instanceof JettisonJSONObject);

        tr.commit();
        session.close();
    }

    public void testSimpleJsonObject() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        RawAliasedJsonObject jsonObject = new RawAliasedJsonObject("a", "{id : 1, value : \"test\", obj : { objValue1 : \"4\", arr : [1, 2]}}");
        session.save(jsonObject);

        Resource resource = session.loadResource("a", 1);
        assertEquals("test", resource.getValue("value"));
        assertEquals("4", resource.getValue("objValue1"));
        assertEquals(new Integer(4), resource.getObject("objValue1"));
        assertEquals(2, resource.getProperties("arr").length);
        assertEquals("1", resource.getValues("arr")[0]);
        assertEquals("2", resource.getValues("arr")[1]);
        assertEquals(new Integer(1), resource.getObjects("arr")[0]);
        assertEquals(new Integer(2), resource.getObjects("arr")[1]);

        JsonObject jettisonJsonObject = (JsonObject) session.load("a", 1);
        assertTrue(jettisonJsonObject instanceof JettisonJSONObject);

        tr.commit();
        session.close();
    }

    public void testSimpleJsonObjectDouble() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        RawAliasedJsonObject jsonObject = new RawAliasedJsonObject("b", "{id : 1, value : \"test\", obj : { objValue1 : \"4\", arr : [1, 2]}}");
        session.save(jsonObject);

        Resource resource = session.loadResource("b", 1);
        assertEquals("test", resource.getValue("value"));
        assertEquals(2, resource.getValues("objValue1").length);
        assertEquals("4", resource.getValue("objValue1"));
        assertEquals(new Integer(4), resource.getObject("objValue1"));
        assertEquals(4, resource.getProperties("arr").length);
        assertEquals("1", resource.getValues("arr")[0]);
        assertEquals("2", resource.getValues("arr")[1]);
        assertEquals(new Integer(1), resource.getObjects("arr")[0]);
        assertEquals(new Integer(2), resource.getObjects("arr")[1]);

        JsonObject jettisonJsonObject = (JsonObject) session.load("b", 1);
        assertTrue(jettisonJsonObject instanceof JettisonJSONObject);

        tr.commit();
        session.close();
    }
}