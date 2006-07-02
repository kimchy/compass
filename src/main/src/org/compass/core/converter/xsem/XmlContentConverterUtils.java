package org.compass.core.converter.xsem;

import org.compass.core.config.CompassConfigurable;
import org.compass.core.config.CompassEnvironment;
import org.compass.core.config.CompassSettings;
import org.compass.core.config.ConfigurationException;
import org.compass.core.util.ClassUtils;

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
        String type = settings.getSetting(CompassEnvironment.Converter.XmlContent.TYPE);
        if (type == null) {
            throw new ConfigurationException("xmlContent type configuration can not be found, please set it in the configuration settings");
        }
        XmlContentConverter xmlContentConverter = null;
        try {
            xmlContentConverter = (XmlContentConverter) ClassUtils.forName(type).newInstance();
        } catch (Exception e) {
            throw new ConfigurationException("Failed to create xmlContent [" + type + "]", e);
        }
        if (xmlContentConverter instanceof CompassConfigurable) {
            ((CompassConfigurable) xmlContentConverter).configure(settings);
        }
        if (xmlContentConverter instanceof SupportsXmlContentWrapper) {
            String wrapper = settings.getSetting(CompassEnvironment.Converter.XmlContent.WRAPPER, CompassEnvironment.Converter.XmlContent.WRAPPER_PROTOTYPE);
            if (!((SupportsXmlContentWrapper) xmlContentConverter).supports(wrapper)) {
                throw new SupportsXmlContentWrapper.NotSupportedXmlContentWrapperException(xmlContentConverter, wrapper);
            }
        }
        return xmlContentConverter;
    }
}
