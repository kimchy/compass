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

package org.compass.core.xml;

import java.io.Serializable;

/**
 * A wrapper for an Xml Object. Used with XSEM to support mapping between Xml
 * and Search Engine. The Xml Object can be an Xml element/attribute/... .
 *
 * @author kimchy
 */
public interface XmlObject extends Serializable {

    /**
     * Returns the name of the xml object. Should be the element/attribute name.
     */
    String getName();

    /**
     * Returns the value of the xml object.
     */
    String getValue();

    /**
     * Returns a list of xml objects matching the given xpath expression.
     * Note, that the actual xml implementation might support only xpath
     * expression compliation, so it is ok not to implement this method.
     *
     * @param path The xpath expression
     * @return A list of xml objects matching the given xpath expression
     * @throws Exception
     */
    XmlObject[] selectPath(String path) throws Exception;

    /**
     * Returns <code>true</code> of the xml object supports xpath expression
     * compilation.
     */
    boolean canCompileXpath();

    /**
     * Compiles the given xpath expression.
     *
     * @param path The xpath expression
     * @return The compiled xpath expression
     * @throws Exception
     */
    XmlXPathExpression compile(String path) throws Exception;
}
