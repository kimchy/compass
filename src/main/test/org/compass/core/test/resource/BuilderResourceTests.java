package org.compass.core.test.resource;

import org.compass.core.Property;
import org.compass.core.config.CompassConfiguration;
import org.compass.core.mapping.ExcludeFromAll;
import static org.compass.core.mapping.rsem.builder.RSEM.*;

/**
 * @author kimchy
 */
public class BuilderResourceTests extends ResourceTests {

    @Override
    protected String[] getMappings() {
        return new String[0];
    }

    @Override
    protected void addExtraConf(CompassConfiguration conf) {
        conf.addResourceMapping(
                resource("a")
                        .add(id("id"))
        );
        conf.addResourceMapping(
                resource("b")
                        .add(id("id1"))
                        .add(id("id2"))
        );
        conf.addResourceMapping(
                resource("c")
                        .add(id("id"))
                        .add(property("value1"))
                        .add(property("value2").store(Property.Store.YES).index(Property.Index.ANALYZED))
                        .add(property("value3").store(Property.Store.COMPRESS).index(Property.Index.ANALYZED))
                        .add(property("value4").store(Property.Store.YES).index(Property.Index.NOT_ANALYZED))
                        .add(property("value5").store(Property.Store.YES).index(Property.Index.ANALYZED).converter("mydate"))
                        .add(property("value6"))
        );
        conf.addResourceMapping(
                resource("d").extendsAliases("a")
                        .add(property("value1"))
        );
        conf.addContractMapping(
                contract("cont1")
                        .add(id("id"))
        );
        conf.addContractMapping(
                contract("cont2")
                        .add(property("value1"))
        );
        conf.addContractMapping(
                contract("cont3").extendsAliases("cont1")
                        .add(property("value2"))
        );
        conf.addResourceMapping(
                resource("e").extendsAliases("cont2", "cont3")
                        .add(property("value1").store(Property.Store.NO))
        );
        conf.addResourceMapping(
                resource("f")
                        .add(id("id1").excludeFromAll(ExcludeFromAll.YES))
                        .add(id("id2").excludeFromAll(ExcludeFromAll.NO))
                        .add(property("value1").excludeFromAll(ExcludeFromAll.YES))
                        .add(property("value2").excludeFromAll(ExcludeFromAll.NO))
        );
        conf.addResourceMapping(
                resource("g")
                        .add(id("id"))
                        .add(property("value").converter("int").format("000000.00"))
        );
    }
}
