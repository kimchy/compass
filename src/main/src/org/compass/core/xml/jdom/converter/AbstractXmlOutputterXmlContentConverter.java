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

import java.io.IOException;

import org.compass.core.CompassException;
import org.compass.core.config.CompassConfigurable;
import org.compass.core.config.CompassEnvironment;
import org.compass.core.config.CompassSettings;
import org.compass.core.converter.ConversionException;
import org.compass.core.converter.xsem.XmlContentConverter;
import org.compass.core.util.StringBuilderWriter;
import org.compass.core.xml.XmlObject;
import org.compass.core.xml.jdom.JDomXmlObject;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

/**
 * A base class for JDOM xml content converters. Already implements
 * {@link #toXml(org.compass.core.xml.XmlObject)} using JDOM <code>XmlOutput</code>.
 *
 * @author kimchy
 */
public abstract class AbstractXmlOutputterXmlContentConverter implements XmlContentConverter, CompassConfigurable {

    private boolean compact;

    public void configure(CompassSettings settings) throws CompassException {
        String outputFormat = settings.getGloablSettings().getSetting(CompassEnvironment.Xsem.XmlContent.JDom.OUTPUT_FORMAT, "default");
        if ("default".equals(outputFormat)) {
            compact = false;
        } else if ("compact".equals(outputFormat)) {
            compact = true;
        }
    }

    /**
     * Converts the {@link org.compass.core.xml.XmlObject} (assumes it is a {@link org.compass.core.xml.jdom.JDomXmlObject}) into
     * an xml string. Uses JDOM <code>XmlOutputter</code> and <code>Format</code>
     * (in a compact mode) to perform it.
     *
     * @param xmlObject The xml object to convert into an xml string (must be a {@link org.compass.core.xml.jdom.JDomXmlObject} implementation).
     * @return An xml string representation of the xml object
     * @throws org.compass.core.converter.ConversionException
     *          Should not really happne...
     */
    public String toXml(XmlObject xmlObject) throws ConversionException {
        JDomXmlObject jDomXmlObject = (JDomXmlObject) xmlObject;
        XMLOutputter outp = new XMLOutputter();
        if (compact) {
            outp.setFormat(Format.getCompactFormat());
        }
        StringBuilderWriter stringWriter = StringBuilderWriter.Cached.cached();
        try {
            outp.output(jDomXmlObject.getElement(), stringWriter);
        } catch (IOException e) {
            throw new ConversionException("This should not happen", e);
        }
        return stringWriter.toString();
    }
}