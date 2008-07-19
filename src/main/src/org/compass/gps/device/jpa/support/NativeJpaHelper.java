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

package org.compass.gps.device.jpa.support;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.EntityManagerFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.compass.core.util.ClassUtils;
import org.compass.gps.device.jpa.JpaGpsDeviceException;
import org.compass.gps.device.jpa.NativeJpaExtractor;
import org.compass.jboss.device.jpa.JBossNativeHibernateJpaExtractor;
import org.compass.spring.device.jpa.SpringNativeJpaExtractor;

/**
 * @author kimchy
 */
public abstract class NativeJpaHelper {

    private static final Log log = LogFactory.getLog(NativeJpaHelper.class);

    private static final NativeJpaExtractor[] extractors;

    static {
        ArrayList<NativeJpaExtractor> extractorsList = new ArrayList<NativeJpaExtractor>();
        try {
            extractorsList.add(new SpringNativeJpaExtractor());
        } catch (Throwable t) {
            // not in classpath
        }
        try {
            extractorsList.add(new JBossNativeHibernateJpaExtractor());
        } catch (Throwable t) {
            // not in classpath
        }
        extractors = extractorsList.toArray(new NativeJpaExtractor[extractorsList.size()]);
        if (log.isDebugEnabled()) {
            log.debug("Using native JPA extractors " + Arrays.toString(extractors));
        }
    }

    public static interface NativeJpaCallback<T> {

        T onHibernate();

        T onTopLinkEssentials();

        T onOpenJPA();

        T onEclipseLink();

        T onUnknown();
    }

    public static <T> T detectNativeJpa(EntityManagerFactory emf, NativeJpaCallback<T> callback) throws JpaGpsDeviceException {
        EntityManagerFactory nativeEmf = extractNativeJpa(emf);

        Set interfaces = ClassUtils.getAllInterfacesAsSet(nativeEmf);
        Set<String> interfacesAsStrings = new HashSet<String>();
        for (Object anInterface : interfaces) {
            interfacesAsStrings.add(((Class) anInterface).getName());
        }
        interfacesAsStrings.add(nativeEmf.getClass().getName());

        T retVal;
        if (interfacesAsStrings.contains("org.hibernate.ejb.HibernateEntityManagerFactory")) {
            retVal = callback.onHibernate();
        } else
        if (interfacesAsStrings.contains("oracle.toplink.essentials.internal.ejb.cmp3.EntityManagerFactoryImpl")) {
            retVal = callback.onTopLinkEssentials();
        } else if (interfacesAsStrings.contains("org.eclipse.persistence.internal.jpa.EntityManagerFactoryImpl")) {
            retVal = callback.onEclipseLink();
        } else if (interfacesAsStrings.contains("org.apache.openjpa.persistence.OpenJPAEntityManagerFactory")) {
            retVal = callback.onOpenJPA();
        } else {
            retVal = callback.onUnknown();
        }
        return retVal;
    }

    public static EntityManagerFactory extractNativeJpa(EntityManagerFactory emf) {
        if (extractors.length == 0) {
            return emf;
        }
        EntityManagerFactory nativeEmf = emf;
        do {
            emf = nativeEmf;
            for (NativeJpaExtractor extractor : extractors) {
                nativeEmf = extractor.extractNative(nativeEmf);
            }
        } while (nativeEmf != emf);

        return nativeEmf;
    }
}
