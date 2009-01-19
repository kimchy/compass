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

package org.compass.core.impl;

import org.compass.core.CompassDetachedHits;
import org.compass.core.CompassException;
import org.compass.core.CompassHighlightedText;
import org.compass.core.CompassHit;
import org.compass.core.CompassQuery;
import org.compass.core.Resource;
import org.compass.core.mapping.AliasMapping;
import org.compass.core.mapping.osem.ClassMapping;
import org.compass.core.spi.InternalCompassHits;
import org.compass.core.spi.InternalCompassSession;

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

    private CompassHighlightedText[] highlightedText;

    private CompassQuery query;

    private CompassQuery suggestedQuery;

    public DefaultCompassDetachedHits(InternalCompassHits hits, InternalCompassSession session, int from, int size,
                                      CompassQuery query, CompassQuery suggestedQuery)
            throws CompassException, IllegalArgumentException {
        this.query = query;
        this.suggestedQuery = suggestedQuery;
        this.length = size;
        if (from < 0) {
            throw new IllegalArgumentException("Can't preload with negative from [" + from + "]");
        }
        if ((from + size) > hits.getLength()) {
            this.length = hits.getLength() - from;
        }
        if (this.length < 0) {
            this.length = 0;
        }
        this.totalLength = hits.getLength();
        resources = new Resource[this.length];
        scores = new float[this.length];
        datas = new Object[this.length];
        this.hits = new CompassHit[this.length];
        highlightedText = new CompassHighlightedText[this.length];
        for (int i = 0; i < this.length; i++) {
            int location = from + i;
            resources[i] = hits.resource(location);
            scores[i] = hits.score(location);
            this.hits[i] = new DefaultCompassHit(this, i);
            highlightedText[i] = hits.highlightedText(location);
            AliasMapping aliasMapping = session.getMapping().getAliasMapping(resources[i].getAlias());
            if (aliasMapping instanceof ClassMapping) {
                datas[i] = session.getByResource(resources[i]);
            }
        }
    }

    public CompassQuery getQuery() {
        return this.query;
    }

    public CompassQuery getSuggestedQuery() {
        return this.suggestedQuery;
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

    public CompassHighlightedText highlightedText(int n) throws CompassException {
        return highlightedText[n];
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
