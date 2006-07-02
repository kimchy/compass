package org.compass.core.load.xsem;

import java.io.InputStreamReader;
import java.io.Reader;

import org.compass.core.converter.xsem.XmlContentConverter;
import org.compass.core.converter.mapping.xsem.XmlContentMappingConverter;
import org.compass.core.xml.XmlObject;
import org.compass.core.xml.XmlXPathExpression;
import org.compass.core.xml.dom4j.converter.SAXReaderXmlContentConverter;
import org.compass.core.xml.dom4j.converter.XPP3ReaderXmlContentConverter;
import org.compass.core.xml.dom4j.converter.XPPReaderXmlContentConverter;
import org.compass.core.xml.javax.converter.NodeXmlContentConverter;
import org.compass.core.config.CompassSettings;
import org.compass.core.config.CompassEnvironment;

/**
 * @author kimchy
 */
public class XmlContentConverterLoadTester {

    public static void main(String[] args) throws Exception {

        XmlContentConverter[] converters = new XmlContentConverter[]{new SAXReaderXmlContentConverter(),
                new XPPReaderXmlContentConverter(), new XPP3ReaderXmlContentConverter(),
                new NodeXmlContentConverter()};

        for (int i = 0; i < converters.length; i++) {
            System.gc();
            CompassSettings settings = new CompassSettings();
            settings.setSetting(CompassEnvironment.Converter.XmlContent.TYPE, converters[i].getClass().getName());
            settings.setSetting(CompassEnvironment.Converter.XmlContent.WRAPPER, CompassEnvironment.Converter.XmlContent.WRAPPER_PROTOTYPE);
            XmlContentMappingConverter xmlContentMappingConverter = new XmlContentMappingConverter();
            xmlContentMappingConverter.configure(settings);
            testConverter(xmlContentMappingConverter.getXmlContentConverter(), 500);
        }
    }

    private static void testConverter(XmlContentConverter converter, int iterNum) throws Exception {
        long now = System.nanoTime();
        for (int i = 0; i < iterNum; i++) {
            converter.fromXml("a", readData("data2"));
        }
        long total = System.nanoTime() - now;
        System.out.println("[" + converter + "] Average fromXml nano time: " + (total / iterNum));

        XmlObject xmlObject = converter.fromXml("a", readData("data2"));

        now = System.nanoTime();
        for (int i = 0; i < iterNum; i++) {
            converter.toXml(xmlObject);
        }
        total = System.nanoTime() - now;
        System.out.println("[" + converter + "] Average toXml nano time: " + (total / iterNum));

        XmlXPathExpression expr = xmlObject.compile("/xml-fragment/data/id");
        now = System.nanoTime();
        for (int i = 0; i < iterNum; i++) {
            expr.select(xmlObject);
        }
        total = System.nanoTime() - now;
        System.out.println("[" + converter + "] Average xpath nano time: " + (total / iterNum));
    }

    private static Reader readData(String path) {
        path = "org/compass/core/test/xml/" + path + ".xml";
        return new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream(path));
    }

}
