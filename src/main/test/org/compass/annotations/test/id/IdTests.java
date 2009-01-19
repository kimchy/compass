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

package org.compass.annotations.test.id;

import org.compass.annotations.test.AbstractAnnotationsTestCase;
import org.compass.annotations.test.Converted;
import org.compass.core.CompassHits;
import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.config.CompassConfiguration;
import org.compass.core.mapping.CompassMapping;
import org.compass.core.mapping.ResourcePropertyMapping;
import org.compass.core.mapping.osem.ClassIdPropertyMapping;
import org.compass.core.mapping.osem.ClassMapping;
import org.compass.core.spi.InternalCompassSession;

/**
 * @author kimchy
 */
public class IdTests extends AbstractAnnotationsTestCase {

    protected void addExtraConf(CompassConfiguration conf) {
        conf.addClass(A.class).addClass(B.class);
    }

    public void testIdsPaths() throws Exception {

        InternalCompassSession session = (InternalCompassSession) openSession();

        CompassMapping mapping = session.getMapping();
        ClassMapping AMApping = (ClassMapping) mapping.getRootMappingByAlias("A");
        ClassIdPropertyMapping[] idMappings = AMApping.getClassIdPropertyMappings();
        assertEquals(1, idMappings.length);
        ResourcePropertyMapping[] resourcePropertyMappings = idMappings[0].getResourceIdMappings();
        assertEquals(1, resourcePropertyMappings.length);
        assertEquals("test", resourcePropertyMappings[0].getPath().getPath());
        assertEquals("test", resourcePropertyMappings[0].getName());

        CompassTransaction tr = session.beginTransaction();

        A a = new A();
        a.setId(1);
        a.setValue("value");
        session.save(a);

        a = session.load(A.class, 1);
        assertEquals("value", a.getValue());

        CompassHits hits = session.find("value");
        assertEquals(1, hits.length());

        tr.commit();
        session.close();
    }

    public void testConvertedId() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        B b = new B();
        b.id = new Converted("value1", "value2");

        session.save(b);

        session.load(B.class, b.id);

        tr.commit();
        session.close();
    }

}
