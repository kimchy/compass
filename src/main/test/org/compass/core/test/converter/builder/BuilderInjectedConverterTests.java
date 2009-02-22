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

package org.compass.core.test.converter.builder;

import org.compass.core.CompassSession;
import org.compass.core.config.CompassConfiguration;
import org.compass.core.converter.ConversionException;
import org.compass.core.converter.Converter;
import org.compass.core.converter.basic.AbstractBasicConverter;
import org.compass.core.mapping.ResourcePropertyMapping;
import static org.compass.core.mapping.osem.builder.OSEM.*;
import org.compass.core.marshall.MarshallingContext;
import org.compass.core.test.AbstractTestCase;

/**
 * Test the injection of an actual instance of a converter when using the mapping builder API.
 *
 * @author kimchy
 */
public class BuilderInjectedConverterTests extends AbstractTestCase {

    @Override
    protected void addExtraConf(CompassConfiguration conf) {
        Converter converter = new AbstractBasicConverter<String>() {
            @Override
            protected String doFromString(String str, ResourcePropertyMapping resourcePropertyMapping, MarshallingContext context) throws ConversionException {
                return str.substring(1);
            }

            @Override
            protected String doToString(String o, ResourcePropertyMapping resourcePropertyMapping, MarshallingContext context) {
                return "x" + o;
            }
        };
        conf.addMapping(searchable(A.class)
                .add(id("id").accessor("field"))
                .add(property("value").accessor("field").add(metadata("value").converter(converter)))
        );
    }

    public void testBuilderInejctedConverter() {
        CompassSession session = openSession();

        A a = new A();
        a.id = 1;
        a.value = "value";
        session.save(a);

        assertEquals(1, session.find("xvalue").length());
        assertEquals(0, session.find("value").length());

        session.close();
    }
}
