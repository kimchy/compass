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

package org.compass.core.lucene.engine.analyzer.synonym;

import java.io.IOException;
import java.util.LinkedList;

import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;

/**
 * A synonym provider which uses {@link SynonymLookupProvider} to provide
 * synonyms for a given value.
 *
 * @author kimchy
 */
public class SynonymFilter extends TokenFilter {

    public static final String TOKEN_TYPE_SYNONYM = "SYNONYM";

    private LinkedList synonymStack;

    private SynonymLookupProvider synonymLookupProvider;

    public SynonymFilter(TokenStream in, SynonymLookupProvider synonymLookupProvider) {
        super(in);
        this.synonymLookupProvider = synonymLookupProvider;
        this.synonymStack = new LinkedList();
    }

    public Token next() throws IOException {
        if (synonymStack.size() > 0) {
            return (Token) synonymStack.removeFirst();
        }

        Token token = input.next();
        if (token == null) {
            return null;
        }

        addAliasesToStack(token);

        return token;
    }

    private void addAliasesToStack(Token token) {
        String[] synonyms = synonymLookupProvider.lookupSynonyms(token.termText());

        if (synonyms == null) {
            return;
        }

        for (int i = 0; i < synonyms.length; i++) {
            Token synToken = new Token(synonyms[i], token.startOffset(), token.endOffset(), TOKEN_TYPE_SYNONYM);
            synToken.setPositionIncrement(0);
            synonymStack.addFirst(synToken);
        }
    }
}
