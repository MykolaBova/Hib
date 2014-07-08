package simpleorm.examples;

import java.util.List;

import simpleorm.dataset.SFieldScalar;
import simpleorm.dataset.SQuery;
import simpleorm.dataset.SRecordGeneric;
import simpleorm.dataset.SRecordTransient;
import simpleorm.examples.EnumInKeyRecord.TypeTestEnum;
import simpleorm.sessionjdbc.SSessionJdbc;
import simpleorm.utils.SException;
import simpleorm.utils.SLog;

/**
 * Tests Queries
 * @author aberglas
 */
public class QueryTests {
    	public static void main(String[] argv) throws Exception {
		SSessionJdbc ses = TestUte.initializeTest(QueryTests.class); // Look at this code
		try {
			TestUte.createDeptEmp(ses);
			TestUte.createEnumInKeyRecordDDL(ses);
            queryRawTest();
            queryOrderByTest();
            queryTest();
            queryJoinTest();
			queryOffsetTests();
            
		} finally {
			ses.close();
		}
	}

    static void queryRawTest() {
       SLog.getSessionlessLogger().message("\n################ queryRawTest #################");
  		SSessionJdbc ses = SSessionJdbc.getThreadLocalSession();
        ses.begin();
        List<SRecordGeneric> Sals = ses.rawQuery(
              "SELECT E.DEPT_ID, SUM(E.SALARY) as \"SUMSAL\" FROM  XX_EMPLOYEE E " +
              " WHERE E.DEPT_ID IS NOT NULL" +
              " GROUP BY E.DEPT_ID ORDER BY E.DEPT_ID", true);
        TestUte.assertEqual(20000.0, Sals.get(1).get("SUMSAL"));      

        Object Sal200 = ses.rawQuerySingle(
              "SELECT SUM(E.SALARY) as SUMSAL FROM  XX_EMPLOYEE E " +
              " WHERE E.DEPT_ID = ?  GROUP BY E.DEPT_ID ORDER BY E.DEPT_ID",true, "200");
        TestUte.assertEqual(20000.0, Sal200);
        
        // Test rawQueryMap and rawQueryOneMap
        SRecordGeneric salarySum = ses.rawQuery(
                "SELECT SUM(E.SALARY) as \"SUMSAL\" FROM  XX_EMPLOYEE E " +
                " WHERE E.DEPT_ID = ?  GROUP BY E.DEPT_ID ORDER BY E.DEPT_ID",true, "200").exactlyOne();
          TestUte.assertEqual(20000.0, salarySum.get("SUMSAL"));
          
        salarySum = ses.rawQuery(
                  "SELECT SUM(E.SALARY) as \"SUMSAL\" FROM  XX_EMPLOYEE E " +
                  " WHERE E.DEPT_ID = ?  GROUP BY E.DEPT_ID ORDER BY E.DEPT_ID",true, "200").exactlyOne();
            TestUte.assertEqual(20000.0, salarySum.get("SUMSAL"));
        
        boolean testPassed = false;
        try {
        	ses.rawQuery(
                    "SELECT SUM(E.SALARY) as \"SUMSAL\" FROM  XX_EMPLOYEE E " +
                    " WHERE E.DEPT_ID = ?  GROUP BY E.DEPT_ID ORDER BY E.DEPT_ID",true, "none")
                    .exactlyOne();
        }
        catch (SException.Data e){
        	testPassed = true; // fine
        }
        TestUte.assertTrue(testPassed);
        ses.commit();

    }
    
    /** Make sure multiple orderBy are taken into account and their precedence is respected */
    static void queryOrderByTest() {
        SLog.getSessionlessLogger().message("\n################ queryOrderByTest #################");
   		SSessionJdbc ses = SSessionJdbc.getThreadLocalSession();
         ses.begin();
         SQuery<Department> qry = new SQuery<Department>(Department.DEPARTMENT);
         qry.descending(Department.BUDGET).ascending(Department.MAX_SALARY).ascending(Department.NAME).ascending(Department.NAME).ascending(Department.BUDGET);
         List<Department> depts = ses.query(qry);
         
         TestUte.assertEqual("400", depts.get(0).getString(Department.DEPT_ID));
         TestUte.assertEqual("500", depts.get(1).getString(Department.DEPT_ID));
         TestUte.assertEqual("300", depts.get(2).getString(Department.DEPT_ID));
         TestUte.assertEqual("200", depts.get(3).getString(Department.DEPT_ID));
         TestUte.assertEqual("100", depts.get(4).getString(Department.DEPT_ID));
         
         ses.commit();

     }
    static private void checkMaxSalary(List<Department> depts, double goal) {
        double ms =0;
        for (Department d : depts)
            ms += d.getDouble(Department.MAX_SALARY);
        TestUte.assertEqual(ms, goal);
    }
	static void queryTest() {
		SLog.getSessionlessLogger().message("\n################ queryTest #################");
		SSessionJdbc ses = SSessionJdbc.getThreadLocalSession();
		ses.begin();

		// / BUDGET > ?, IN
		SQuery<Department> deptq = new SQuery<Department>(Department.DEPARTMENT)
            .gt(Department.BUDGET,	new Integer(10000))
			.lt(Department.NAME, "H")
            .in(Department.BUDGET, 30000, 40000)
            .descending(Department.NAME);
        List<Department> deptq1 = ses.query(deptq);        
        checkMaxSalary(deptq1, 150000 + 120000);
        

 		Department d100 = ses.findOrCreate(Department.DEPARTMENT, "100");
		@SuppressWarnings("unused")
		Department d200 = ses.findOrCreate(Department.DEPARTMENT, "200");
		SQuery<Employee> empdeptqa = new SQuery<Employee>(Employee.EMPLOYEE)
				.eq(Employee.DEPARTMENT, d100);
                //.ne(Employee.DEPARTMENT, d200)
				//.isNotNull(Employee.DEPARTMENT); 
		Employee e100a = ses.query(empdeptqa).oneOrNone();
		TestUte.assertEqual(e100a.getString(Employee.NAME), "One00");
        
        SQuery<Employee> empdeptqb = new SQuery<Employee>(Employee.EMPLOYEE)
		   .isNull(Employee.DEPARTMENT); 
		Employee e100b = ses.query(empdeptqb).oneOrNone();
		TestUte.assertEqual(e100b.getString(Employee.NAME), "Three00");

		ses.commit();	
        
        ses.begin();
          SQuery<Employee> empraw = new SQuery<Employee>(Employee.EMPLOYEE).select(new SFieldScalar[]{Employee.EMPEE_ID, Employee.NAME})
              .rawSql("SELECT \"Empee*Id\", ENAME || 'X' FROM XX_EMPLOYEE WHERE \"Empee*Id\" = ? ", "100");
          Employee e100q = ses.query(empraw).oneOrNone();
          TestUte.assertEqual("One00X", e100q.getString(Employee.NAME));
        ses.commit();
				
        // Create and find record with a SFieldEnum in primary key
        ses.begin();
        EnumInKeyRecord record = ses.create(EnumInKeyRecord.META, "REC_0001", TypeTestEnum.TYPE_A);
        TestUte.assertTrue(record!=null);
        ses.commitAndDetachDataSet();
        
        ses.begin();
        EnumInKeyRecord record2 = ses.find(EnumInKeyRecord.META, "REC_0001", TypeTestEnum.TYPE_A);
        TestUte.assertTrue(record2!=null);
        TestUte.assertEqual(record, record2);
        ses.commit();
	}
	
	static void queryJoinTest() {
        SLog.getSessionlessLogger().message("\n################ queryJoinTest #################");

  		SSessionJdbc ses = SSessionJdbc.getThreadLocalSession();

		ses.begin();
		SQuery<Employee> joinQ1 = new SQuery<Employee>(Employee.EMPLOYEE)
            .innerJoin(Employee.DEPARTMENT)
            .eq(Department.NAME, "F100");

        Employee e100Q1 = ses.query(joinQ1).oneOrNone();
		Department dept = (Department) e100Q1.findReference(Employee.DEPARTMENT);
		TestUte.assertTrue(dept.wasInCache());
		TestUte.assertEqual(e100Q1.getString(Employee.NAME), "One00");
		TestUte.assertEqual(e100Q1.findReference(Employee.DEPARTMENT).getString(Department.NAME), "F100");

        
        SQuery<Employee> subQ1 = new SQuery<Employee>(Employee.EMPLOYEE) 
          .rawPredicate(
            "? = (SELECT BUDGET FROM XX_DEPARTMENT D WHERE D.DEPT_ID = XX_EMPLOYEE.DEPT_ID)",
            20000);
        Employee e300s1 = ses.query(subQ1).oneOrNone();
        TestUte.assertEqual(e300s1.getString(Employee.NAME), "Two00");
        
//        SQuery<Employee> subQ2 = new SQuery<Employee>(Employee.meta) 
//          .fieldRelopParameter(Employee.MANAGER, Employee.NAME, "=", "One00");
//        List<Employee> e100subs = ses.query(subQ2);
//        TestUte.assertEqual(e100subs.size(), 2);
        
        SQuery<Employee> rawJoinQ = new SQuery<Employee>(Employee.EMPLOYEE).as("XX_EMPLOYEE")
            .rawLeftJoin("XX_EMPLOYEE", Department.DEPARTMENT, "D.DEPT_ID = XX_EMPLOYEE.DEPT_ID").as("D")
            .rawPredicate("D.DNAME = 'F100'");

        @SuppressWarnings("unused")
		Employee e100Q1b = ses.query(rawJoinQ).oneOrNone();
        dept = (Department) e100Q1.findReference(Employee.DEPARTMENT);
		TestUte.assertTrue(dept.wasInCache());
		TestUte.assertEqual(e100Q1.getString(Employee.NAME), "One00");
		TestUte.assertEqual(e100Q1.findReference(Employee.DEPARTMENT).getString(Department.NAME), "F100");
        
        ses.commit();
    }
    
    static void queryOffsetTests() {
		SLog.getSessionlessLogger().message("\n################ queryOffsetTests #################");
        queryOffsetTest(0, Integer.MAX_VALUE, 60000);
        queryOffsetTest(0, 1, 10000);
        queryOffsetTest(1, 1, 20000);
        queryOffsetTest(2, 1, 30000);       
        queryOffsetTest(5, 1, 0);       
        queryOffsetTest(1, 10, 50000);       
    }
        
    static void queryOffsetTest(int offset, int limit, double expected) {
    	SSessionJdbc ses = SSessionJdbc.getThreadLocalSession();
		ses.begin();

		// offset/limit tests
		List<Employee> res = ses.query(new SQuery<Employee>(Employee.EMPLOYEE)
            .ascending(Employee.EMPEE_ID)
            .setOffset(offset).setLimit(limit));
        double sum=0;
        for (Employee emp : res) {
            sum += emp.getDouble(Employee.SALARY);
		}
		TestUte.assertEqual("" + expected, sum + "");
		ses.commit();
		
	}
    
    static void queryAliasTest() {
    	SSessionJdbc ses = SSessionJdbc.getThreadLocalSession();
		ses.begin();

		// alias tests
		List<Employee> res = ses.query(new SQuery<Employee>(Employee.EMPLOYEE).as("empalias")
				                                           .eq("empalias", Employee.DEPT_ID, "100"));
        
		TestUte.assertTrue(res.size() == 1);
		Employee emp = res.get(0);
		TestUte.assertEqual(emp.getString(Employee.NAME), "One00");
		ses.commit();
		
	}

}
