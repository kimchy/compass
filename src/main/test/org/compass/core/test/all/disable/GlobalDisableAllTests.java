/*
 * Copyright 2004-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.compass.core.test.all.disable;

import org.compass.core.CompassSession;
import org.compass.core.config.CompassEnvironment;
import org.compass.core.config.CompassSettings;
import org.compass.core.test.AbstractTestCase;

/**
 * @author kimchy
 */
public class GlobalDisableAllTests extends AbstractTestCase {

    @Override
    protected String[] getMappings() {
        return new String[]{"all/disable/mapping.cpm.xml"};
    }

    @Override
    protected void addSettings(CompassSettings settings) {
        super.addSettings(settings);
        settings.setBooleanSetting(CompassEnvironment.All.ENABLED, false);
    }

    public void testGlobalDisableAll() {
        CompassSession session = openSession();

        A a = new A();
        a.id = 1;
        a.value = "test";
        session.save(a);

        assertEquals(0, session.find("test").length());

        session.close();
    }
}
