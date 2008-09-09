package org.compass.core.test.component.inheritance1;

import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.config.CompassEnvironment;
import org.compass.core.config.CompassSettings;
import org.compass.core.test.AbstractTestCase;

/**
 * This holds a pretty complex relationship that has been reported as
 * broken in Compass. The main goal here is to test that the relationship
 * is supported.
 *
 * @author kimchy
 */
public class ComponentInheritance1NoFilterDuplicatesTests extends AbstractTestCase {


    protected String[] getMappings() {
        return new String[] {"component/inheritance1/Father.cpm.xml", "component/inheritance1/Child.cpm.xml"};
    }

    protected void addSettings(CompassSettings settings) {
        settings.setBooleanSetting(CompassEnvironment.Osem.FILTER_DUPLICATES, false);
    }

    public void testCorrectFatherSave() throws Exception {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        FatherImpl.resetId();
        ChildImpl.resetId();

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