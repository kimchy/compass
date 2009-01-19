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

package org.compass.sample.petclinic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.support.MutableSortDefinition;
import org.springframework.beans.support.PropertyComparator;

/**
 * Simple JavaBean business object representing a pet.
 * 
 * @author Ken Krebs
 * @author Juergen Hoeller
 */

public class Pet extends NamedEntity {

    private Date birthDate;

    private PetType type;

    private Owner owner;

    private Set visits;

    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }

    public Date getBirthDate() {
        return this.birthDate;
    }

    public void setType(PetType type) {
        this.type = type;
    }

    public PetType getType() {
        return type;
    }

    protected void setOwner(Owner owner) {
        this.owner = owner;
    }

    public Owner getOwner() {
        return owner;
    }

    protected void setVisitsInternal(Set visits) {
        this.visits = visits;
    }

    protected Set getVisitsInternal() {
        if (this.visits == null) {
            this.visits = new HashSet();
        }
        return this.visits;
    }

    public List getVisits() {
        List sortedVisits = new ArrayList(getVisitsInternal());
        PropertyComparator.sort(sortedVisits, new MutableSortDefinition("date", false, false));
        return Collections.unmodifiableList(sortedVisits);
    }

    public void addVisit(Visit visit) {
        getVisitsInternal().add(visit);
        visit.setPet(this);
    }
}
