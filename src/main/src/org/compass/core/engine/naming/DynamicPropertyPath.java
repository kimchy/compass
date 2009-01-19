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

package org.compass.core.engine.naming;

import java.util.Arrays;

import org.compass.core.util.StringUtils;

/**
 * A dynamic path implementations. Holds the path elements in an array,
 * and constrcut it each time {@link #getPath()} is called.
 * <p/>
 * Benefits of using this implementation is its low memory footprint,
 * while extra processing is made for runtime path construction
 * (mainly during marshalling/unmarshalling operations)
 * <p/>
 * {@link #hintStatic()} uses the same implementation, and retains its dynamic nature.
 *
 * @author kimchy
 * @author lexi
 * @see DynamicPropertyNamingStrategy
 */
public class DynamicPropertyPath implements PropertyPath {

    private final String[] steps;

    private static final char delimiter = '/';

    private int hash;

    public DynamicPropertyPath(String step) {
        this.steps = new String[]{step.intern()};
    }

    public DynamicPropertyPath(String[] steps) {
        this.steps = steps;
    }

    public DynamicPropertyPath(PropertyPath root, String name) {
        if (root instanceof DynamicPropertyPath) {
            String[] rootSteps = ((DynamicPropertyPath) root).steps;
            steps = new String[rootSteps.length + 1];
            System.arraycopy(rootSteps, 0, steps, 0, rootSteps.length);
            steps[rootSteps.length] = name.intern();
        } else {
            steps = new String[2];
            steps[0] = root.getPath();
            steps[1] = name.intern();
        }
    }

    public String getPath() {
        return StringUtils.arrayToDelimitedString(steps, delimiter);
    }

    public PropertyPath hintStatic() {
        return this;
    }

    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }
        if ((null == obj) || (!(obj instanceof PropertyPath))) {
            return false;
        }

        if (obj instanceof DynamicPropertyPath) {
            final DynamicPropertyPath path = (DynamicPropertyPath) obj;
            return Arrays.equals(steps, path.steps);
        } else {
            final PropertyPath path = (PropertyPath) obj;
            return getPath().equals(path.getPath());
        }
    }

    public int hashCode() {
        int h = hash;
        if (h == 0) {
            for (int index = 0; index < steps.length; index++) {
                if (index > 0)
                    h = h * 31 + delimiter;
                h = hashCode(h, steps[index]);
            }
            hash = h;
        }
        return h;
    }

    private int hashCode(final int hash, String string) {
        int h = hash;
        for (int i = 0; i < string.length(); i++) {
            h = h * 31 + string.charAt(i);
        }
        return h;

    }
}