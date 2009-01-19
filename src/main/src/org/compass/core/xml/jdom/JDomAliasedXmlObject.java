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

package org.compass.core.xml.jdom;

import java.util.Map;

import org.compass.core.xml.AliasedXmlObject;
import org.jdom.Document;
import org.jdom.Element;

/**
 * A JDOM (http://www.jdom.org) implementation of {@link org.compass.core.xml.AliasedXmlObject}.
 *
 * @author kimchy
 */
public class JDomAliasedXmlObject extends JDomXmlObject implements AliasedXmlObject {

    private String alias;

    public JDomAliasedXmlObject(String alias, Document document) {
        super(document);
        this.alias = alias;
    }

    public JDomAliasedXmlObject(String alias, Element element) {
        super(element);
        this.alias = alias;
    }

    public JDomAliasedXmlObject(String alias, Document document, Map<String, String> namespaces) {
        super(document, namespaces);
        this.alias = alias;
    }

    public JDomAliasedXmlObject(String alias, Element element, Map<String, String> namespaces) {
        super(element, namespaces);
        this.alias = alias;
    }

    public String getAlias() {
        return this.alias;
    }
}