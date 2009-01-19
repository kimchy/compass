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

package org.compass.gps.device.hibernate.onetoone;

import java.io.Serializable;

/**
 */
public class Address implements Serializable{
    
    private Long id;
    private String zipcode;
    private User user;
    
    public Address (){
        
    }
    
    public Address (User u) {
        this.user = u;
        //this.zipcode = "91234";
        u.setShippingaddress(this);
    }
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    
    public String getZipcode() {
        return zipcode;
    }

    public void setZipcode(String zipcode) {
        this.zipcode = zipcode;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser ( User user ){
        this.user =user;
    }
}
