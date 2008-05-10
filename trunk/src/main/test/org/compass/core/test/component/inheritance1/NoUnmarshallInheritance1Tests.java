package org.compass.core.test.component.inheritance1;

import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.Resource;
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
public class NoUnmarshallInheritance1Tests extends AbstractTestCase {

    protected String[] getMappings() {
        return new String[]{"component/inheritance1/Father.cpm.xml", "component/inheritance1/Child.cpm.xml"};
    }


    protected void addSettings(CompassSettings settings) {
        settings.setBooleanSetting(CompassEnvironment.Osem.SUPPORT_UNMARSHALL, false);
    }

    public void testCorrectFatherSave() throws Exception {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        FatherImpl.resetId();

        FatherImpl father = new FatherImpl("Sir Ivan");
        FavouriteSonImpl favouriteSon = new FavouriteSonImpl("Ivan Jr", father);
        father.setFavouriteSon(favouriteSon);

        DaughterImpl daughter = new DaughterImpl("Betty Jr", father);
        father.getChildren().add(daughter);

        session.save(father);

        Resource resource = session.loadResource("father", father.getId());
        assertEquals(8, resource.getProperties().length);
        assertEquals("father", resource.getValue("alias"));
        assertEquals("0", resource.getValue("$/father/id"));
        assertEquals("Sir Ivan", resource.getProperties("name")[0].getStringValue());
        assertEquals("Ivan Jr", resource.getProperties("name")[1].getStringValue());
        assertEquals("child", resource.getProperties("childalias")[0].getStringValue());
        assertEquals("Betty Jr", resource.getProperties("name")[2].getStringValue());
        assertEquals("child", resource.getProperties("childalias")[1].getStringValue());

        tr.commit();
        session.close();
    }
}
