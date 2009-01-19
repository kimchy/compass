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

package org.compass.core.test.inheritance;

import java.util.HashSet;

import org.compass.core.CompassHits;
import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.Resource;
import org.compass.core.config.CompassEnvironment;
import org.compass.core.config.CompassSettings;
import org.compass.core.mapping.AliasMapping;
import org.compass.core.mapping.CompassMapping;
import org.compass.core.spi.InternalCompass;
import org.compass.core.test.AbstractTestCase;

public class InheritanceTests extends AbstractTestCase {

    protected String[] getMappings() {
        return new String[] { "inheritance/Inheritance.cpm.xml" };
    }

    protected void addSettings(CompassSettings settings) {
        settings.setBooleanSetting(CompassEnvironment.All.EXCLUDE_ALIAS, false);
    }

    public void testExtendingAliases() {
        CompassMapping compassMapping = ((InternalCompass)getCompass()).getMapping();
        AliasMapping aliasMapping = compassMapping.getAliasMapping("base");
        assertEquals(3, aliasMapping.getExtendingAliases().length);
        HashSet aliases = new HashSet();
        aliases.add("override");
        aliases.add("override1");
        aliases.add("extends");
        for (int i = 0; i < aliasMapping.getExtendingAliases().length; i++) {
            assertTrue(aliases.contains(aliasMapping.getExtendingAliases()[i]));
        }
    }

    public void testPolyQuery() throws Exception {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        Long id = new Long(1);
        ExtendsA extendsA = new ExtendsA();
        extendsA.setId(id);
        extendsA.setValue("value");
        extendsA.setExtendsValue("evalue");

        session.save("extends", extendsA);

        extendsA = (ExtendsA) session.load("extends", id);
        assertEquals("value", extendsA.getValue());

        id = new Long(1);
        extendsA = new ExtendsA();
        extendsA.setId(id);
        extendsA.setValue("value");
        extendsA.setExtendsValue("evalue");

        session.save("override", extendsA);

        extendsA = (ExtendsA) session.load("override", id);
        assertEquals("value", extendsA.getValue());

        CompassHits hits = session.queryBuilder().polyAlias("base").hits();
        assertEquals(2, hits.length());

        hits = session.find("base");
        assertEquals(2, hits.length());

        tr.commit();
        session.close();
    }

    public void testSimpleExtends() throws Exception {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        Long id = new Long(1);
        ExtendsA extendsA = new ExtendsA();
        extendsA.setId(id);
        extendsA.setValue("value");
        extendsA.setExtendsValue("evalue");

        session.save("extends", extendsA);

        extendsA = (ExtendsA) session.load("extends", id);
        assertEquals("value", extendsA.getValue());

        tr.commit();
        session.close();
    }

    public void testSimpleExtendsWithOverride() throws Exception {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        Long id = new Long(1);
        ExtendsA extendsA = new ExtendsA();
        extendsA.setId(id);
        extendsA.setValue("value");
        extendsA.setExtendsValue("evalue");

        session.save("override", extendsA);

        extendsA = (ExtendsA) session.load("override", id);
        assertEquals("value", extendsA.getValue());

        Resource r = session.loadResource("override", id);
        assertNull(r.getProperty("mvalue"));
        assertNotNull(r.getProperty("mvalue1"));

        tr.commit();
        session.close();
    }

    public void testSimpleExtendsWithNoOverride() throws Exception {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        Long id = new Long(1);
        ExtendsA extendsA = new ExtendsA();
        extendsA.setId(id);
        extendsA.setValue("value");
        extendsA.setExtendsValue("evalue");

        session.save("override1", extendsA);

        extendsA = (ExtendsA) session.load("override1", id);
        assertEquals("value", extendsA.getValue());

        Resource r = session.loadResource("override1", id);
        assertNotNull(r.getProperty("mvalue"));
        assertNotNull(r.getProperty("mvalue1"));

        tr.commit();
        session.close();
    }
}
