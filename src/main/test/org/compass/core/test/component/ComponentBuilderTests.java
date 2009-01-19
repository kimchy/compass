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

package org.compass.core.test.component;

import org.compass.core.Property;
import org.compass.core.config.CompassConfiguration;
import static org.compass.core.mapping.osem.builder.OSEM.*;

/**
 * @author kimchy
 */
public class ComponentBuilderTests extends ComponentTests {

    @Override
    protected String[] getMappings() {
        return new String[0];
    }

    @Override
    protected void addExtraConf(CompassConfiguration conf) {
        super.addExtraConf(conf);
        conf.addMapping(
                searchable(SimpleRoot.class).alias("sr")
                        .add(id("id"))
                        .add(property("value").add(metadata("value")))
                        .add(component("firstComponent").refAlias("sc"))
                        .add(component("secondComponent").refAlias("sc"))
        );


        conf.addMapping(
                searchable(SimpleComponent.class).alias("sc").root(false)
                        .add(property("value").add(metadata("value")))
        );
        conf.addMapping(
                searchable(CFirst.class).alias("first")
                        .add(id("id"))
                        .add(property("value").add(metadata("value")))
                        .add(component("second").refAlias("second"))
        );
        conf.addMapping(
                searchable(CSecond.class).alias("second").root(false)
                        .add(property("value").add(metadata("value")))
                        .add(component("third").refAlias("third"))
        );
        conf.addMapping(
                searchable(CThird.class).alias("third").root(false)
                        .add(property("value").add(metadata("value")))
                        .add(component("fourth").refAlias("fourth"))
        );
        conf.addMapping(
                searchable(CFourth.class).alias("fourth").root(false)
                        .add(property("value").add(metadata("value")))
        );

        conf.addMapping(
                searchable(SimpleRootId.class).alias("id-sr")
                    .add(id("id"))
                    .add(property("value").add(metadata("mvalue")))
                    .add(component("firstComponent").refAlias("id-sc"))
                    .add(component("secondComponent").refAlias("id-sc"))
        );
        conf.addMapping(
                searchable(SimpleComponentId.class).alias("id-sc")
                    .add(id("id").add(metadata("id-sc").store(Property.Store.YES).index(Property.Index.NOT_ANALYZED)))
                    .add(property("value").add(metadata("mvalue")))
        );
    }
}
