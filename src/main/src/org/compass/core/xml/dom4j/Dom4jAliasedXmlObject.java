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

package org.compass.core.xml.dom4j;

import java.util.Map;

import org.compass.core.xml.AliasedXmlObject;
import org.dom4j.Node;

/**
 * A dom4j (http://www.dom4j.org) implementation of {@link AliasedXmlObject}.
 * 
 * @author kimchy
 */
public class Dom4jAliasedXmlObject extends Dom4jXmlObject implements AliasedXmlObject {

    private String alias;

    public Dom4jAliasedXmlObject(String alias, Node node) {
        super(node);
        this.alias = alias;
    }

    public Dom4jAliasedXmlObject(String alias, Node node, Map<String, String> namespaces) {
        super(node, namespaces);
        this.alias = alias;
    }

    public String getAlias() {
        return this.alias;
    }
}
