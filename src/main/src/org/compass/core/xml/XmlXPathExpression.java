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

/**
 * A compiled representation of an xpath expression.
 *
 * @author kimchy
 */
public interface XmlXPathExpression {

    /**
     * Executes the given compiled xpath expression against the given xml object.
     *
     * @param xmlObject The xml object to execute the compiled xpath expression against
     * @return A list of xml objects matching the given compiled xpath expression
     * @throws Exception
     */
    XmlObject[] select(XmlObject xmlObject) throws Exception;
}
