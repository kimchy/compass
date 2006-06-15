/*
 * Copyright 2004-2006 the original author or authors.
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

import java.io.StringWriter;
import java.io.IOException;

import org.compass.core.converter.ConversionException;
import org.compass.core.converter.mapping.xsem.AbstractXmlContentMappingConverter;
import org.compass.core.mapping.xsem.XmlContentMapping;
import org.compass.core.xml.XmlObject;
import org.compass.core.xml.dom4j.Dom4jXmlObject;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

/**
 * @author kimchy
 */
public abstract class AbstractDom4jXmlContentMappingConverter extends AbstractXmlContentMappingConverter {

    protected String toString(XmlObject xmlObject, XmlContentMapping xmlContentMapping) throws ConversionException {
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
