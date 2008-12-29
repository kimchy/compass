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
        conf.addMapping(
                json("a")
                        .add(id("id"))
                        .add(property("value"))
                        .add(array("arr").element(property().valueConverter("int")))
        );
        conf.addMapping(
                json("b")
                        .add(id("id"))
                        .add(property("value"))
                        .add(array("arr").indexName("xarr").element(property().valueConverter("int")))
        );
        conf.addMapping(
                json("c")
                        .add(id("id"))
                        .add(property("value"))
                        .add(array("arr").element(object().add(property("arr-value"))))
        );
    }
}
