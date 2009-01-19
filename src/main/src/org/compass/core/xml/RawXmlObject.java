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

package org.compass.core.xml;

import java.io.Reader;
import java.util.Map;

import org.compass.core.CompassException;
import org.compass.core.util.reader.StringReader;

/**
 * <p>An {@link XmlObject} that has an xml string representation. Mainly used for simpliciy,
 * where Compass will use the configured {@link org.compass.core.converter.xsem.XmlContentConverter}
 * in order to convert to xml string into the actual {@link XmlObject} implementation.
 * <p/>
 * <p>This object will only be used when saving xml object into Compass. When Compass returns xml objects
 * as a restult of a query or get/load operations, the actual {@link XmlObject} will be returned.
 * <p/>
 * <p>Naturally, since the xml string will only be parsed when Compass will convert this object, all the
 * {@link XmlObject} methods are not implemented. The {@link XmlObject} is just used as a marker interface
 * to use the correct xsem supported converters.
 *
 * @author kimchy
 */
public class RawXmlObject implements XmlObject {

    private Reader xml;

    /**
     * Creates a new String based xml object using a String holding the actual xml content.
     */
    public RawXmlObject(String xml) {
        this.xml = new StringReader(xml);
    }

    /**
     * Creates a new String based xml object using a Reader holding the actual xml content.
     */
    public RawXmlObject(Reader xml) {
        this.xml = xml;
    }

    public Reader getXml() {
        return this.xml;
    }

    public void setNamespaces(Map<String, String> namespaces) {
        throw new CompassException("Operation not allowed on RawXmlObject");
    }

    public Map<String, String> getNamespaces() {
        return null;
    }

    public String getName() {
        throw new CompassException("Operation not allowed on RawXmlObject");
    }

    public String getValue() {
        throw new CompassException("Operation not allowed on RawXmlObject");
    }

    public XmlObject[] selectPath(String path) throws Exception {
        throw new CompassException("Operation not allowed on RawXmlObject");
    }

    public boolean canCompileXpath() {
        throw new CompassException("Operation not allowed on RawXmlObject");
    }

    public XmlXPathExpression compile(String path) throws Exception {
        throw new CompassException("Operation not allowed on RawXmlObject");
    }
}
