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

package org.compass.core.test.converter;

import org.compass.core.config.CompassConfiguration;
import org.compass.core.mapping.osem.ManagedId;
import static org.compass.core.mapping.osem.builder.OSEM.*;

/**
 * Same tests as {@link org.compass.core.test.converter.ConverterTests} just using OSEM builder
 * to build the mappings and inject actual instances of the converter.
 */
public class ConverterBuilderTests extends ConverterTests {

    @Override
    protected String[] getMappings() {
        return new String[0];
    }

    @Override
    protected void addExtraConf(CompassConfiguration conf) {
        super.addExtraConf(conf);
        conf.addMapping(
                searchable(A.class).alias("a")
                        .add(id("id"))
                        .add(property("value").managedId(ManagedId.FALSE).add(metadata("mvalue").converter(new SampleConverter("XXX1"))))
        );
        conf.addMapping(
                searchable(A.class).alias("a1")
                        .add(id("id"))
                        .add(property("value").managedId(ManagedId.FALSE).add(metadata("mvalue").converter(new SampleConverter("YYY"))))
        );
        conf.addMapping(
                searchable(A.class).alias("a3")
                        .add(id("id"))
                        .add(property("intVal").add(metadata("intVal")))
        );
    }
}
