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

package org.compass.core.util.proxy.extractor;

import org.compass.core.CompassException;
import org.compass.core.config.CompassSettings;
import org.hibernate.Hibernate;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.HibernateProxyHelper;

/**
 * Uses Hibernate {@link org.hibernate.proxy.HibernateProxyHelper#getClassWithoutInitializingProxy(Object)}
 * in order to get a class wrapped by a {@link org.hibernate.proxy.HibernateProxy}.
 *
 * @author kimchy
 */
public class HibernateProxyExtractor implements ProxyExtractor {

    private boolean initializePorxy;

    public void configure(CompassSettings settings) throws CompassException {
        initializePorxy = settings.getSettingAsBoolean("compass.marshalling.hibernate.initializeProxy", true);
    }

    public Class getTargetClass(Object obj) {
        if (obj instanceof HibernateProxy) {
            return HibernateProxyHelper.getClassWithoutInitializingProxy(obj);
        }
        return obj.getClass();
    }

    public Object initalizeProxy(Object obj) {
        if (initializePorxy) {
            Hibernate.initialize(obj);
            if (obj instanceof HibernateProxy) {
                // further initialize the object since initlaize does not init lazy fields
                return ((HibernateProxy) obj).getHibernateLazyInitializer().getImplementation();
            }
        }
        return obj;
    }
}
