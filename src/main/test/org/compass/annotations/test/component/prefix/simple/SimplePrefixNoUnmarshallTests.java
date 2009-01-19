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

package org.compass.annotations.test.component.prefix.simple;

import org.compass.annotations.test.AbstractAnnotationsTestCase;
import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.Resource;
import org.compass.core.config.CompassConfiguration;
import org.compass.core.config.CompassEnvironment;
import org.compass.core.config.CompassSettings;
import org.compass.core.mapping.ResourcePropertyMapping;

/**
 * @author kimchy
 */
public class SimplePrefixNoUnmarshallTests extends AbstractAnnotationsTestCase {

    protected void addSettings(CompassSettings settings) {
        settings.setBooleanSetting(CompassEnvironment.Osem.SUPPORT_UNMARSHALL, false);
    }

    protected void addExtraConf(CompassConfiguration conf) {
        conf.addClass(A.class).addClass(B.class);
    }

    public void testSimpleComponentPrefixNoUnmarshall() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        A a = new A();
        a.id = 1;
        a.b = new B();
        a.b.id = 2;
        a.b.value = "value1";
        session.save(a);

        Resource resource = session.loadResource(A.class, 1);
        assertEquals("value1", resource.getValue("test_value"));

        ResourcePropertyMapping rp = getCompass().getMapping().getResourcePropertyMappingByPath("A.b.value");
        assertNotNull(rp);
        assertEquals("test_value", rp.getName());

        assertEquals(1, session.find("A.b.value:value1").length());

        tr.commit();
        session.close();
    }
}