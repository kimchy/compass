package org.compass.annotations.test.id;

import org.compass.annotations.test.AbstractAnnotationsTestCase;
import org.compass.core.CompassHits;
import org.compass.core.CompassTransaction;
import org.compass.core.config.CompassConfiguration;
import org.compass.core.impl.InternalCompassSession;
import org.compass.core.mapping.CompassMapping;
import org.compass.core.mapping.ResourcePropertyMapping;
import org.compass.core.mapping.osem.ClassIdPropertyMapping;
import org.compass.core.mapping.osem.ClassMapping;

/**
 * @author kimchy
 */
public class IdTests extends AbstractAnnotationsTestCase {

    protected void addExtraConf(CompassConfiguration conf) {
        conf.addClass(A.class);
    }

    public void testIdsPaths() throws Exception {

        InternalCompassSession session = (InternalCompassSession) openSession();

        CompassMapping mapping = session.getMapping();
        ClassMapping AMApping = (ClassMapping) mapping.getRootMappingByAlias("A");
        ClassIdPropertyMapping[] idMappings = AMApping.getClassPropertyIdMappings();
        assertEquals(1, idMappings.length);
        ResourcePropertyMapping[] resourcePropertyMappings = idMappings[0].getIdMappings();
        assertEquals(1, resourcePropertyMappings.length);
        assertEquals("test", resourcePropertyMappings[0].getPath());
        assertEquals("test", resourcePropertyMappings[0].getName());

        CompassTransaction tr = session.beginTransaction();

        A a = new A();
        a.setId(1);
        a.setValue("value");
        session.save(a);

        a = (A) session.load(A.class, 1);
        assertEquals("value", a.getValue());

        CompassHits hits = session.find("value");
        assertEquals(1, hits.length());

        tr.commit();
        session.close();
    }

}
