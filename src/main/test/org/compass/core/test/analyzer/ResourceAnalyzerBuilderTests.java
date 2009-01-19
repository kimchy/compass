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
import static org.compass.core.mapping.rsem.builder.RSEM.*;

/**
 * @author kimchy
 */
public class ResourceAnalyzerBuilderTests extends ResourceAnalyzerTests {

    @Override
    protected String[] getMappings() {
        return new String[0];
    }

    @Override
    protected void addExtraConf(CompassConfiguration conf) {
        super.addExtraConf(conf);
        conf.addMapping(
                resource("a")
                        .add(id("id"))
                        .add(property("value"))
        );
        conf.addMapping(
                resource("b").analyzer("simple")
                        .add(id("id"))
                        .add(property("value"))
        );
        conf.addMapping(
                resource("c")
                        .add(id("id").analyzer("simple"))
                        .add(property("value"))
        );
        conf.addMapping(
                resource("d")
                        .add(id("id"))
                        .add(property("value").analyzer("simple"))
                        .add(property("value2"))
        );
        conf.addMapping(
                resource("e").analyzer("simple")
                        .add(id("id"))
                        .add(property("value").analyzer("default"))
                        .add(property("value2"))
        );
        conf.addMapping(
                resource("f")
                        .add(id("id"))
                        .add(property("value"))
        );
        conf.addMapping(
                resource("g")
                        .add(id("id"))
                        .add(analyzer("analyzer"))
                        .add(property("value"))
        );
        conf.addMapping(
                resource("h")
                        .add(id("id"))
                        .add(analyzer("analyzer").nullAnalyzer("simple"))
                        .add(property("value"))
        );
        conf.addMapping(
                resource("i")
                        .add(id("id"))
                        .add(analyzer("analyzer").nullAnalyzer("simple"))
                        .add(property("value").analyzer("default"))
        );
    }
}
