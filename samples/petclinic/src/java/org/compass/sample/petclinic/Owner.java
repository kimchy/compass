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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.springframework.beans.support.MutableSortDefinition;
import org.springframework.beans.support.PropertyComparator;

/**
 * Simple JavaBean domain object representing an owner.
 * 
 * @author Ken Krebs
 * @author Juergen Hoeller
 */

public class Owner extends Person {

    private Set pets;

    protected void setPetsInternal(Set pets) {
        this.pets = pets;
    }

    protected Set getPetsInternal() {
        if (this.pets == null) {
            this.pets = new HashSet();
        }
        return this.pets;
    }

    public List getPets() {
        List sortedPets = new ArrayList(getPetsInternal());
        PropertyComparator.sort(sortedPets, new MutableSortDefinition("name", true, true));
        return Collections.unmodifiableList(sortedPets);
    }

    public void addPet(Pet pet) {
        getPetsInternal().add(pet);
        pet.setOwner(this);
    }

    /**
     * Return the Pet with the given name, or null if none found for this Owner.
     * 
     * @param name
     *            to test
     * @return true if pet name is already in use
     */
    public Pet getPet(String name) {
        return getPet(name, false);
    }

    /**
     * Return the Pet with the given name, or null if none found for this Owner.
     * 
     * @param name
     *            to test
     * @return true if pet name is already in use
     */
    public Pet getPet(String name, boolean ignoreNew) {
        name = name.toLowerCase();
        for (Iterator it = getPetsInternal().iterator(); it.hasNext();) {
            Pet pet = (Pet) it.next();
            if (!ignoreNew || !pet.isNew()) {
                String compName = pet.getName();
                compName = compName.toLowerCase();
                if (compName.equals(name)) {
                    return pet;
                }
            }
        }
        return null;
    }
}
