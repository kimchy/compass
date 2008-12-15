package org.compass.core.test.json.array;

import org.compass.core.config.CompassConfiguration;
import static org.compass.core.mapping.json.builder.JSEM.*;

/**
 * @author kimchy
 */
public class SimpleJsonArrayBuilderTests extends SimpleJsonArrayTests {

    @Override
    protected String[] getMappings() {
        return new String[0];
    }

    @Override
    protected void addExtraConf(CompassConfiguration conf) {
        conf.addResourceMapping(
                json("a")
                        .add(id("id"))
                        .add(property("value"))
                        .add(array("arr").set(property().valueConverter("int")))
        );
        conf.addResourceMapping(
                json("b")
                        .add(id("id"))
                        .add(property("value"))
                        .add(array("arr").indexName("xarr").set(property().valueConverter("int")))
        );
        conf.addResourceMapping(
                json("c")
                        .add(id("id"))
                        .add(property("value"))
                        .add(array("arr").set(object().add(property("arr-value"))))
        );
    }
}
