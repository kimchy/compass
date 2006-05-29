package org.compass.core.xml.dom4j;

import org.compass.core.xml.AliasedXmlObject;
import org.dom4j.Node;

/**
 * @author kimchy
 */
public class Dom4jAliasedXmlObject extends Dom4jXmlObject implements AliasedXmlObject {

    private String alias;

    public Dom4jAliasedXmlObject(String alias, Node node) {
        super(node);
        this.alias = alias;
    }

    public String getAlias() {
        return this.alias;
    }
}
