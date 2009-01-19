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

package org.compass.core.test.component.cyclic4;

import java.util.ArrayList;
import java.util.List;

/**
 * @author kimchy
 */
public class Father {

    Long id;

    Child child1;

    Child child2;

    List children = new ArrayList();

    protected Father() {
    }

    public Father(long id) {
        this(id, null, null);
    }

    public Father(long id, Child child1, Child child2) {
        this.id = new Long(id);
        this.child1 = child1;
        this.child2 = child2;
    }
}
