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

package org.compass.annotations.test.component.deeplevel1;

import java.util.Arrays;

import org.compass.annotations.test.AbstractAnnotationsTestCase;
import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.config.CompassConfiguration;

/**
 * @author kimchy
 */
public class DataMovesAroundTest extends AbstractAnnotationsTestCase {


    protected void addExtraConf(CompassConfiguration conf) {
        conf.addClass(A.class).addClass(B.class).addClass(C.class).addClass(D.class);
    }

    public void testWeirdness() throws Exception {
        A original = new A();
        original.setId(1);

        B b1 = createB(2, 5);
        B b2 = createB(3, null);
        original.setBs(Arrays.asList(b2, b1));

        // save
        CompassSession session = openSession();
        CompassTransaction transaction = session.beginTransaction();
        session.save(original);
        transaction.commit();
        session.close();

        // fetch
        session = openSession();
        transaction = session.beginTransaction();
        A retrieved = (A) session.load(A.class, original.getId());
        transaction.commit();
        session.close();

        // validate
        assertEquals(2, retrieved.getBs().size());
        b2 = retrieved.getBs().get(0);
        assertEquals(3, b2.getD().getId().intValue());
        assertEquals("3", b2.getD().getValue());
        assertNull(b2.getC());

        b1 = retrieved.getBs().get(1);
        assertEquals(2, b1.getD().getId().intValue());
        assertEquals("2", b1.getD().getValue());
        assertEquals(5, b1.getC().getId().intValue());
        assertEquals("5", b1.getC().getValue());
    }

    private B createB(Integer dId, Integer cId) {
        D d = new D();
        d.setId(dId);
        d.setValue(dId.toString());

        C c = null;
        if (cId != null) {
            c = new C();
            c.setId(cId);
            c.setValue(cId.toString());
        }

        return new B(c, d);
    }
}
