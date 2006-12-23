/*
 * Copyright 2004-2006 the original author or authors.
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

package org.compass.core.mapping;

import org.compass.core.CompassException;
import org.compass.core.util.Parameter;

/**
 * Cascade mappings responsible for getting objects for cascading
 * operations.
 *
 * @author kimchy
 */
public interface CascadeMapping {

    public static final class Cascade extends Parameter {

        private Cascade(String name) {
            super(name);
        }

        public static final Cascade DELETE = new Cascade("DELETE");

        public static final Cascade SAVE = new Cascade("SAVE");

        public static final Cascade CREATE = new Cascade("CREATE");

        public static final Cascade ALL = new Cascade("ALL");

        public static String toString(Cascade cascade) {
            if (cascade == Cascade.DELETE) {
                return "delete";
            } else if (cascade == Cascade.SAVE) {
                return "save";
            } else if (cascade == Cascade.CREATE) {
                return "create";
            } else if (cascade == Cascade.ALL) {
                return "all";
            }
            throw new IllegalArgumentException("Can't find cascade for [" + cascade + "]");
        }

        public static Cascade fromString(String cascade) {
            if ("delete".equalsIgnoreCase(cascade)) {
                return Cascade.DELETE;
            } else if ("save".equalsIgnoreCase(cascade)) {
                return Cascade.SAVE;
            } else if ("create".equalsIgnoreCase(cascade)) {
                return Cascade.CREATE;
            } else if ("all".equalsIgnoreCase(cascade)) {
                return Cascade.ALL;
            }
            throw new IllegalArgumentException("Can't find cascade for [" + cascade + "]");
        }
    }

    Object getCascadeValue(Object root) throws CompassException;

    void setCascades(Cascade[] cascades);

    Cascade[] getCascades();

    boolean shouldCascadeDelete();

    boolean shouldCascadeCreate();

    boolean shouldCascadeSave();

    boolean shouldCascade(Cascade cascade);
}
