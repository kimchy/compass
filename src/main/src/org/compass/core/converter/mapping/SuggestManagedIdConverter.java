/*
 * Copyright 2004-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.compass.core.converter.mapping;

import org.compass.core.converter.Converter;
import org.compass.core.mapping.osem.ManagedId;

/**
 * Allows for a converter to implement it and suggest what the managed id setting will
 * be.
 *
 * <p>Mostly used by converters that can't/won't unmarshall data, thus returning
 * {@link ManagedId#FALSE}.
 *
 * @author kimchy
 */
public interface SuggestManagedIdConverter<T> extends Converter<T> {

    /**
     * Some converters can suggest (or basically force) the managed id setting
     * of a class property.
     */
    ManagedId suggestManagedId();
}
