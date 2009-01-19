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

package org.compass.core.test.poly;

/**
 * @author kimchy
 */
public class Component {

    private Long id;

    private PolyInterface pi1;

    private PolyInterface pi2;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PolyInterface getPi1() {
        return pi1;
    }

    public void setPi1(PolyInterface pi1) {
        this.pi1 = pi1;
    }

    public PolyInterface getPi2() {
        return pi2;
    }

    public void setPi2(PolyInterface pi2) {
        this.pi2 = pi2;
    }
}
