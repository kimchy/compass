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

package org.compass.spring.device;

import org.compass.core.spi.InternalCompass;
import org.compass.core.transaction.TransactionFactory;
import org.compass.core.util.Assert;
import org.compass.gps.CompassGpsException;
import org.compass.gps.CompassGpsDevice;
import org.compass.gps.device.AbstractGpsDeviceWrapper;
import org.compass.gps.spi.CompassGpsInterfaceDevice;
import org.compass.spring.transaction.SpringSyncTransactionFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * A Spring Transaction device wrapper, which starts a new transaction (with transaction propagation of
 * REQUIRES_NEW) for the device index operation.
 * <p/>
 * When using {@link SpringSyncTransactionFactory}, this gps device wrapper should be used to wrap all
 * the devices within the application. Spring <code>PlatformTransactionManager</code> can either be
 * injected, or the device wrapper will try to get it from the configured {@link org.compass.spring.LocalCompassBean}
 * that is associated with the device {@link org.compass.gps.CompassGps}.
 * <p/>
 * By default, {@link #setAllowNoTransactionManager(boolean)} is set to <code>true</code>, so changing from
 * {@link SpringSyncTransactionFactory} to {@link org.compass.core.transaction.LocalTransactionFactory} will not
 * require additional configuration changes. Changing it to <code>false</code> will mean that a transaction manager
 * must be accessible (either by setting the transcation manager, or associating one with the local compass bean).
 *
 * @author kimchy
 */
public class SpringSyncTransactionGpsDeviceWrapper extends AbstractGpsDeviceWrapper implements InitializingBean {

    private PlatformTransactionManager transactionManager;

    private boolean allowNoTransactionManager = true;

    public SpringSyncTransactionGpsDeviceWrapper() {

    }

    public SpringSyncTransactionGpsDeviceWrapper(CompassGpsDevice device) {
        setGpsDevice(device);
    }

    public void afterPropertiesSet() throws Exception {
        Assert.notNull(gpsDevice, "Must set wrapped gpsDevice");
    }

    /**
     * If a Spring <code>PlatformTransactionManager<code> is available, will use it to execute the wrapped gps device
     * index operation within a new transcation with a propagation level of REQUIRES_NEW.
     */
    public void index() throws CompassGpsException {
        if (transactionManager == null) {
            TransactionFactory transactionFactory =
                    ((InternalCompass) ((CompassGpsInterfaceDevice) gpsDevice.getGps()).getIndexCompass()).getTransactionFactory();
            if (transactionFactory instanceof SpringSyncTransactionFactory) {
                SpringSyncTransactionFactory springSyncTransactionFactory = (SpringSyncTransactionFactory) transactionFactory;
                transactionManager = springSyncTransactionFactory.getTransactionManager();
            }
        }
        if (transactionManager == null) {
            if (allowNoTransactionManager) {
                if (log.isDebugEnabled()) {
                    log.debug("No transaction manager found, will not execute the index operation within its own transaction");
                }
                gpsDevice.index();
            } else {
                throw new CompassGpsException("No transaction manager is found, and it is not allowed");
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Startin a new Spring transaction for device [" + gpsDevice.getName() + "] index operation");
            }
            TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
            transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
            transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                    gpsDevice.index();
                }
            });
        }
    }

    /**
     * Sets the Spring <code>PlatformTransactionManager</code> that will be used to start a new transaction
     * for the {@link #index()} operation. Note, this is an optioanl parameter, since if not set, Compass will
     * try and get Spring transaction manager from the associated {@link org.compass.spring.LocalCompassBean}.
     */
    public void setTransactionManager(PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    /**
     * Should the device allows for cases where no Spring transaction manager is provided (for example, when
     * using Compass {@link org.compass.core.transaction.LocalTransactionFactory} and not setting an transaction
     * manager. In such cases, no new transaction will be started.
     * <p/>
     * Defaults to <code>true</code>.
     */
    public void setAllowNoTransactionManager(boolean allowNoTransactionManager) {
        this.allowNoTransactionManager = allowNoTransactionManager;
    }
}
