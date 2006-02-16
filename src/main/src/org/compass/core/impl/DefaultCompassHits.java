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
import org.compass.core.CompassHighlighter;
import org.compass.core.CompassHit;
import org.compass.core.CompassHits;
import org.compass.core.Resource;
import org.compass.core.engine.SearchEngineHits;

/**
 * 
 * @author kimchy
 * 
 */
public class DefaultCompassHits extends AbstractCompassHits implements CompassHits {

    private SearchEngineHits hits;

    private InternalCompassSession session;

    public DefaultCompassHits(SearchEngineHits hits, InternalCompassSession session) {
        this.hits = hits;
        this.session = session;
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
        ResourceIdKey key = new ResourceIdKey(session.getMapping(), resource);
        Resource cachedResource = session.getFirstLevelCache().getResource(key);
        if (cachedResource != null) {
            return cachedResource;
        }
        session.getFirstLevelCache().setResource(key, resource);
        return resource;
    }

    public int getLength() {
        return hits.getLength();
    }

    public float score(int n) throws CompassException {
        return hits.score(n);
    }

    public CompassHighlighter highlighter(int n) throws CompassException {
        return new DefaultCompassHighlighter(session, hits.getHighlighter(), resource(n));
    }

    public CompassDetachedHits detach() throws CompassException {
        return detach(0, hits.getLength());
    }

    public CompassDetachedHits detach(int from, int size) throws CompassException, IllegalArgumentException {
        return new DefaultCompassDetachedHits(hits, session, from, size);
    }

    public void close() throws CompassException {
        hits.close();
    }
}
