package org.compass.core.converter.xsem;

import org.compass.core.config.CompassConfigurable;
import org.compass.core.config.CompassEnvironment;
import org.compass.core.config.CompassSettings;
import org.compass.core.config.ConfigurationException;
import org.compass.core.util.ClassUtils;
import org.compass.core.xml.dom4j.converter.SAXReaderXmlContentConverter;
import org.compass.core.xml.dom4j.converter.STAXReaderXmlContentConverter;
import org.compass.core.xml.dom4j.converter.XPP3ReaderXmlContentConverter;
import org.compass.core.xml.dom4j.converter.XPPReaderXmlContentConverter;
import org.compass.core.xml.javax.converter.NodeXmlContentConverter;
import org.compass.core.xml.javax.converter.StaxNodeXmlContentConverter;
import org.compass.core.xml.jdom.converter.SAXBuilderXmlContentConverter;
import org.compass.core.xml.jdom.converter.STAXBuilderXmlContentConverter;

/**
 * A set of utilities for {@link XmlContentConverter}.
 *
 * @author kimchy
 */
public abstract class XmlContentConverterUtils {

    /**
     * Creates a new {@link XmlContentConverter} based on the given settings.
     */
    public static XmlContentConverter createXmlContentConverter(CompassSettings settings) throws ConfigurationException {
        String type = settings.getGloablSettings().getSetting(CompassEnvironment.Xsem.XmlContent.TYPE);
        if (type == null) {
            throw new ConfigurationException("[" + CompassEnvironment.Xsem.XmlContent.TYPE + "] configuration can not be found, please set it in the configuration settings");
        }
        XmlContentConverter xmlContentConverter;
        if (CompassEnvironment.Xsem.XmlContent.JDom.TYPE_SAX.equals(type)) {
            xmlContentConverter = new SAXBuilderXmlContentConverter();
        } else if (CompassEnvironment.Xsem.XmlContent.JDom.TYPE_STAX.equals(type)) {
            xmlContentConverter = new STAXBuilderXmlContentConverter();
        } else if (CompassEnvironment.Xsem.XmlContent.Dom4j.TYPE_STAX.equals(type)) {
            xmlContentConverter = new STAXReaderXmlContentConverter();
        } else if (CompassEnvironment.Xsem.XmlContent.Dom4j.TYPE_SAX.equals(type)) {
            xmlContentConverter = new SAXReaderXmlContentConverter();
        } else if (CompassEnvironment.Xsem.XmlContent.Dom4j.TYPE_XPP.equals(type)) {
            xmlContentConverter = new XPPReaderXmlContentConverter();
        } else if (CompassEnvironment.Xsem.XmlContent.Dom4j.TYPE_XPP3.equals(type)) {
            xmlContentConverter = new XPP3ReaderXmlContentConverter();
        } else if (CompassEnvironment.Xsem.XmlContent.Javax.TYPE_NODE.equals(type)) {
            xmlContentConverter = new NodeXmlContentConverter();
        } else if (CompassEnvironment.Xsem.XmlContent.Javax.TYPE_STAX.equals(type)) {
            xmlContentConverter = new StaxNodeXmlContentConverter();
        } else {
            try {
                xmlContentConverter = (XmlContentConverter) ClassUtils.forName(type, settings.getClassLoader()).newInstance();
            } catch (Exception e) {
                throw new ConfigurationException("Failed to create xmlContent [" + type + "]", e);
            }
        }
        if (xmlContentConverter instanceof CompassConfigurable) {
            ((CompassConfigurable) xmlContentConverter).configure(settings);
        }
        if (xmlContentConverter instanceof SupportsXmlContentWrapper) {
            String wrapper = settings.getGloablSettings().getSetting(CompassEnvironment.Xsem.XmlContent.WRAPPER, CompassEnvironment.Xsem.XmlContent.WRAPPER_PROTOTYPE);
            if (!((SupportsXmlContentWrapper) xmlContentConverter).supports(wrapper)) {
                throw new SupportsXmlContentWrapper.NotSupportedXmlContentWrapperException(xmlContentConverter, wrapper);
            }
        }
        return xmlContentConverter;
    }
}
