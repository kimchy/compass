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

package org.compass.core.test.json.contract;

import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.Resource;
import org.compass.core.json.RawAliasedJsonObject;
import org.compass.core.test.AbstractTestCase;

/**
 * @author kimchy
 */
public class SimpleJsonContractTests extends AbstractTestCase {

    protected String[] getMappings() {
        return new String[]{"json/contract/mapping.cpm.xml"};
    }

    public void testDotPath() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        RawAliasedJsonObject jsonObject = new RawAliasedJsonObject("a", "{id : 1, value : \"test\"}");
        session.save(jsonObject);

        assertEquals(1, session.find("a.value:test").length());
        assertEquals(1, session.find("contract2.value:test").length());

        tr.commit();
        session.close();
    }

    public void testSimpleJson() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        RawAliasedJsonObject jsonObject = new RawAliasedJsonObject("a", "{id : 1, value : \"test\"}");
        session.save(jsonObject);

        Resource resource = session.loadResource("a", 1);
        assertEquals("test", resource.getValue("value"));

        tr.commit();
        session.close();
    }
}