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
import java.util.LinkedList;
import java.util.ListIterator;

import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;

/**
 * Orders Tokens in a window first by their startOffset ascending.
 * endOffset is currently ignored.
 *
 * <p>This is meant to work around fickleness in the highlighter only.  It
 * can mess up token positions and should not be used for indexing or querying.
 *
 * @author kimchy
 */
public class TokenOrderingFilter extends TokenFilter {
    private final int windowSize;
    private final LinkedList<Token> queue = new LinkedList<Token>();
    private boolean done = false;

    public TokenOrderingFilter(TokenStream input, int windowSize) {
        super(input);
        this.windowSize = windowSize;
    }

    @Override
    public Token next() throws IOException {
        while (!done && queue.size() < windowSize) {
            Token newTok = input.next();
            if (newTok == null) {
                done = true;
                break;
            }

            // reverse iterating for better efficiency since we know the
            // list is already sorted, and most token start offsets will be too.
            ListIterator<Token> iter = queue.listIterator(queue.size());
            while (iter.hasPrevious()) {
                if (newTok.startOffset() >= iter.previous().startOffset()) {
                    // insertion will be before what next() would return (what
                    // we just compared against), so move back one so the insertion
                    // will be after.
                    iter.next();
                    break;
                }
            }
            iter.add(newTok);
        }

        return queue.isEmpty() ? null : queue.removeFirst();
    }
}
