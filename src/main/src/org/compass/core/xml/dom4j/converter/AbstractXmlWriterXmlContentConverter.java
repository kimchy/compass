package org.compass.core.xml.dom4j.converter;

import java.io.IOException;
import java.io.StringWriter;

import org.compass.core.converter.ConversionException;
import org.compass.core.converter.xsem.XmlContentConverter;
import org.compass.core.xml.XmlObject;
import org.compass.core.xml.dom4j.Dom4jXmlObject;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

/**
 * A base class for dom4j xml content converters. Already implements
 * {@link #toXml(org.compass.core.xml.XmlObject)} using dom4j <code>XmlWriter</code>.
 *
 * @author kimchy
 */
public abstract class AbstractXmlWriterXmlContentConverter implements XmlContentConverter {

    /**
     * Converts the {@link XmlObject} (assumes it is a {@link org.compass.core.xml.dom4j.Dom4jXmlObject}) into
     * an xml string. Uses dom4j <code>XmlWriter</code> and <code>OutputFormat</code>
     * (in a compact mode) to perform it.
     *
     * @param xmlObject The xml object to convert into an xml string (must be a {@link org.compass.core.xml.dom4j.Dom4jXmlObject} implementation).
     * @return An xml string representation of the xml object
     * @throws ConversionException Should not really happne...
     */
    public String toXml(XmlObject xmlObject) throws ConversionException {
        Dom4jXmlObject dom4jXmlObject = (Dom4jXmlObject) xmlObject;
        StringWriter stringWriter = new StringWriter();
        OutputFormat outputFormat = OutputFormat.createCompactFormat();
        XMLWriter xmlWriter = new XMLWriter(stringWriter, outputFormat);
        try {
            xmlWriter.write(dom4jXmlObject.getNode());
        } catch (IOException e) {
            throw new ConversionException("This should not happen", e);
        }
        return stringWriter.toString();
    }
}
