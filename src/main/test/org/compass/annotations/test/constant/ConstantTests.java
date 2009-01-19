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

package org.compass.annotations.test.constant;

import org.compass.annotations.test.AbstractAnnotationsTestCase;
import org.compass.core.CompassHits;
import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.config.CompassConfiguration;

/**
 * @author kimchy
 */
public class ConstantTests extends AbstractAnnotationsTestCase {

    protected void addExtraConf(CompassConfiguration conf) {
        conf.addClass(A.class);
    }

    public void testCollectionWithGenericsParameter() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        A a = new A();
        a.id = 1;

        session.save(a);

        a = (A) session.load(A.class, 1);

        CompassHits hits = session.find("val11");
        assertEquals(1, hits.length());
        a = (A) hits.data(0);
        assertEquals(1, a.id);

        hits = session.find("const2:val21");
        assertEquals(1, hits.length());
        a = (A) hits.data(0);
        assertEquals(1, a.id);

        tr.commit();
        session.close();
    }
}
