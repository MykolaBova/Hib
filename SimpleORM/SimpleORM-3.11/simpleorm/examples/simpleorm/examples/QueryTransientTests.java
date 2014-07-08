package simpleorm.examples;

import java.util.List;

import simpleorm.dataset.SQuery;
import simpleorm.dataset.SQueryTransient;
import simpleorm.dataset.SRecordGeneric;
import simpleorm.dataset.SRecordTransient;
import simpleorm.sessionjdbc.SSessionJdbc;
import simpleorm.utils.SLog;

/**
 * Tests AggregateQueries
 * @author franck routier aka alci
 */
public class QueryTransientTests {
    	public static void main(String[] argv) throws Exception {
		SSessionJdbc ses = TestUte.initializeTest(QueryTransientTests.class); // Look at this code
		
		try {
//			TestUte.createDeptEmp(ses);
			ses.begin();
			TestUte.dropAllTables(ses);
			ses.commit();
			TestUte.createPayrolls(ses);
			TestUte.createEnumInKeyRecordDDL(ses);
            queryGroupByTest();
            queryCountTest();
            querySumTest();
            queryJoinSumTest();
            
		} finally {
			ses.close();
		}
	}
    	
	static void queryGroupByTest() {
        SLog.getSessionlessLogger().message("\n################ queryGroupByTest #################");

  		SSessionJdbc ses = SSessionJdbc.getThreadLocalSession();

		ses.begin();
		SQuery<Employee> joinQ1 = new SQuery<Employee>(Employee.EMPLOYEE);

		SQueryTransient aggQry = new SQueryTransient(joinQ1)
		                                       .groupBy("XX_EMPLOYEE", Employee.DEPT_ID).as("dept")
		                                       .sum("XX_EMPLOYEE", Employee.SALARY).as("salaries");
		
        List<SRecordTransient> result = ses.queryTransient(aggQry);
        
        TestUte.assertTrue(result.size() == 3);
        	    
	    ses.commit();
	}
	
	static void queryCountTest() {
        SLog.getSessionlessLogger().message("\n################ queryCountTest #################");

  		SSessionJdbc ses = SSessionJdbc.getThreadLocalSession();

		ses.begin();
		SQuery<Employee> joinQ1 = new SQuery<Employee>(Employee.EMPLOYEE);

		SQueryTransient aggQry = new SQueryTransient(joinQ1)
		                                       .count().as("numberOfEmps");
		
		SRecordGeneric result = ses.queryTransient(aggQry).exactlyOne();
        
        TestUte.assertTrue(result.getInt("numberOfEmps").equals(3));
        
        SQueryTransient aggQry2 = new SQueryTransient(joinQ1)
        										.count("xx_employee", Employee.DEPT_ID).as("numberOfEmps");;
        
        result = ses.queryTransient(aggQry2).exactlyOne();
        TestUte.assertTrue(result.getInt("numberOfEmps").equals(2));    // SQL doesn't count nulls
	    
	    ses.commit();
	}
	
	static void querySumTest() {
        SLog.getSessionlessLogger().message("\n################ querySumTest #################");

  		SSessionJdbc ses = SSessionJdbc.getThreadLocalSession();

		ses.begin();
		SQuery<Employee> joinQ1 = new SQuery<Employee>(Employee.EMPLOYEE);

		SQueryTransient aggQry = new SQueryTransient(joinQ1)
		                                       .sum("XX_EMPLOYEE", Employee.SALARY).as("salaries");
		
		SRecordTransient result = ses.queryTransient(aggQry).exactlyOne();
        
        TestUte.assertTrue(result.getDouble("salaries") == new Double(60000));
        
	    ses.commit();
	}
	
	static void queryJoinSumTest() {
        SLog.getSessionlessLogger().message("\n################ queryJoinSumTest #################");

  		SSessionJdbc ses = SSessionJdbc.getThreadLocalSession();

  		ses.begin();
  	    // Select departments and their employees if any.
  	    // Retrieve empoyees in cache
  	    Employee newEmp = ses.create(Employee.EMPLOYEE, "400");
  	    newEmp.setString(Employee.DEPT_ID, "100");
  	    newEmp.setString(Employee.NAME, "I'm new here");
  	    newEmp.setDouble(Employee.SALARY, 22000.0);
  	    ses.flush(); // Now dept100 has two employees
  	    SQuery<Department> leftQ = new SQuery<Department>(Department.DEPARTMENT).as("dept")
  	                                                 .innerJoin("dept", Employee.DEPARTMENT).as("emp");
  	    // should retrieve dep100 and dep200 (other have no emps), and employees 100, 200 and 400
  	    SQueryTransient aggQry = new SQueryTransient(leftQ)
  	    											.sum("emp", Employee.SALARY).as("sum_sal")
  	    											.groupBy("dept", Department.NAME).as("dname")
  	    											.ascending("dname");
  	    List<? extends SRecordGeneric> result = ses.queryTransient(aggQry);
  	    TestUte.assertEqual(result.get(0).getString("dname"), "D200");
  	    TestUte.assertEqual(result.get(0).getDouble("sum_sal"), 20000.0);

	}
	    
}
