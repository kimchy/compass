package org.compass.core.test.dynamic.velocity;

import org.compass.core.config.CompassConfiguration;
import static org.compass.core.mapping.osem.builder.OSEM.*;

/**
 * @author kimchy
 */
public class VelocityDynamicBuilderTests extends VelocityDynamicTests {

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
                        .add(dynamicMetadata("test", "velocity", "$data.getValue() $data.getValue2()"))
        );
    }
}
