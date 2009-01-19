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

package org.compass.spring.web.mvc;

/**
 * The results object returned by
 * {@link org.compass.spring.web.mvc.CompassIndexController} when an index
 * operation has occured.
 * 
 * @author kimchy
 */
public class CompassIndexResults {

    private long indexTime;

    public CompassIndexResults(long indexTime) {
        this.indexTime = indexTime;
    }

    /**
     * Returns the time it took to index using <code>CompassGps</code>. Tims
     * is in milli-seconds.
     * 
     * @return How long it took to index in milli-seconds
     */
    public long getIndexTime() {
        return indexTime;
    }

}
