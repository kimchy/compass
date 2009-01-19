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

package org.compass.gps.device.hibernate.lifecycle;

import org.hibernate.event.AbstractCollectionEvent;
import org.hibernate.event.PostDeleteEvent;
import org.hibernate.event.PostInsertEvent;
import org.hibernate.event.PostUpdateEvent;

/**
 * Allows to hook filtering of object during the mirroring process.
 *
 * <p>Note, Compass event listeners (within {@link org.compass.core.events}) should be
 * used instead of this filter for generic cases that do not require the actual Hibernate
 * event objects.
 *
 * @author kimchy
 */
public interface HibernateMirrorFilter {

    /**
     * Should the post insert hibernate event be filtered or not
     *
     * @param postInsertEvent The Hibernate post insert event
     * @return <code>true</code> if the event should be filtered, <code>false</code> otherwise
     */
    boolean shouldFilterInsert(PostInsertEvent postInsertEvent);

    /**
     * Should the post update hibernate event be filtered or not
     *
     * @param postUpdateEvent The Hibernate post update event
     * @return <code>true</code> if the event should be filtered, <code>false</code> otherwise
     */
    boolean shouldFilterUpdate(PostUpdateEvent postUpdateEvent);

    /**
     * Should the post delete hibernate event be filtered or not
     *
     * @param postDeleteEvent The Hibernate post delete event
     * @return <code>true</code> if the event should be filtered, <code>false</code> otherwise
     */
    boolean shouldFilterDelete(PostDeleteEvent postDeleteEvent);

    /**
     * Should the post collection event be filtered or not.
     */
    boolean shouldFilterCollection(AbstractCollectionEvent postCollectionEvent);
}