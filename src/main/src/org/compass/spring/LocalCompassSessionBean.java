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

package org.compass.spring;

import org.compass.core.Compass;
import org.compass.core.CompassSession;
import org.compass.core.spi.InternalCompassSession;
import org.compass.core.support.session.CompassSessionTransactionalProxy;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * A factory for {@link org.compass.core.CompassSession} that creates a special proxied
 * session which can be used within code that runs within an external transaction (Spring, JTA).
 * The session can be used without using Compass transaction management code or even without
 * closing the session.
 * <p/>
 * It is optional to set the {@link org.compass.core.Compass} instance that will be used to create
 * the transactional Compass session. If there is a single Compass instance defined within the spring
 * application context, it will be automatically used.
 *
 * @author kimchy
 */
public class LocalCompassSessionBean implements FactoryBean, InitializingBean, ApplicationContextAware {

    private ApplicationContext applicationContext;

    private Compass compass;

    /**
     * Transactional aware proxied Compass session
     */
    private CompassSession compassSession;

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * If no Compass instance was set, will try to use Spring application context
     * to get a Compass instance.
     *
     * @throws Exception
     */
    public void afterPropertiesSet() throws Exception {
        if (compass == null) {
            String[] compassesNames = this.applicationContext.getBeanNamesForType(Compass.class);
            if (compassesNames.length == 1) {
                compass = (Compass) this.applicationContext.getBean(compassesNames[0]);
            } else {
                throw new IllegalArgumentException("compass instance not set and application context has more than one compass instance");
            }
        }
        compassSession = CompassSessionTransactionalProxy.newProxy(compass);
    }

    /**
     * Sets the Compass instance used to open session. It is optional
     * to set the Compass instance, since if there is a single instance
     * defined within the application context, it will be automatically used.
     *
     * @param compass The Compass instance to use for openning the session
     */
    public void setCompass(Compass compass) {
        this.compass = compass;
    }

    public Object getObject() throws Exception {
        return compassSession;
    }

    public Class getObjectType() {
        return InternalCompassSession.class;
    }

    public boolean isSingleton() {
        return true;
    }
}
