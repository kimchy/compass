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

package org.compass.core.impl;

import org.compass.core.CompassDetachedHits;
import org.compass.core.CompassException;
import org.compass.core.CompassHit;
import org.compass.core.CompassHitIterator;
import org.compass.core.Resource;
import org.compass.core.spi.InternalCompassSession;
import org.compass.core.engine.SearchEngineHits;

/**
 * @author kimchy
 */
public class DefaultCompassDetachedHits extends AbstractCompassHits implements CompassDetachedHits {

    private int length;

    private int totalLength;

    private Resource[] resources;

    private Object[] datas;

    private float[] scores;

    private CompassHit[] hits;

    public DefaultCompassDetachedHits(SearchEngineHits hits, InternalCompassSession session, int from, int size)
            throws CompassException, IllegalArgumentException {
        this.length = size;
        if (from < 0) {
            throw new IllegalArgumentException("Can't preload with negative from [" + from + "]");
        }
        if ((from + size) > hits.getLength()) {
            this.length = hits.getLength() - from;
        }
        this.totalLength = hits.getLength();
        resources = new Resource[this.length];
        scores = new float[this.length];
        datas = new Object[this.length];
        this.hits = new CompassHit[this.length];
        for (int i = 0; i < this.length; i++) {
            resources[i] = hits.getResource(from + i);
            scores[i] = hits.score(from + i);
            this.hits[i] = new DefaultCompassHit(this, i);
            if (session.getMapping().hasClassMapping(resources[i].getAlias())) {
                datas[i] = session.getByResource(resources[i]);
            }
        }
    }

    public float score(int n) throws CompassException, IllegalArgumentException {
        return scores[n];
    }

    public Resource resource(int n) throws CompassException, IllegalArgumentException {
        return resources[n];
    }

    public Object data(int n) throws CompassException, IllegalArgumentException {
        return datas[n];
    }

    public CompassHitIterator iterator() throws CompassException {
        return new DefaultCompassHitIterator(this);
    }

    public CompassHit hit(int n) throws CompassException {
        return hits[n];
    }

    public int getLength() {
        return length;
    }

    public int getTotalLength() {
        return totalLength;
    }

    public int totalLength() {
        return totalLength;
    }

    public Resource[] getResources() throws CompassException {
        return resources;
    }

    public Object[] getDatas() throws CompassException {
        return datas;
    }

    public CompassHit[] getHits() throws CompassException {
        return hits;
    }

}
