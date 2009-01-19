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

package org.compass.gps.device.jpa.toplink;

import java.util.HashMap;
import javax.persistence.EntityManagerFactory;

import oracle.toplink.essentials.PersistenceProvider;
import org.compass.gps.device.jpa.AbstractSimpleJpaGpsDeviceTests;
import org.compass.gps.device.jpa.JpaGpsDevice;

/**
 * @author kimchy
 */
public class TopLinkEssentialsSimpleJpaGpsDeviceTests extends AbstractSimpleJpaGpsDeviceTests {

    @Override
    protected void addDeviceSettings(JpaGpsDevice device) {
        device.setInjectEntityLifecycleListener(true);
    }

    protected EntityManagerFactory doSetUpEntityManagerFactory() {
        return new PersistenceProvider().createEntityManagerFactory("toplink", new HashMap());
    }
}
