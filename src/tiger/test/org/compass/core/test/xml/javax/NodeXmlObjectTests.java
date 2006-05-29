package org.compass.core.test.xml.javax;

import java.io.Reader;

import javax.xml.parsers.DocumentBuilderFactory;

import org.compass.core.test.xml.AbstractXmlObjectTests;
import org.compass.core.xml.AliasedXmlObject;
import org.compass.core.xml.javax.NodeAliasedXmlObject;
import org.xml.sax.InputSource;
import org.w3c.dom.Document;

/**
 * @author kimchy
 */
public class NodeXmlObjectTests extends AbstractXmlObjectTests {

    protected AliasedXmlObject buildAliasedXmlObject(String alias, Reader data) throws Exception {
        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(data));
        return new NodeAliasedXmlObject(alias, document);
    }
}
