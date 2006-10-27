package org.compass.core.test.component.inheritance1;

import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.test.AbstractTestCase;

/**
 * @author kimchy
 */
public class ComponentInheritance1Tests extends AbstractTestCase {


    protected String[] getMappings() {
        return new String[] {"component/inheritance1/Father.cpm.xml", "component/inheritance1/Child.cpm.xml"};
    }

    public void testCorrectFatherSave() throws Exception {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        FatherImpl father = new FatherImpl("Sir Ivan");
        FavouriteSonImpl favouriteSon = new FavouriteSonImpl("Ivan Jr", father);
        father.setFavouriteSon(favouriteSon);

        DaughterImpl daughter = new DaughterImpl("Betty Jr", father);
        father.getChildren().add(daughter);

        session.save(father);

        father = (FatherImpl) session.load("father", father.getId());
        assertEquals("Sir Ivan", father.getName());
        assertNotNull(father.getFavouriteSon());
        assertEquals("Ivan Jr", father.getFavouriteSon().getName());
        assertSame(father, father.getFavouriteSon().getFather());
        assertEquals(1, father.getChildren().size());
        assertEquals("Betty Jr", ((DaughterImpl)father.getChildren().iterator().next()).getName());
        assertSame(father, ((DaughterImpl)father.getChildren().iterator().next()).getFather());

        tr.commit();
        session.close();
    }
}
