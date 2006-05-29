package org.compass.core.xml.javax;

import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.compass.core.xml.XmlObject;
import org.compass.core.xml.XmlXPathExpression;
import org.compass.core.xml.AliasedXmlObject;
import org.w3c.dom.Node;

/**
 * @author kimchy
 */
public class NodeAliasedXmlObject extends NodeXmlObject implements AliasedXmlObject {

    private String alias;

    public NodeAliasedXmlObject(String alias, Node node) {
        super(node);
        this.alias = alias;
    }

    public String getAlias() {
        return this.alias;
    }
}
