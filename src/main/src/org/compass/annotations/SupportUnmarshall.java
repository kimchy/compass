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

package org.compass.annotations;

/**
 * Controls if the {@link Searchable} class support un-marshalling (the process
 * of converting the raw resource read from the search engine into a domain model
 * object).
 *
 * @author kimchy
 */
public enum SupportUnmarshall {
	/**
	 * Defaults to Compass global osem setting supportUnmarshall. See
     * {@link org.compass.core.config.CompassEnvironment.Osem#SUPPORT_UNMARSHALL}.
 	 */
    NA,
    /**
     * The searchable class will support un-marshalling. Will override
     * anything set in Compass global osem setting supportUnmarshall.
     */
    TRUE,
    /**
     * The searchable class will not support un-marshalling. Will override
     * anything set in Compass global osem setting supportUnmarshall.
     */
    FALSE
}
