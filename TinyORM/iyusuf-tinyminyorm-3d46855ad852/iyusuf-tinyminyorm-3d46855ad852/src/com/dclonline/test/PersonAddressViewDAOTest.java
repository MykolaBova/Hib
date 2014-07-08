package com.dclonline.test;

import static org.junit.Assert.*;

import java.util.ArrayList;

import javax.sql.DataSource;

import org.junit.Test;

import com.dclonline.db.DataSourceMySql;
import com.dclonline.db.Person;
import com.dclonline.db.PersonAddressView;
import com.dclonline.db.PersonAddressViewDAO;
import com.dclonline.db.PersonDAO;

public class PersonAddressViewDAOTest {

	@Test
	public void testGetAllPersons() {
		try {
			DataSource ds = new DataSourceMySql().getDataSource();
			ArrayList<PersonAddressView> rows = new PersonAddressViewDAO(ds).getAllRows();
			for(PersonAddressView v : rows){
				System.out.println(v.toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
		
	}

}
