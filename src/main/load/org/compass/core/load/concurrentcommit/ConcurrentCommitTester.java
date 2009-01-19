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

package org.compass.core.load.concurrentcommit;

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
public class ConcurrentCommitTester {

    private static final String[] datas = new String[]{"The big brown fox", "The hitchiker guide to the galaxy",
            "White russian", "The player of Games",
            "But it's so simple. All I have to do is divine from what I know" +
                    "of you: are you the sort of man who would put the poison into his" +
                    "own goblet or his enemy's? Now, a clever man would put the poison" +
                    "into his own goblet, because he would know that only a great fool" +
                    "would reach for what he was given. I am not a great fool, so I" +
                    "can clearly not choose the wine in front of you. But you must" +
                    "have known I was not a great fool, you would have counted on it," +
                    "so I can clearly not choose the wine in front of me.",
            "I am the law, now give me my white russian"};

    public static void main(String[] args) throws Exception {
        CompassConfiguration conf = new CompassConfiguration();
        conf.configure("/org/compass/core/load/concurrentcommit/compass.cfg.xml");
        File testPropsFile = new File("compass.test.properties");
        if (testPropsFile.exists()) {
            Properties testProps = new Properties();
            testProps.load(new FileInputStream(testPropsFile));
            conf.getSettings().addSettings(testProps);
        }
        conf.addResource("org/compass/core/load/concurrentcommit/A.cpm.xml");
        Compass compass = conf.buildCompass();
        compass.getSearchEngineIndexManager().deleteIndex();
        compass.getSearchEngineIndexManager().createIndex();

        System.out.println("Starting Test");
        long time = System.currentTimeMillis();
        for (long i = 0; i < 200D; i++) {
            System.out.println("Cycle [" + i + "]");
            A a = new A();
            a.id = i;
            a.value1 = datas[(int) (i % datas.length)];
            a.value2 = datas[(int) ((i + 1) % datas.length)];
            a.indexTime = new Date();

            CompassSession session = compass.openSession();
            CompassTransaction tr = session.beginTransaction();

            session.save("a1", a);
            session.save("a2", a);
            session.save("a3", a);
            session.save("a4", a);
            session.save("a5", a);

            tr.commit();
            session.close();

            if ((i % 10) == 0) {
                System.out.println("Optimizing [" + i + "]");
                compass.getSearchEngineOptimizer().optimize();
            }
        }

        System.out.println("Test Finished in [" + (System.currentTimeMillis() - time) + "]");

        compass.close();
    }
}
