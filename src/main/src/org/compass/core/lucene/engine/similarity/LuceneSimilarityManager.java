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

package org.compass.core.lucene.engine.similarity;

import org.apache.lucene.search.DefaultSimilarity;
import org.apache.lucene.search.Similarity;
import org.compass.core.CompassException;
import org.compass.core.config.CompassConfigurable;
import org.compass.core.config.CompassSettings;
import org.compass.core.config.ConfigurationException;
import org.compass.core.lucene.LuceneEnvironment;
import org.compass.core.util.ClassUtils;

/**
 * A {@link org.apache.lucene.search.Similarity} manager managing both the index {@link org.apache.lucene.search.Similarity}
 * and search {@link org.apache.lucene.search.Similarity}.
 *
 * @author kimchy
 */
public class LuceneSimilarityManager implements CompassConfigurable {

    private Similarity indexSimilarity;

    private Similarity searchSimilarity;

    public void configure(CompassSettings settings) throws CompassException {
        String defaultType = settings.getSetting(LuceneEnvironment.Similarity.DEFAULT_SIMILARITY_TYPE, DefaultSimilarity.class.getName());
        indexSimilarity = createSimilarity(settings.getSetting(LuceneEnvironment.Similarity.INDEX_SIMILARITY_TYPE, defaultType), settings);
        searchSimilarity = createSimilarity(settings.getSetting(LuceneEnvironment.Similarity.SEARCH_SIMILARITY_TYPE, defaultType), settings);
    }

    /**
     * Returns the index similarity.
     */
    public Similarity getIndexSimilarity() {
        return indexSimilarity;
    }

    /**
     * Returns the search similarity. 
     */
    public Similarity getSearchSimilarity() {
        return searchSimilarity;
    }

    private Similarity createSimilarity(String type, CompassSettings settings) {
        Class similarityClass;
        try {
            similarityClass = ClassUtils.forName(type, settings.getClassLoader());
        } catch (ClassNotFoundException e) {
            throw new ConfigurationException("Failed to create class of type [" + type + "]", e);
        }
        Object similarityInstance;
        try {
            similarityInstance = similarityClass.newInstance();
        } catch (Exception e) {
            throw new ConfigurationException("Failed to create instance of type [" + type + "]", e);
        }
        if (similarityInstance instanceof SimilarityFactory) {
            if (similarityInstance instanceof CompassConfigurable) {
                ((CompassConfigurable) similarityInstance).configure(settings);
            }
            similarityInstance = ((SimilarityFactory) similarityInstance).createSimilarity();
        }
        if (!(similarityInstance instanceof Similarity)) {
            throw new ConfigurationException("Type [" + type + "] is not an instance of Similarity");
        }
        if (similarityInstance instanceof CompassConfigurable) {
            ((CompassConfigurable) similarityInstance).configure(settings);
        }
        return (Similarity) similarityInstance;
    }
}
