/*
 * Copyright 2004-2006 the original author or authors.
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

package org.compass.spring.device.ojb;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.ojb.broker.PersistenceBroker;
import org.compass.gps.device.ojb.OjbGpsDeviceUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.orm.ojb.OjbFactoryUtils;
import org.springframework.orm.ojb.PersistenceBrokerTemplate;

/**
 * Uses Spring's AOP capabilities to attach and remove lifecycle event listeners
 * to the <code>PersistenceBroker</code> (the device acts as the listener).
 * Uses <code>OjbGpsDeviceUtils</code> to perform it on the supplied
 * {@link org.compass.spring.device.ojb.SpringOjbGpsDevice}.
 * <p>
 * Mainly used as a post interceptor with transaction proxies that manage
 * service layer operations on an OJB enabled DAO layer.
 * 
 * @author kimchy
 */
public class SpringOjbGpsDeviceInterceptor implements MethodInterceptor, InitializingBean {

    private SpringOjbGpsDevice ojbGpsDevice;

    public void afterPropertiesSet() throws Exception {
        if (ojbGpsDevice == null) {
            throw new IllegalArgumentException("Must set the ojbGpsDevice property");
        }
    }

    /**
     * Wraps the method invocation with attaching and removing lifecycle event
     * listeners from <code>PersistenceBroker</code>.
     */
    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        PersistenceBroker pb = OjbFactoryUtils.getPersistenceBroker(getPersistenceBrokerTemplate().getPbKey(),
                getPersistenceBrokerTemplate().isAllowCreate());
        OjbGpsDeviceUtils.attachPersistenceBrokerForMirror(ojbGpsDevice, pb);
        try {
            return methodInvocation.proceed();
        } finally {
            OjbGpsDeviceUtils.removePersistenceBrokerForMirror(ojbGpsDevice, pb);
        }
    }

    private PersistenceBrokerTemplate getPersistenceBrokerTemplate() {
        return ojbGpsDevice.getPersistenceBrokerTemplate();
    }

    /**
     * Removes the <code>SpringOjbGpsDevice</code> to be used to attach and
     * remove OJB lifecycle event listeners.
     * 
     * @return
     */
    public SpringOjbGpsDevice getOjbGpsDevice() {
        return ojbGpsDevice;
    }

    /**
     * Sets the <code>SpringOjbGpsDevice</code> to be used to attach and
     * remove OJB lifecycle event listeners.
     * 
     * @param ojbGpsDevice
     */
    public void setOjbGpsDevice(SpringOjbGpsDevice ojbGpsDevice) {
        this.ojbGpsDevice = ojbGpsDevice;
    }
}
