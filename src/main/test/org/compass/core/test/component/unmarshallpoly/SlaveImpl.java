package org.compass.core.test.component.unmarshallpoly;

public class SlaveImpl implements Slave {

	private int id;
	private String name;	
	private Master master;
		
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	public Master getMaster() {
		return master;
	}
	
	public void setMaster(Master master) {
		this.master = master;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("Slave: id=")
			.append(id)
			.append(", name=")
			.append(name);
		if (master!=null) {
			buf.append(", masterProperty: ")
			.append(master.getMasterProperty());
		}
		return buf.toString();
	}
}
