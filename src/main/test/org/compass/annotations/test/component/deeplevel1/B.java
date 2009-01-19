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

import org.compass.annotations.Searchable;
import org.compass.annotations.SearchableComponent;

/**
 * @author kimchy
 */
@Searchable(root = false)
public class B {

    private C c;
    private D d;

    protected B() {
    }

    public B(C c, D d) {
        this.c = c;
        this.d = d;
    }

    @SearchableComponent
    public C getC() {
        return c;
    }

    public void setC(C complianceExemption) {
        this.c = complianceExemption;
    }

    @SearchableComponent
    public D getD() {
        return d;
    }

    public void setD(D compliancePolicy) {
        this.d = compliancePolicy;
    }

    @Override
    public int hashCode() {
        final int PRIME = 31;
        int result = super.hashCode();
        result = PRIME * result + ((d == null) ? 0 : d.hashCode());
        result = PRIME * result + ((c == null) ? 0 : c.hashCode());
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
        final B other = (B) obj;
        if (d == null) {
            if (other.d != null)
                return false;
        } else if (!d.equals(other.d))
            return false;
        if (c == null) {
            if (other.c != null)
                return false;
        } else if (!c.equals(other.c))
            return false;
        return true;
    }
}