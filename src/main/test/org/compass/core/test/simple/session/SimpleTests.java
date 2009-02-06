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

package org.compass.core.test.simple.session;

import org.compass.core.CompassIndexSession;
import org.compass.core.CompassSearchSession;
import org.compass.core.test.AbstractTestCase;

/**
 * @author kimchy
 */
public class SimpleTests extends AbstractTestCase {

    protected String[] getMappings() {
        return new String[]{"simple/session/A.cpm.xml"};
    }

    public void testSimple() throws Exception {
        CompassIndexSession indexSession = getCompass().openIndexSession();

        A a = new A();
        a.id = 1;
        a.value = "value";
        indexSession.save(a);

        indexSession.commit();

        CompassSearchSession searchSession = getCompass().openSearchSession();

        assertNotNull(searchSession.get(A.class, 1));

        searchSession.close();
    }
}