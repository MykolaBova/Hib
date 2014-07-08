package com.dclonline.db;

import java.sql.SQLException;
import java.util.logging.Logger;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource;

public class DataSourceMySql{

	private DataSource dataSource = null;
	
	public DataSource getDataSource() throws Exception {
		InitialContext ic = null;
		try {
            // Construct DataSource
			MysqlConnectionPoolDataSource dataSource = new MysqlConnectionPoolDataSource();
		      dataSource.setUser("iyusuf");
		      dataSource.setPassword("mypassword");
		      dataSource.setServerName("localhost");
		      dataSource.setPort(3306);
		      dataSource.setDatabaseName("test");
            return dataSource;
        } catch (Exception e) {
			throw new Exception(e);
        } 
	}
}
