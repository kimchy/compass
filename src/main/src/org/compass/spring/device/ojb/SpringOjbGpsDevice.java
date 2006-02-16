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

import org.apache.ojb.broker.PersistenceBroker;
import org.compass.gps.CompassGpsException;
import org.compass.gps.device.ojb.OjbGpsDevice;
import org.springframework.orm.ojb.OjbFactoryUtils;
import org.springframework.orm.ojb.PersistenceBrokerTemplate;

/**
 * An extension of the <code>OjbGpsDevice</code> that utilizes Spring ojb
 * features. Uses Spring <code>PersistenceBrokerTemplate</code> and
 * <code>OjbFactoryUtils</code> to get the current
 * <code>PersistenceBroker</code> for batch indexing (the <code>index()</code>
 * operation).
 * <p>
 * You can provide the <code>PersistenceBrokerTemplate</code>, though it is
 * not required since it is created the same way the
 * <code>PersistenceBrokerDaoSupport</code> does.
 * <p>
 * Can be used with
 * {@link org.compass.spring.device.ojb.SpringOjbGpsDeviceInterceptor} to
 * provide real-time data mirroring without the need to write any code.
 * 
 * @author kimchy
 * 
 */
public class SpringOjbGpsDevice extends OjbGpsDevice {

    private PersistenceBrokerTemplate persistenceBrokerTemplate = createPersistenceBrokerTemplate();

    protected PersistenceBrokerTemplate createPersistenceBrokerTemplate() {
        return new PersistenceBrokerTemplate();
    }
    
    /**
     * Uses Spring
     * <code>PersistenceBrokerTemplate<code> and <code>OjbFactoryUtils</code>
     *  to get OJB <code>PersistenceBroker</code>
     */
    protected PersistenceBroker doGetIndexPersistentBroker() throws CompassGpsException {
        return OjbFactoryUtils.getPersistenceBroker(persistenceBrokerTemplate.getPbKey(), persistenceBrokerTemplate
                .isAllowCreate());
    }

    /**
     * Retusn the Spring's <code>PersistenceBrokerTemplate<code>.
     * @return
     */
    public PersistenceBrokerTemplate getPersistenceBrokerTemplate() {
        return persistenceBrokerTemplate;
    }

    /**
     * Sets Spring's
     * <code>PersistenceBrokerTemplate<code> to be used to fetch OJB 
     * <code>PersistenceBroker</code> for batch indexing (the <code>index()</code> operation).
     * <p>
     * This is an optional parameter, since the <code>PersistenceBrokerTemplate</code> can be automatically created.
     * @param persistenceBrokerTemplate
     */
    public void setPersistenceBrokerTemplate(PersistenceBrokerTemplate persistenceBrokerTemplate) {
        this.persistenceBrokerTemplate = persistenceBrokerTemplate;
    }
}
