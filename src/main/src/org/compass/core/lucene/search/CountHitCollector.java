package org.compass.core.lucene.search;

import org.apache.lucene.search.HitCollector;

/**
 * A hit collector that only returns the count of the hits.
 *
 * @author kimchy
 */
public class CountHitCollector extends HitCollector {

    private int totalHits;

    private float minimumScore = 0.0f;

    public CountHitCollector(float minimumScore) {
        this.minimumScore = minimumScore;
    }

    public void collect(int doc, float score) {
        if (score > minimumScore) {
            totalHits++;
        }
    }

    public int getTotalHits() {
        return totalHits;
    }
}
