package com.dclonline.db;

import java.util.ArrayList;
import javax.sql.DataSource;


public class PersonDAO {
	
	private DataSource ds = null;
	
	private PersonDAO(){}
	
	public PersonDAO(DataSource ds){
		this.ds = ds;
	}
	
	public ArrayList<Person> getAllPersons() throws Exception {
		String sql = "select * from persons";
		return callMapper(sql);
	}
	
	public ArrayList<Person> getPersonById(int id) throws Exception {
		String sql = "select * from persons where id="+id;
		return callMapper(sql);
	}
	
	public ArrayList<Person> getPersonsByFirstName(String firstName) throws Exception {
		String sql = "select * from persons where fname='" + firstName + "'";
		return callMapper(sql);
	}
	
	public ArrayList<Person> callMapper(String sql) throws Exception{
		ArrayList<Person> persons = new Mapper().fetchData(ds, Person.class, sql);
		return persons;
	}
}
