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

package org.compass.core;

/**
 * A cached holder of highlighted text.
 *
 * @author kimchy
 * @see CompassHitsOperations#highlightedText(int)
 * @see CompassHits#highlighter(int)
 */
public interface CompassHighlightedText {

    /**
     * Returns the first highlighted text cached.
     */
    String getHighlightedText() throws CompassException;

    /**
     * Returns the highlighted text associated with the given property name.
     */
    String getHighlightedText(String propertyName) throws CompassException;
}
