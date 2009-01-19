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

package org.compass.core.mapping;

import org.compass.core.CompassException;

/**
 * An exception related to mapping errors.
 *
 * @author kimchy
 */
public class MappingException extends CompassException {

    private static final long serialVersionUID = 3977582489495811122L;

    public MappingException(String msg, Throwable root) {
        super(msg, root);
    }

    public MappingException(String s) {
        super(s);
    }
}
