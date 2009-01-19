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

package org.compass.core.test.compositeid;

import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.config.CompassEnvironment;
import org.compass.core.config.CompassSettings;
import org.compass.core.test.AbstractTestCase;

/**
 * @author kimchy
 */
public class CompositeIdTests extends AbstractTestCase {

    protected String[] getMappings() {
        return new String[]{"compositeid/mapping.cpm.xml"};
    }

    protected void addSettings(CompassSettings settings) {
        settings.setGroupSettings(CompassEnvironment.Converter.PREFIX,
                "compId",
                new String[]{CompassEnvironment.Converter.TYPE},
                new String[]{CompositeIdConverter.class.getName()});
    }

    public void testCompositeId() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        A a = new A();
        a.id = new CompositeId();
        a.id.value1 = "1";
        a.id.value2 = "2";
        a.value = "data";
        session.save(a);

        a = (A) session.load(A.class, a.id);
        assertEquals(a.value, "data");
        assertEquals(a.id.value1, "1");
        assertEquals(a.id.value2, "2");


        tr.commit();
        session.close();
    }
}
