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

package org.compass.core.test.component.poly2;

import java.util.ArrayList;
import java.util.List;

import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.config.CompassConfiguration;
import org.compass.core.test.AbstractTestCase;

/**
 * @author kimchy
 */
public class Poly2ComponentTests extends AbstractTestCase {

    protected String[] getMappings() {
        return new String[0];
    }

    protected void addExtraConf(CompassConfiguration conf) {
        conf.addClass(Address.class).addClass(AddressToPipeAssignment.class).addClass(AddressToServiceAssignment.class)
                .addClass(Assignment.class);
    }

    public void testPoly2() {
        CompassSession session = openSession();
        CompassTransaction tx = session.beginTransaction();

        Address pipeAddress = new Address();
        pipeAddress.setId(1l);
        pipeAddress.setName("pipe1");

        AddressToPipeAssignment pa1 = new AddressToPipeAssignment();
        pa1.setPipeName("pipeName1");
        pa1.setStatus("pipeStatus1");
        AddressToServiceAssignment sa1 = new AddressToServiceAssignment();
        sa1.setServiceName("serviceName1");
        sa1.setStatus("serviceStatus1");
        List<Assignment> ass1 = new ArrayList<Assignment>();
        ass1.add(pa1);
        ass1.add(sa1);

        pipeAddress.setAssignment(ass1);


        Address serviceAddress = new Address();
        serviceAddress.setId(2l);
        serviceAddress.setName("service1");

        AddressToPipeAssignment pa2 = new AddressToPipeAssignment();
        pa2.setPipeName("pipeName2");
        pa2.setStatus("pipeStatus2");
        AddressToServiceAssignment sa2 = new AddressToServiceAssignment();
        sa2.setServiceName("serviceName2");
        sa2.setStatus("serviceStatus2");
        List<Assignment> ass2 = new ArrayList<Assignment>();
        ass2.add(pa2);
        ass2.add(sa2);
        serviceAddress.setAssignment(ass2);

        session.create(pipeAddress);
        session.create(serviceAddress);

        pipeAddress = session.load(Address.class, "1");
        assertEquals(2, pipeAddress.getAssignment().size());
        assertEquals(AddressToPipeAssignment.class, pipeAddress.getAssignment().get(0).getClass());
        assertEquals("pipeStatus1", pipeAddress.getAssignment().get(0).getStatus());
        assertEquals("pipeName1", ((AddressToPipeAssignment) pipeAddress.getAssignment().get(0)).getPipeName());
        assertEquals(AddressToServiceAssignment.class, pipeAddress.getAssignment().get(1).getClass());
        assertEquals("serviceStatus1", pipeAddress.getAssignment().get(1).getStatus());
        assertEquals("serviceName1", ((AddressToServiceAssignment) pipeAddress.getAssignment().get(1)).getServiceName());

        serviceAddress = session.load(Address.class, "2");
        assertEquals(2, serviceAddress.getAssignment().size());
        assertEquals(AddressToPipeAssignment.class, serviceAddress.getAssignment().get(0).getClass());
        assertEquals(AddressToServiceAssignment.class, serviceAddress.getAssignment().get(1).getClass());

        tx.commit();
        session.close();
    }
}
