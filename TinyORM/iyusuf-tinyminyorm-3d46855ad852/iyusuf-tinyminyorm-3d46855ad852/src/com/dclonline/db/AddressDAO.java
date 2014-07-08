package com.dclonline.db;

import java.util.ArrayList;
import javax.sql.DataSource;


public class AddressDAO {
	
	private DataSource ds = null;
	
	private AddressDAO(){}
	
	public AddressDAO(DataSource ds){
		this.ds = ds;
	}
	
	public ArrayList<Address> getAllAddresses() throws Exception {
		
		String sql = "select * from addresses";
		
		
		ArrayList<Address> addresses = new Mapper().fetchData(ds, Address.class, sql);
		
		return addresses;

	}
}
