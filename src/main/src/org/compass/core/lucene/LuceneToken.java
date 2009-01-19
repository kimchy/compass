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

package org.compass.core.lucene;

import org.apache.lucene.analysis.Token;
import org.compass.core.CompassToken;

/**
 * @author kimchy
 */
public class LuceneToken implements CompassToken {

    private Token token;

    public LuceneToken(Token token) {
        this.token = token;
    }

    public String getTermText() {
        return new String(token.termBuffer(), 0, token.termLength());
    }

    public String getType() {
        return token.type();
    }

    public int getPositionIncrement() {
        return token.getPositionIncrement();
    }

    public int getStartOffset() {
        return token.startOffset();
    }

    public int getEndOffset() {
        return token.endOffset();
    }

    public Token getLuceneToken() {
        return this.token;
    }

    public String toString() {
        return token.toString();
    }
}
