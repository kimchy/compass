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

package org.compass.gps;

import org.compass.core.CompassException;

/**
 * An exception that occured in Compass Gps. All other Compass Gps runtime
 * exception derive from it.
 * 
 * @author kimchy
 */
public class CompassGpsException extends CompassException {

    private static final long serialVersionUID = 3688784761049395766L;

    public CompassGpsException(String string, Throwable root) {
        super(string, root);
    }

    public CompassGpsException(String s) {
        super(s);
    }
}
