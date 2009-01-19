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

package org.compass.core.lucene.engine;

import java.io.IOException;
import java.util.Comparator;
import java.util.PriorityQueue;

import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.compass.core.CompassTermFreq;
import org.compass.core.engine.SearchEngineException;
import org.compass.core.engine.SearchEngineTermFrequencies;
import org.compass.core.impl.DefaultCompassTermFreq;

/**
 * @author kimchy
 */
public class LuceneSearchEngineTermFrequencies implements SearchEngineTermFrequencies {

    private CompassTermFreq[] termFreqs;

    public LuceneSearchEngineTermFrequencies(String[] propertyNames, int size, LuceneSearchEngineInternalSearch internalSearch) throws SearchEngineException {

        if (internalSearch.isEmpty()) {
            termFreqs = new CompassTermFreq[0];
            return;
        }

        PriorityQueue<CompassTermFreq> queue = new PriorityQueue<CompassTermFreq>(10, new Comparator<CompassTermFreq>() {
            public int compare(CompassTermFreq a, CompassTermFreq b) {
                return (int) (b.getFreq() - a.getFreq());
            }
        });

        for (String propertyName : propertyNames) {
            TermEnum termEnum = null;
            try {
                termEnum = internalSearch.getReader().terms(new Term(propertyName, ""));
                while (termEnum.term() != null && propertyName.equals(termEnum.term().field())) {
                    queue.add(new DefaultCompassTermFreq(termEnum.term().text(), termEnum.docFreq(), propertyName));
                    if (!termEnum.next()) {
                        break;
                    }
                }
            } catch (IOException e) {
                throw new SearchEngineException("Failed to get term freq for proeprty [" + propertyName + "]", e);
            } finally {
                if (termEnum != null) {
                    try {
                        termEnum.close();
                    } catch (IOException e) {
                        // do nothing here, maybe warn?
                    }
                }
            }
        }
        int retSize = size;
        if (queue.size() < size) {
            retSize = queue.size();
        }
        termFreqs = new CompassTermFreq[retSize];
        for (int i = 0; i < termFreqs.length; i++) {
            termFreqs[i] = queue.poll();
        }
    }

    public CompassTermFreq[] getTerms() {
        return termFreqs;
    }

}
