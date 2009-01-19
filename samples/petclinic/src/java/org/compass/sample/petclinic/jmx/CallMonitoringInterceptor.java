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

package org.compass.sample.petclinic.jmx;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.util.StopWatch;

/**
 * Simple interceptor that monitors call count and call invocation time.
 * Implements the CallMonitor management interface.
 * 
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 1.2
 */
public class CallMonitoringInterceptor implements CallMonitor, MethodInterceptor {

    private boolean isEnabled = true;

    private int callCount = 0;

    private long accumulatedCallTime = 0;


    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void reset() {
        this.callCount = 0;
        this.accumulatedCallTime = 0;
    }

    public int getCallCount() {
        return callCount;
    }

    public long getCallTime() {
        return (this.callCount > 0 ? this.accumulatedCallTime / this.callCount : 0);
    }


    public Object invoke(MethodInvocation invocation) throws Throwable {
        if (this.isEnabled) {
            this.callCount++;

            StopWatch sw = new StopWatch(invocation.getMethod().getName());

            sw.start("invoke");
            Object retVal = invocation.proceed();
            sw.stop();

            this.accumulatedCallTime += sw.getTotalTimeMillis();
            return retVal;
        }

        else {
            return invocation.proceed();
        }
    }

}
