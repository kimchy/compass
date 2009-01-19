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

package org.compass.annotations.test.boost.property;

import org.compass.annotations.test.AbstractAnnotationsTestCase;
import org.compass.core.CompassHits;
import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.Resource;
import org.compass.core.config.CompassConfiguration;

/**
 * @author kimchy
 */
public class BoostPropertyTests extends AbstractAnnotationsTestCase {

    protected void addExtraConf(CompassConfiguration conf) {
        conf.addClass(A.class);
    }

    public void testNoBoostProperty() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        A a1  = new A(1, 1, "test");
        session.save(a1);

        A a2 = new A(2, 1, "test");
        session.save(a2);

        CompassHits hits= session.find("test");
        assertEquals(2, hits.length());
        assertEquals(1, ((A) hits.data(0)).id);
        assertEquals(2, ((A) hits.data(1)).id);

        Resource a1resource = session.loadResource(A.class, 1);
        assertEquals(1.0f, a1resource.getBoost(), 0.0001);
        Resource a2resource = session.loadResource(A.class, 2);
        assertEquals(1.0f, a2resource.getBoost(), 0.0001);

        tr.commit();
        session.close();
    }

    public void testBoostProperty() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        A a1  = new A(1, 1, "test");
        session.save(a1);

        A a2 = new A(2, 2, "test");
        session.save(a2);

        CompassHits hits= session.find("test");
        assertEquals(2, hits.length());
        assertEquals(2, ((A) hits.data(0)).id);
        assertEquals(1, ((A) hits.data(1)).id);

        // NOTE, we test it against one since the boost value is not stored in the index
        // so, even though we see that it has taken affect, you still have 1
        Resource a1resource = session.loadResource(A.class, 1);
        assertEquals(1.0f, a1resource.getBoost(), 0.0001);
        Resource a2resource = session.loadResource(A.class, 2);
        assertEquals(1.0f, a2resource.getBoost(), 0.0001);
        
        tr.commit();
        session.close();
    }
}
