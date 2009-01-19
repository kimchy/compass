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

package org.compass.spring.aop;

import org.compass.core.Compass;
import org.compass.core.CompassTemplate;
import org.springframework.beans.factory.InitializingBean;

/**
 * A base class for all Compass Aop interceptors. Holds a reference to a
 * <code>Compass</code> instance.
 * <p>
 * It also holds information regarding the "location" of the data object (the
 * one that will be saved/created/deleted). It can be the return value if the
 * flag <code>useReturnValue</code> is set (it is not set bt default), or one
 * of the method parameters (defaults to the first one - <code>0</code>).
 * 
 * @author kimchy
 * 
 */
public abstract class AbstractCompassInterceptor implements InitializingBean {

    private Compass compass;

    protected CompassTemplate compassTemplate;

    private int parameterIndex = 0;

    private boolean useReturnValue = false;

    public void afterPropertiesSet() throws Exception {
        if (compass == null) {
            throw new IllegalArgumentException("compass property is required");
        }
        compassTemplate = new CompassTemplate(compass);
    }

    /**
     * A helper method that based on the configuration, returns the actual data
     * object.
     */
    protected Object findObject(Object returnValue, Object[] args) {
        if (useReturnValue) {
            return returnValue;
        }
        if (parameterIndex >= args.length) {
            throw new IllegalArgumentException("Set parameter index [" + parameterIndex + "] for a method with ["
                    + args.length + "] arguments");
        }
        return args[parameterIndex];
    }

    public Compass getCompass() {
        return compass;
    }

    public void setCompass(Compass compass) {
        this.compass = compass;
    }

    public int getParameterIndex() {
        return parameterIndex;
    }

    public void setParameterIndex(int parameterIndex) {
        this.parameterIndex = parameterIndex;
    }

    public boolean isUseReturnValue() {
        return useReturnValue;
    }

    public void setUseReturnValue(boolean useReturnValue) {
        this.useReturnValue = useReturnValue;
    }
}
