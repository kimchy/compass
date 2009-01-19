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

package org.compass.core.test.analyzer;

import org.compass.core.config.CompassConfiguration;
import static org.compass.core.mapping.osem.builder.OSEM.*;

/**
 * @author kimchy
 */
public class OsemAnalyzerBuilderTests extends OsemAnalyzerTests {

    @Override
    protected String[] getMappings() {
        return new String[0];
    }

    @Override
    protected void addExtraConf(CompassConfiguration conf) {
        super.addExtraConf(conf);
        conf.addMapping(
                searchable(A.class).alias("a1")
                    .add(id("id"))
                    .add(property("value").add(metadata("value")))
        );
        conf.addMapping(
                searchable(A.class).alias("a2").analyzer("simple")
                    .add(id("id"))
                    .add(property("value").add(metadata("value")))
        );
        conf.addMapping(
                searchable(A.class).alias("a3")
                    .add(id("id"))
                    .add(property("value").add(metadata("value").analyzer("simple")))
                    .add(property("value2").add(metadata("value2")))
        );
        conf.addMapping(
                searchable(A.class).alias("a4").analyzer("simple")
                    .add(id("id"))
                    .add(property("value").add(metadata("value").analyzer("default")))
                    .add(property("value2").add(metadata("value2")))
        );
        conf.addMapping(
                searchable(A.class).alias("a5")
                    .add(id("id"))
                    .add(property("value").add(metadata("value")))
                    .add(property("value2").add(metadata("value2")))
        );
        conf.addMapping(
                searchable(A.class).alias("a6")
                    .add(id("id"))
                     .add(analyzer("analyzer"))
                    .add(property("value").add(metadata("value")))
                    .add(property("value2").add(metadata("value2")))
        );
        conf.addMapping(
                searchable(A.class).alias("a7")
                    .add(id("id"))
                     .add(analyzer("analyzer").nullAnalyzer("simple"))
                    .add(property("value").add(metadata("value")))
                    .add(property("value2").add(metadata("value2")))
        );
    }
}
