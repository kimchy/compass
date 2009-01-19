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

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.compass.core.CompassHit;
import org.compass.core.CompassHitsOperations;

/**
 * @author kimchy
 */
public class DefaultCompassHitIterator implements Iterator<CompassHit> {

    private CompassHitsOperations compassHits;

    private int hitNumber = 0;

    DefaultCompassHitIterator(CompassHitsOperations compassHits) {
        this.compassHits = compassHits;
    }

    public boolean hasNext() {
        return hitNumber < compassHits.getLength();
    }

    public CompassHit next() {
        if (hitNumber == compassHits.getLength()) {
            throw new NoSuchElementException();
        }

        return compassHits.hit(hitNumber++);
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

    public int length() {
        return compassHits.getLength();
    }
}
