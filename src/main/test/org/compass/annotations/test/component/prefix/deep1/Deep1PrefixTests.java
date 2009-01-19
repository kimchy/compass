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

package org.compass.annotations.test.component.prefix.deep1;

import org.compass.annotations.test.AbstractAnnotationsTestCase;
import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.Resource;
import org.compass.core.config.CompassConfiguration;

/**
 * @author kimchy
 */
public class Deep1PrefixTests extends AbstractAnnotationsTestCase {

    protected void addExtraConf(CompassConfiguration conf) {
        conf.addClass(Order.class).addClass(Customer.class).addClass(Address.class);
    }

    public void testDeepLevelComponentPrefix() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        Order order = new Order();
        order.id = 1;
        order.firstCustomer = new Customer();
        order.firstCustomer.id = 2;
        order.firstCustomer.name = "name1";
        order.firstCustomer.homeAddress = new Address();
        order.firstCustomer.homeAddress.id = 3;
        order.firstCustomer.homeAddress.location = "firstHome";
        order.firstCustomer.workAddress = new Address();
        order.firstCustomer.workAddress.id = 4;
        order.firstCustomer.workAddress.location = "firstWork";

        order.secondCustomer = new Customer();
        order.secondCustomer.id = 5;
        order.secondCustomer.name = "name2";
        order.secondCustomer.homeAddress = new Address();
        order.secondCustomer.homeAddress.id = 6;
        order.secondCustomer.homeAddress.location = "secondHome";
        order.secondCustomer.workAddress = new Address();
        order.secondCustomer.workAddress.id = 7;
        order.secondCustomer.workAddress.location = "secondWork";
        session.save(order);

        Resource resource = session.loadResource(Order.class, 1);
        assertEquals("name1", resource.getValue("first_name"));
        assertEquals("name2", resource.getValue("second_name"));
        assertEquals("firstHome", resource.getValue("first_home_location"));
        assertEquals("firstWork", resource.getValue("first_work_location"));
        assertEquals("secondHome", resource.getValue("second_home_location"));
        assertEquals("secondWork", resource.getValue("second_work_location"));

        assertEquals("firstHome", resource.getValue("first_home_dyn1"));
        assertEquals("firstWork", resource.getValue("first_work_dyn1"));
        assertEquals("secondHome", resource.getValue("second_home_dyn1"));
        assertEquals("secondWork", resource.getValue("second_work_dyn1"));

        assertEquals("addr", resource.getValue("first_work_const1"));
        assertEquals("addr", resource.getValue("second_work_const1"));

        assertEquals(1, session.find("Order.firstCustomer.name:name1").length());
        assertEquals(1, session.find("Order.firstCustomer.homeAddress.location:firstHome").length());
        assertEquals(1, session.find("Order.firstCustomer.homeAddress.location.first_home_location:firstHome").length());
        assertEquals(1, session.find("Order.firstCustomer.homeAddress.location.location:firstHome").length());

        order = session.load(Order.class, 1);
        assertEquals("name1", order.firstCustomer.name);
        assertEquals("name2", order.secondCustomer.name);
        assertEquals("firstHome", order.firstCustomer.homeAddress.location);
        assertEquals("firstWork", order.firstCustomer.workAddress.location);
        assertEquals("secondHome", order.secondCustomer.homeAddress.location);
        assertEquals("secondWork", order.secondCustomer.workAddress.location);


        // make sure no internal ids were created (since the prefix distinguish them apart)
        assertNull(resource.getValue("$/Order/firstCustomer/homeAddress/location"));
        assertNull(resource.getValue("$/Order/firstCustomer/workAddress/location"));
        assertNull(resource.getValue("$/Order/secondCustomer/homeAddress/location"));
        assertNull(resource.getValue("$/Order/secondCustomer/workAddress/location"));

        tr.commit();
        session.close();
    }
}