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

package org.compass.core.load.simple;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import org.compass.core.Compass;
import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.config.CompassConfiguration;

/**
 * @author kimchy
 */
public class ClientLoadTester {

    public static void main(String[] args) throws Exception {
        CompassConfiguration conf = new CompassConfiguration();
        conf.configure("/org/compass/core/load/simple/compass.cfg.xml");
        File testPropsFile = new File("compass.test.properties");
        if (testPropsFile.exists()) {
            Properties testProps = new Properties();
            testProps.load(new FileInputStream(testPropsFile));
            conf.getSettings().addSettings(testProps);
        }
        conf.addClass(A.class);
        Compass compass = conf.buildCompass();

        while (true) {
            CompassSession session = compass.openSession();
            CompassTransaction tr = session.beginTransaction();

            System.out.println("COUNT: " + session.queryBuilder().matchAll().count());

            tr.commit();
            session.close();
        }
    }
}
