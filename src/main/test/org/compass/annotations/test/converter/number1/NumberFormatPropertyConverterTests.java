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

package org.compass.annotations.test.converter.number1;

import org.compass.annotations.test.AbstractAnnotationsTestCase;
import org.compass.core.CompassQuery;
import org.compass.core.CompassQueryBuilder;
import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.config.CompassConfiguration;

/**
 * @author kimchy
 */
public class NumberFormatPropertyConverterTests extends AbstractAnnotationsTestCase {

    protected void addExtraConf(CompassConfiguration conf) {
        conf.addClass(A1.class);
    }

    public void testPropertyLongFormat() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        A1 a = new A1();
        a.id = 1;
        a.property = 300l;
        session.save(a);

        CompassQueryBuilder queryBuilder = session.queryBuilder().convertOnlyWithDotPath(true);
        CompassQuery query = queryBuilder.ge("property", 300L);
        assertEquals("property:[300 TO *]", query.toString());

        queryBuilder = session.queryBuilder().convertOnlyWithDotPath(true);
        query = queryBuilder.ge("A1.property", 300L);
        assertEquals("+property:[00000300 TO *] +((alias:A1 extendedAlias:A1)~1)", query.toString());

        queryBuilder = session.queryBuilder().convertOnlyWithDotPath(false);
        query = queryBuilder.ge("property", 300L);
        assertEquals("property:[00000300 TO *]", query.toString());

        queryBuilder = session.queryBuilder();
        query = queryBuilder.ge("A1.property", 300L);
        assertEquals("+property:[00000300 TO *] +((alias:A1 extendedAlias:A1)~1)", query.toString());

        tr.commit();
        session.close();
    }
}