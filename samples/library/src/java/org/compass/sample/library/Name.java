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

package org.compass.sample.library;

/**
 * 
 * @author kimchy
 */
public class Name {

    private String title;

    private String firstName;

    private String lastName;

    public Name() {

    }

    public Name(String title, String firstName, String lastName) {
        this.title = title;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof Name)) {
            return false;
        }

        Name otherName = (Name) other;
        if (title != null) {
            if (!title.equals(otherName.getTitle())) {
                return false;
            }
        }
        if (firstName != null) {
            if (!firstName.equals(otherName.getFirstName())) {
                return false;
            }
        }
        if (lastName != null) {
            if (!lastName.equals(otherName.getLastName())) {
                return false;
            }
        }
        return true;
    }

    public int hashCode() {
        int hash = 1;
        hash = hash * 31 + title == null ? 0 : title.hashCode();
        hash = hash * 31 + firstName == null ? 0 : firstName.hashCode();
        hash = hash * 31 + lastName == null ? 0 : lastName.hashCode();
        return hash;
    }

    public String toString() {
        return title + " " + " " + firstName + " " + lastName;
    }
}
