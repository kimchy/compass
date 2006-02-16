/*
 * Copyright 2004-2006 the original author or authors.
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

import java.util.ArrayList;
import java.util.Date;

import net.sf.hibernate.Session;
import net.sf.hibernate.SessionFactory;
import net.sf.hibernate.Transaction;
import net.sf.hibernate.cfg.Configuration;
import net.sf.hibernate.cfg.Environment;

import org.compass.core.CompassHits;
import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.gps.device.hibernate.Hibernate2GpsDevice;

/**
 * @author kimchy
 */
public class Hibernate2GpsDeviceTests extends AbstractHibernateGpsDeviceTests {

    private SessionFactory sessionFactory;

    private AuctionItem mainItem;

    private User mainBidder;

    protected void setUp() throws Exception {
        super.setUp();

        Configuration conf = new Configuration().configure("/org/compass/gps/device/hibernate/eg/hibernate2.cfg.xml")
                .setProperty(Environment.HBM2DDL_AUTO, "create");

        sessionFactory = conf.buildSessionFactory();

        // set up the initial set of data
        Session s = sessionFactory.openSession();
        Transaction tx = s.beginTransaction();

        User seller = new User();
        seller.setUserName("oldirty");
        seller.setName(new Name("ol' dirty", null, "bastard"));
        seller.setEmail("oldirty@hibernate.org");
        seller.setAuctions(new ArrayList());
        s.save(seller);
        User bidder1 = new User();
        bidder1.setUserName("1E1");
        bidder1.setName(new Name("oney", new Character('1'), "one"));
        bidder1.setEmail("oney@hibernate.org");
        bidder1.setBids(new ArrayList());
        s.save(bidder1);
        User bidder2 = new User();
        bidder2.setUserName("izi");
        bidder2.setName(new Name("iz", null, "inizi"));
        bidder2.setEmail("izi@hibernate.org");
        bidder2.setBids(new ArrayList());
        s.save(bidder2);

        for (int i = 0; i < 3; i++) {
            AuctionItem item = new AuctionItem();
            item.setDescription("auction item " + i);
            item.setEnds(new Date());
            item.setBids(new ArrayList());
            item.setSeller(seller);
            item.setCondition(i * 3 + 2);
            for (int j = 0; j < i; j++) {

                Bid bid = new Bid();
                bid.setBidder(bidder1);
                bid.setAmount(j);
                bid.setDatetime(new Date());
                bid.setItem(item);
                item.getBids().add(bid);
                bidder1.getBids().add(bid);

                Bid bid2 = new Bid();
                bid2.setBidder(bidder2);
                bid2.setAmount(j + 0.5f);
                bid2.setDatetime(new Date());
                bid2.setItem(item);
                item.getBids().add(bid2);
                bidder2.getBids().add(bid2);
            }
            seller.getAuctions().add(item);
            mainItem = item;
        }
        mainBidder = bidder2;

        BuyNow buyNow = new BuyNow();
        buyNow.setAmount(1.0f);
        buyNow.setDatetime(new Date());
        buyNow.setBidder(mainBidder);
        buyNow.setItem(mainItem);
        mainBidder.getBids().add(buyNow);
        mainItem.getBids().add(buyNow);

        tx.commit();
        s.close();

        Hibernate2GpsDevice device = new Hibernate2GpsDevice();
        device.setName("hibernateDevice");
        device.setSessionFactory(sessionFactory);
        compassGps.addGpsDevice(device);
        compassGps.start();
    }

    protected void tearDown() throws Exception {
        sessionFactory.close();
        compassGps.stop();
        super.tearDown();
    }

    public void testReindex() throws Exception {
        compassGps.index();
        CompassSession sess = mirrorCompass.openSession();
        CompassTransaction tr = sess.beginTransaction();

        CompassHits users = sess.find("bastard");
        assertEquals(1, users.getLength());

        tr.commit();
    }
}
