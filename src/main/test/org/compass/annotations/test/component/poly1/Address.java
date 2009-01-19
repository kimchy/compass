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

package org.compass.annotations.test.component.poly1;

import java.util.List;

import org.compass.annotations.Searchable;
import org.compass.annotations.SearchableComponent;
import org.compass.annotations.SearchableId;
import org.compass.annotations.SearchableProperty;

@Searchable
public class Address {

    Long id;

    String name;

    List<Assignment> assignment;

    @SearchableProperty
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @SearchableComponent
    public List<Assignment> getAssignment() {
        return assignment;
    }

    public void setAssignment(List<Assignment> assignment) {
        this.assignment = assignment;
    }

    @SearchableId
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}