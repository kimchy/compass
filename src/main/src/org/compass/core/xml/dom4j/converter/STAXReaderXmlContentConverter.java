package org.compass.core.xml.dom4j.converter;

import java.io.Reader;
import java.io.StringWriter;
import javax.xml.stream.XMLStreamException;

import org.compass.core.CompassException;
import org.compass.core.config.CompassConfigurable;
import org.compass.core.config.CompassEnvironment;
import org.compass.core.config.CompassSettings;
import org.compass.core.converter.ConversionException;
import org.compass.core.converter.xsem.SupportsXmlContentWrapper;
import org.compass.core.xml.AliasedXmlObject;
import org.compass.core.xml.XmlObject;
import org.compass.core.xml.dom4j.Dom4jAliasedXmlObject;
import org.compass.core.xml.dom4j.Dom4jXmlObject;
import org.dom4j.Document;
import org.dom4j.io.STAXEventReader;
import org.dom4j.io.STAXEventWriter;

/**
 * A dom4j xml content mapping converter, which uses dom4j <code>STAXEventReader</code> to
 * convert the xml string into a {@link org.compass.core.xml.dom4j.Dom4jAliasedXmlObject}.
 *
 * @author kimchy
 */
public class STAXReaderXmlContentConverter extends AbstractXmlWriterXmlContentConverter
        implements SupportsXmlContentWrapper, CompassConfigurable {

    private STAXEventReader staxEventReader;

    public void configure(CompassSettings settings) throws CompassException {
        staxEventReader = doCreateStaxReader(settings);
    }

    protected STAXEventReader doCreateStaxReader(CompassSettings settings) {
        return new STAXEventReader();
    }

    /**
     * This converter does not support a singleton wrapper strategy.
     */
    //TODO is this true?
    public boolean supports(String wrapper) {
        return !CompassEnvironment.Converter.XmlContent.WRAPPER_SINGLETON.equals(wrapper);
    }

    /**
     * Uses dom4j <code>XPP3Reader</code> to convert the given xml string into a {@link org.compass.core.xml.dom4j.Dom4jAliasedXmlObject}.
     *
     * @param alias The alias that will be associated with the {@link org.compass.core.xml.AliasedXmlObject}
     * @param xml   The xml string to convert into an {@link org.compass.core.xml.dom4j.Dom4jAliasedXmlObject}
     * @return A {@link org.compass.core.xml.dom4j.Dom4jAliasedXmlObject} parsed from the given xml string and associated with the given alias
     * @throws org.compass.core.converter.ConversionException
     *          In case the xml parsing failed
     */
    public AliasedXmlObject fromXml(String alias, Reader xml) throws ConversionException {
        Document doc;
        try {
            doc = staxEventReader.readDocument(xml);
        } catch (Exception e) {
            throw new ConversionException("Failed to parse alias[" + alias + "] xml[" + xml + "]", e);
        }
        return new Dom4jAliasedXmlObject(alias, doc.getRootElement());
    }

    public String toXml1(XmlObject xmlObject) throws ConversionException {
        //TODO for some reason, this does not marshall at all, and also, will it be faster than dom4j XMLWriter?
        Dom4jXmlObject dom4jXmlObject = (Dom4jXmlObject) xmlObject;
        StringWriter stringWriter = new StringWriter();
        try {
            STAXEventWriter xmlWriter = new STAXEventWriter(stringWriter);
            xmlWriter.writeNode(dom4jXmlObject.getNode());
        } catch (XMLStreamException e) {
            throw new ConversionException("Failed to write xml", e);
        }
        return stringWriter.toString();
    }
}