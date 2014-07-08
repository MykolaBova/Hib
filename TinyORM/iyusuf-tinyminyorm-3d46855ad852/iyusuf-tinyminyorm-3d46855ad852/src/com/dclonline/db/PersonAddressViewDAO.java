package com.dclonline.db;

import java.util.ArrayList;

import javax.sql.DataSource;

public class PersonAddressViewDAO {
	private DataSource ds = null;
	
	private PersonAddressViewDAO(){}
	
	public PersonAddressViewDAO(DataSource ds){
		this.ds = ds;
	}
	
	public ArrayList<PersonAddressView> getAllRows() throws Exception {
		
		String sql= "SELECT " +
					"p.lname as lastName, p.fname as firstName, a.zip as zipCode " +
					"FROM   " +
					"test.persons p INNER JOIN test.addresses a " +
					"ON " +
					"p.id = a.personid";
		
		ArrayList<PersonAddressView> personAddressViewRows = new Mapper().fetchData(ds, PersonAddressView.class, sql);
		
		return personAddressViewRows;

	}
}

