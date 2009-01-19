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

/**
 * @author Gavin King
 */
public class Bid extends Persistent {
    private AuctionItem item;

    private float amount;

    private Date datetime;

    private User bidder;

    public AuctionItem getItem() {
        return item;
    }

    public void setItem(AuctionItem item) {
        this.item = item;
    }

    public float getAmount() {
        return amount;
    }

    public Date getDatetime() {
        return datetime;
    }

    public void setAmount(float f) {
        amount = f;
    }

    public void setDatetime(Date date) {
        datetime = date;
    }

    public User getBidder() {
        return bidder;
    }

    public void setBidder(User user) {
        bidder = user;
    }

    public String toString() {
        return bidder.getUserName() + " $" + amount;
    }

    public boolean isBuyNow() {
        return false;
    }

}
