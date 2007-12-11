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

package org.compass.core.config;

import junit.framework.TestCase;
import org.compass.annotations.config.CompassAnnotationsConfiguration;
import org.compass.core.util.JdkVersion;

/**
 * @author kimchy
 */
public class CompassConfigurationFactoryTests extends TestCase {

    public void testAnnotationsFactory() {
        CompassConfiguration conf = CompassConfigurationFactory.newConfiguration();
        if (JdkVersion.getMajorJavaVersion() >= JdkVersion.JAVA_15) {
            assertEquals(CompassAnnotationsConfiguration.class, conf.getClass());
        } else {
            assertEquals(CompassConfiguration.class, conf.getClass());
        }
    }
}
