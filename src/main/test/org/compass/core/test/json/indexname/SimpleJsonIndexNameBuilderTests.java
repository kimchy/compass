package org.compass.core.test.json.indexname;

import org.compass.core.config.CompassConfiguration;
import static org.compass.core.mapping.json.builder.JSEM.*;

/**
 * @author kimchy
 */
public class SimpleJsonIndexNameBuilderTests extends SimpleJsonIndexNameTests {

    protected String[] getMappings() {
        return new String[0];
    }

    @Override
    protected void addExtraConf(CompassConfiguration conf) {
        conf.addResourceMapping(
                json("a")
                        .add(id("id"))
                        .add(property("value").indexName("ivalue"))
                        .add(array("arr").indexName("iarr").set(property().valueConverter("int")))
        );
    }
}