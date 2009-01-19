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

package org.compass.annotations;

/**
 * Specifies whether and how a meta-data property will be stored.
 *
 * @author kimchy
 */
public enum Store {
    /**
     * Let Copmass derive the store. By default, it will be YES.
     */
    NA,

    /**
     * Do not store the property value in the index.
     */
    NO,

    /**
     * Store the original property value in the index. This is useful for short texts
     * like a document's title which should be displayed with the results. The value
     * is stored in its original form, i.e. no analyzer is used before it is stored.
     */
    YES,
    
    /**
     * Store the original property value in the index in a compressed form. This is
     * useful for long documents and for binary valued fields.
     */
    COMPRESS
}
