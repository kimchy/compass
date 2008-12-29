package org.compass.core.test.json.array.dynamic;

import org.compass.core.config.CompassConfiguration;
import static org.compass.core.mapping.json.builder.JSEM.*;

/**
 * @author kimchy
 */
public class SimpleDynamicJsonArrayBuilderTests extends SimpleDynamicJsonArrayTests {

    @Override
    protected String[] getMappings() {
        return new String[0];
    }

    @Override
    protected void addExtraConf(CompassConfiguration conf) {
        conf.addMapping(json("a")
                .add(id("id"))
                .add(property("value"))
                .add(array("arr").dynamic(true))
        );
        conf.addMapping(json("b").dynamic(true)
                .add(id("id"))
        );
    }
}
