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

import java.util.Date;

/**
 * Simple JavaBean domain object representing a visit.
 * 
 * @author Ken Krebs
 */
public class Visit extends Entity {

    /** Holds value of property date. */
    private Date date;

    /** Holds value of property description. */
    private String description;

    /** Holds value of property pet. */
    private Pet pet;

    /** Creates a new instance of Visit for the current date */
    public Visit() {
        this.date = new Date();
    }

    /**
     * Getter for property date.
     * 
     * @return Value of property date.
     */
    public Date getDate() {
        return this.date;
    }

    /**
     * Setter for property date.
     * 
     * @param date
     *            New value of property date.
     * 
     */

    public void setDate(Date date) {
        this.date = date;
    }

    /**
     * Getter for property description.
     * 
     * @return Value of property description.
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * Setter for property description.
     * 
     * @param description
     *            New value of property description.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Getter for property pet.
     * 
     * @return Value of property pet.
     * 
     */
    public Pet getPet() {
        return this.pet;
    }

    /**
     * Setter for property pet.
     * 
     * @param pet
     *            New value of property pet.
     */
    protected void setPet(Pet pet) {
        this.pet = pet;
    }
}
