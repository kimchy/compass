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

package org.compass.annotations.test.converter.enum1;

import org.compass.annotations.test.AbstractAnnotationsTestCase;
import org.compass.core.CompassHits;
import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.config.CompassConfiguration;

/**
 * @author kimchy
 */
public class EnumTests extends AbstractAnnotationsTestCase {

    protected void addExtraConf(CompassConfiguration conf) {
        conf.addClass(A.class);
    }

    public void testEnumConverter() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        A a = new A();
        a.id = 1;
        a.value1 = AType.TEST1;
        a.value2 = AType.TEST2;
        a.value3 = B.Type.Call;
        session.save(a);

        a = (A) session.load(A.class, 1);
        assertEquals(AType.TEST1, a.value1);
        assertEquals(AType.TEST2, a.value2);
        assertEquals(B.Type.Call, a.value3);

        CompassHits hits = session.find("value1:test1");
        assertEquals(1, hits.length());

        hits = session.find("value3:call");
        assertEquals(1, hits.length());

        tr.commit();
        session.close();
    }

}
