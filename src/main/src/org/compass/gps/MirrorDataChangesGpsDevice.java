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

package org.compass.gps;

/**
 * A Gps Device that can also perform mirroring operation. Mirror operations
 * means that the device can reflect real time data changes done in the data
 * source to the index. In other words, the device can detect all the changes
 * made to the data source since the last indexing/mirroring operation and
 * reflect it to the index.
 * 
 * @author kimchy
 */
public interface MirrorDataChangesGpsDevice extends CompassGpsDevice {

    /**
     * Should the device perform real time data mirroring.
     * 
     * @return <code>true</code> if the device mirror data changes
     */
    public boolean isMirrorDataChanges();

    /**
     * Sets if the device will perform real time data changes mirroring.
     * 
     * @param mirrorDataChanges Should the device mirror data changes
     */
    public void setMirrorDataChanges(boolean mirrorDataChanges);
}
