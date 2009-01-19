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

package org.compass.core.test.managedidconverter;

import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.Resource;
import org.compass.core.config.CompassEnvironment;
import org.compass.core.config.CompassSettings;
import org.compass.core.test.AbstractTestCase;

/**
 * @author kimchy
 */
public class ManagedIdConverterTests extends AbstractTestCase {

    protected String[] getMappings() {
        return new String[]{"managedidconverter/A.cpm.xml"};
    }

    protected void addSettings(CompassSettings settings) {
        settings.setGroupSettings(CompassEnvironment.Converter.PREFIX,
                "simple",
                new String[]{CompassEnvironment.Converter.TYPE},
                new String[]{SimpleConverter
                        .class.getName()});

    }


    public void testManagedIdConverter() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        A a = new A();
        a.id = 1;
        a.value1 = "value1";
        a.value2 = "value2";
        session.save("a", a);

        a = (A) session.load("a", new Integer(1));
        assertEquals("value1", a.value1);
        assertEquals("value2", a.value2);

        // check that the simple converter was applied to the managed id created
        Resource resource = session.loadResource("a", new Integer(1));
        assertEquals("Xvalue1", resource.getValue("$/a/value1"));
        assertEquals("Xvalue2", resource.getValue("$/a/value2"));

        tr.commit();
        session.close();
    }
}
