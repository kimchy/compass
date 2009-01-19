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

package org.compass.gps.device.support.parallel;

/**
 * <p>Represents a groups of indexable conetnt associated with a name
 * and provides the list of sub indexes it will be indexed into (for
 * parallel indexing).
 *
 * <p>A typical implementation can be an ORM based device that has a
 * Class as the Index Entity (with a simple select associated with it).
 * In such a case, the index entity represents all the data that can
 * be fetched basde on the Class type.
 *
 * @author kimchy
 */
public interface IndexEntity {

    /**
     * Returns the name of the index entity.
     */
    String getName();

    /**
     * Returns a list of the sub indexes this indexable
     * content the index entity represents is going to
     * be indexed into. Used for parallel indexing.
     */
    String[] getSubIndexes();
}
