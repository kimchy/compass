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

package org.compass.core.events;

import org.compass.core.CompassQuery;

/**
 * An event called before a query is executed to delete all its matching resources.
 * Maps to the <code>delete</code> opeation on the session.
 *
 * <p>The listener can implement {@link org.compass.core.config.CompassAware},
 * {@link org.compass.core.config.CompassMappingAware}, or {@link org.compass.core.config.CompassConfigurable}
 * in order to be injected with other relevant instances of Compass.
 *
 * @author kimchy
 */
public interface PreDeleteQueryEventListener {

    /**
     * An event called before an a query is executed to delete all its matching resources.
     * Maps to the <code>delete</code> opeation on the session. Returns if this operation should be filtered or not.
     */
    FilterOperation onPreDelete(CompassQuery query);
}