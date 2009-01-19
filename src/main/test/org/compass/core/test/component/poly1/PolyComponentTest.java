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

package org.compass.core.test.component.poly1;

import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.test.AbstractTestCase;


/**
 */
public class PolyComponentTest extends AbstractTestCase {

    protected String[] getMappings() {
        return new String[]{"component/poly1/WithPolyComponent.cpm.xml", "component/poly1/WithoutPolyComponent.cpm.xml"};
    }

    public void testNotNullComponentHavingInheritanceMappingCanBeIndexed() {

        Root root = new Poly("component text");
        WithPolyComponent toIndex = new WithPolyComponent();
        toIndex.setProperty("root text");
        toIndex.setId(new Integer(1));
        toIndex.setRoot(root);

        // save
        CompassSession session = openSession();
        CompassTransaction transaction = session.beginTransaction();
        session.save(toIndex);
        transaction.commit();
        session.close();

        // fetch
        session = openSession();
        transaction = session.beginTransaction();

        WithPolyComponent retrieved = (WithPolyComponent) session.load(WithPolyComponent.class, toIndex.getId());
        transaction.commit();
        session.close();

        assertEquals(retrieved, toIndex);
    }


    public void testNullComponentHavingInheritanceMappingCanBeIndexed() throws Exception {
        WithPolyComponent toIndex = new WithPolyComponent();
        toIndex.setProperty("root text");
        toIndex.setId(new Integer(1));
        toIndex.setRoot(null);

        // save
        CompassSession session = openSession();
        CompassTransaction transaction = session.beginTransaction();
        session.create(toIndex);
        transaction.commit();
        session.close();

        // fetch
        session = openSession();
        transaction = session.beginTransaction();

        WithPolyComponent retrieved = (WithPolyComponent) session.load(WithPolyComponent.class, toIndex.getId());
        transaction.commit();
        session.close();

        assertEquals(retrieved, toIndex);

    }

    public void testNotNullComponentWithoutInheritanceMappingCanBeIndexed() {

        SimpleComponent simpleComponent = new SimpleComponent();
        simpleComponent.setComponentProperty("component text");

        WithoutPolyComponent toIndex = new WithoutPolyComponent();
        toIndex.setId(new Integer(1));
        toIndex.setProperty("text");
        toIndex.setSimpleComponent(simpleComponent);

        // save
        CompassSession session = openSession();
        CompassTransaction transaction = session.beginTransaction();
        session.create(toIndex);
        transaction.commit();
        session.close();

        // fetch
        session = openSession();
        transaction = session.beginTransaction();

        WithoutPolyComponent retrieved = (WithoutPolyComponent) session.load(WithoutPolyComponent.class, toIndex.getId());
        transaction.commit();
        session.close();

        assertEquals(retrieved, toIndex);
    }

    public void testNullComponentWithoutInheritanceMappingCanBeIndexed() {
        WithoutPolyComponent toIndex = new WithoutPolyComponent();
        toIndex.setId(new Integer(1));
        // save
        CompassSession session = openSession();
        CompassTransaction transaction = session.beginTransaction();
        session.create(toIndex);
        transaction.commit();
        session.close();

        // fetch
        session = openSession();
        transaction = session.beginTransaction();

        WithoutPolyComponent retrieved = (WithoutPolyComponent) session.load(WithoutPolyComponent.class, toIndex.getId());
        transaction.commit();
        session.close();

        assertEquals(retrieved, toIndex);

    }

}
