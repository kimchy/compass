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

package org.compass.core.load.translog;

import java.io.File;
import java.io.FileInputStream;
import java.util.Date;
import java.util.Properties;

import org.compass.core.Compass;
import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.config.CompassConfiguration;

/**
 * @author kimchy
 */
public class TransLogTester {

    public static void main(String[] args) throws Exception {
        int numberOfInstances = 100000;
        if (args.length > 0) {
            numberOfInstances = Integer.parseInt(args[0]);
        }

        CompassConfiguration conf = new CompassConfiguration();
        conf.configure("/org/compass/core/load/translog/compass.cfg.xml");
        File testPropsFile = new File("compass.test.properties");
        if (testPropsFile.exists()) {
            Properties testProps = new Properties();
            testProps.load(new FileInputStream(testPropsFile));
            conf.getSettings().addSettings(testProps);
        }
        conf.addClass(A.class);
        Compass compass = conf.buildCompass();
        compass.getSearchEngineIndexManager().deleteIndex();
        compass.getSearchEngineIndexManager().verifyIndex();

        long time = System.currentTimeMillis();
        CompassSession session = compass.openSession();
//        session.getSettings().setBooleanSetting(LuceneEnvironment.Transaction.Processor.Lucene.CONCURRENT_OPERATIONS, true);
//        session.getSettings().setBooleanSetting(LuceneEnvironment.Transaction.Processor.ReadCommitted.CONCURRENT_OPERATIONS, true);
        CompassTransaction tr = session.beginTransaction();
        for (int i = 0; i < numberOfInstances; i++) {
            if (i % 1000 == 0) {
                System.out.println("Indexed [" + i + "] instances");
            }
            A a = new A();
            a.setId(new Long(i));
            a.setData1("test data 1");
            a.setData2("test data 2");
            a.setIndexTime(new Date());
            session.save(a);
        }
        System.out.println("Committing Transaction");
        tr.commit();
        session.close();
        System.out.println("Indexing Complete, took [" + (System.currentTimeMillis() - time) + "ms]");
    }
}
