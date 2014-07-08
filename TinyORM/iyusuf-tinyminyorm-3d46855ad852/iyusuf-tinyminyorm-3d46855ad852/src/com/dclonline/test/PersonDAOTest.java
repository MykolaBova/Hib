package com.dclonline.test;

import static org.junit.Assert.*;

import java.util.ArrayList;

import javax.sql.DataSource;

import org.junit.Test;

import com.dclonline.db.DataSourceMySql;
import com.dclonline.db.Person;
import com.dclonline.db.PersonDAO;

public class PersonDAOTest {

	@Test
	public void testGetAllPersons() {
		try {
			DataSource ds = new DataSourceMySql().getDataSource();
			ArrayList<Person> persons = new PersonDAO(ds).getAllPersons();
			for(Person p : persons){
				System.out.println(p.toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
		
	}
	
	@Test
	public void testGetPersonById() {
		int id = 1;
		try {
			DataSource ds = new DataSourceMySql().getDataSource();
			ArrayList<Person> persons = new PersonDAO(ds).getPersonById(id);
			for(Person p : persons){
				System.out.println(p.toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
		
	}
	
	@Test
	public void testGetPersonByFirstName() {
		String firstName = "John";
		try {
			DataSource ds = new DataSourceMySql().getDataSource();
			ArrayList<Person> persons = new PersonDAO(ds).getPersonsByFirstName(firstName);
			for(Person p : persons){
				System.out.println(p.toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
		
	}
	

}
