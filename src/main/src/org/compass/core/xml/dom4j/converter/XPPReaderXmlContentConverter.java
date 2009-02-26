/*
 * Copyright 2004-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.compass.core.xml.dom4j.converter;

import java.io.Reader;

import org.compass.core.CompassException;
import org.compass.core.config.CompassConfigurable;
import org.compass.core.config.CompassEnvironment;
import org.compass.core.config.CompassSettings;
import org.compass.core.converter.ConversionException;
import org.compass.core.converter.xsem.SupportsXmlContentWrapper;
import org.compass.core.xml.AliasedXmlObject;
import org.compass.core.xml.dom4j.Dom4jAliasedXmlObject;
import org.dom4j.Document;
import org.dom4j.io.XPPReader;

/**
 * A dom4j xml content mapping converter, which uses dom4j <code>XPPReader</code> to
 * convert the xml string into a {@link org.compass.core.xml.dom4j.Dom4jAliasedXmlObject}.
 *
 * @author kimchy
 */
public class XPPReaderXmlContentConverter extends AbstractXmlWriterXmlContentConverter
        implements SupportsXmlContentWrapper, CompassConfigurable {

    private XPPReader xppReader;

    public void configure(CompassSettings settings) throws CompassException {
        super.configure(settings);
        xppReader = doCreateXPPReader(settings);
    }

    protected XPPReader doCreateXPPReader(CompassSettings settings) {
        return new XPPReader();
    }

    /**
     * This converter does not support a singleton wrapper strategy.
     */
    public boolean supports(String wrapper) {
        return !CompassEnvironment.Xsem.XmlContent.WRAPPER_SINGLETON.equals(wrapper);
    }

    /**
     * Uses dom4j <code>XPPReader</code> to convert the given xml string into a {@link org.compass.core.xml.dom4j.Dom4jAliasedXmlObject}.
     *
     * @param alias The alias that will be associated with the {@link org.compass.core.xml.AliasedXmlObject}
     * @param xml   The xml string to convert into an {@link org.compass.core.xml.dom4j.Dom4jAliasedXmlObject}
     * @return A {@link org.compass.core.xml.dom4j.Dom4jAliasedXmlObject} parsed from the given xml string and associated with the given alias
     * @throws ConversionException In case the xml parsing failed
     */
    public AliasedXmlObject fromXml(String alias, Reader xml) throws ConversionException {
        Document doc;
        try {
            doc = xppReader.read(xml);
        } catch (Exception e) {
            throw new ConversionException("Failed to parse alias[" + alias + "] xml[" + xml + "]", e);
        }
        return new Dom4jAliasedXmlObject(alias, doc.getRootElement());
    }
}
