package simpleorm.examples;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import simpleorm.dataset.SDataSet;
import simpleorm.dataset.SQuery;
import simpleorm.dataset.SQueryMode;
import simpleorm.dataset.SSelectMode;
import simpleorm.sessionjdbc.SDataLoader;
import simpleorm.sessionjdbc.SSessionJdbc;
import simpleorm.utils.SException;
import simpleorm.utils.SLog;

/**
 * Basic benchmarks that compare SimpleORM performance to raw JDBC. No
 * significant degedation has been found. See the whitepaper for a full
 * description of the tests and results.
 * <p>
 */

public class Benchmarks {

	final static int nrRows = 1000;

	static long startTime;

	static long timeTaken;

	public static void main(String[] argv) throws Exception {
		TestUte.initializeTest(Benchmarks.class); // Look at this code.
		try {
			// rawTests();
			testInit();
			SLog.getSessionlessLogger().setLevel(0);
			jdbcInsert();
			jdbcQuerySequential();
			jdbcQueryRandom();
			jdbcQueryOneRecord();
			jdbcUpdateRandom();
			jdbcUpdateBulk();

			testInit();
			simpleormInsert();
			simpleormJoinQuerySequential();
			simpleormQueryRandom();
			simpleormQueryOneRecord();
			simpleormUpdateRandom();
			simpleormUpdateBulk();
		} finally {
			SSessionJdbc.getThreadLocalSession().close();
		}
	}

	/** Rough simulation of key SimpleORM steps. */
	static void rawTests() {
		SSessionJdbc ses = SSessionJdbc.getThreadLocalSession();
		ses.begin();
		startTime = System.currentTimeMillis();
		HashMap hm = new HashMap();
		for (int hx = 0; hx < nrRows * 10; hx++) {
			// Empty Loop
			// foo("zz");
			String key = "that " + hx; // 0.004 ms
			hm.put(key, "YYYY"); // 0.005 ms (inlc str concat)
			hm.get(key); // 0.002 ms
			// Connection con = SSession.getBegunJDBCConnection();
		}
		SLog.getSessionlessLogger().message("rawTests " + timeTaken());
	}

	static boolean foo(String bar) {
		return (bar.equals("xxx"));
	}

	/** Prepare for tests, Delete old data. */
	static void testInit() throws Exception {
		SLog.getSessionlessLogger().message("Initializing");
		SSessionJdbc ses = SSessionJdbc.getThreadLocalSession();
		ses.begin();

		// / Delete any old data from a previous run.
		//try {
		TestUte.dropTableNoError(ses, "XX_EMPLOYEE"); //	ses.rawUpdateDB("DROP TABLE XX_EMPLOYEE");
        TestUte.dropTableNoError(ses, "XX_DEPARTMENT"); //	ses.rawUpdateDB("DROP TABLE XX_DEPARTMENT");
		//} catch (SException.Jdbc ex) {
		//}
		; // Tables may not exist.
		ses.rawUpdateDB(ses.getDriver().createTableSQL(Department.DEPARTMENT));
		ses.rawUpdateDB(ses.getDriver().createTableSQL(Employee.EMPLOYEE));

		SDataLoader deptDL = new SDataLoader(ses, Department.DEPARTMENT);
		deptDL.insertRecords(new Object[][] {
				{ "D100", "One00", "Count Pennies", "10000", "200000" },
				{ "D200", "Two00", "Be Happy", "20000", "150000" },
				{ "D300", "Three00", "Enjoy Life", "30000", "100000" } });

		ses.commit();
	}

	static double theTotSal;

	static long insertTime;

	static void jdbcInsert() throws Exception {
		SSessionJdbc ses = SSessionJdbc.getThreadLocalSession();
		ses.begin();
		Connection con = ses.getJdbcConnection();
		startTime = System.currentTimeMillis();

		PreparedStatement insp = 
            con.prepareStatement("INSERT INTO XX_EMPLOYEE (\"Empee*Id\", ENAME, PHONE_NR, SALARY, NR_DEPENDENTS, DEPT_ID) VALUES (?, ?, ?, ?, ?, ?)");

		double totSal = 0;
		for (int row = 0; row < nrRows; row++) {
			insp.setString(1, row + 1000000 + "");
			insp.setString(2, "Name " + row);
			insp.setString(3, "(123) 456 789");
			insp.setDouble(4, row * 10);
			totSal += row * 10;
			insp.setInt(5, row % 4);
			insp.setString(6, "D" + ((row % 3) + 1) + "00");
			insp.executeUpdate();
		}

		ses.commit();
		SLog.getSessionlessLogger().message("jdbcInsert " + timeTaken() + " (microseconds per row).  ");
		insertTime = timeTaken;
		theTotSal = totSal;
	}

	static void simpleormInsert() throws Exception {
		startTime = System.currentTimeMillis();
		SSessionJdbc ses = SSessionJdbc.getThreadLocalSession();
		ses.begin();

		double totSal = 0;
		for (int row = 0; row < nrRows; row++) {
			Employee insp = ses.findOrCreate(Employee.EMPLOYEE, SQueryMode.SASSUME_CREATE, row +1000000+ "");
			insp.setString(Employee.NAME, "Name " + row);
			insp.setString(Employee.PHONE_NR, "(123) 456 789");
			insp.setDouble(Employee.SALARY, row * 10);
			totSal += row * 10;
			insp.setInt(Employee.NR_DEPENDENTS, row % 4);
			insp.setReference(Employee.DEPARTMENT,
			      ses.findOrCreate(Department.DEPARTMENT, "D" + ((row % 3) + 1) + "00"));
		}

		ses.commit();
		TestUte.assertTrue(totSal == theTotSal);
		sormResult("simpleormInsert", insertTime, ses);
	}

	static long seqTime;

	static double theTotBud = 0;

	static void jdbcQuerySequential() throws Exception {
		SSessionJdbc ses = SSessionJdbc.getThreadLocalSession();
		ses.begin();
        theTotBud=0;
		Connection con = ses.getJdbcConnection();
		startTime = System.currentTimeMillis();

		PreparedStatement selp = con
				.prepareStatement("SELECT \"Empee*Id\", E.ENAME, PHONE_NR, SALARY, NR_DEPENDENTS, D.BUDGET FROM XX_EMPLOYEE E, XX_DEPARTMENT D WHERE E.DEPT_ID = D.DEPT_ID ORDER BY E.\"Empee*Id\"");
		// Warning: this is an inner join. A better query on most DBMSs
		// is to use a subsect which will effectively produce an outer join.

		ResultSet rs1 = selp.executeQuery();
		double totSal = 0;
		while (rs1.next()) {
			String id = rs1.getString(1);
			String name = rs1.getString(2);
			String phone = rs1.getString(3);
			double sal = rs1.getDouble(4);
			totSal += sal;
			int deps = rs1.getInt(5);
			double bud = rs1.getDouble(6);
			theTotBud += bud;
		}
		rs1.close();

		ses.commit();
		SLog.getSessionlessLogger().message("jdbcQuerySequential (join) " + timeTaken() + ses.getStatistics());
		seqTime = timeTaken;
		TestUte.assertEqual(totSal, theTotSal);
	}
	
	static void simpleormJoinQuerySequential() throws Exception {
		
		startTime = System.currentTimeMillis();
		SSessionJdbc ses = SSessionJdbc.getThreadLocalSession();
		ses.begin();

		// Now use join. Will maximize cache use.
		//selp = Employee.meta.select((String) null, null);
		SQuery<Employee> query = new SQuery<Employee>(Employee.EMPLOYEE).innerJoin(Employee.DEPARTMENT).select(SSelectMode.SNORMAL)
            .ascending(Employee.EMPEE_ID);
		//SResultSet rs1 = query.execute();
		List<Employee> rs1 = ses.query(query);
		double totSal = 0;
		double totBud = 0;
		//while (rs1.next()) {
		for (Employee emp : rs1) {
//			Employee emp = (Employee) rs1.getRecord();
			String id = emp.getString(Employee.EMPEE_ID);
			String name = emp.getString(Employee.NAME);
			String phone = emp.getString(Employee.PHONE_NR);
			double sal = emp.getDouble(Employee.SALARY);
			totSal += sal;
			int deps = emp.getInt(Employee.NR_DEPENDENTS);
			// Won't trigger query
			Department dept = emp.findReference(emp.DEPARTMENT);
			double bud = dept.getDouble(dept.BUDGET);
			totBud += bud;
		}
        TestUte.assertEqual(theTotSal, totSal); // 49950000.0

		ses.commit();
		TestUte.assertTrue(totSal == theTotSal);
		TestUte.assertTrue(totBud == theTotBud);
		sormResult("simpleormQuerySequential (join)", seqTime,ses);
		
		// Test offset
		ses.begin();
		query = new SQuery<Employee>(Employee.EMPLOYEE).innerJoin(Employee.DEPARTMENT).select(SSelectMode.SNONE)
            .ascending(Employee.EMPEE_ID)
            .setLimit(1).setOffset(552);
		rs1 = ses.query(query);
		String name = "";
		//while (rs1.next()) {
		for (Employee emp : rs1) {
//			Employee emp = (Employee) rs1.getRecord();
			name = emp.getString(Employee.NAME);
			TestUte.assertTrue( ! emp.isValid(Employee.DEPARTMENT)); // join with null selectMode should not retrieve reference
		}

		if (nrRows == 1000) TestUte.assertEqual("Name 552", name);
        
		ses.commit();
		
	}

	static double theRandTotSal;

	static long randTime;

	static void jdbcQueryRandom() throws Exception {
		SSessionJdbc ses = SSessionJdbc.getThreadLocalSession();
        theRandTotSal=0;
		ses.begin();
		Connection con = ses.getJdbcConnection();
		startTime = System.currentTimeMillis();

		PreparedStatement selp = con
				.prepareStatement("SELECT ENAME, PHONE_NR, SALARY, NR_DEPENDENTS FROM XX_EMPLOYEE WHERE \"Empee*Id\" = ?");

		Random rand = new Random(0);
		double totSal = 0;
		for (int row = 0; row < nrRows; row++) {
			int key = (rand.nextInt() & 0x7FFFFFFF) % nrRows + 1000000;
			selp.setString(1, new Integer(key).toString());
			ResultSet rs1 = selp.executeQuery();
			if (!rs1.next())
				throw new SException.Test("Could not find " + key);
			String name = rs1.getString(1);
			String phone = rs1.getString(2);
			double sal = rs1.getDouble(3);
			totSal += sal;
			int deps = rs1.getInt(4);
			rs1.close();
		}

		ses.commit();
		SLog.getSessionlessLogger().message("jdbcQueryRandom " + timeTaken() + ses.getStatistics());
		randTime = timeTaken;
		theRandTotSal = totSal;
	}

	static void simpleormQueryRandom() throws Exception {
		startTime = System.currentTimeMillis();
		SSessionJdbc ses = SSessionJdbc.getThreadLocalSession();
		ses.begin();

		Random rand = new Random(0);
		double totSal = 0;
		for (int row = 0; row < nrRows; row++) {
			int key = (rand.nextInt() & 0x7FFFFFFF) % nrRows + 1000000;
			Employee emp = ses.findOrCreate(Employee.EMPLOYEE, key + "");
			String id = emp.getString(Employee.EMPEE_ID);
			String name = emp.getString(Employee.NAME);
			String phone = emp.getString(Employee.PHONE_NR);
			double sal = emp.getDouble(Employee.SALARY);
			totSal += sal;
			int deps = emp.getInt(Employee.NR_DEPENDENTS);
		}

		ses.commit();
		TestUte.assertTrue(totSal == theRandTotSal);
		sormResult("simpleormQueryRandom", randTime, ses);
	}

	static double oneTotSal; 
    static long fieldTime;

	static void jdbcQueryOneRecord() throws Exception {
        oneTotSal=0;
		SSessionJdbc ses = SSessionJdbc.getThreadLocalSession();
		ses.begin();
		Connection con = ses.getJdbcConnection();;

        double totSal = 0;
        startTime = System.currentTimeMillis();
        for (int row = 0; row < nrRows; row++) {
            PreparedStatement selp = con.prepareStatement("SELECT ENAME, PHONE_NR, SALARY, NR_DEPENDENTS FROM XX_EMPLOYEE WHERE \"Empee*Id\" = ?");

            selp.setString(1, "1000000");
            ResultSet rs1 = selp.executeQuery();
            rs1.next();
            String name = rs1.getString(1);
            String phone = rs1.getString(2);
            double sal = rs1.getDouble(3);
            totSal += sal;
            int deps = rs1.getInt(4);
            rs1.close();
            selp.close();
        }

		ses.commit();
		SLog.getSessionlessLogger().message("jdbcQueryOneRecord " + (timeTaken()) + ses.getStatistics());
        oneTotSal = totSal;
		fieldTime = timeTaken;
	}

	static void simpleormQueryOneRecord() throws Exception {
		SSessionJdbc ses = SSessionJdbc.getThreadLocalSession();
		ses.begin();

	     double totSal = 0;
        startTime = System.currentTimeMillis();
        for (int row = 0; row < nrRows; row++) {
            Employee emp = ses.findOrCreate(Employee.EMPLOYEE, "1000000");
            String id = emp.getString(Employee.EMPEE_ID);
            String name = emp.getString(Employee.NAME);
            String phone = emp.getString(Employee.PHONE_NR);
            double sal = emp.getDouble(Employee.SALARY);
            totSal += sal;
            int deps = emp.getInt(Employee.NR_DEPENDENTS);
        }

		ses.commit();
        TestUte.assertEqual(oneTotSal, totSal);
		sormResult("simpleormQueryOneRecord", fieldTime, ses);        
	}

	static double theUpdTotSal;

	static long rupdTime;

	static void jdbcUpdateRandom() throws Exception {
        theUpdTotSal=0;
		SSessionJdbc ses = SSessionJdbc.getThreadLocalSession();
		ses.begin();
		Connection con = ses.getJdbcConnection();
		startTime = System.currentTimeMillis();

		PreparedStatement selp = con
				.prepareStatement("UPDATE XX_EMPLOYEE SET SALARY = ? WHERE \"Empee*Id\" = ?");

		Random rand = new Random(1);
		double totSal = 0;
		for (int row = 0; row < nrRows; row++) {
			int key = (rand.nextInt() & 0x7FFFFFFF) % nrRows + 1000000;
			selp.setString(2, new Integer(key).toString());
			double sal = key - 1000000;
			totSal += sal;
			selp.setInt(1, key);
			selp.executeUpdate();
		}

		ses.commit();
		SLog.getSessionlessLogger().message("jdbcUpdateRandom " + timeTaken()+ ses.getStatistics());
		theUpdTotSal = totSal;
		rupdTime = timeTaken;
		
	}

	static void simpleormUpdateRandom() throws Exception {
		startTime = System.currentTimeMillis();
		SSessionJdbc ses = SSessionJdbc.getThreadLocalSession();
		ses.begin();

		Random rand = new Random(1);
		double totSal = 0;
		for (int row = 0; row < nrRows; row++) {
			int key = (rand.nextInt() & 0x7FFFFFFF) % nrRows + 1000000;
			Employee emp = ses.findOrCreate(Employee.EMPLOYEE, key + "");
			double sal = key - 1000000;
			totSal += sal;
			emp.setDouble(emp.SALARY, sal);
		}

		SDataSet ds =  ses.commitAndDetachDataSet();
		TestUte.assertTrue(totSal == theUpdTotSal);
		sormResult("simpleormUpdateRandom", rupdTime, ses); 
		
		startTime = System.currentTimeMillis();
		int nds = 10;
		SDataSet[] dss = new SDataSet[nds];
		for (int dx=0; dx<nds; dx++) 
			dss[dx] = ds.clone(); 
		SLog.getSessionlessLogger().message(nds + " Clones of DataSet with " + nrRows + " Rows " + (System.currentTimeMillis() - startTime) + ses.getStatistics());

	}
 
	static void jdbcUpdateBulk() throws Exception {
//		SSessionJdbc ses = SSessionJdbc.getThreadLocalSession();
//		ses.begin();
//		Connection con = ses.getJdbcConnection();
//		startTime = System.currentTimeMillis();
//
//		PreparedStatement selp = con
//				.prepareStatement("UPDATE XX_EMPLOYEE SET SALARY = SALARY * 0.1");
//		selp.executeUpdate();
//
//		ses.commit();
//		SLog.getSessionlessLogger().message("jdbcUpdateBulk " + timeTaken() + ses.getStatistics());
	}

	static void simpleormUpdateBulk() throws Exception {
		SSessionJdbc ses = SSessionJdbc.getThreadLocalSession();
		ses.begin();
		Connection con = ses.getJdbcConnection();
		startTime = System.currentTimeMillis();

		ses.rawUpdateDB("UPDATE XX_EMPLOYEE SET SALARY = SALARY * 0.1");

		ses.commit();
		SLog.getSessionlessLogger().message("simpleormUpdateBulk " + timeTaken());
	}

    /** Microseconds per row. */
	static long timeTaken() {
		timeTaken = ((System.currentTimeMillis() - startTime)*1000) / nrRows;
		return timeTaken;
	}

    static long beginTime = System.currentTimeMillis();
	/**
	 * Makes timeing assertions, but only for PostgreSQL. Other databases vary,
	 * especially HSQL with its in memory/checkpointing model.
	 */
	static void sormResult(String stage, long jdbc, SSessionJdbc session) {
        long sorm = timeTaken();
        SLog.getSessionlessLogger().message("--- " + stage + "                                         ".substring(0, 40 - stage.length()) + sorm + "   (" + jdbc + ") (microsecond/row) " 
            + session.getStatistics());
//        if (sorm > 2 * jdbc
//            && !( session.getDriver() instanceof SDriverHSQL) )
//        SLog.getSessionlessLogger().message("========== TOO SLOW =========)" );            
        // Hsql is a weird in memory database, not a realistic test.
	}

}

//  8 Aug 08.  Oracle.  Equals is bad.
// ------ simpleormInsert                         3089   (733)
// ------ simpleormQuerySequential (join)         187   (47)
// ------ simpleormQueryRandom                    453   (265)
// ------ simpleormQueryOneRecord                 0   (515)
// ------ simpleormUpdateRandom                   950   (640)
// ---simpleormUpdateBulk 250

// 9 Aug 08 Oracle Equals Fiexed.
//------ simpleormInsert                         936   (593) time 6926 ctime 0
//------ simpleormQuerySequential (join)         78   (47) time 7004 ctime 0
//------ simpleormQueryRandom                    359   (281) time 7472 ctime 0
//------ simpleormQueryOneRecord                 16   (515) time 7488 ctime 0
//------ simpleormUpdateRandom                   686   (265) time 8174 ctime 0
//---simpleormUpdateBulk 47

// 9 Aug 08 HSQL
//------ simpleormInsert                         124   (46) time 312 ctime 0
//------ simpleormQuerySequential (join)         32   (16) time 344 ctime 0
//------ simpleormQueryRandom                    47   (16) time 406 ctime 0
//------ simpleormQueryOneRecord                 16   (31) time 422 ctime 0
//------ simpleormUpdateRandom                   93   (47) time 515 ctime 0
//---simpleormUpdateBulk 47

// These numbers do not make sense.  
// Delta for Oracle should be JDBC Oracle + SOrm HSQL, but somehow much larger.
// Also slower with -server!