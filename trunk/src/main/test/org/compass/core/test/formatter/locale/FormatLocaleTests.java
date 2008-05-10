package org.compass.core.test.formatter.locale;

import java.util.Calendar;
import java.util.Locale;

import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.Resource;
import org.compass.core.config.CompassSettings;
import org.compass.core.test.AbstractTestCase;

/**
 * @author kimchy
 */
public class FormatLocaleTests extends AbstractTestCase {

    protected String[] getMappings() {
        return new String[]{"formatter/locale/mapping.cpm.xml"};
    }


    protected void addSettings(CompassSettings settings) {
        settings.setSetting("compass.converter.float.format.locale", Locale.GERMAN.toString());
    }

    public void testCustomLocale() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        A a = new A();
        a.id = 1;
        a.value = 123.456f;
        Calendar cal = Calendar.getInstance();
        cal.set(2000, 1, 1);
        a.date = cal.getTime();
        session.save(a);

        Resource resource = session.loadResource(A.class, "1");
        // german separator is "," not "." - we verify that the locale was accepted
        assertEquals("123,46", resource.getValue("value"));


        tr.commit();
        session.close();
    }
}
