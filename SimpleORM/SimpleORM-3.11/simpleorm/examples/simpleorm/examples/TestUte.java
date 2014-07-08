package simpleorm.examples;

import java.io.FileInputStream;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import simpleorm.sessionjdbc.SDataLoader;
import simpleorm.sessionjdbc.SDriver;
import simpleorm.sessionjdbc.SSessionJdbc;
import simpleorm.utils.SException;
import simpleorm.utils.SLog;
import simpleorm.utils.SUte;

/**
 * Utilities used by all the test cases.
 */
public class TestUte {

    /** Trivial datasource implementation.
     * Would normally get the data source from a connection pooler.
     */
    static class MyDataSource implements DataSource {
        String dburl, dbUserName, dbPassword, dbDriver;

        MyDataSource() {
            dburl = systemProperties.getProperty("database.url", "jdbc:hsqldb:hsqlTempFiles;shutdown=true;");
            if (dburl == null) {
                throw new SException.Error("database.url property not found");
            }
            dburl = dburl.trim();
            dbUserName = systemProperties.getProperty("database.username", "sa");
            dbPassword = systemProperties.getProperty("database.password",  "");

            dbDriver = systemProperties.getProperty("database.driver",  "org.hsqldb.jdbcDriver");
            try {
                Class.forName(dbDriver);
            } catch (Exception ex) {
                throw new SException.Jdbc("Loading " + dbDriver, ex);
            }
        }

        public Connection getConnection() throws SQLException {
            Connection con = java.sql.DriverManager.getConnection(dburl, dbUserName, dbPassword);
            return con;
        }

        public Connection getConnection(String username, String password) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public PrintWriter getLogWriter() throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public int getLoginTimeout() throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void setLogWriter(PrintWriter out) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void setLoginTimeout(int seconds) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public boolean isWrapperFor(Class<?> iface) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public <T> T unwrap(Class<T> iface) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    /**
     * Create and attach a SimpleORM connection.
     */
    static public SSessionJdbc initializeTest(String testName, boolean deprecated) throws Exception {
        System.err.println("\n==== Test " + testName + " ====");
        System.setOut(System.err); // For bad traces to System.out..

        String traceLevel = systemProperties.getProperty("trace.level");
//        System.err.println("IT TL " + traceLevel);
        if (traceLevel != null && !traceLevel.equals(""))
            SLog.getSessionlessLogger().setLevel(Integer.parseInt(traceLevel));
        
        if (false) { // The following is all that is needed
        	Class.forName("org.hsqldb.jdbcDriver");
            Connection con = java.sql.DriverManager.getConnection("jdbc:hsqldb:hsqlTempFiles;shutdown=true;", "sa", "");
            return SSessionJdbc.open(con, testName);
        } else
        	return SSessionJdbc.open(makeDataSource(), testName);        
    }

    static public SSessionJdbc initializeTest(Class test) throws Exception {
        return initializeTest((test.getSimpleName()+"            ").substring(0,8), false);
    }

    static DataSource makeDataSource() {
        String dataSourceName= systemProperties.getProperty("database.datasource",  null);
        DataSource ds;
        if (dataSourceName == null)
          ds = new MyDataSource();
        else {
          // ## This is far too Oracle dependent.
          String dburl = systemProperties.getProperty("database.url", "jdbc:oracle:thin:scott/tiger@orcl").trim();
            try {
                SLog.getSessionlessLogger().connections("Opening DataSource " + dataSourceName + " " + dburl);
                Class<DataSource> dsc = (Class<DataSource>)Class.forName(dataSourceName);
                ds = dsc.newInstance();
                Method[] allMethods = dsc.getDeclaredMethods();
                Method setURL=null;
                for (Method mth: allMethods) 
                    if ("setURL".equals(mth.getName())) 
                        setURL = mth;
                setURL.invoke(ds, dburl);
            } catch (Exception ex) {throw new SException.Error("While Opening " + dataSourceName + " " + dburl, ex); }
        }         
        return ds;
    }
    
    /**
     * These need to be dropped in the correct order to avoid problems with
     * referential integreity constrains.
     */
    static void dropAllTables(SSessionJdbc ses) {
        dropTableNoError(ses, "XX_UGLY_PAY_DTL");
        dropTableNoError(ses, "XX_PSLIP_DTL");
        dropTableNoError(ses, "XX_PAY_SLIP");
        dropTableNoError(ses, "XX_PAY_PERIOD");
        dropTableNoError(ses, "XX_VALIDATED");
        dropTableNoError(ses, "XX_EMPLOYEE");
        dropTableNoError(ses, "XX_DEPARTMENT");
        dropTableNoError(ses, "XX_ENUM_IN_KEY");
        dropTableNoError(ses, "XX_STUDENT");
        dropTableNoError(ses, "XX_LECTURER");
        dropTableNoError(ses, "RefXX");
        dropTableNoError(ses, "RefYY");
        dropTableNoError(ses, "DUAL");
    }

    	/**
	 * Utility routine for dropping tables. Hides JDBC exception caused if the
	 * table does not exist. Mainly used in test cases. (Does not use the
	 * SRecord objects so that they do not all have to be loaded during specific
	 * tests.) Dispatched to SDriver.
	 * <p>
	 * 
	 * WARNING: JDBC error suppression is crude -- a table may indeed exist and
	 * still not be dropped for other reasons, eg. referential integrity.
	 * <p>
	 * 
	 * WARNING: Due to bugs in JDBC etc. each dropped table must be in its own
	 * transaction in case of errors. This routine commits changes.
	 */
	static public void dropTableNoError(SSessionJdbc ses, String table) {
        ses.flush();
		ses.getDriver().dropTableNoError(table);
		ses.commit();
		ses.begin();
	}

    /** Create and populate basic Dept and Emp tables. */
    static void createDeptEmp(SSessionJdbc ses) throws Exception {
        System.out.println("################ CreateDeptEmp #################");
        int level = SLog.getSessionlessLogger().getLevel();
        SLog.getSessionlessLogger().setLevel(20);

        createDeptEmpDDL(ses);
        ses.begin();

        // Create some test data
        SDataLoader deptDL = new SDataLoader(ses, Department.DEPARTMENT);
        Department depts[] = (Department[]) deptDL.insertRecords(new Object[][]{
                {"100", "F100", "Count Pennies", "10000", "200000"},
                {"200", "D200", "Be Happy", "20000", "300000"},
                {"300", "E300", "Enjoy Life", "30000", "150000"},
                {"400", "H400", "Fill Tables", "40000", "100000"},
                {"500", "A500", "More Data", "40000", "120000"}
            });
        SDataLoader empDL = new SDataLoader(ses, Employee.EMPLOYEE);

        Employee e100 = (Employee) empDL.insertRecord(new Object[]{
            "100","One00", "123 456 7890", "10000", "3", "100"/*dept*/, null /*mgr*/, Employee.EType.PERMANENT});
        Employee[] emps1 = (Employee[]) empDL.insertRecords(new Object[][]{
                {"200", "Two00", "123 456 7890", "20000", "0", "200"/*dept*/, "100", Employee.EType.CONTRACT},
                {"300", "Three00", "123 456 7890", "30000", "1", null,"100", "CASUAL"}   });
        ses.commit();

        SLog.getSessionlessLogger().setLevel(level);
        System.out.println("\n################ CreateDeptEmp END #################\n");
    }
    
    static void createDeptEmpDDL(SSessionJdbc ses) {

        ses.begin();
        dropAllTables(ses);

        ses.rawUpdateDB(ses.getDriver().createTableSQL(Department.DEPARTMENT));

        ses.rawUpdateDB(ses.getDriver().createTableSQL(Employee.EMPLOYEE));

        ses.commit();
    }
    
    static void createEnumInKeyRecordDDL(SSessionJdbc ses) {

        ses.begin();

        ses.rawUpdateDB(ses.getDriver().createTableSQL(EnumInKeyRecord.META));

        ses.commit();
    }
    
    static void createPayrollsDDL(SSessionJdbc ses) {
    	
		ses.begin();

		// / Delete any old data from a previous run.
		dropTableNoError(ses, "XX_UGLY_PAY_DTL");
        dropTableNoError(ses, "XX_PSLIP_DTL");
        dropTableNoError(ses, "XX_PAY_SLIP");
        dropTableNoError(ses, "XX_PAY_PERIOD");

		int level = SLog.getSessionlessLogger().getLevel();
		SLog.getSessionlessLogger().setLevel(20);

		// / Dump the internal definitions of the records, mainly for debugging.
		SLog.getSessionlessLogger().message("Period Fields "
				+ SUte.allFieldsString(Payroll.Period.meta));
		SLog.getSessionlessLogger().message("PaySlip Fields "
				+ SUte.allFieldsString(Payroll.PaySlip.meta));
		SLog.getSessionlessLogger().message("SlipDetail Fields "
				+ SUte.allFieldsString(Payroll.PaySlipDetail.meta));
		SLog.getSessionlessLogger().message("UglySlipDetail Fields "
				+ SUte.allFieldsString(Payroll.UglyPaySlipDetail.meta));

        SDriver sd = ses.getDriver();
		ses.rawUpdateDB(sd.createTableSQL(Payroll.Period.meta));
		ses.rawUpdateDB(sd.createTableSQL(Payroll.PaySlip.meta));
		ses.rawUpdateDB(sd.createTableSQL(Payroll.PaySlipDetail.meta));
		ses.rawUpdateDB(sd.createTableSQL(Payroll.UglyPaySlipDetail.meta));

		SLog.getSessionlessLogger().setLevel(level);

		ses.commit();
    }
    
    static void createPayrolls(SSessionJdbc ses) throws Exception {
		System.out.println("################ Payroll Test Init #################");
		
		createDeptEmp(ses);
		createPayrollsDDL(ses);

		// Create some PaySlip and details
		ses.begin();

		// / Create Periods
		SDataLoader prdDL = new SDataLoader(ses, Payroll.Period.meta);
		Payroll.Period[] prds = (Payroll.Period[]) prdDL
				.insertRecords(new Object[][] { { 2001, 1, "1234" },
						{ 2002, 1, "2345" }, { 2002, 2, "3456" } });

		// / Create PaySlips
		Employee e100 = ses.findOrCreate(Employee.EMPLOYEE, "100");
		Employee e200 = ses.findOrCreate(Employee.EMPLOYEE, "200");

		SDataLoader slipDL = new SDataLoader(ses, Payroll.PaySlip.meta);
		Payroll.PaySlip[] slips = (Payroll.PaySlip[]) slipDL
				.insertRecords(new Object[][] {
						{ "100", 2001, 1, "2001, 1, 100" },
						{ "200", 2001, 1, "2001, 1, 200"},
						{ "100", 2002, 2, "2002, 2, 100"},
						{ "200", 2002, 2, "2002, 2, 200"},
						{ "200", 2002, 1, "2002, 1, 100"} });
		// / Create PaySlipDetails
		SDataLoader detailDL = new SDataLoader(ses, Payroll.PaySlipDetail.meta);
		Payroll.PaySlipDetail[] details = (Payroll.PaySlipDetail[]) detailDL
				.insertRecords(new Object[][] { { "100", 2001, 1, 11, 123 },
						{ "100", 2001, 1, 12, 234 }, { "200", 2001, 1, 11, 345 },
						{ "100", 2002, 2, 13, 456 }, { "200", 2002, 1, 22, 567 } });
		ses.commit();
    }
    
    static void assertTrue(boolean cond) {
        if (!cond) {
            throw new SException.Test("Assertion Failed, see stack trace.");
        }
    }

    static void assertEqual(Object s1, Object s2) {
        if (s1 == null && s2 == null) {
            return;
        }
        if (s1 == null || s2 == null) {
            throw new SException.Test("Assertion Failed: " + s1 + " != " + s2);
        }
        if (s1 instanceof Number && s2 instanceof Number) { // needed as 0L != 0!
            Number s1n = (Number)s1;
            Number s2n = (Number)s2;
            if (s1n.doubleValue() != s2n.doubleValue())
                throw new SException.Test("Assertion Failed: doubled " + s1 + " != " + s2);
        } else
            if (!s1.equals(s2)) {
                throw new SException.Test("Assertion Failed: " + s1 + " != " + s2);
        }
    }
    static SystemProperties systemProperties = new SystemProperties();

    /**
     * Simple property mechanism, reads from the ~/simpleotm.properties file.
     * Used by Default configuration to set the database.name etc.
     */
    static public class SystemProperties {

        /**
         * Causes the property cache to be emptied so that properties are
         * re-read from the property file next time a property is required.
         */
        public void refreshProperties() {
            properties = null;
        }
        protected Properties properties;

        protected void loadProperties() {
            if (properties == null) {
                String useHsql = System.getProperty("useHsql");
                System.err.println("===USE HSQL " + useHsql +".");
                if ("true".equals(useHsql)) {
                    properties = new Properties(); // will default to HSQL.
                    return;
                }
                String propsFile = null;
                try {
                    String home = System.getProperty("user.home");
                    propsFile = home + "/simpleorm.properties";
                    SLog.getSessionlessLogger().connections("Loading properties from " + propsFile);
                    properties = new Properties(System.getProperties());
                    FileInputStream in = new FileInputStream(propsFile);
                    properties.load(in);
                    in.close();
                } catch (Exception ex) {
                    throw new SException.Error(
                        "While obtaining Properties from " + propsFile, ex);
                }
            }
        }

        public String getProperty(String property) {
            loadProperties();
            String prop = properties.getProperty(property);
            return prop == null ? null : prop.trim();
        // The .trim() is important!
        // I once spent a long time tacking down a trailing space in a
        // properties file driver name!
        }

        public String getProperty(String property, String defalt) {
            String val = getProperty(property);
            return val == null ? defalt : val;
        }

        public void setProperty(String property, String value) {
            loadProperties();
            properties.setProperty(property, value);
        }
    }
}
