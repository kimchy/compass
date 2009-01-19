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

package org.compass.gps.device;

import java.util.ArrayList;
import java.util.Iterator;

import org.compass.core.CompassSession;
import org.compass.gps.CompassGpsException;
import org.compass.gps.IndexPlan;

/**
 * @author kimchy
 */
public class MockIndexGpsDevice extends AbstractGpsDevice {

    private ArrayList objects = new ArrayList();

    protected void doIndex(CompassSession session, IndexPlan indexPlan) throws CompassGpsException {
        for (Iterator it = objects.iterator(); it.hasNext();) {
            session.create(it.next());
        }
    }

    public void clear() {
        objects.clear();
    }

    public void add(Object o) {
        objects.add(o);
    }

    public void add(Long id, String value) {
        MockIndexGpsDeviceObject object = new MockIndexGpsDeviceObject();
        object.id = id;
        object.value = value;
        objects.add(object);
    }
}
