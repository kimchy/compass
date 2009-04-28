/*
 * Copyright 2004-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.compass.core.test.inheritance.aliasquery;

import java.util.HashSet;

import org.compass.core.CompassSession;
import org.compass.core.mapping.AliasMapping;
import org.compass.core.mapping.CompassMapping;
import org.compass.core.test.AbstractTestCase;

/**
 * @author kimchy
 */
public class AliasQueryInheritanceTests extends AbstractTestCase {

    protected String[] getMappings() {
        return new String[]{"inheritance/aliasquery/mapping.cpm.xml"};
    }


    public void testExtendedAliases() {
        CompassMapping compassMapping = getCompass().getMapping();
        AliasMapping aliasMapping = compassMapping.getAliasMapping("a");
        assertEquals(2, aliasMapping.getExtendingAliases().length);
        HashSet aliases = new HashSet();
        aliases.add("b");
        aliases.add("c");
        for (int i = 0; i < aliasMapping.getExtendingAliases().length; i++) {
            assertTrue(aliases.contains(aliasMapping.getExtendingAliases()[i]));
        }
    }

    public void testPolyAlias() {
        CompassSession session = openSession();
        A a  = new A();
        a.id = 1;
        a.valueA = "valueA";
        session.save(a);
        session.flush();

        B b = new B();
        b.id = 2;
        b.valueA = "valueA";
        b.valueB = "valueB";
        session.save(b);
        session.flush();

        C c  = new C();
        c.id = 3;
        c.valueA = "valueA";
        c.valueB = "valueB";
        c.valueC = "valueC";
        session.save(c);
        session.flush();

        assertEquals(3, session.find("alias:a").length());

        assertEquals(3, session.find("a.valueA:valueA").length());
        assertEquals(2, session.find("b.valueA:valueA").length());
        assertEquals(1, session.find("c.valueA:valueA").length());

        assertEquals(2, session.find("b.valueB:valueB").length());
        assertEquals(1, session.find("c.valueB:valueB").length());

        assertEquals(1, session.find("c.valueC:valueC").length());

        assertEquals(3, session.queryBuilder().polyAlias("a").hits().length());
        assertEquals(1, session.queryBuilder().alias("a").hits().length());

        assertEquals(2, session.queryBuilder().polyAlias("b").hits().length());
        assertEquals(1, session.queryBuilder().alias("b").hits().length());

        assertEquals(1, session.queryBuilder().polyAlias("c").hits().length());
        assertEquals(1, session.queryBuilder().alias("c").hits().length());

        a = session.load(A.class, 1);
        assertEquals(A.class, a.getClass());

        // poly get/load
        a = session.load(A.class, 2);
        assertEquals(B.class, a.getClass());

        // poly get/load
        a = session.load(A.class, 3);
        assertEquals(C.class, a.getClass());

        a = session.load(B.class, 2);
        assertEquals(B.class, a.getClass());

        // poly get/load
        a = session.load(B.class, 3);
        assertEquals(C.class, a.getClass());

        a = session.load(C.class, 3);
        assertEquals(C.class, a.getClass());

        session.close();
    }
}