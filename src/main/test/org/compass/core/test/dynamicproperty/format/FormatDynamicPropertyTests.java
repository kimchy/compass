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

package org.compass.core.test.dynamicproperty.format;

import org.compass.core.CompassSession;
import org.compass.core.test.AbstractTestCase;

/**
 * @author kimchy
 */
public class FormatDynamicPropertyTests extends AbstractTestCase {

    @Override
    protected String[] getMappings() {
        return new String[]{"dynamicproperty/format/mapping.cpm.xml"};
    }

    public void testSimpleFormatDynamicProperty() {
        CompassSession session = openSession();

        A a = new A();
        a.id = 1;
        a.dyna = new Dyna(2, 4);

        session.save(a);

        assertEquals(1, session.find("002:0004").length());

        session.close();
    }
}