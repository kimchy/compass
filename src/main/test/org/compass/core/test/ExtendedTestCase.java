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

package org.compass.core.test;

import java.lang.reflect.Method;
import java.util.Vector;

import junit.framework.Test;
import junit.framework.TestCase;

/**
 * A simple extension to Junit <code>TestCase</code> allowing for
 * {@link #beforeTestCase()} and {@link #afterTestCase()} callbacks.
 * <p/>
 * Note, the callbacks will only work if running the whole test case
 * and not just one test.
 *
 * @author kimchy
 */
public abstract class ExtendedTestCase extends TestCase {

    private static int testCount = 0;

    private static int totalTestCount = -1;

    private static boolean disableAfterTestCase = false;

    protected ExtendedTestCase() {
        super();
    }

    protected ExtendedTestCase(String name) {
        super(name);
    }

    public void runBare() throws Throwable {
        Throwable exception = null;
        if (totalTestCount == -1) {
            totalTestCount = countTotalTests();
        }
        if (testCount == 0) {
            beforeTestCase();
        }
        testCount++;
        try {
            super.runBare();
        } catch (Throwable running) {
            exception = running;
        }
        if (testCount == totalTestCount) {
            totalTestCount = -1;
            testCount = 0;
            if (!disableAfterTestCase) {
                try {
                    afterTestCase();
                } catch (Exception afterTestCase) {
                    if (exception == null) exception = afterTestCase;
                }
            } else {
                disableAfterTestCase = false;
            }
        }
        if (exception != null) throw exception;
    }

    protected static void disableAfterTestCase() {
        disableAfterTestCase = true;
    }

    /**
     * Called before any tests within this test case.
     *
     * @throws Exception
     */
    protected void beforeTestCase() throws Exception {

    }

    /**
     * Called after all the tests within the test case
     * have executed.
     *
     * @throws Exception
     */
    protected void afterTestCase() throws Exception {

    }

    private int countTotalTests() {
        int count = 0;
        Class superClass = getClass();
        Vector names = new Vector();
        while (Test.class.isAssignableFrom(superClass)) {
            Method[] methods = superClass.getDeclaredMethods();
            for (int i = 0; i < methods.length; i++) {
                Method method = methods[i];
                String name = method.getName();
                if (names.contains(name))
                    continue;
                names.addElement(name);
                if (isTestMethod(method)) {
                    count++;
                }
            }
            superClass = superClass.getSuperclass();
        }
        return count;
    }

    private boolean isTestMethod(Method m) {
        String name = m.getName();
        Class[] parameters = m.getParameterTypes();
        Class returnType = m.getReturnType();
        return parameters.length == 0 && name.startsWith("test") && returnType.equals(Void.TYPE);
    }

}
