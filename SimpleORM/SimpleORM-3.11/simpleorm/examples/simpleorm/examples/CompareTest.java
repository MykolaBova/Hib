package simpleorm.examples;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import simpleorm.dataset.SDataSet;
import simpleorm.dataset.SFieldDouble;
import simpleorm.dataset.SFieldMeta;
import simpleorm.dataset.SFieldReference;
import simpleorm.dataset.SFieldScalar;
import simpleorm.dataset.SFieldString;
import simpleorm.dataset.SQuery;
import simpleorm.dataset.SRecordInstance;
import simpleorm.dataset.SRecordMeta;
import simpleorm.dataset.SFieldFlags;
import simpleorm.dataset.validation.SValidatorEnumeratedValues;
import simpleorm.dataset.validation.SValidatorI;
import simpleorm.sessionjdbc.SSessionJdbc;
import simpleorm.sessionjdbc.SDataLoader;
import simpleorm.utils.SException;
import simpleorm.utils.SLog;
import simpleorm.utils.SRecordComparator;

/**
 * Tests validation, OnFieldValidate etc. (Basic validation test is also in
 * ADemo/Employee.)
 */

public class CompareTest  {
	
	public static void main(String[] argv) throws Exception {
		TestUte.initializeTest(CompareTest.class); // Look at this code.
		try {
			testInit();
			compareTest();
		} finally {
			SSessionJdbc.getThreadLocalSession().close();
		}
	}

	/** Prepare for tests, Delete old data. */
	static void testInit() throws Exception {
		System.out.println("################ Init #################");
		SSessionJdbc ses = SSessionJdbc.getThreadLocalSession();

        TestUte.createDeptEmp(ses);
	}

	static void compareTest() {
		System.out.println("################ compareTest #################");
		
		SSessionJdbc ses = SSessionJdbc.getThreadLocalSession();
		ses.begin();
		ses.query(new SQuery<Employee>(Employee.EMPLOYEE).descending(Employee.EMPEE_ID));
		SDataSet ds = ses.commitAndDetachDataSet();
		
		List<Employee> emps = ds.findAllRecords(Employee.EMPLOYEE);
		// by pkey
		Collections.shuffle(emps);
		Collections.sort(emps, new SRecordComparator<Employee>(Employee.EMPLOYEE));
		TestUte.assertEqual(emps.get(0).getString(Employee.EMPEE_ID), "100");
		TestUte.assertEqual(emps.get(1).getString(Employee.EMPEE_ID), "200");
		TestUte.assertEqual(emps.get(2).getString(Employee.EMPEE_ID), "300");
		
		// Order by descending salary
		Collections.shuffle(emps);
		Collections.sort(emps, new SRecordComparator<Employee>(true, Employee.SALARY));
		TestUte.assertEqual(emps.get(0).getString(Employee.EMPEE_ID), "300");
		TestUte.assertEqual(emps.get(1).getString(Employee.EMPEE_ID), "200");
		TestUte.assertEqual(emps.get(2).getString(Employee.EMPEE_ID), "100");
		
		// Order by dept (null last)
		Collections.shuffle(emps);
		Collections.sort(emps, new SRecordComparator<Employee>(Employee.DEPT_ID));
		TestUte.assertEqual(emps.get(0).getString(Employee.EMPEE_ID), "100");
		TestUte.assertEqual(emps.get(1).getString(Employee.EMPEE_ID), "200");
		TestUte.assertEqual(emps.get(2).getString(Employee.EMPEE_ID), "300");
		
		// Create with null key and compare
		Employee e4 = ds.createWithNullKey(Employee.EMPLOYEE);
		e4.setString(Employee.NAME, "first");
		Employee e5 = ds.createWithNullKey(Employee.EMPLOYEE);
		e5.setString(Employee.NAME, "second");
		emps = ds.findAllRecords(Employee.EMPLOYEE);		
		
		Collections.shuffle(emps);
		Collections.sort(emps, new SRecordComparator<Employee>(Employee.EMPLOYEE));
		TestUte.assertEqual(emps.get(0).getString(Employee.EMPEE_ID), "100");
		TestUte.assertEqual(emps.get(1).getString(Employee.EMPEE_ID), "200");
		TestUte.assertEqual(emps.get(2).getString(Employee.EMPEE_ID), "300");
		// comparing on null keys is not consistent with equals... beware. 
//		TestUte.assertEqual(emps.get(3).getString(Employee.NAME), "first");
//		TestUte.assertEqual(emps.get(4).getString(Employee.NAME), "second");

		
		// Sort by manager (can be null), then by type, then by name
		// Must be set to sort on it...
		e4.setNull(Employee.MANAGER_EMPEE_ID);
		e5.setNull(Employee.MANAGER_EMPEE_ID);
		e4.setNull(Employee.ETYPE);
		e5.setNull(Employee.ETYPE);
		
		Collections.shuffle(emps);
		Collections.sort(emps, new SRecordComparator<Employee>(Employee.MANAGER_EMPEE_ID, Employee.ETYPE, Employee.NAME));
		TestUte.assertEqual(emps.get(0).getString(Employee.EMPEE_ID), "300");
		TestUte.assertEqual(emps.get(1).getString(Employee.EMPEE_ID), "200");
		TestUte.assertEqual(emps.get(2).getString(Employee.EMPEE_ID), "100");
		TestUte.assertEqual(emps.get(3).getString(Employee.NAME), "first");
		TestUte.assertEqual(emps.get(4).getString(Employee.NAME), "second");
		
		// Sort on reference
		Collections.shuffle(emps);
		Collections.sort(emps, new SRecordComparator<Employee>(Employee.MANAGER));
		TestUte.assertTrue(emps.get(0).getString(Employee.EMPEE_ID).equals("300") || emps.get(0).getString(Employee.EMPEE_ID).equals("200"));
		TestUte.assertTrue(emps.get(1).getString(Employee.EMPEE_ID).equals("300") || emps.get(1).getString(Employee.EMPEE_ID).equals("200"));
		
		System.out.println("################ compareTest passed #################");

	}

}
