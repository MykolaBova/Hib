package simpleorm.examples;

import java.sql.Connection;

import simpleorm.dataset.SQuery;
import simpleorm.dataset.SQueryResult;
import simpleorm.sessionjdbc.SDataLoader;
import simpleorm.sessionjdbc.SSessionJdbc;
import simpleorm.utils.SLog;

/** Minimal amount of code just to demo API. */
public class A0TinyExample {
	
	public static void main(String[] argv) throws Exception {
    	Class.forName("org.hsqldb.jdbcDriver");
    	Connection con = java.sql.DriverManager.getConnection("jdbc:hsqldb:hsqlTempFiles;shutdown=true;", "sa", "");
		SSessionJdbc ses = SSessionJdbc.open(con, "Tiny");
		
		ses.begin();
		ses.getDriver().dropTableNoError("XX_DEPARTMENT");
		ses.commit();
		
		ses.begin();
        ses.rawUpdateDB(ses.getDriver().createTableSQL(Department.DEPARTMENT));
		ses.commit();
		
		ses.begin();
		SDataLoader deptDL = new SDataLoader(ses, Department.DEPARTMENT);
		deptDL.insertRecords(new Object[][] {
				{ "100", "One00", "Count Pennies", 10000, 200000 },
				{ "200", "Two00", "Be Happy", 20000, 300000 },
				{ "300", "Three00", "Enjoy Life", 30000, 150000 } });
		ses.commit();

		ses.begin();
		SQueryResult<Department> res  = ses.query(new SQuery(Department.DEPARTMENT));
		SLog.getSessionlessLogger().message("Departments: " + res);		
		ses.commit();
		ses.close();
	}

}
