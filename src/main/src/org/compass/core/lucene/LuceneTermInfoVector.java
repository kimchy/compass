/*
 * Copyright 2004-2006 the original author or authors.
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

import org.apache.lucene.index.TermFreqVector;
import org.apache.lucene.index.TermPositionVector;
import org.apache.lucene.index.TermVectorOffsetInfo;
import org.compass.core.CompassTermInfoVector;

/**
 * @author kimchy
 */
public class LuceneTermInfoVector implements CompassTermInfoVector {

    private TermFreqVector termFreqVector;

    public class LuceneOffsetInfo implements CompassTermInfoVector.OffsetInfo {

        private TermVectorOffsetInfo offsetInfo;

        public LuceneOffsetInfo(TermVectorOffsetInfo offsetInfo) {
            this.offsetInfo = offsetInfo;
        }

        public int getEndOffset() {
            return offsetInfo.getEndOffset();
        }

        public int getStartOffset() {
            return offsetInfo.getStartOffset();
        }

        public boolean equals(Object o) {
            return offsetInfo.equals(o);
        }

        public int hashCode() {
            return offsetInfo.hashCode();
        }
    }

    public LuceneTermInfoVector(TermFreqVector termFreqVector) {
        this.termFreqVector = termFreqVector;
    }

    public String getProperty() {
        return termFreqVector.getField();
    }

    public int size() {
        return termFreqVector.size();
    }

    public String[] getTerms() {
        return termFreqVector.getTerms();
    }

    public int[] getTermFrequencies() {
        return termFreqVector.getTermFrequencies();
    }

    public int indexOf(String term) {
        return termFreqVector.indexOf(term);
    }

    public int[] indexesOf(String[] terms, int start, int len) {
        return termFreqVector.indexesOf(terms, start, len);
    }

    public int[] getTermPositions(int index) {
        return ((TermPositionVector) termFreqVector).getTermPositions(index);
    }

    public OffsetInfo[] getOffsets(int index) {
        TermVectorOffsetInfo[] infos = ((TermPositionVector) termFreqVector).getOffsets(index);
        if (infos == null) {
            return null;
        }
        LuceneOffsetInfo[] luceneInfos = new LuceneOffsetInfo[infos.length];
        for (int i = 0; i < infos.length; i++) {
            luceneInfos[i] = new LuceneOffsetInfo(infos[i]);
        }
        return luceneInfos;
    }

    public TermFreqVector getTermFreqVector() {
        return termFreqVector;
    }
}
