package org.compass.annotations.test.mapping.index;

import org.compass.annotations.test.AbstractAnnotationsTestCase;
import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.Resource;
import org.compass.core.config.CompassConfiguration;

/**
 * @author kimchy
 */
public class AnnotationMappingIndexTests extends AbstractAnnotationsTestCase {

    protected void addExtraConf(CompassConfiguration conf) {
        conf.addClass(A.class);
    }

    public void testAnnotationIndexMapping() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        A a = new A();
        a.id = 1;
        a.untokenized = "test me";
        a.tokenized = "test me";
        a.no = "test me";
        session.save(a);

        Resource r = session.loadResource(A.class, 1);
        assertTrue(r.getProperty("untokenized").isIndexed());
        assertFalse(r.getProperty("untokenized").isTokenized());

        assertTrue(r.getProperty("tokenized").isIndexed());
        assertTrue(r.getProperty("tokenized").isTokenized());

        assertFalse(r.getProperty("no").isIndexed());
        assertFalse(r.getProperty("no").isTokenized());

        tr.commit();
        session.close();
    }
}
