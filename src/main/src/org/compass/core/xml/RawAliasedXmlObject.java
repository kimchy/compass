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

/**
 * Extends {@link RawXmlObject} and adds association with an alias.
 *
 * @author kimchy
 */
public class RawAliasedXmlObject extends RawXmlObject implements AliasedXmlObject {

    private String alias;

    public RawAliasedXmlObject(String alias, String xml) {
        super(xml);
        this.alias = alias;
    }

    public RawAliasedXmlObject(String alias, Reader xml) {
        super(xml);
        this.alias = alias;
    }

    public String getAlias() {
        return this.alias;
    }
}
