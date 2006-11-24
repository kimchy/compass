package org.compass.core.test.property.inputstream;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.test.AbstractTestCase;

/**
 * @author kimchy
 */
public class InputStreamTests extends AbstractTestCase {

    protected String[] getMappings() {
        return new String[]{"property/inputstream/mapping.cpm.xml"};
    }

    public void testInputStream() throws IOException {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        Long id = new Long(1);
        InputStreamType o = new InputStreamType();
        o.setId(id);

        byte[] bytes = new byte[]{1, 2, 3};
        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
        o.setInputStream(inputStream);
        session.save(o);

        tr.commit();

        tr = session.beginTransaction();

        o = (InputStreamType) session.load(InputStreamType.class, id);
        assertEquals(id, o.getId());
        assertNotNull(o.getInputStream());
        assertEquals(1, o.getInputStream().read());
        assertEquals(2, o.getInputStream().read());
        assertEquals(3, o.getInputStream().read());

        tr.commit();
    }

    public void testBinaryArray() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        Long id = new Long(1);
        byte[] bytes = new byte[]{1, 2, 3};
        Byte[] oBytes = new Byte[]{new Byte((byte) 1), new Byte((byte) 2), new Byte((byte) 3)};
        BinaryArrayType o = new BinaryArrayType();
        o.setId(id);
        o.setBytes(bytes);
        o.setOBytes(oBytes);

        session.save(o);

        o = (BinaryArrayType) session.load(BinaryArrayType.class, id);
        assertNotNull(o.getBytes());
        assertEquals(3, o.getBytes().length);
        assertEquals(1, o.getBytes()[0]);
        assertEquals(2, o.getBytes()[1]);
        assertEquals(3, o.getBytes()[2]);

        assertEquals(3, o.getOBytes().length);
        assertEquals(new Byte((byte) 1), o.getOBytes()[0]);
        assertEquals(new Byte((byte) 2), o.getOBytes()[1]);
        assertEquals(new Byte((byte) 3), o.getOBytes()[2]);

        tr.commit();
        session.close();
    }
}
