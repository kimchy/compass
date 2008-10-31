package org.compass.core.test.inheritance;

import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.mapping.AliasMapping;
import org.compass.core.mapping.CompassMapping;
import org.compass.core.mapping.ResourcePropertyMapping;
import org.compass.core.mapping.osem.ClassMapping;
import org.compass.core.mapping.osem.RefAliasObjectMapping;
import org.compass.core.spi.InternalCompass;
import org.compass.core.spi.InternalCompassSession;
import org.compass.core.test.AbstractTestCase;

public class PolyCollectionTests extends AbstractTestCase {

    protected String[] getMappings() {
        return new String[]{"inheritance/PolyCollection.cpm.xml"};
    }

    public void testClassBasedRefAliasIdentification() {
        CompassMapping compassMapping = ((InternalCompass) getCompass()).getMapping();
        ClassMapping classMapping = (ClassMapping) compassMapping.getMappingByAlias("cComponentNoRefAlias");
        assertEquals(2, ((RefAliasObjectMapping) classMapping.getMapping("a")).getRefAliases().length);
    }

    public void testPolyComponentCollection() throws Exception {
        CompassMapping compassMapping = ((InternalCompass) getCompass()).getMapping();
        AliasMapping aliasMapping = compassMapping.getAliasMapping("polybase");
        assertEquals(1, aliasMapping.getExtendingAliases().length);
        aliasMapping = compassMapping.getAliasMapping("contract");
        assertEquals(2, aliasMapping.getExtendingAliases().length);

        InternalCompassSession session = (InternalCompassSession) openSession();
        CompassTransaction tr = session.beginTransaction();

        CompassMapping mapping = session.getMapping();
        ClassMapping bComponent = (ClassMapping) mapping.getRootMappingByAlias("bComponent");
        ResourcePropertyMapping rpMapping = bComponent.getResourcePropertyMappingByDotPath("a.value");
        assertNotNull(rpMapping);
        // this mvalue is shared between polybase and polyextends, so there should not be
        // an internal id
        assertEquals("mvalue", rpMapping.getName());
        assertEquals("mvalue", rpMapping.getPath().getPath());
        rpMapping = bComponent.getResourcePropertyMappingByDotPath("a.value.mvalue");
        assertNotNull(rpMapping);
        assertEquals("bComponent", rpMapping.getRootAlias());
        assertEquals("mvalue", rpMapping.getName());
        assertEquals("mvalue", rpMapping.getPath().getPath());
        rpMapping = bComponent.getResourcePropertyMappingByDotPath("a.extendsValue");
        assertEquals("bComponent", rpMapping.getRootAlias());
        assertNotNull(rpMapping);
        assertEquals("mextendsValue", rpMapping.getName());
        assertEquals("mextendsValue", rpMapping.getPath().getPath()); // no internal id needed
        rpMapping = bComponent.getResourcePropertyMappingByDotPath("a.extendsValue.mextendsValue");
        assertNotNull(rpMapping);
        assertEquals("bComponent", rpMapping.getRootAlias());
        assertEquals("mextendsValue", rpMapping.getName());
        assertEquals("mextendsValue", rpMapping.getPath().getPath());


        B b = new B();
        b.id = 1;

        Long id = new Long(1);
        ExtendsA extendsA = new ExtendsA();
        extendsA.setId(id);
        extendsA.setValue("value");
        extendsA.setExtendsValue("evalue");
        extendsA.setD(new D("edvalue"));
        b.a.add(extendsA);

        id = new Long(2);
        BaseA base = new BaseA();
        base.setId(id);
        base.setValue("baseValue");
        base.setD(new D("bdvalue"));
        b.a.add(base);

        session.save("bComponent", b);

        b = (B) session.load("bComponent", new Long(1));
        assertEquals(2, b.a.size());
        extendsA = (ExtendsA) b.a.get(0);
        assertEquals("value", extendsA.getValue());
        assertEquals("evalue", extendsA.getExtendsValue());
        assertEquals("edvalue", extendsA.getD().value);
        base = (BaseA) b.a.get(1);
        assertEquals("baseValue", base.getValue());
        assertEquals("bdvalue", base.getD().value);

        tr.commit();
        session.close();
    }

    public void testPolyReferenceCollection() throws Exception {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        B b = new B();
        b.id = 1;

        Long id = new Long(1);
        ExtendsA extendsA = new ExtendsA();
        extendsA.setId(id);
        extendsA.setValue("value");
        extendsA.setExtendsValue("evalue");
        b.a.add(extendsA);
        session.save("polyextends", extendsA);

        id = new Long(2);
        BaseA base = new BaseA();
        base.setId(id);
        base.setValue("baseValue");
        b.a.add(base);
        session.save("polybase", base);

        session.save("bReference", b);

        b = (B) session.load("bReference", new Long(1));
        assertEquals(2, b.a.size());
        extendsA = (ExtendsA) b.a.get(0);
        assertEquals("value", extendsA.getValue());
        assertEquals("evalue", extendsA.getExtendsValue());
        base = (BaseA) b.a.get(1);
        assertEquals("baseValue", base.getValue());

        tr.commit();
        session.close();
    }

    public void testPolyComponent() throws Exception {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        C c = new C();
        c.id = 1;

        Long id = new Long(1);
        ExtendsA extendsA = new ExtendsA();
        extendsA.setId(id);
        extendsA.setValue("value");
        extendsA.setExtendsValue("evalue");
        c.a = extendsA;

        session.save("cComponent", c);

        c = (C) session.load("cComponent", new Long(1));
        extendsA = (ExtendsA) c.a;
        assertEquals("value", extendsA.getValue());
        assertEquals("evalue", extendsA.getExtendsValue());

        c = new C();
        c.id = 2;
        id = new Long(2);
        BaseA base = new BaseA();
        base.setId(id);
        base.setValue("baseValue");
        c.a = base;

        session.save("cComponent", c);

        c = (C) session.load("cComponent", new Long(2));
        assertEquals("baseValue", c.a.getValue());

        tr.commit();
        session.close();
    }
}
