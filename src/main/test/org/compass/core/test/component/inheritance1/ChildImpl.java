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

package org.compass.core.test.component.inheritance1;

/**
 * @author kimchy
 */
public class ChildImpl implements Child {

    private static int currentId;

    public static synchronized String nextId() {
        return String.valueOf(currentId++);
    }

    public static synchronized void resetId() {
        currentId = 0;
    }

    protected String id;
    protected String name;
    protected Father father;

    protected ChildImpl() {
    }

    protected ChildImpl(String name, Father father) {
        this.name = name;
        this.father = father;
        this.id = nextId();
    }

    public String getName() {
        return name;
    }

    public Father getFather() {
        return father;
    }
}
