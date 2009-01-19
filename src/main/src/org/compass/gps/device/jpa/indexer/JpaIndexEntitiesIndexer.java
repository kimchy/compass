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

package org.compass.gps.device.jpa.indexer;

import org.compass.gps.device.jpa.JpaGpsDevice;
import org.compass.gps.device.support.parallel.IndexEntitiesIndexer;

/**
 * Extends index enteties indexer and allows for the device to be set on it
 *
 * @author kimchy
 */
public interface JpaIndexEntitiesIndexer extends IndexEntitiesIndexer {

    /**
     * Sets the jpa gps device for the given indexer. Called once when the device
     * starts up.
     */
    void setJpaGpsDevice(JpaGpsDevice jpaGpsDevice);
}
