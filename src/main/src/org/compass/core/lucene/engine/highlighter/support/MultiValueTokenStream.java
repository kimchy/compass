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

package org.compass.core.lucene.engine.highlighter.support;

import java.io.IOException;
import java.io.StringReader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;

/**
 * Helper class which creates a single TokenStream out of values from a
 * multi-valued field.
 *
 * @author kimchy
 */
public class MultiValueTokenStream extends TokenStream {

    private String fieldName;
    private String[] values;
    private Analyzer analyzer;
    private int curIndex;                  // next index into the values array
    private int curOffset;                 // offset into concatenated string
    private TokenStream currentStream;     // tokenStream currently being iterated
    private boolean orderTokenOffsets;

    /**
     * Constructs a TokenStream for consecutively-analyzed field values
     *
     * @param fieldName name of the field
     * @param values    array of field data
     * @param analyzer  analyzer instance
     */
    public MultiValueTokenStream(String fieldName, String[] values,
                                 Analyzer analyzer, boolean orderTokenOffsets) {
        this.fieldName = fieldName;
        this.values = values;
        this.analyzer = analyzer;
        curIndex = -1;
        curOffset = 0;
        currentStream = null;
        this.orderTokenOffsets = orderTokenOffsets;
    }

    /**
     * Returns the next token in the stream, or null at EOS.
     */
    @Override
    public Token next() throws IOException {
        int extra = 0;
        if (currentStream == null) {
            curIndex++;
            if (curIndex < values.length) {
                currentStream = analyzer.tokenStream(fieldName, new StringReader(values[curIndex]));
                if (orderTokenOffsets) currentStream = new TokenOrderingFilter(currentStream, 10);
                // add extra space between multiple values
                if (curIndex > 0)
                    extra = analyzer.getPositionIncrementGap(fieldName);
            } else {
                return null;
            }
        }
        Token nextToken = currentStream.next();
        if (nextToken == null) {
            curOffset += values[curIndex].length();
            currentStream = null;
            return next();
        }
        // create an modified token which is the offset into the concatenated
        // string of all values
        Token offsetToken = new Token(nextToken.termText(),
                nextToken.startOffset() + curOffset,
                nextToken.endOffset() + curOffset);
        offsetToken.setPositionIncrement(nextToken.getPositionIncrement() + extra * 10);
        return offsetToken;
    }

    /**
     * Returns all values as a single String into which the Tokens index with
     * their offsets.
     */
    public String asSingleValue() {
        StringBuilder sb = new StringBuilder();
        for (String str : values)
            sb.append(str);
        return sb.toString();
    }

}
