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
 * A Token is an occurence of a term from the text of a field.  It consists of
 * a term's text, the start and end offset of the term in the text of the field,
 * and a type string.
 *
 * @author kimchy
 */
public interface CompassToken {

    String getTermText();

    String getType();

    int getPositionIncrement();

    int getStartOffset();

    int getEndOffset();
}
