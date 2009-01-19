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

package org.compass.core.test.component.inferalias;

import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.test.AbstractTestCase;
import org.compass.core.test.component.SimpleComponent;
import org.compass.core.test.component.SimpleRoot;

/**
 * @author kimchy
 */
public class InferComponentRefAliasTests extends AbstractTestCase {

    protected String[] getMappings() {
        return new String[]{"component/inferalias/InferRefAlias.cpm.xml",
                "component/SimpleComponent.cpm.xml"};
    }

    public void testInferRefAlias() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        Long id = new Long(1);

        SimpleRoot root = new SimpleRoot();
        root.setId(id);
        root.setValue("test");
        SimpleComponent first = new SimpleComponent();
        first.setValue("test1");
        root.setFirstComponent(first);
        SimpleComponent second = new SimpleComponent();
        second.setValue("test2");
        root.setSecondComponent(second);
        session.save("sr-infer", root);

        root = (SimpleRoot) session.load("sr-infer", id);
        assertEquals("test", root.getValue());
        assertNotNull(root.getFirstComponent());
        assertEquals("test1", root.getFirstComponent().getValue());
        assertNotNull(root.getSecondComponent());
        assertEquals("test2", root.getSecondComponent().getValue());

        tr.commit();
    }

}
