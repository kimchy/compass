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

import java.io.StringReader;

import org.compass.core.converter.ConversionException;
import org.compass.core.mapping.xsem.XmlContentMapping;
import org.compass.core.xml.AliasedXmlObject;
import org.compass.core.xml.dom4j.Dom4jAliasedXmlObject;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;

/**
 * @author kimchy
 */
public class SAXReaderXmlContentMappingConverter extends AbstractDom4jXmlContentMappingConverter {

    public AliasedXmlObject fromString(String alias, String xml) throws ConversionException {
        SAXReader saxReader = new SAXReader();
        Document doc;
        try {
            doc = saxReader.read(new StringReader(xml));
        } catch (DocumentException e) {
            throw new ConversionException("Failed to parse alias[" + alias + "] xml[" + xml + "]", e);
        }
        return new Dom4jAliasedXmlObject(alias, doc.getRootElement());
    }
}
