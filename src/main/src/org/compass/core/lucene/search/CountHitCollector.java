package org.compass.core.lucene.search;

import org.apache.lucene.search.HitCollector;

/**
 * A hit collector that only returns the count of the hits.
 *
 * @author kimchy
 */
public class CountHitCollector extends HitCollector {

    private int totalHits;

    public void collect(int doc, float score) {
        if (score > 0.0f) {
            totalHits++;
        }
    }

    public int getTotalHits() {
        return totalHits;
    }
}
