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

import java.util.HashMap;

import org.compass.core.CompassDetachedHits;
import org.compass.core.CompassException;
import org.compass.core.CompassHighlightedText;
import org.compass.core.CompassHighlighter;
import org.compass.core.CompassHit;
import org.compass.core.CompassQuery;
import org.compass.core.Resource;
import org.compass.core.engine.SearchEngineHits;
import org.compass.core.spi.InternalCompassHighlightedText;
import org.compass.core.spi.InternalCompassHits;
import org.compass.core.spi.InternalCompassSession;
import org.compass.core.spi.InternalResource;
import org.compass.core.spi.ResourceKey;

/**
 * @author kimchy
 */
public class DefaultCompassHits extends AbstractCompassHits implements InternalCompassHits {

    private final SearchEngineHits hits;

    private final InternalCompassSession session;

    private final CompassQuery query;

    private HashMap<Integer, InternalCompassHighlightedText> highlightedTextHolder;
    
    private CompassQuery suggestedQuery;

    public DefaultCompassHits(SearchEngineHits hits, InternalCompassSession session, CompassQuery query) {
        this.hits = hits;
        this.session = session;
        this.query = query;
    }

    public SearchEngineHits getSearchEngineHits() {
        return this.hits;
    }

    public CompassHit hit(int n) throws CompassException {
        return new DefaultCompassHit(this, n);
    }

    public Object data(int n) throws CompassException {
        Resource resource = resource(n);
        return session.getByResource(resource);
    }

    public Resource resource(int n) throws CompassException {
        Resource resource = hits.getResource(n);
        ResourceKey key = ((InternalResource) resource).getResourceKey();
        Resource cachedResource = session.getFirstLevelCache().getResource(key);
        if (cachedResource != null) {
            return cachedResource;
        }
        session.getFirstLevelCache().setResource(key, resource);
        return resource;
    }

    public CompassQuery getQuery() {
        return this.query;
    }

    public CompassQuery getSuggestedQuery() {
        if (suggestedQuery == null) {
            suggestedQuery = getQuery().getSuggestedQuery();
        }
        return suggestedQuery;
    }

    public int getLength() {
        return hits.getLength();
    }

    public float score(int n) throws CompassException {
        return hits.score(n);
    }

    public CompassHighlighter highlighter(int n) throws CompassException {
        return new DefaultCompassHighlighter(session, this, n);
    }

    public CompassDetachedHits detach() throws CompassException {
        return detach(0, hits.getLength());
    }

    public CompassDetachedHits detach(int from, int size) throws CompassException, IllegalArgumentException {
        return new DefaultCompassDetachedHits(this, session, from, size, getQuery(), getSuggestedQuery());
    }

    public CompassHighlightedText highlightedText(int n) throws CompassException {
        if (highlightedTextHolder == null) {
            return null;
        }
        return highlightedTextHolder.get(new Integer(n));
    }

    public void setHighlightedText(int n, String propertyName, String highlihgtedText) {
        if (highlightedTextHolder == null) {
            highlightedTextHolder = new HashMap<Integer, InternalCompassHighlightedText>();
        }

        InternalCompassHighlightedText hitHighlightedText = highlightedTextHolder.get(n);
        if (hitHighlightedText == null) {
            hitHighlightedText = new DefaultCompassHighlightedText();
            highlightedTextHolder.put(n, hitHighlightedText);
        }

        hitHighlightedText.setHighlightedText(propertyName, highlihgtedText);
    }

    public void close() throws CompassException {
        hits.close();
    }
}
