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
import java.io.IOException;
import java.io.PrintStream;
import java.util.Date;
import java.util.Properties;

import org.compass.core.Compass;
import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.Resource;
import org.compass.core.config.CompassConfiguration;
import org.compass.core.lucene.util.LuceneHelper;

/**
 * @author kimchy
 */
public class LoadTester {

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

    private long numberPerCycle = 10;

    private int numberOfCycles = 200;

    public void runTests(PrintStream printer) throws IOException {
        printer.println("Run: Number Of Cycles[" + numberOfCycles + "] Number Per Cycle [" + numberPerCycle + "]");
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
        runTest("Default", compass, printer);
        compass.close();
    }

    public void runTest(String runName, Compass compass, PrintStream writer) throws IOException {
        compass.getSearchEngineIndexManager().deleteIndex();
        compass.getSearchEngineIndexManager().verifyIndex();
        writer.println(runName);
        writer.println("Cycle\tTotal\tSave\tFind\tCommit\tFind\tFind\tLoad\tLoad\tLoadR\tTermInfo\tOptimize");
        long temp;
        long totalCycleTime[] = new long[numberOfCycles];
        long saveTime[] = new long[numberOfCycles];
        long commitTime[] = new long[numberOfCycles];
        long findBeforeCommit[] = new long[numberOfCycles];
        long findAfterCommit[] = new long[numberOfCycles];
        long findAfterCommit2[] = new long[numberOfCycles];
        long load[] = new long[numberOfCycles];
        long load2[] = new long[numberOfCycles];
        long loadR[] = new long[numberOfCycles];
        long termInfo[] = new long[numberOfCycles];
        long optimize[] = new long[numberOfCycles];
        for (int cycle = 0; cycle < numberOfCycles; cycle++) {
            CompassSession session = compass.openSession();
            CompassTransaction tr = session.beginTransaction();
            long cycleStartTime = System.currentTimeMillis();
            for (long i = 0; i < numberPerCycle; i++) {
                A a = new A();
                long id = cycle * numberPerCycle + i;
                a.setId(new Long(id));
                a.setData1(datas[(int) (id % datas.length)]);
                a.setData2(datas[(int) ((id + 1) % datas.length)]);
                a.setIndexTime(new Date());

                temp = System.currentTimeMillis();
                session.save("a1", a);
                session.save("a2", a);
                saveTime[cycle] += System.currentTimeMillis() - temp;
            }

            temp = System.currentTimeMillis();
            try {
                session.find("white");
                findBeforeCommit[cycle] = System.currentTimeMillis() - temp;
            } catch (Exception e) {
                findBeforeCommit[cycle] = -1;
            }

            temp = System.currentTimeMillis();
            tr.commit();
            commitTime[cycle] = System.currentTimeMillis() - temp;

            tr = session.beginTransaction();
            temp = System.currentTimeMillis();
            try {
                session.find("white");
                findAfterCommit[cycle] = System.currentTimeMillis() - temp;
            } catch (Exception e) {
                findAfterCommit[cycle] = -1;
            }
            tr.commit();

            tr = session.beginTransaction();
            temp = System.currentTimeMillis();
            try {
                session.find("white");
                findAfterCommit2[cycle] = System.currentTimeMillis() - temp;
            } catch (Exception e) {
                findAfterCommit2[cycle] = -1;
            }
            tr.commit();

            tr = session.beginTransaction();
            temp = System.currentTimeMillis();
            try {
                session.load("a1", new Long(1));
                load[cycle] = System.currentTimeMillis() - temp;
            } catch (Exception e) {
                load[cycle] = -1;
            }
            tr.commit();

            tr = session.beginTransaction();
            temp = System.currentTimeMillis();
            try {
                session.load("a1", new Long(1));
                load2[cycle] = System.currentTimeMillis() - temp;
            } catch (Exception e) {
                load2[cycle] = -1;
            }
            tr.commit();

            tr = session.beginTransaction();
            temp = System.currentTimeMillis();
            try {
                session.loadResource("a1", new Long(1));
                loadR[cycle] = System.currentTimeMillis() - temp;
            } catch (Exception e) {
                loadR[cycle] = -1;
            }
            tr.commit();

            tr = session.beginTransaction();
            temp = System.currentTimeMillis();
            try {
                Resource r = session.loadResource("a1", new Long(1));
                LuceneHelper.getTermFreqVectors(session, r);
                termInfo[cycle] = System.currentTimeMillis() - temp;
            } catch (Exception e) {
                termInfo[cycle] = -1;
            }
            tr.commit();

            temp = System.currentTimeMillis();
            compass.getSearchEngineOptimizer().optimize();
            optimize[cycle] = System.currentTimeMillis() - temp;

            long cycleEndTime = System.currentTimeMillis();
            totalCycleTime[cycle] = (cycleEndTime - cycleStartTime);
            writer.print("" + cycle + "\t" + totalCycleTime[cycle]);
            writer.print("\t" + saveTime[cycle]);
            writer.print("\t" + findBeforeCommit[cycle]);
            writer.print("\t" + commitTime[cycle]);
            writer.print("\t" + findAfterCommit[cycle]);
            writer.print("\t" + findAfterCommit2[cycle]);
            writer.print("\t" + load[cycle]);
            writer.print("\t" + load2[cycle]);
            writer.print("\t" + loadR[cycle]);
            writer.print("\t" + termInfo[cycle]);
            writer.print("\t" + optimize[cycle]);
            writer.println();
            writer.flush();
        }
        writer.println("Cycle\tTotal\tSave\tFind\tCommit\tFind\tFind\tLoad\tLoad\tLoadR\tTermInfo\tOptimize");
        writer.print("AVG\t" + average(totalCycleTime));
        writer.print("\t" + average(saveTime));
        writer.print("\t" + average(findBeforeCommit));
        writer.print("\t" + average(commitTime));
        writer.print("\t" + average(findAfterCommit));
        writer.print("\t" + average(findAfterCommit2));
        writer.print("\t" + average(load));
        writer.print("\t" + average(load2));
        writer.print("\t" + average(loadR));
        writer.print("\t" + average(termInfo));
        writer.print("\t" + average(optimize));
        writer.println();
        writer.flush();
    }

    private long average(long[] values) {
        long count = 0;
        for (int i = 0; i < values.length; i++) {
            count += values[i];
        }
        return (long) (((float) count) / values.length);
    }

    public int getNumberOfCycles() {
        return numberOfCycles;
    }

    public void setNumberOfCycles(int numberOfCycles) {
        this.numberOfCycles = numberOfCycles;
    }

    public long getNumberPerCycle() {
        return numberPerCycle;
    }

    public void setNumberPerCycle(long numberPerCycle) {
        this.numberPerCycle = numberPerCycle;
    }

    public static void main(String[] args) throws IOException {
        LoadTester loadTester = new LoadTester();
        if (args.length > 0) {
            loadTester.setNumberOfCycles(Integer.parseInt(args[0]));
        }
        if (args.length > 1) {
            loadTester.setNumberPerCycle(Long.parseLong(args[1]));
        }
        loadTester.runTests(System.out);
    }
}
