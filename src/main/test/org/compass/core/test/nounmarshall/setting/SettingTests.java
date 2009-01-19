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

package org.compass.core.test.nounmarshall.setting;

import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.Resource;
import org.compass.core.config.CompassEnvironment;
import org.compass.core.config.CompassSettings;
import org.compass.core.test.AbstractTestCase;

/**
 * @author kimchy
 */
public class SettingTests extends AbstractTestCase {

    protected String[] getMappings() {
        return new String[]{"nounmarshall/setting/A.cpm.xml"};
    }

    protected void addSettings(CompassSettings settings) {
        settings.setBooleanSetting(CompassEnvironment.Osem.SUPPORT_UNMARSHALL, false);
    }

    public void testNoUnmarshall() throws Exception {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        A a = new A();
        a.id = 1;
        a.value = "value";
        a.value2 = "value2";
        session.save(a);

        Resource resource = session.loadResource(A.class, new Long(1));
        assertNotNull(resource);
        assertEquals(5, resource.getProperties().length);
        assertEquals("a", resource.getAlias());
        assertEquals(2, resource.getProperties("value").length);

        a = session.load(A.class, 1);
        assertEquals(1, a.id);
        assertNull(a.value);
        assertNull(a.value2);

        tr.commit();
        session.close();
    }

}
