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

import org.compass.core.CompassException;
import org.compass.core.CompassHighlightedText;
import org.compass.core.CompassHit;
import org.compass.core.CompassHitsOperations;
import org.compass.core.Resource;

/**
 * @author kimchy
 */
public class DefaultCompassHit implements CompassHit {

    private static final long serialVersionUID = 3617578210385408048L;

    private Resource resource;

    private Object data;

    private float score;

    private boolean resolvedResource;

    private boolean resolvedData;

    private CompassHitsOperations compassHits;

    private int hitNumber;

    DefaultCompassHit(CompassHitsOperations compassHits, int hitNumber) {
        this.compassHits = compassHits;
        this.hitNumber = hitNumber;
    }

    public Object getData() {
        fetchTheData();
        return data;
    }

    public Object data() throws CompassException {
        return getData();
    }

    public Resource getResource() throws CompassException {
        fetchTheResource();
        return resource;
    }

    public Resource resource() throws CompassException {
        return getResource();
    }

    public float getScore() throws CompassException {
        fetchTheResource();
        return score;
    }

    public float score() throws CompassException {
        return getScore();
    }

    public CompassHighlightedText getHighlightedText() throws CompassException {
        return compassHits.highlightedText(hitNumber);
    }

    public CompassHighlightedText highlightedText() throws CompassException {
        return getHighlightedText();
    }

    public String getAlias() throws CompassException {
        fetchTheResource();
        return resource.getAlias();
    }

    public String alias() throws CompassException {
        return getAlias();
    }

    private void fetchTheData() throws CompassException {
        fetchTheResource();
        if (!resolvedData) {
            data = compassHits.data(hitNumber);
            resolvedData = true;
        }
    }

    private void fetchTheResource() throws CompassException {
        if (!resolvedResource) {
            resource = compassHits.resource(hitNumber);
            score = compassHits.score(hitNumber);
            resolvedResource = true;
        }
    }

}
