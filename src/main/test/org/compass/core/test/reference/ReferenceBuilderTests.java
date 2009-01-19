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

package org.compass.core.test.reference;

import org.compass.core.config.CompassConfiguration;
import static org.compass.core.mapping.osem.builder.OSEM.*;

/**
 * @author kimchy
 */
public class ReferenceBuilderTests extends ReferenceTests {

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
                        .add(property("value").add(metadata("value")))
                        .add(reference("b"))
        );
        conf.addMapping(
                searchable(B.class).alias("b")
                        .add(id("id"))
                        .add(property("value").add(metadata("value")))
        );
        conf.addMapping(
                searchable(X.class).alias("x")
                        .add(id("id"))
                        .add(property("value").add(metadata("value")))
                        .add(reference("y").refAlias("y"))
        );
        conf.addMapping(
                searchable(Y.class).alias("y")
                        .add(id("id"))
                        .add(property("value").add(metadata("value")))
        );
        conf.addMapping(
                searchable(Cyclic1.class).alias("cyclic1")
                        .add(id("id"))
                        .add(property("value").add(metadata("value")))
                        .add(reference("cyclic2").refAlias("cyclic2"))
        );
        conf.addMapping(
                searchable(Cyclic2.class).alias("cyclic2")
                        .add(id("id"))
                        .add(property("value").add(metadata("value")))
                        .add(reference("cyclic1").refAlias("cyclic1"))
        );
        conf.addMapping(
                searchable(X.class).alias("x1")
                        .add(id("id"))
                        .add(property("value").add(metadata("value")))
                        .add(reference("y").refAlias("y1").refComponentAlias("y2"))
        );
        conf.addMapping(
                searchable(Y.class).alias("y1")
                        .add(id("id"))
                        .add(property("value").add(metadata("value")))
        );
        conf.addMapping(
                searchable(Y.class).alias("y2").root(false)
                        .add(property("value").add(metadata("value1")))
        );
        conf.addMapping(
                searchable(ManyToMany1.class).alias("many1")
                        .add(id("id").accessor("field"))
                        .add(property("value").accessor("field").add(metadata("value")))
                        .add(reference("many2").accessor("field").refAlias("many2"))
        );
        conf.addMapping(
                searchable(ManyToMany2.class).alias("many2")
                        .add(id("id").accessor("field"))
                        .add(property("value").accessor("field").add(metadata("value")))
                        .add(reference("many1").accessor("field").refAlias("many1"))
        );
    }
}
