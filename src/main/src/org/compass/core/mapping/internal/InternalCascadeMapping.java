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

package org.compass.core.mapping.internal;

import org.compass.core.mapping.Cascade;
import org.compass.core.mapping.CascadeMapping;

/**
 * A internal mutable interface of cascade mapping definition.
 *
 * @author kimchy
 */
public interface InternalCascadeMapping extends CascadeMapping {

    /**
     * Sets the cascade mapping definitions.
     */
    void setCascades(Cascade[] cascades);
}
