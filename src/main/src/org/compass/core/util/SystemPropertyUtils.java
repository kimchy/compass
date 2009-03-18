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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Helper class for resolving placeholders in texts. Usually applied to file paths.
 *
 * <p>A text may contain <code>${...}</code> placeholders, to be resolved as
 * system properties: e.g. <code>${user.dir}</code>.
 *
 * @author kimchy
 * @see #PLACEHOLDER_PREFIX
 * @see #PLACEHOLDER_SUFFIX
 * @see System#getProperty(String)
 */
public abstract class SystemPropertyUtils {

    /**
     * Prefix for system property placeholders: "${"
     */
    public static final String PLACEHOLDER_PREFIX = "${";

    /**
     * Suffix for system property placeholders: "}"
     */
    public static final String PLACEHOLDER_SUFFIX = "}";

    private static final Log logger = LogFactory.getLog(SystemPropertyUtils.class);


    /**
     * Resolve ${...} placeholders in the given text,
     * replacing them with corresponding system property values.
     *
     * @param text the String to resolve
     * @return the resolved String
     * @see #PLACEHOLDER_PREFIX
     * @see #PLACEHOLDER_SUFFIX
     */
    public static String resolvePlaceholders(String text) {
        if (text == null) {
            return null;
        }
        
        StringBuffer buf = new StringBuffer(text);

        // The following code does not use JDK 1.4's StringBuffer.indexOf(String)
        // method to retain JDK 1.3 compatibility. The slight loss in performance
        // is not really relevant, as this code will typically just run on startup.

        int startIndex = text.indexOf(PLACEHOLDER_PREFIX);
        while (startIndex != -1) {
            int endIndex = buf.toString().indexOf(PLACEHOLDER_SUFFIX, startIndex + PLACEHOLDER_PREFIX.length());
            if (endIndex != -1) {
                String placeholder = buf.substring(startIndex + PLACEHOLDER_PREFIX.length(), endIndex);
                int nextIndex = endIndex + PLACEHOLDER_SUFFIX.length();
                try {
                    String propVal = System.getProperty(placeholder);
                    if (propVal == null) {
                        // Fall back to searching the system environment.
                        propVal = System.getenv(placeholder);
                    }
                    if (propVal != null) {
                        buf.replace(startIndex, endIndex + PLACEHOLDER_SUFFIX.length(), propVal);
                        nextIndex = startIndex + propVal.length();
                    } else {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Could not resolve placeholder '" + placeholder + "' in [" + text +
                                    "] as system property: neither system property nor environment variable found");
                        }
                    }
                }
                catch (Throwable ex) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Could not resolve placeholder '" + placeholder + "' in [" + text + "] as system property: " + ex);
                    }
                }
                startIndex = buf.toString().indexOf(PLACEHOLDER_PREFIX, nextIndex);
            } else {
                startIndex = -1;
            }
        }

		return buf.toString();
	}

}
