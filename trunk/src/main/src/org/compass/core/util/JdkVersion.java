/*
 * Copyright 2002-2006 the original author or authors.
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

package org.compass.core.util;

/**
 * Helper class used to find the current Java/JDK version.
 * Usually we want to find if we're in a 1.4 or higher JVM.
 * (Spring does not support 1.2 JVMs.)
 *
 * @author kimchy
 */
public class JdkVersion {

    public static final int JAVA_13 = 0;
    public static final int JAVA_14 = 1;
    public static final int JAVA_15 = 2;
    public static final int JAVA_16 = 3;
    public static final int JAVA_17 = 4;


    private static final String javaVersion;

    private static final int majorJavaVersion;

    static {
        javaVersion = System.getProperty("java.version");
        // Version String should look like "1.4.1_02"
        if (javaVersion.indexOf("1.7.") != -1) {
            majorJavaVersion = JAVA_17;
        } else if (javaVersion.indexOf("1.6.") != -1) {
            majorJavaVersion = JAVA_16;
        } else if (javaVersion.indexOf("1.5.") != -1) {
            majorJavaVersion = JAVA_15;
        } else if (javaVersion.indexOf("1.4.") != -1) {
            majorJavaVersion = JAVA_14;
        } else {
            // Else leave 1.3 as default (it's either 1.3 or unknown).
            majorJavaVersion = JAVA_13;
        }
    }


    /**
     * Return the full Java version string, as returned by
     * <code>System.getProperty("java.version")</code>.
     */
    public static String getJavaVersion() {
        return javaVersion;
    }

    /**
     * Get the major version code. This means we can do things like
     * <code>if (getMajorJavaVersion() < JAVA_14)</code>.
     *
     * @return a code comparable to the JAVA_XX codes in this class
     * @see #JAVA_13
     * @see #JAVA_14
     * @see #JAVA_15
     */
    public static int getMajorJavaVersion() {
        return majorJavaVersion;
    }

}
