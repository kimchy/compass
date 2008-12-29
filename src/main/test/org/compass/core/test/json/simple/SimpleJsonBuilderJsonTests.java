package org.compass.core.test.json.simple;

import org.compass.core.Property;
import org.compass.core.config.CompassConfiguration;
import static org.compass.core.mapping.json.builder.JSEM.*;

/**
 * @author kimchy
 */
public class SimpleJsonBuilderJsonTests extends SimpleJsonTests {

    protected String[] getMappings() {
        return new String[0];
    }

    @Override
    protected void addExtraConf(CompassConfiguration conf) {
        conf.addMapping(
                json("a")
                        .add(id("id"))
                        .add(property("value"))
        );
        conf.addMapping(
                json("b")
                        .add(id("id"))
                        .add(property("value"))
                        .add(content("test"))
        );
        conf.addMapping(
                json("c")
                        .add(id("id"))
                        .add(property("int").valueConverter("int").format("0000"))
                        .add(property("float").valueConverter("float").format("0000.00").store(Property.Store.COMPRESS))
        );
        conf.addMapping(
                json("d")
                        .add(id("id"))
                        .add(property("value").nullValue("kablam"))
        );
        conf.addMapping(
                json("e")
                        .add(id("id"))
                        .add(property("value"))
                        .add(property("value").valueConverter("int").format("0000"))
        );
    }
}