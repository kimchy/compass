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

package org.compass.annotations.test.component.deeplevel1;

import java.util.List;

import org.compass.annotations.Searchable;
import org.compass.annotations.SearchableComponent;
import org.compass.annotations.SearchableId;

@Searchable
public class A {
    private Integer id;
    private List<B> bs;

    @SearchableId
    public Integer getId() {
        return id;
    }

    public void setId(Integer ssId) {
        this.id = ssId;
    }

    @SearchableComponent
    public List<B> getBs() {
        return bs;
    }

    public void setBs(List<B> complianceChecks) {
        this.bs = complianceChecks;
    }

    @Override
    public int hashCode() {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final A other = (A) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }
}