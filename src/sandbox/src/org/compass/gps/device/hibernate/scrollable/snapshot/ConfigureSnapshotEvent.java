/*
 * Copyright 2004-2008 the original author or authors.
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

package org.compass.gps.device.hibernate.scrollable.snapshot;

import java.util.List;

import org.hibernate.Session;

/**
 * A configuration event, works with
 * {@link org.compass.gps.device.hibernate.scrollable.snapshot.HibernateSnapshotEventListener#configure(ConfigureSnapshotEvent)}.Holds
 * a list of all the mappings.
 */
public class ConfigureSnapshotEvent extends AbstractSnapshotEvent {

    private List mappings;

    public ConfigureSnapshotEvent(Session session, List mappings) {
        super(session);
        this.mappings = mappings;
    }

    public List getMappings() {
        return mappings;
    }
}
