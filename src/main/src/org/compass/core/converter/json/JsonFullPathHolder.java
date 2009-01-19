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

package org.compass.core.converter.json;

import java.util.LinkedList;

/**
 * @author kimchy
 */
public class JsonFullPathHolder {

    public static final String CONTEXT_KEY = "$jsonFullPathHolder";

    private LinkedList<String> paths = new LinkedList<String>();

    private StringBuilder sb = new StringBuilder();

    public void addPath(String path) {
        paths.add(path);
    }

    public void removePath() {
        paths.removeLast();
    }

    public String calculatePath() {
        sb.setLength(0);
        int i = 0;
        for (String path : paths) {
            sb.append(path);
            if (++i < paths.size()) {
                sb.append('.');
            }
        }
        return sb.toString();
    }
}
