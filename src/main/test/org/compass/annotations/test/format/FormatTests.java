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

package org.compass.annotations.test.format;

import org.compass.annotations.test.AbstractAnnotationsTestCase;
import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.Resource;
import org.compass.core.config.CompassConfiguration;

/**
 * @author kimchy
 */
public class FormatTests extends AbstractAnnotationsTestCase {

    protected void addExtraConf(CompassConfiguration conf) {
        conf.addClass(A.class);
    }

    public void testLongFormat() throws Exception {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        A a = new A();
        a.id = 1;
        a.value = 2;
        session.save(a);

        Resource resource = session.loadResource(A.class, 1);
        assertEquals("002", resource.getValue("value"));

        // A.value will actually tell Compass about the converter as well
        assertEquals(1, session.find("A.value:2").length());
        // A.value.value will actually tell Compass about the converter as well
        assertEquals(1, session.find("A.value.value:2").length());
        // The all property is not aware of the converter specified for the value
        assertEquals(0, session.find("2").length());
        assertEquals(1, session.find("002").length());

        tr.commit();
        session.close();
    }
}