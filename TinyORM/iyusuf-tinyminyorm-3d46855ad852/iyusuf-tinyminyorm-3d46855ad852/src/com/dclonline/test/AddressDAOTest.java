package com.dclonline.test;

import static org.junit.Assert.*;

import java.util.ArrayList;

import javax.sql.DataSource;

import org.junit.Test;

import com.dclonline.db.Address;
import com.dclonline.db.AddressDAO;
import com.dclonline.db.DataSourceMySql;
import com.dclonline.db.Person;
import com.dclonline.db.PersonDAO;

public class AddressDAOTest {

	@Test
	public void testGetAllPersons() {
		try {
			DataSource ds = new DataSourceMySql().getDataSource();
			ArrayList<Address> addresses = new AddressDAO(ds).getAllAddresses();
			for(Address a : addresses){
				System.out.println(a.toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
}
