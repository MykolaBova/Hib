package com.dclonline.test;

import static org.junit.Assert.*;

import org.junit.Test;

import com.dclonline.db.DataSourceMySql;

import javax.sql.DataSource;

public class DataSourceTest {

	@Test
	public void test() {
		DataSource ds = null;
		try {
			ds = new DataSourceMySql().getDataSource();
			ds.getConnection();
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

}
