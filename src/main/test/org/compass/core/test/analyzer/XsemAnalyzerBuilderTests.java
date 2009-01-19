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
import static org.compass.core.mapping.xsem.builder.XSEM.*;

/**
 * @author kimchy
 */
public class XsemAnalyzerBuilderTests extends XsemAnalyzerTests {

    @Override
    protected String[] getMappings() {
        return new String[0];
    }

    @Override
    protected void addExtraConf(CompassConfiguration conf) {
        super.addExtraConf(conf);
        conf.addMapping(
                xml("a").xpath("/data")
                    .add(id("id").indexName("id"))
                    .add(property("value").indexName("value"))
        );
        conf.addMapping(
                xml("b").xpath("/data").analyzer("simple")
                    .add(id("id").indexName("id"))
                    .add(property("value").indexName("value"))
        );
        conf.addMapping(
                xml("c").xpath("/data")
                    .add(id("id").indexName("id").analyzer("simple"))
                    .add(property("value").indexName("value"))
        );
        conf.addMapping(
                xml("d").xpath("/data")
                    .add(id("id").indexName("id"))
                    .add(property("value").indexName("value").analyzer("simple"))
                    .add(property("value2").indexName("value2"))
        );
        conf.addMapping(
                xml("e").xpath("/data").analyzer("simple")
                    .add(id("id").indexName("id"))
                    .add(property("value").indexName("value").analyzer("default"))
                    .add(property("value2").indexName("value2"))
        );
        conf.addMapping(
                xml("f").xpath("/data")
                    .add(id("id").indexName("id"))
                    .add(property("value").indexName("value"))
        );
        conf.addMapping(
                xml("g").xpath("/data")
                    .add(id("id").indexName("id"))
                    .add(analyzer("analyzer", "analyzer"))
                    .add(property("value").indexName("value"))
        );
        conf.addMapping(
                xml("h").xpath("/data")
                    .add(id("id").indexName("id"))
                    .add(analyzer("analyzer", "analyzer").nullAnalyzer("simple"))
                    .add(property("value").indexName("value"))
        );
        conf.addMapping(
                xml("i").xpath("/data")
                    .add(id("id").indexName("id"))
                    .add(analyzer("analyzer", "analyzer").nullAnalyzer("simple"))
                    .add(property("value").indexName("value").analyzer("default"))
        );
    }
}
