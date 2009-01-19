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

package org.compass.core.accessor;

import org.compass.core.CompassException;

/**
 * Indicates that an expected getter or setter method could not be found on a
 * class.
 * <p>
 * Initial version taken from hibernate.
 * </p>
 * 
 * @author kimchy
 */
public class PropertyNotFoundException extends CompassException {

    private static final long serialVersionUID = 3544953272776732729L;

    public PropertyNotFoundException(String s) {
        super(s);
    }

}
