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

package org.compass.core.converter.mapping.xsem;

import org.compass.core.xml.XmlObject;
import org.compass.core.xml.XmlXPathExpression;
import org.compass.core.mapping.xsem.XPathEnabledMapping;
import org.compass.core.converter.ConversionException;

/**
 * @author kimchy
 */
public abstract class XmlConverterUtils {

    public static XmlObject[] select(XmlObject xmlObject, XPathEnabledMapping xpathMapping) throws ConversionException {
        if (xpathMapping.getXPathExpression() != null) {
            try {
                return xpathMapping.getXPathExpression().select(xmlObject);
            } catch (Exception e) {
                throw new ConversionException("Failed to select xpath [" + xpathMapping.getXPath() + "]", e);
            }
        }
        if (xmlObject.canCompileXpath()) {
            XmlXPathExpression xPathExpression = null;
            try {
                xPathExpression = xmlObject.compile(xpathMapping.getXPath());
            } catch (Exception e) {
                throw new ConversionException("Failed to compile xpath [" + xpathMapping.getXPath() + "]", e);
            }
            xpathMapping.setXPathExpression(xPathExpression);
            try {
                return xPathExpression.select(xmlObject);
            } catch (Exception e) {
                throw new ConversionException("Failed to select xpath [" + xpathMapping.getXPath() + "]", e);
            }
        }
        try {
            return xmlObject.selectPath(xpathMapping.getXPath());
        } catch (Exception e) {
            throw new ConversionException("Failed to select xpath [" + xpathMapping.getXPath() + "]", e);
        }
    }
}
