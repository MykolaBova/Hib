package com.dclonline.db;

import java.sql.Date;

public class Person implements TableDefinition{

	public Person(){};
	
	//Database Fields
	private int id ;
	private String fname ;
	private String lname ;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getFname() {
		return fname;
	}
	public void setFname(String fname) {
		this.fname = fname;
	}
	public String getLname() {
		return lname;
	}
	public void setLname(String lname) {
		this.lname = lname;
	}
	
	
	@Override
	public String toString() {
		return "Persons [id=" + id + ", fname=" + fname + ", lname=" + lname
				+ "]";
	}

}
