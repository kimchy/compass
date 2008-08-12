package org.compass.core.test.component.unmarshallpoly;

import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.test.AbstractTestCase;

public class NoUnmarshallComponentTest extends AbstractTestCase {

    protected String[] getMappings() {
        return new String[]{"component/unmarshallpoly/mapping.cpm.xml"};
    }

    public void test() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();
	
        Master master = new MasterImpl();
        master.setMasterProperty("masterProp1");
        Slave slave = new SlaveImpl();
        slave.setId(1);
        slave.setMaster(master);
        slave.setName("slave1");
        session.save(slave);
        
        Slave slave1 = session.load(Slave.class, 1);
        assertNotNull(slave1);        
        assertEquals("slave1", slave1.getName());
        assertNull(slave1.getMaster());
        
        master = new MasterImpl();
		master.setMasterProperty("prop1");
		
		slave = new SlaveImpl();
		slave.setId(1);
		slave.setMaster(master);
		slave.setName("slave1");
		
		Slave slave2 = new SlaveImpl();
		slave2.setId(2);
		slave2.setMaster(master);
		slave2.setName("slave2");
		
		session.save(slave);
		session.save(slave2);
				
        slave1 = session.load(Slave.class, 1);
        assertNotNull(slave1);
        assertEquals(slave.getName(), slave1.getName());

		tr.commit();
        session.close();
    }
    
}
