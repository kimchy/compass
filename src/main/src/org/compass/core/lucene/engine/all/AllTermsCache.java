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

package org.compass.core.lucene.engine.all;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.analysis.Token;
import org.compass.core.config.CompassSettings;
import org.compass.core.mapping.CompassMapping;
import org.compass.core.mapping.ResourceMapping;

/**
 * @author kimchy
 */
public class AllTermsCache {

    private final Map<String, Token[]> aliasTokensPerRootAlias = new HashMap<String, Token[]>();

    public AllTermsCache(CompassSettings settings, CompassMapping mapping) {
        for (ResourceMapping resourceMapping : mapping.getRootMappings()) {
            if (resourceMapping.getAllMapping().isExcludeAlias()) {
                aliasTokensPerRootAlias.put(resourceMapping.getAlias(), new Token[0]);
            } else {
                List<Token> aliasTokens = new ArrayList<Token>();
                aliasTokens.add(new Token(resourceMapping.getAlias().toLowerCase(), 0, resourceMapping.getAlias().length()));
                for (String extendedAlias : resourceMapping.getExtendedAliases()) {
                    aliasTokens.add(new Token(extendedAlias.toLowerCase(), 0, extendedAlias.length()));
                }
                aliasTokensPerRootAlias.put(resourceMapping.getAlias(), aliasTokens.toArray(new Token[aliasTokens.size()]));
            }
        }
    }

    public Token[] getAliasTerms(String alias) {
        return aliasTokensPerRootAlias.get(alias);
    }
}
