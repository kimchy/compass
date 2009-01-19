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

import java.util.List;

/**
 * @author Gavin King
 */
public class User extends Persistent {
    private String userName;

    private String password;

    private String email;

    private Name name;

    private List bids;

    private List auctions;

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getUserName() {
        return userName;
    }

    public void setEmail(String string) {
        email = string;
    }

    public void setPassword(String string) {
        password = string;
    }

    public void setUserName(String string) {
        userName = string;
    }

    public List getAuctions() {
        return auctions;
    }

    public List getBids() {
        return bids;
    }

    public void setAuctions(List list) {
        auctions = list;
    }

    public void setBids(List list) {
        bids = list;
    }

    public String toString() {
        return userName;
    }

    public Name getName() {
        return name;
    }

    public void setName(Name name) {
        this.name = name;
    }

}
