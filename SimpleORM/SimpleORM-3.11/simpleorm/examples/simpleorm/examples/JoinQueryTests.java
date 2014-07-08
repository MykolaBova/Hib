package simpleorm.examples;

import java.util.Collection;
import java.util.List;

import simpleorm.dataset.SQuery;
import simpleorm.dataset.SSelectMode;
import simpleorm.examples.Payroll.PaySlip;
import simpleorm.sessionjdbc.SSessionJdbc;
import simpleorm.utils.SLog;

/**
 * Tests JoinQueries
 * @author franck routier aka alci
 */
public class JoinQueryTests {
    	public static void main(String[] argv) throws Exception {
		SSessionJdbc ses = TestUte.initializeTest(JoinQueryTests.class); // Look at this code
		try {
			//TestUte.createDeptEmp(ses);
			TestUte.createPayrolls(ses);
			TestUte.createEnumInKeyRecordDDL(ses);
            queryJoinTest();
            selfJoinTest();
            selfJoinTest2();
            leftJoinTest();
            rawJoinTest();
            oneToManyJoinTest();
            
		} finally {
			ses.close();
		}
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
        TestUte.assertEqual(e300s1.getString(e300s1.NAME), "Two00");

        SQuery<Employee> rawJoinQ = new SQuery<Employee>(Employee.EMPLOYEE).as("XX_EMPLOYEE")
        .rawLeftJoin("XX_EMPLOYEE", Department.DEPARTMENT, "D.DEPT_ID = XX_EMPLOYEE.DEPT_ID").as("D")
        .rawPredicate("D.DNAME = 'F100'");
//    	.rawJoin("JOIN XX_DEPARTMENT D ON D.DEPT_ID = XX_EMPLOYEE.DEPT_ID AND D.DNAME = 'F100'");

	    Employee e100Q1b = ses.query(rawJoinQ).oneOrNone();
	    dept = (Department) e100Q1.findReference(Employee.DEPARTMENT);
		TestUte.assertTrue(dept.wasInCache());
		TestUte.assertEqual(e100Q1.getString(Employee.NAME), "One00");
		TestUte.assertEqual(e100Q1.findReference(Employee.DEPARTMENT).getString(Department.NAME), "F100");
	    
	    ses.commit();
	}
	    
    static void selfJoinTest() {
        
    	SSessionJdbc ses = SSessionJdbc.getThreadLocalSession();
        SLog.getSessionlessLogger().message("\n---------- testing self join ------------");
        ses.begin();
        // Select employees whose manager's dept is 100
        // Don't retrieve manager
        SQuery<Employee> subQ2 = new SQuery<Employee>(Employee.EMPLOYEE).as("emp")
                                                     .innerJoin("emp", Employee.MANAGER).select(SSelectMode.SNONE).as("manager")
                                                     .eq("manager", Employee.DEPT_ID, "100")
                                                     .descending("emp", Employee.SALARY);
        List<Employee> e100subs = ses.query(subQ2);
        TestUte.assertEqual(e100subs.size(), 2);
        Employee sub = e100subs.get(0);
        TestUte.assertEqual(sub.getString(Employee.EMPEE_ID), "300");
        Employee manager = sub.findReference(Employee.MANAGER);
        TestUte.assertTrue( ! manager.wasInCache()); // was not retrieved by previous query (SNONE)
        TestUte.assertEqual(manager.getString(Employee.EMPEE_ID), "100");
        ses.commit();
        SLog.getSessionlessLogger().message("\n--------------- selfjoin test passed ------------");
        
    }
    
    static void selfJoinTest2() {
        
    	SSessionJdbc ses = SSessionJdbc.getThreadLocalSession();
        SLog.getSessionlessLogger().message("\n---------- testing self join plus third table------------");
        ses.begin();
        // Select employees whose manager's dept is 100
        // Don't retrieve manager
        SQuery<Employee> subQ2 = new SQuery<Employee>(Employee.EMPLOYEE).as("emp")
                                                     .innerJoin("emp", Employee.MANAGER).select(SSelectMode.SNONE).as("manager")
                                                     .innerJoin("manager", Employee.DEPARTMENT)
                                                     .eq(Department.MISSION, "Count Pennies")
                                                     .descending("emp", Employee.SALARY);
        List<Employee> e100subs = ses.query(subQ2);
        TestUte.assertEqual(e100subs.size(), 2);
        Employee sub = e100subs.get(0);
        TestUte.assertEqual(sub.getString(Employee.EMPEE_ID), "300");
        Employee manager = sub.findReference(Employee.MANAGER);
        TestUte.assertTrue( ! manager.wasInCache()); // was not retrieved by previous query (SNONE)
        TestUte.assertEqual(manager.getString(Employee.EMPEE_ID), "100");
        Department dept = manager.findReference(Employee.DEPARTMENT);
        TestUte.assertTrue(dept.wasInCache()); // has been retrieved by joining
        TestUte.assertEqual(dept.getString(Department.MISSION), "Count Pennies");
        ses.commit();
        SLog.getSessionlessLogger().message("\n--------------- selfjoin plus third table test passed ------------");
        
    }
    
    static void leftJoinTest() {
        
    	SSessionJdbc ses = SSessionJdbc.getThreadLocalSession();
        SLog.getSessionlessLogger().message("\n---------- testing left join ------------");
        ses.begin();
        // Select departments and their employees if any.
        // Retrieve empoyees in cache
        SQuery<Department> leftQ = new SQuery<Department>(Department.DEPARTMENT)
                                                     .leftJoin(Employee.DEPARTMENT);
        List<Department> depts = ses.query(leftQ);
        TestUte.assertTrue(depts.size() == 5);
        Collection<Employee> emps = ses.getDataSet().findAllRecords(Employee.EMPLOYEE);
        TestUte.assertTrue(emps.size() == 2);
        ses.commit();        
        
        SLog.getSessionlessLogger().message("\n--------------- left join test passed ------------");
        
    }
    
    static void rawJoinTest() {
        
    	SSessionJdbc ses = SSessionJdbc.getThreadLocalSession();
        SLog.getSessionlessLogger().message("\n---------- testing raw join ------------");
        ses.begin();
        // Select departments and their employees if any.
        // Retrieve empoyees in cache
        SQuery<Department> leftQ = new SQuery<Department>(Department.DEPARTMENT).as("DEPT")
                                                     .rawLeftJoin("DEPT", Employee.EMPLOYEE, "DEPT.DEPT_ID=EMP.DEPT_ID").as("EMP");
        List<Department> depts = ses.query(leftQ);
        TestUte.assertTrue(depts.size() == 5);
        Collection<Employee> emps = ses.getDataSet().findAllRecords(Employee.EMPLOYEE);
        TestUte.assertTrue(emps.size() == 2);
        ses.commit();        
        
        SLog.getSessionlessLogger().message("\n--------------- raw join test passed ------------");
        
    }
    
    static void oneToManyJoinTest() {
        
    	SSessionJdbc ses = SSessionJdbc.getThreadLocalSession();
        SLog.getSessionlessLogger().message("\n---------- testing one to many join ------------");
        ses.begin();
        // Select departments and their employees if any.
        // Retrieve empoyees in cache
        Employee newEmp = ses.create(Employee.EMPLOYEE, "400");
        newEmp.setString(Employee.DEPT_ID, "100");
        newEmp.setString(Employee.NAME, "I'm new here");
        ses.flush(); // Now dept100 has two employees
        SQuery<Department> leftQ = new SQuery<Department>(Department.DEPARTMENT)
                                                     .innerJoin(Employee.DEPARTMENT);
        // should retrieve dep100 and dep200 (other have no emps), and employees 100, 200 and 400
        List<Department> depts = ses.query(leftQ);
        TestUte.assertTrue(depts.size() == 3); // dept 100 is twice in the list
        Department dept100 = depts.get(0);
        TestUte.assertEqual(dept100.getString(Department.DEPT_ID), "100");
        Collection<Employee> emps = ses.getDataSet().findAllRecords(Employee.EMPLOYEE);
        TestUte.assertTrue(emps.size() == 3);
        List<Employee> d100Emps = ses.getDataSet().queryReferencing(dept100, Employee.DEPARTMENT);
        TestUte.assertTrue(d100Emps.size() == 2);
        ses.commit();
        
        // More eager test, testing on inconsistent key names on both sides
        ses.begin();
        // Select employees and their payslips.
        SQuery<Employee> qry = new SQuery<Employee>(Employee.EMPLOYEE)
                                                     .innerJoin(PaySlip.EMPLOYEE);
        // should retrieve dep100 and dep200 (other have no emps), and employees 100, 200 and 400
        List<Employee> empLst = ses.query(qry);
//        TestUte.assertTrue(depts.size() == 3); // dept 100 is twice in the list
//        Department dept100 = depts.get(0);
//        TestUte.assertEqual(dept100.getString(Department.DEPT_ID), "100");
//        Collection<Employee> emps = ses.getDataSet().findAllRecords(Employee.EMPLOYEE);
//        TestUte.assertTrue(emps.size() == 3);
//        List<Employee> d100Emps = ses.getDataSet().queryReferencing(dept100, Employee.DEPARTMENT);
//        TestUte.assertTrue(d100Emps.size() == 2);
        ses.commit();
        
        SLog.getSessionlessLogger().message("\n--------------- one to many join test passed ------------");
        
    }    

}
