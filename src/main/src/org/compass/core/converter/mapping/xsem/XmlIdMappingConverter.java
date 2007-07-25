package org.compass.core.converter.mapping.xsem;

import org.compass.core.Resource;
import org.compass.core.converter.ConversionException;
import org.compass.core.converter.Converter;
import org.compass.core.mapping.Mapping;
import org.compass.core.mapping.xsem.XmlPropertyMapping;
import org.compass.core.marshall.MarshallingContext;
import org.compass.core.xml.XmlObject;

/**
 * Responsible for converting {@link XmlObject} based on {@link org.compass.core.mapping.xsem.XmlIdMapping}.
 * Uses the {@link org.compass.core.mapping.xsem.XmlIdMapping} value converter to actually convert the given
 * value.
 * <p/>
 * The converter executes the given xpath expression associated with {@link org.compass.core.mapping.xsem.XmlIdMapping}
 * and uses the FIRST one as the xml object to convert using the value converter.
 * <p/>
 * Also note, that un-marshalling is not supported.
 *
 * @author kimchy
 */
public class XmlIdMappingConverter implements Converter {

    public boolean marshall(Resource resource, Object root, Mapping mapping, MarshallingContext context) throws ConversionException {
        if (root == null) {
            return false;
        }

        XmlObject xmlObject = (XmlObject) root;
        XmlPropertyMapping xmlPropertyMapping = (XmlPropertyMapping) mapping;

        XmlObject[] xmlObjects = XmlConverterUtils.select(xmlObject, xmlPropertyMapping);
        if (xmlObjects == null || xmlObjects.length == 0) {
            throw new ConversionException("Id with xpath [" + xmlPropertyMapping.getXPath() + "] returned no values");
        }
        boolean stored = xmlPropertyMapping.getValueConverter().marshall(resource, xmlObjects[0], xmlPropertyMapping, context);
        if (!stored) {
            throw new ConversionException("No id value for xpath expression [" + xmlPropertyMapping.getXPath() + "]");
        }
        return stored;
    }

    public Object unmarshall(Resource resource, Mapping mapping, MarshallingContext context) throws ConversionException {
        throw new ConversionException("Not supported, please use xml-content mapping");
    }

}
