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

package org.compass.core.xml.jdom.converter;

import java.io.Reader;
import javax.xml.stream.XMLStreamReader;

import org.compass.core.CompassException;
import org.compass.core.config.CompassConfigurable;
import org.compass.core.config.CompassEnvironment;
import org.compass.core.config.CompassSettings;
import org.compass.core.converter.ConversionException;
import org.compass.core.converter.xsem.SupportsXmlContentWrapper;
import org.compass.core.xml.AliasedXmlObject;
import org.compass.core.xml.jdom.JDomAliasedXmlObject;
import org.compass.core.xml.jdom.converter.support.StAXBuilder;
import org.jdom.Document;

/**
 * A JDOM content mapping converter, which uses JDOM {@link org.compass.core.xml.jdom.converter.support.StAXBuilder} to
 * convert the xml string into a {@link org.compass.core.xml.jdom.JDomAliasedXmlObject}.
 *
 * @author kimchy
 */
public class STAXBuilderXmlContentConverter extends AbstractXmlOutputterXmlContentConverter
        implements SupportsXmlContentWrapper, CompassConfigurable {

    private StAXBuilder stAXBuilder;

    javax.xml.stream.XMLInputFactory xmlInputFactory = javax.xml.stream.XMLInputFactory.newInstance();

    public void configure(CompassSettings settings) throws CompassException {
        super.configure(settings);
        stAXBuilder = doCreateStAXBuilder(settings);
    }

    protected StAXBuilder doCreateStAXBuilder(CompassSettings settings) {
        return new StAXBuilder();
    }

    /**
     * This converter does not support a singleton wrapper strategy.
     */
    public boolean supports(String wrapper) {
        return !CompassEnvironment.Xsem.XmlContent.WRAPPER_SINGLETON.equals(wrapper);
    }

    /**
     * Uses JDOM <code>StAXBuilder</code> to convert the given xml string into a {@link org.compass.core.xml.jdom.JDomAliasedXmlObject}.
     *
     * @param alias The alias that will be associated with the {@link org.compass.core.xml.AliasedXmlObject}
     * @param xml   The xml string to convert into an {@link org.compass.core.xml.jdom.JDomAliasedXmlObject}
     * @return A {@link org.compass.core.xml.jdom.JDomAliasedXmlObject} parsed from the given xml string and associated with the given alias
     * @throws org.compass.core.converter.ConversionException
     *          In case the xml parsing failed
     */
    public AliasedXmlObject fromXml(String alias, Reader xml) throws ConversionException {
        Document doc;
        try {
            XMLStreamReader sr = xmlInputFactory.createXMLStreamReader(xml);
            doc = stAXBuilder.build(sr);
            sr.close();
        } catch (Exception e) {
            throw new ConversionException("Failed to parse alias[" + alias + "] xml[" + xml + "]", e);
        }
        return new JDomAliasedXmlObject(alias, doc.getRootElement());
    }
}