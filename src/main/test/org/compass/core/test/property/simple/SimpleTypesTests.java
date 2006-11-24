package org.compass.core.test.property.simple;

import java.io.File;
import java.math.BigDecimal;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.test.AbstractTestCase;

/**
 * @author kimchy
 */
public class SimpleTypesTests extends AbstractTestCase {

    protected String[] getMappings() {
        return new String[]{"property/simple/mapping.cpm.xml"};
    }

    public void testSimpleTypes() throws Exception {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        Long id = new Long(1);
        SimpleTypes o = new SimpleTypes();
        o.setId(id);
        o.setOBigDecimal(new BigDecimal(22.22d));
        o.setOBoolean(Boolean.TRUE);
        o.setOByte(new Byte((byte) 1));
        o.setOChar(new Character('A'));
        Date date = new Date();
        o.setODate(date);
        Calendar c = Calendar.getInstance();
        c.set(2001, 1, 1);
        o.setOCalendar(c);
        o.setODouble(new Double(2.456d));
        o.setOFile(new File("test/testfile"));
        o.setOInteger(new Integer(23456));
        o.setOLong(new Long(345345));
        o.setOShort(new Short((short) 234));
        o.setOString("TEST");
        o.setOStringBuffer(new StringBuffer("TEST STRING BUFFER"));
        o.setOURL(new URL("http://www.opensymphony.com/compass"));
        o.setSBoolean(true);
        o.setSByte((byte) 2);
        o.setSChar('B');
        o.setSDouble(12.34456d);
        o.setSInt(8786095);
        o.setSLong(234234);
        o.setSShort((short) 34554);
        o.setSFloat(23.45f);
        o.setOFloat(new Float(567.567f));
        o.setOLocale(Locale.ENGLISH);

        session.save(o);

        o = (SimpleTypes) session.load(SimpleTypes.class, id);
        assertEquals(new BigDecimal(22.22d), o.getOBigDecimal());
        assertEquals(Boolean.TRUE, o.getOBoolean());
        assertEquals(new Byte((byte) 1), o.getOByte());
        assertEquals(new Character('A'), o.getOChar());
        assertEquals(date, o.getODate());
        assertEquals(c, o.getOCalendar());
        assertEquals(new Double(2.456d), o.getODouble());
        assertEquals(new File("test/testfile").getAbsolutePath(), o.getOFile().getAbsolutePath());
        assertEquals(new Integer(23456), o.getOInteger());
        assertEquals(new Long(345345), o.getOLong());
        assertEquals(new Short((short) 234), o.getOShort());
        assertEquals("TEST", o.getOString());
        assertEquals("TEST STRING BUFFER", o.getOStringBuffer().toString());
        assertEquals(new URL("http://www.opensymphony.com/compass").toExternalForm(), o.getOURL().toExternalForm());
        assertEquals(true, o.isSBoolean());
        assertEquals((byte) 2, o.getSByte());
        assertEquals('B', o.getSChar());
        assertEquals(12.34456d, o.getSDouble(), 0.000001d);
        assertEquals(8786095, o.getSInt());
        assertEquals(234234, o.getSLong());
        assertEquals((short) 34554, o.getSShort());
        assertEquals(23.45f, o.getSFloat(), 0.000001f);
        assertEquals(new Float(567.567f), o.getOFloat());
        assertEquals(Locale.ENGLISH, o.getOLocale());

        tr.commit();
        session.close();

        session = openSession();
        tr = session.beginTransaction();

        o = (SimpleTypes) session.load(SimpleTypes.class, id);
        assertEquals(new BigDecimal(22.22d), o.getOBigDecimal());
        assertEquals(Boolean.TRUE, o.getOBoolean());
        assertEquals(new Byte((byte) 1), o.getOByte());
        assertEquals(new Character('A'), o.getOChar());
        assertEquals(date, o.getODate());
        assertEquals(c, o.getOCalendar());
        assertEquals(new Double(2.456d), o.getODouble());
        assertEquals(new File("test/testfile").getAbsolutePath(), o.getOFile().getAbsolutePath());
        assertEquals(new Integer(23456), o.getOInteger());
        assertEquals(new Long(345345), o.getOLong());
        assertEquals(new Short((short) 234), o.getOShort());
        assertEquals("TEST", o.getOString());
        assertEquals("TEST STRING BUFFER", o.getOStringBuffer().toString());
        assertEquals(new URL("http://www.opensymphony.com/compass").toExternalForm(), o.getOURL().toExternalForm());
        assertEquals(true, o.isSBoolean());
        assertEquals((byte) 2, o.getSByte());
        assertEquals('B', o.getSChar());
        assertEquals(12.34456d, o.getSDouble(), 0.000001d);
        assertEquals(8786095, o.getSInt());
        assertEquals(234234, o.getSLong());
        assertEquals((short) 34554, o.getSShort());
        assertEquals(23.45f, o.getSFloat(), 0.000001f);
        assertEquals(new Float(567.567f), o.getOFloat());
        assertEquals(Locale.ENGLISH, o.getOLocale());

        tr.commit();
        session.close();
    }
}
