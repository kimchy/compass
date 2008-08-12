package org.compass.core.test.component.unmarshallpoly;

public interface Slave {

	public int getId();

	public void setId(int id);

	public Master getMaster();

	public void setMaster(Master master);

	public String getName();

	public void setName(String name);

}