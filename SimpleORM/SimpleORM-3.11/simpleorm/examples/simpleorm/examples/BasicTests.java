package simpleorm.examples;

import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import simpleorm.dataset.SDataSet;
import simpleorm.dataset.SFieldMeta;
import simpleorm.dataset.SFieldReference;
import simpleorm.dataset.SFieldScalar;
import simpleorm.dataset.SQuery;
import simpleorm.dataset.SRecordMeta;
import simpleorm.sessionjdbc.SDataLoader;
import simpleorm.sessionjdbc.SSessionJdbc;
import simpleorm.utils.SException;
import simpleorm.utils.SLog;
import simpleorm.utils.SUte;

/**
 * Similar to ADemo tests, but with more assertions.
 */

public class BasicTests {

	public static void main(String[] argv) throws Exception {
		SLog.getSessionlessLogger().setLevel(0); //Uncomment to reduce trace output.
		TestUte.initializeTest("BasicTests", true); // use deprecated attach.
		try {
			createDeptEmp();
			deptTest();
			empTest();
			metaTest();
			mapTest();
			removeRecordTest();
		} finally {
			SSessionJdbc.getThreadLocalSession().close();
		}
	}

	/** Prepare for tests, Delete old data. */
	static void createDeptEmp() throws Exception {
		SLog.getSessionlessLogger().message("################ CreateDeptEmp #################");
		SSessionJdbc ses = SSessionJdbc.getThreadLocalSession();
		ses.begin();

		TestUte.dropAllTables(ses);
		ses.rawUpdateDB(ses.getDriver().createTableSQL(Department.DEPARTMENT));
		ses.rawUpdateDB(ses.getDriver().createTableSQL(Employee.EMPLOYEE));

		ses.commit();
	}

	/** Basic examples/tests not involving foreign keys. */
	static void deptTest() throws Exception {
		SLog.getSessionlessLogger().message("################ deptTest #################");
		SSessionJdbc ses = SSessionJdbc.getThreadLocalSession();
		ses.begin();

		// / Create some Departments using the SDataLoader
		SDataLoader deptDL = new SDataLoader(ses, Department.DEPARTMENT);
		Department d2 = (Department) deptDL.insertRecord(
            "100",	"One00XX", "Count Pennies", "10000", "200000" );
		ses.flush(); // SQL Insert
		d2.setString(Department.NAME, "One00"); // Causes SQL Update.
		deptDL.insertRecords(new Object[][] {
				{ "200", "Two00", "Be Happy", "20000", "150000" },
				{ "300", "Three00", "Enjoy Life", "30000", "300000" } });

		ses.commit();
		ses.begin();

		// / Retrieve a Department and check that we have the right one.
		String key = "100";
		Department department = ses.findOrCreate(Department.DEPARTMENT, key);
		department.assertNotNewRow();

		// / Retrieve the Name using explicit meta data.
		//HashMap deptMap = (HashMap) Department.meta.getProperty(SFIELD_MAP);
		//SFieldMeta deptName = (SFieldMeta) Department
		//SFieldMeta deptName2 = Department.meta.getField("NAME");
		//TestUte.assertTrue(deptName == deptName2);
		SFieldMeta deptName = Department.NAME;
		String name = department.getString(deptName);
		if (!"One00".equals(name))
			throw new Exception("Dept Name " + name);
		TestUte.assertEqual("DNAME", Department.NAME.getColumnName());

		// / Query the same Department again. This does not query the database.
		String oo = "1";
		String key2 = oo + "00"; // don't want it == key
		TestUte.assertTrue(!department.wasInCache());
		Department department2 = ses.findOrCreate(Department.DEPARTMENT, key2);
		TestUte.assertTrue(department2.wasInCache());
		if (department != department2)
			throw new Exception("Departments not identical" + department
					+ department2);

		// / Create a new, empty department and set some column values.
		Department newDept = ses.findOrCreate(Department.DEPARTMENT, "900");
		newDept.assertNewRow();
		newDept.setString(Department.NAME, "New900");
		newDept.setString(Department.BUDGET, "90000");

		ses.flush();

		// / At this point there should be three departments with > $10,000.
		selectDepartments(ses,20000 + 30000 + 90000);

		// / Rollback new 900 Department. Flush() does not mean commit.
		ses.rollback();

		ses.begin();

		// / Delete Department 300
		Department delDept = ses.find(Department.DEPARTMENT, "300");
		delDept.deleteRecord();

		// / Insert and then Delete Department 500 -- ie do nothing.
		Department insDelDept = ses.findOrCreate(Department.DEPARTMENT, "500");
		insDelDept.setString(Department.BUDGET, "666");
		insDelDept.deleteRecord(); // No SQL generated.

		ses.commit();
		ses.begin();

		// / Check only one department left > $10,000.
		selectDepartments(ses,20000);
		selectDepartments(ses,20000 + 10);

		ses.commit();
		ses.begin();

		selectDepartments(ses,20000 + 20); // Now updated.

//		ArrayList<Department> al = Department.meta
//				.select("budget > 10000", "name").execute()
//				.getSArrayList(1000);
		List<Department> al = ses.query(
            new SQuery<Department>(Department.DEPARTMENT)
            .gt(Department.BUDGET, 10000).descending(Department.NAME));
		if (al.size() != 1)
			throw new SException.Test("Wrong SArrayList size " + al.size());

		ses.commit();
	}

	/**
	 * Query all the departments with Budget > 10000 and check that the total
	 * budget == total. This uses the lower level SQL interface, ADemo does the
	 * same thing using the Query interface.
	 */
	static void selectDepartments(SSessionJdbc session,int total) {

		// / Prepare and execute the query. See QueryTest.java for the query
		// builder.
//		SPreparedStatement<Department> stmt = Department.meta.select("budget > ?",
//				"name DESC");
//		stmt.setDouble(1, 10000);
//		SResultSet<Department> res = stmt.execute();
		List<Department> res = session.query(
            new SQuery<Department>(Department.DEPARTMENT)
            .gt(Department.BUDGET, 10000).descending(Department.NAME));

		// / loop through the results, adding up the budgets.
		double totBudget = 0;
//		while (res.next()) {
//			Department dept = (Department) res.getRecord();
		for (Department dept : res) {
			double budget = dept.getDouble(Department.BUDGET);
			SLog.getSessionlessLogger().message("DEPARTMENT: " + dept.getString(Department.NAME)
					+ " $" + budget);
			totBudget += budget;
			dept.setDouble(Department.BUDGET, budget + 10);
		}

		// / Check that the total is what we expect.
		if (totBudget != total)
			throw new SException.Test("Wrong Total " + totBudget);
	}

	/**
	 * Examples/Tests of the (non identifying) foreign key relationship from
	 * Employee to Department, and from Employee to Employee.Manager.
	 */
	static void empTest() { // Foreign Keys
		SLog.getSessionlessLogger().message("################ empTest #################");
		SSessionJdbc ses = SSessionJdbc.getThreadLocalSession();
		ses.begin();

		// / Dump all the fields in the Employee record. Note that the
		// / DEPT_ID is included even though it is not explicitly part of the
		// / Employee record's definition.
		SLog.getSessionlessLogger().message("Employee Fields " + SUte.allFieldsString(Employee.EMPLOYEE));

		// / Create an Employee
		SDataLoader empDL = new SDataLoader(ses, Employee.EMPLOYEE);
		Employee e100 = (Employee) empDL.insertRecord(
            "100", "One00", "123 456 7890", "10000", "3", null, null, Employee.EType.PERMANENT);

		// / Explicitily set the Department
		Department d100 = ses.findOrCreate(Department.DEPARTMENT, "100");
		Department d200 = ses.findOrCreate(Department.DEPARTMENT, "200");

		e100.setReference(e100.DEPARTMENT, d200);
	
		// / Retrieve the Department using explicit meta data.
		SFieldReference deptRef = (SFieldReference) Employee.EMPLOYEE.getField("DEPT");
		System.out.println("deptRef "+(deptRef == null ? "NULL":deptRef.getRecordMeta().getTableName()));
		Department dept1 = (Department) e100.findReference(deptRef);
		if (!dept1.getString(d100.NAME).equals("Two00"))
			throw new SException.Test("Bad Dept Two00 " + e100 + dept1);
	
		// / Null the Departent
		e100.setReference(e100.DEPARTMENT, null);
		Department dept2 = e100.findReference(e100.DEPARTMENT);
		if (dept2 != null)
			throw new SException.Test("Bad Dept Null " + e100 + dept2);
	
		// / And reassign it.
		e100.setReference(e100.DEPARTMENT, d100);
		Department dept3 = e100.findReference(e100.DEPARTMENT);
		if (!dept3.getString(d100.NAME).equals("One00"))
			throw new SException.Test("Bad Dept One00 " + e100 + dept3);
	
		SLog.getSessionlessLogger().message("e100#" + e100.allFields());

		// / Create more Employees setting the Department using the DataLoader
		Employee[] emps1 = (Employee[]) empDL
				.insertRecords(new Object[][] {
						{ "200", "Two00", null, null, "0", null, "100", Employee.EType.CONTRACT },
						{ "300", "Three00", "123 456 7890", "30000", "1", "200", "100", "CASUAL" } });
		if (emps1[0].getString(Employee.PHONE_NR) != null
				|| emps1[0].getInt(Employee.SALARY) != 0 // 0 for null
															// follows JDBC
				|| !emps1[0].isEmpty(Employee.SALARY)
				|| emps1[0].getObject(Employee.SALARY) != null)
			throw new SException.Test("Bad Employee Nulls" + emps1[0]);
	
		empDL.insertRecords(new Object[][] { // Twice should not cause grief
						{ "200", "Two00", null, null, "0", null, "100", Employee.EType.CONTRACT }});

		// / Check e100 still valid after a flush()
		ses.flush();
		if (!e100.getString(Employee.NAME).equals("One00"))
			throw new SException.Test("Flush() corrupted " + e100);
	
		ses.commit();
		ses.begin();
        
        // Check Dirty setting.
        Department ddep = ses.find(Department.DEPARTMENT, "200");
        Employee demployee = ses.find(Employee.EMPLOYEE, "300");
        // TestUte.assertTrue(demployee.isValid(Employee.DEPARTMENT));  FAILS, probably should not.
        if (false) {
          TestUte.assertEqual(ddep, demployee.getObject(Employee.DEPARTMENT)); 
          TestUte.assertTrue(demployee.isValid(Employee.DEPARTMENT));  // OK.  But get above hides bug below.
        }
        demployee.setPhoneNumber( "123 456 7890"); // Not dierty because this was the existing value
        TestUte.assertTrue( ! demployee.isDirty());
        demployee.setReference( Employee.DEPARTMENT, ddep ); // Not dierty because this was the existing value
        TestUte.assertTrue(demployee.isValid(Employee.DEPARTMENT));
        TestUte.assertTrue( ! demployee.isDirty());  //THIS FAILS if not GOT

		ses.commit();
		ses.begin();

		// / Check e100 record NOT valid after a commit()
		// / (locks released so need to requery).
		try {
			e100.getString(Employee.NAME); // Error as e100 destroyed by commit.
			throw new SException.Test("e100 not destroyed.");
		} catch (SException.Error er) {}

		// / Retrieve e100 again, and show all its field values.
		// / Compare allFields() with the concise toString() that just shows
		// keys.
		Employee e100a = ses.findOrCreate(Employee.EMPLOYEE, "100");
	
		// / Get the referenced d100 Department. Noe that the SQL query
		// / only happens here.
       TestUte.assertTrue(!e100a.isValid(Employee.DEPARTMENT));
		Department d100a = (Department)e100a.getObject(Employee.DEPARTMENT);
		Department d100a2 = e100a.findReference(e100a.DEPARTMENT);
		Department d100a3 = e100a.findReference(e100a.DEPARTMENT);
        TestUte.assertTrue(d100a==d100a2 && d100a==d100a3);
           

		// / Change an Employee's manager, creating a loop, and check.
		Employee e300 = ses.findOrCreate(Employee.EMPLOYEE, "300");
		e100a.setReference(e100a.MANAGER, e300);
		Employee e300manager = e100a.findReference(Employee.MANAGER);

		// So e200, e300 --> e100; e100 --> e300; ie. a loop, OK.
		Employee e100b = e300.findReference(e300.MANAGER);
		if (e100b != e100a)
			throw new SException.Test("e300.getReference " + e100a + e300
					+ e100b);

		// / Check database updated correctly, this time using rawQueryDB().
		ses.getDataSet().dumpDataSet();
		ses.flush();
		Object salSum = ses.rawQuerySingle("SELECT SUM(M.SALARY) FROM XX_EMPLOYEE M, XX_EMPLOYEE E "
						+ " WHERE M.\"Empee*Id\" = E.MANAGER_EMPEE_ID ",true);

		// "SELECT SUM(" // No Subselects in MySQL
		// + " (SELECT SALARY FROM XX_EMPLOYEE M "
		// + " WHERE M.EMPEE_ID = E.MANAGER_EMPEE_ID)) "
		// + "FROM XX_EMPLOYEE E"); // Sum of all manager's salary.
		if (((Number) salSum).intValue() != 10000 + 10000 + 30000)
			throw new SException.Test("Bad Mgr Salary sum " + salSum);

		ses.commit();

	}

	/** Test of meta data and properties. */
	static void metaTest() {
		SLog.getSessionlessLogger().message("################ metaTest #################");
		SFieldScalar empeeId = (SFieldScalar) Employee.EMPLOYEE.getField("Empee*Id");
		TestUte.assertTrue(empeeId == Employee.EMPEE_ID);
		Integer size = empeeId.getMaxSize();
		TestUte.assertTrue(size.intValue() == 20);

		SFieldReference mgr = Employee.MANAGER;
		//int idx = SJSharp.object2Int(mgr.getProperty(SFIELD_INDEX));
		SRecordMeta emp = mgr.getRecordMeta();
		//SFieldMeta[] flds = (SFieldMeta[]) emp.getProperty(SFIELD_METAS);
		//TestUte.assertTrue(mgr == flds[idx]);
		
		SLog.getSessionlessLogger().message("Test initial value...");
		// Test that initial value is set on create
		SDataSet ds = new SDataSet();
		Department d10 = ds.create(Department.DEPARTMENT, "10");
		TestUte.assertEqual(d10.getDouble(Department.MAX_SALARY), new Double(8999));
		// Test that initial value is commited to database and can be read back
		SSessionJdbc ses = SSessionJdbc.getThreadLocalSession();
		ses.begin(ds);
		ses.commit();
		ses.begin();
		d10 = ses.find(Department.DEPARTMENT, "10");
		TestUte.assertEqual(d10.getDouble(Department.MAX_SALARY), new Double(8999));
		ses.commit();
		// Test that initial value can be set to null or to another value
		ses.begin();
		// on a retrieved record
		d10 = ses.find(Department.DEPARTMENT, "10");
		d10.setNull(Department.MAX_SALARY);
		// on a new record
		Department newDept = ses.create(Department.DEPARTMENT, "20");
		newDept.setNull(Department.MAX_SALARY);
		ses.commit();
		ses.begin();
		d10 = ses.find(Department.DEPARTMENT, "10");
		newDept = ses.find(Department.DEPARTMENT, "20");
		TestUte.assertTrue(d10.isNull(Department.MAX_SALARY));
		TestUte.assertTrue(newDept.isNull(Department.MAX_SALARY));
		ses.commit();
		ses.begin();
		d10 = ses.find(Department.DEPARTMENT, "10");
		d10.setDouble(Department.MAX_SALARY, new Double(99999));
		ses.commit();
		ses.begin();
		d10 = ses.find(Department.DEPARTMENT, "10");
		TestUte.assertEqual(d10.getDouble(Department.MAX_SALARY), new Double(99999));
		ses.commit();
	}
	
	/** Test of SRecordInstance implementation of Map. */
	static void mapTest() {
		SLog.getSessionlessLogger().message("################ mapTest #################");
				
		// Test that put(key, value) is retrieved by getXXX(field)
		SDataSet ds = new SDataSet();
		Department d1000 = ds.create(Department.DEPARTMENT, "1000");
		d1000.put(Department.MAX_SALARY.getFieldName(), 10000.0);
		TestUte.assertEqual(d1000.getDouble(Department.MAX_SALARY), new Double(10000));

		// Test that value is commited to database and can be read back
		SSessionJdbc ses = SSessionJdbc.getThreadLocalSession();
		ses.begin(ds);
		ses.commit();
		ses.begin();
		d1000 = ses.find(Department.DEPARTMENT, "1000");
		
		// with field accessor
		TestUte.assertEqual(d1000.getDouble(Department.MAX_SALARY), new Double(10000));
		
		// with map.get()
		TestUte.assertEqual(d1000.getDouble(Department.MAX_SALARY.getFieldName()), new Double(10000));
		ses.commit();
		
		// Test that a value can be set to null using put
		ses.begin();
		
		// on a retrieved record
		d1000 = ses.find(Department.DEPARTMENT, "1000");
		d1000.put(Department.MAX_SALARY.getFieldName(), null);
		
		// on a new record
		Department newDept = ses.create(Department.DEPARTMENT, "2000");
		newDept.put(Department.MAX_SALARY.getFieldName(), null);
		ses.commit();
		ses.begin();
		d1000 = ses.find(Department.DEPARTMENT, "1000");
		newDept = ses.find(Department.DEPARTMENT, "20");
		TestUte.assertTrue(d1000.isNull(Department.MAX_SALARY));
		TestUte.assertTrue(newDept.isNull(Department.MAX_SALARY));
		ses.commit();
		
		// test entrySet
		ses.begin();
		d1000 = ses.find(Department.DEPARTMENT, "1000");
		Set<Entry<String, Object>> es = d1000.entrySet();
		boolean esok = false;
		for (Entry<String, Object> entry : es) {
			if (entry.getKey().equals(Department.DEPT_ID.getFieldName())) {
				TestUte.assertEqual("1000", entry.getValue());
				esok = true;
			}
			if (entry.getKey().equals(Department.MISSION.getFieldName()))
				entry.setValue("Shiny new mission");
		}
		TestUte.assertTrue(esok);
		TestUte.assertEqual(d1000.size(),  5);
		TestUte.assertEqual(d1000.values().size(), 5);
		TestUte.assertEqual(d1000.keySet().size(), 5);
		TestUte.assertEqual(d1000.entrySet().size(), 5);
		TestUte.assertTrue(d1000.containsKey(Department.DEPT_ID.getFieldName()));
		TestUte.assertTrue(d1000.keySet().contains(Department.DEPT_ID.getFieldName()));
		
		
		TestUte.assertEqual("Shiny new mission", d1000.getString(Department.MISSION));
		ses.commit();
		
		// test size()
		ses.begin();
		d1000 = ses.query(new SQuery<Department>(Department.DEPARTMENT)
				                                         .eq(Department.DEPT_ID, "1000")
				                                         .select(new SFieldScalar[]{Department.DEPT_ID, Department.NAME})
				                     ).exactlyOne();
		TestUte.assertTrue(d1000.size() == 2);
		ses.commit();
		
	}

	/** Test of dirtyRecordIndex consistence */
	static void removeRecordTest() {
		
		// Test that dirty record index are consistent in dataset 
		SDataSet ds = new SDataSet();
		Department d10 = ds.create(Department.DEPARTMENT, "10");
		Department d20 = ds.create(Department.DEPARTMENT, "20");

		// remove the first dirty record, and verify indexes are not shifted 
		ds.removeRecord(d10);
		// add another dept
		Department d11 = ds.create(Department.DEPARTMENT, "11");
		
		// remove the second dirty record...
		ds.removeRecord(d20);
		
		// try to persist
		SSessionJdbc ses = SSessionJdbc.getThreadLocalSession();
		
		ses.begin(ds);
		ses.commit();
		
	}

}
