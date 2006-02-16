/*
 * Copyright 2004-2006 the original author or authors.
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

package org.compass.core.test.converter;

import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.Resource;
import org.compass.core.config.CompassConfiguration;
import org.compass.core.test.AbstractTestCase;

/**
 * @author kimchy
 */
public class ConverterTests extends AbstractTestCase {

    protected String[] getMappings() {
        return new String[] { "converter/Converter.cpm.xml" };
    }

    protected void addExtraConf(CompassConfiguration conf) {
        SampleConverter sampleConverter = new SampleConverter();
        sampleConverter.setSeperator("YYY");
        conf.registerConverter("yyy", sampleConverter);
    }

    public void testConverter() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        Long id = new Long(1);
        A o = new A();
        o.setId(id);
        A.TwoStringsValue tsv = new A.TwoStringsValue();
        tsv.setValue1("test1");
        tsv.setValue2("test2");
        o.setValue(tsv);

        session.save("a", o);

        o = (A) session.load("a", id);
        tsv = o.getValue();
        assertEquals("test1", tsv.getValue1());
        assertEquals("test2", tsv.getValue2());

        Resource resource = session.loadResource("a", id);
        assertEquals("test1XXXtest2", resource.get("mvalue"));

        tr.commit();
    }

    public void testConverterYYY() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        Long id = new Long(1);
        A o = new A();
        o.setId(id);
        A.TwoStringsValue tsv = new A.TwoStringsValue();
        tsv.setValue1("test1");
        tsv.setValue2("test2");
        o.setValue(tsv);

        session.save("a1", o);

        o = (A) session.load("a1", id);
        tsv = o.getValue();
        assertEquals("test1", tsv.getValue1());
        assertEquals("test2", tsv.getValue2());

        Resource resource = session.loadResource("a1", id);
        assertEquals("test1YYYtest2", resource.get("mvalue"));

        tr.commit();
    }
}
