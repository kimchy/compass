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

import java.util.ArrayList;

import org.compass.core.CompassException;
import org.compass.core.config.CompassConfigurable;
import org.compass.core.config.CompassSettings;
import org.compass.core.util.ClassUtils;

/**
 * A helper class that based on which jars exists in the classpath, tries to get the actual
 * class out of an object.
 *
 * <p>Order in which libraries are used: {@link org.compass.core.util.proxy.extractor.SpringProxyExtractor},
 * {@link org.compass.core.util.proxy.extractor.HibernateProxyExtractor}.
 *
 * @author kimchy
 */
public class ProxyExtractorHelper implements CompassConfigurable {

    private ProxyExtractor[] extractors;

    public void configure(CompassSettings settings) throws CompassException {
        ArrayList<ProxyExtractor> extractorsList = new ArrayList<ProxyExtractor>();
        try {
            ClassUtils.forName("org.springframework.aop.support.AopUtils", settings.getClassLoader());
            ProxyExtractor extractor = new SpringProxyExtractor();
            extractor.configure(settings);
            extractorsList.add(extractor);
        } catch (Throwable e) {
            // not in the classpath
        }
        try {
            ClassUtils.forName("org.hibernate.proxy.HibernateProxyHelper", settings.getClassLoader());
            ProxyExtractor extractor = new HibernateProxyExtractor();
            extractor.configure(settings);
            extractorsList.add(extractor);
        } catch (Throwable e) {
            // not in the classpath
        }
        // TODO allow for pluing proxy extractor
        extractors = extractorsList.toArray(new ProxyExtractor[extractorsList.size()]);
    }

    public Class getTargetClass(Object obj) {
        Class objClass = obj.getClass();
        for (ProxyExtractor extractor : extractors) {
            Class clazz = extractor.getTargetClass(obj);
            if (!clazz.equals(objClass)) {
                return clazz;
            }
        }
        return objClass;
    }

    public Object initializeProxy(Object obj) {
        for (ProxyExtractor extractor : extractors) {
            obj = extractor.initalizeProxy(obj);
        }
        return obj;
    }
}
