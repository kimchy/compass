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

import org.compass.core.Compass;
import org.compass.core.config.CompassEnvironment;

/**
 * @author kimchy
 */
public class FileHandlerMonitor {

    private static final boolean windows;
    private static final boolean enabled;

    static {
        windows = System.getProperty("os.name").toLowerCase().startsWith("windows");
        enabled = System.getProperty("compass.test.validateFileHandler", "false").equals("true");
    }

    public static FileHandlerMonitor getFileHandlerMonitor(Compass compass) {
        String connection = compass.getSettings().getSetting(CompassEnvironment.CONNECTION);
        if (connection.startsWith("file://") || connection.indexOf("://") == -1) {
            if (connection.startsWith("file://")) {
                connection = connection.substring("file://".length());
            }
            return new FileHandlerMonitor(connection);
        }
        return new FileHandlerMonitor(null);
    }

    private File file;

    public FileHandlerMonitor(String filePath) {
        if (filePath != null) {
            file = new File(filePath);
        }
    }

    public void verifyNoHandlers() throws Exception {
        FileHandlerMonitor.FileHandlers handlers = handlers();
        if (handlers == null) {
            return;
        }
        if (handlers.hasHandlers()) {
            throw new Exception("File Handlers still exist \n" + handlers.getRawOutput());
        }
    }

    public FileHandlers handlers() throws Exception {
        if (!enabled || file == null) {
            return new FileHandlers(null);
        }
        String command;
        if (windows) {
            command = "lib/handle/handle.exe " + file.getAbsolutePath();
        } else {
            command = "lsof | grep " + file.getAbsolutePath();
        }
        Process process = Runtime.getRuntime().exec(command);
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder sb = new StringBuilder();
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            process.waitFor();
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
            if (windows) {
                return output != null && output.indexOf("pid") != -1;
            } else {
                return StringUtils.hasLength(output);
            }
        }

        public String getRawOutput() {
            return output;
        }
    }
}
