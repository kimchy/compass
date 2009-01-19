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

package org.compass.core.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

/**
 * @author kimchy
 */
public class FileHandlerMonitor {

    private File file;

    public FileHandlerMonitor(String filePath) {
        file = new File(filePath);
    }

    public FileHandlerMonitor(File file) {
        this.file = file;
    }

    public FileHandlers handlers() throws Exception {
        String osName = System.getProperty("os.name");
        if (osName.toLowerCase().startsWith("windows")) {
            throw new UnsupportedOperationException("File handlers not supported on windows");
        }
        String command = "lsof | grep " + file.getAbsolutePath();
        Process process = Runtime.getRuntime().exec(command);
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder sb = new StringBuilder();
        try {
            int val = reader.read();
            while (val != -1) {
                sb.append((char) val);
                val = reader.read();
                try {
                    process.exitValue();
                    // process died, bail
                    break;
                } catch (IllegalThreadStateException e) {
                    // all is well, process still alive
                }
            }
        } finally {
            try {
                process.getInputStream().close();
            } catch (Exception e1) {
                // do nothing
            }
            try {
                process.getOutputStream().close();
            } catch (Exception e1) {
                // do nothing
            }
            try {
                process.getErrorStream().close();
            } catch (Exception e1) {
                // do nothing
            }
            process.destroy();
        }
        return new FileHandlers(sb.toString());
    }

    public static class FileHandlers {

        private String output;

        public FileHandlers(String output) {
            this.output = output;
        }

        public boolean hasHandlers() {
            return StringUtils.hasLength(output);
        }

        public String getRawOutput() {
            return output;
        }
    }
}
