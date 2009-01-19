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

package org.compass.gps.device.hibernate.eg;

/**
 * @author Gavin King
 */
public class Name {
    private String firstName;

    private String lastName;

    private Character initial;

    private Name() {
    }

    public Name(String first, Character middle, String last) {
        firstName = first;
        initial = middle;
        lastName = last;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public Character getInitial() {
        return initial;
    }

    public void setInitial(Character initial) {
        this.initial = initial;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String toString() {
        StringBuffer buf = new StringBuffer().append(firstName).append(' ');
        if (initial != null)
            buf.append(initial).append(' ');
        return buf.append(lastName).toString();
    }

}
