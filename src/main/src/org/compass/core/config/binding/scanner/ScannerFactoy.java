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

package org.compass.core.config.binding.scanner;

import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * @author kimchy
 */
public class ScannerFactoy {

    public static Scanner create(String basePackage, URL url, Filter filter) throws IOException {
        String urlString = url.toString();
        urlString = urlString.substring(0, urlString.lastIndexOf(basePackage));
        if (urlString.endsWith("!/")) {
            urlString = urlString.substring(4);
            urlString = urlString.substring(0, urlString.length() - 2);
            url = new URL(urlString);
        }


        if (!urlString.endsWith("/")) {
            return new JarScanner(basePackage, url.openStream(), filter);
        } else if (url.getProtocol().equals("file")) {
            File f = new File(url.getPath());
            if (f.isDirectory()) {
                return new FileScanner(basePackage, f, filter);
            } else {
                return new JarScanner(basePackage, url.openStream(), filter);
            }
        } else {
            throw new IOException("Protocol [" + url.getProtocol() + "] is not supported by scanner");
        }
    }
}
