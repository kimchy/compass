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

import java.util.ArrayList;
import java.util.Date;

import org.compass.core.CompassHits;
import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.gps.device.hibernate.dep.Hibernate3GpsDevice;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;

/**
 * @author kimchy
 */
public class Hibernate3GpsDeviceTests extends AbstractHibernateGpsDeviceTests {

    private SessionFactory sessionFactory;

    private AuctionItem mainItem;

    private User mainBidder;

    private User userToDelete;

    protected void setUp() throws Exception {
        super.setUp();

        Configuration conf = new Configuration().configure("/org/compass/gps/device/hibernate/eg/hibernate3.cfg.xml")
                .setProperty(Environment.HBM2DDL_AUTO, "create");
        sessionFactory = conf.buildSessionFactory();

        Hibernate3GpsDevice device = new Hibernate3GpsDevice();
        device.setSessionFactory(sessionFactory);
        device.setName("hibernateDevice");
        compassGps.addGpsDevice(device);
        compassGps.start();

        // set up the initial set of data
        Session s = sessionFactory.openSession();
        Transaction tx = s.beginTransaction();

        userToDelete = new User();
        userToDelete.setUserName("deleteme");
        userToDelete.setName(new Name("delete", null, "me"));
        s.save(userToDelete);

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
    }

    protected void tearDown() throws Exception {
        sessionFactory.close();
        compassGps.stop();
        super.tearDown();
    }

    public void testSetUpData() throws Exception {
        CompassSession sess = mirrorCompass.openSession();
        CompassTransaction tr = sess.beginTransaction();

        CompassHits users = sess.find("bastard");
        assertEquals(1, users.getLength());

        tr.commit();
    }

    public void testSimpleDelete() throws Exception {
        // find all the bids
        CompassSession sess = mirrorCompass.openSession();
        CompassTransaction tr = sess.beginTransaction();
        User user = (User) sess.get(User.class, userToDelete.getId());
        assertNotNull(user);

        // delete all the bids using hibernate
        Session hibSess = sessionFactory.openSession();
        Transaction hibTrans = hibSess.beginTransaction();
        user = (User) hibSess.load(User.class, userToDelete.getId());
        hibSess.delete(user);
        hibTrans.commit();
        hibSess.close();

        // check that it was reflected in compass
        user = (User) sess.get(User.class, userToDelete.getId());
        assertNull(user);
        tr.commit();
        sess.close();
    }

    public void testSimpleUpdate() throws Exception {
        CompassSession sess = mirrorCompass.openSession();
        CompassTransaction tr = sess.beginTransaction();
        CompassHits users = sess.find("bastard");
        assertEquals(1, users.getLength());

        Session hibSess = sessionFactory.openSession();
        Transaction hibTrans = hibSess.beginTransaction();
        User user = (User) hibSess.load(User.class, ((User) users.data(0)).getId());
        user.getName().setLastName("snow");
        hibTrans.commit();
        hibSess.close();

        users = sess.find("bastard");
        assertEquals(0, users.getLength());
        users = sess.find("snow");
        assertEquals(1, users.getLength());
        tr.commit();
        sess.close();
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
