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

package org.compass.core.test.first.cache;

import org.compass.core.CompassHits;
import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.Property;
import org.compass.core.Resource;
import org.compass.core.cache.first.PlainFirstLevelCache;
import org.compass.core.config.CompassEnvironment;
import org.compass.core.config.CompassSettings;
import org.compass.core.test.AbstractTestCase;

/**
 * @author kimchy
 */
public class FirstCacheTests extends AbstractTestCase {

    protected String[] getMappings() {
        return new String[]{"first/cache/first-cache.cpm.xml"};
    }

    protected void addSettings(CompassSettings settings) {
        super.addSettings(settings);
        settings.setSetting(CompassEnvironment.Cache.FirstLevel.TYPE, PlainFirstLevelCache.class.getName());
    }

    public void testResourceCache() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        Resource r = getResourceFactory().createResource("r");
        Property id = getResourceFactory().createProperty("id", "1", Property.Store.YES, Property.Index.NOT_ANALYZED);
        r.addProperty(id);
        r.addProperty(getResourceFactory().createProperty("mvalue", "this is a test", Property.Store.YES, Property.Index.ANALYZED));

        session.save(r);

        Resource cachedR = session.getResource("r", "1");
        assertEquals("this is a test", r.getValue("mvalue"));

        // compass creates a new Resource for the saved resource
        // so we check for equals on alias and id
        assertTrue(r.getAlias().equals(cachedR.getAlias()));
        assertTrue(r.getValue("id").equals(cachedR.getValue("id")));
        r = cachedR;

        cachedR = session.getResource("r", (Object) new String[]{"1"});
        assertEquals("this is a test", r.getValue("mvalue"));

        assertTrue(r == cachedR);

        CompassHits hits = session.find("test");
        assertEquals(1, hits.getLength());
        assertTrue(r == hits.resource(0));

        session.evict(r);
        cachedR = session.getResource("r", "1");
        assertFalse(r == cachedR);

        tr.commit();
    }

    public void testObjectCached() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        Long id = new Long(1);
        A a = new A();
        a.setId(id);
        a.setValue("value");

        session.save(a);

        A cachedA = session.get(A.class, id);
        assertTrue(a == cachedA);

        CompassHits hits = session.find("value");
        assertEquals(1, hits.getLength());
        assertTrue(a == hits.data(0));

        session.evict(a);
        cachedA = session.get(A.class, id);
        assertFalse(a == cachedA);

        session.save(a);
        cachedA = session.get(A.class, id);
        assertTrue(a == cachedA);
        session.delete("a", id);
        cachedA = session.get(A.class, id);
        assertFalse(a == cachedA);

        tr.commit();
    }
}
