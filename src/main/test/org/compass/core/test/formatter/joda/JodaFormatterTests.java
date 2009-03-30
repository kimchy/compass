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

package org.compass.core.test.formatter.joda;

import org.compass.core.CompassSession;
import org.compass.core.test.AbstractTestCase;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * @author kimchy
 */
public class JodaFormatterTests extends AbstractTestCase {

    @Override
    protected String[] getMappings() {
        return new String[]{"formatter/joda/mapping.cpm.xml"};
    }

    public void testJodaFormatter() {
        DateTimeFormatter yyyyMMdd = DateTimeFormat.forPattern("yyyy-MM-dd");
        DateTimeFormatter ddMMyyyy = DateTimeFormat.forPattern("dd-MM-yyyy");
        CompassSession session = openSession();

        A a = new A();
        a.id = 1;
        a.isoDate = new DateTime();
        a.formatDate = new DateTime();
        a.multiDate = new DateTime();
        session.save(a);

        A loadedA = session.load(A.class, 1);
        assertEquals(a.isoDate, loadedA.isoDate);
        assertEquals(yyyyMMdd.parseDateTime(yyyyMMdd.print(a.formatDate)), loadedA.formatDate);
        assertEquals(yyyyMMdd.parseDateTime(yyyyMMdd.print(a.multiDate)), loadedA.multiDate);

        assertEquals(1, session.find("formatDate:" + yyyyMMdd.print(a.formatDate)).length());
        assertEquals(1, session.find("multiDate:" + yyyyMMdd.print(a.formatDate)).length());
        assertEquals(1, session.find("multiDate:" + ddMMyyyy.print(a.formatDate)).length());

        session.commit();
    }
}
