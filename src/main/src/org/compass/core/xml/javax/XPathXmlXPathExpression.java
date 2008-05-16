package org.compass.core.xml.javax;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;

import org.compass.core.xml.XmlObject;
import org.compass.core.xml.XmlXPathExpression;
import org.w3c.dom.NodeList;

/**
 * A java 5 implementation of {@link XmlXPathExpression} wrapping a {@link XPathExpression}.
 *
 * @author kimchy
 */
public class XPathXmlXPathExpression implements XmlXPathExpression {

    private XPathExpression xPathExpression;

    public XPathXmlXPathExpression(XPathExpression xPathExpression) {
        this.xPathExpression = xPathExpression;
    }

    public XmlObject[] select(XmlObject xmlObject) throws Exception {
        NodeXmlObject nodelXmlObject = (NodeXmlObject) xmlObject;
        NodeList nodeList = (NodeList) xPathExpression.evaluate(nodelXmlObject.getNode(), XPathConstants.NODESET);
        if (nodeList == null) {
            return null;
        }
        XmlObject[] xmlObjects = new XmlObject[nodeList.getLength()];
        for (int i = 0; i < xmlObjects.length; i++) {
            xmlObjects[i] = new NodeXmlObject(nodeList.item(i), nodelXmlObject.getNamespaces());
        }
        return xmlObjects;
    }
}
