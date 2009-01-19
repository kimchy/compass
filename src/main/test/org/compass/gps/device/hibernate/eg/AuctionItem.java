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

import java.util.Date;
import java.util.List;

public class AuctionItem extends Persistent {
    private String description;

    private List bids;

    private Bid successfulBid;

    private User seller;

    private Date ends;

    private int condition;

    public List getBids() {
        return bids;
    }

    public String getDescription() {
        return description;
    }

    public User getSeller() {
        return seller;
    }

    public Bid getSuccessfulBid() {
        return successfulBid;
    }

    public void setBids(List bids) {
        this.bids = bids;
    }

    public void setDescription(String string) {
        description = string;
    }

    public void setSeller(User user) {
        seller = user;
    }

    public void setSuccessfulBid(Bid bid) {
        successfulBid = bid;
    }

    public Date getEnds() {
        return ends;
    }

    public void setEnds(Date date) {
        ends = date;
    }

    public int getCondition() {
        return condition;
    }

    public void setCondition(int i) {
        condition = i;
    }

    public String toString() {
        return description + " (" + condition + "/10)";
    }

}
