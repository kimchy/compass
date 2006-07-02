package org.compass.core.test.xml.javax;

import java.io.Reader;
import javax.xml.parsers.DocumentBuilderFactory;

import org.compass.core.config.CompassEnvironment;
import org.compass.core.config.CompassSettings;
import org.compass.core.converter.mapping.xsem.XmlContentMappingConverter;
import org.compass.core.test.xml.AbstractXmlObjectTests;
import org.compass.core.xml.AliasedXmlObject;
import org.compass.core.xml.javax.NodeAliasedXmlObject;
import org.compass.core.xml.javax.converter.NodeXmlContentConverter;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

/**
 * @author kimchy
 */
public class NodeXmlObjectTests extends AbstractXmlObjectTests {

    protected void addSettings(CompassSettings settings) {
        settings.setGroupSettings(CompassEnvironment.Converter.PREFIX, CompassEnvironment.Converter.DefaultTypeNames.Mapping.XML_CONTENT_MAPPING,
                new String[]{CompassEnvironment.Converter.TYPE, CompassEnvironment.Converter.XmlContent.TYPE},
                new String[]{XmlContentMappingConverter.class.getName(), NodeXmlContentConverter.class.getName()});
    }

    protected AliasedXmlObject buildAliasedXmlObject(String alias, Reader data) throws Exception {
        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(data));
        return new NodeAliasedXmlObject(alias, document);
    }

    public void testData4XmlContent() throws Exception {
        innerTestData4XmlContent();
    }

}
