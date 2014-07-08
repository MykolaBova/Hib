package simpleorm.examples;

import java.util.List;

import simpleorm.dataset.SFieldMeta;
import simpleorm.dataset.SFieldScalar;
import simpleorm.dataset.SQuery;
import simpleorm.dataset.SQueryMode;
import simpleorm.dataset.SSelectMode;
import simpleorm.sessionjdbc.SSessionJdbc;
import simpleorm.utils.SException;
import simpleorm.utils.SLog;
import static simpleorm.dataset.SSelectMode.*;
import static simpleorm.dataset.SQueryMode.*;


/**
 * Tests and demonstrates selective column queries, locking and flushing details
 * as well as derived columns and joins. Note that care is taken to trap
 * attempts to access unqueried fields and so prevent errors that could
 * otherwise be the source of nasty bugs as the application scales.
 */
public class ColumnCacheTest{

	public static void main(String[] argv) throws Exception {
		SSessionJdbc ses = TestUte.initializeTest(ColumnCacheTest.class); // Look at this code
		try {
			TestUte.createDeptEmp(ses);
            keyOnlyTest();
			findOrCreateTest();
			selectTest();
			referenceTest();
			updateOrderTest();
			columnQueryTest();
		} finally {
			ses.close();
		}
	}
    
    static void keyOnlyTest() {
		SSessionJdbc ses = SSessionJdbc.getThreadLocalSession();
		ses.begin();

        Employee e13 = ses.create(Employee.EMPLOYEE, "E13");
        try {
            ses.flush();
            throw new SException.Test("Mandatory NOT NULL (NAME) failed");
        } catch (SException.Jdbc je) {};
        ses.rollback();

  		ses.begin();
        Department d13a = ses.findOrCreate(Department.DEPARTMENT, "D13");
        TestUte.assertTrue(d13a.isNewRow());
        ses.commit();

   		ses.begin();
        Department d13b = ses.findOrCreate(Department.DEPARTMENT, "D13");
        TestUte.assertTrue(!d13b.isNewRow());
        ses.commit();

        ses.begin();
        Department d13c = ses.create(Department.DEPARTMENT, "D13");
        TestUte.assertTrue(d13c.isNewRow()); // Not checked at .create time
        d13c.setString(d13c.NAME, "Duplicate not allowed");
        try {
            ses.flush();
            throw new SException.Test("Duplicate not detected");
        } catch (SException.Jdbc dk) {
          if (dk.getRecordInstance() != d13c)
              throw new SException.Test("Exception wrong Instance ", dk);
        } 
        ses.rollback();

}

	static void findOrCreateTest() {
		SSessionJdbc ses = SSessionJdbc.getThreadLocalSession();
		ses.begin();

		// / Employee.RESUME not normally retrieved, need to explicitly query.
		// / Firs create a Resume. Setting unqueried fields is OK.
		Employee e100a = ses.findOrCreate(Employee.EMPLOYEE, "100");
		String res = "My life is but a walking shadow ... signifying nothing.";
		e100a.setString(e100a.RESUME, res);

		ses.commit();
		ses.begin();

		// / Trap attempt to access unqueried RESUME
		Employee e100b = ses.findOrCreate(Employee.EMPLOYEE, "100");

        TestUte.assertTrue(!e100b.isValid(Employee.RESUME));
        try {
    		String rsm = e100b.getString(Employee.RESUME); // Unqueried;
            throw new SException.Test("Invalid access not caught");
        } catch (SException e){}
		// / Retrieve it properly and check value.
		Employee e100c = ses.findOrCreate(Employee.EMPLOYEE, SALL, "100");
		// This actually issues a query to get the extra column.
		TestUte.assertTrue(res.equals(e100c.getString(e100b.RESUME)));
		TestUte.assertTrue(e100b == e100c);
		try {
			e100b.setString(e100b.EMPEE_ID, "XXX"); // Primary Key
			throw new SException.Test("Set Primary Key not trapped");
		} catch (SException.Error ke) {
			SLog.getSessionlessLogger().message("Key Failure " + ke);
		}

		ses.commit();
		System.out.println("----------------- Primary key update test Succeded ----");
		ses.begin();

		// / Retrieve a record without Lokcing and try to update or delete it.
		Department d100a = ses.findOrCreate(Department.DEPARTMENT, SREAD_ONLY, "100");
		try {
			d100a.setString(d100a.NAME, "OOPS"); // Should fail
			throw new SException.Test("Set unlocked record " + d100a);
		} catch (SException.Error se) {
			SLog.getSessionlessLogger().message("Set Failure " + se);
		}

		try {
			d100a.deleteRecord(); // Should fail
			throw new SException.Test("Deleted unlocked record " + d100a);
		} catch (SException.Error de) {
			SLog.getSessionlessLogger().message("Delete Failure " + de);
		}

		// / Try to update a non-queried for update field
		Department d100b = ses.findOrCreate(Department.DEPARTMENT, SDESCRIPTIVE, "100");
		d100b.setString(d100b.NAME, "DOne00a"); // OK, queried and locked
		d100b.getDouble(d100b.BUDGET); // OK, queried but not locked
		if (d100a != d100b)
			throw new SException.Test("Diff Depts " + d100a + d100b);

// The following case no longer checked with removal of readOnly Fields.  But OK as always optimistic locked.
//		try {
//			d100b.setDouble(d100b.BUDGET, 666); // Should fail
//			throw new SException.Test("Update unlocked field " + d100b);
//		} catch (SException.Error e) {
//		}

		Department d100c = ses.findOrCreate(Department.DEPARTMENT, "100");
		d100b.setDouble(d100b.BUDGET, 1234); // Should finally be OK

		// / Create a new Record. Can update fields because new, so we
		// / reliably know the values of all columns (null, or default
		// / later).
		Department d900 = ses.findOrCreate(Department.DEPARTMENT, SDESCRIPTIVE, "900");
		d900.assertNewRow();

		d900.setString(d900.NAME, "Five00");

		ses.flush(); // Inserts DEPT_ID, NAME

		d900.setDouble(d900.BUDGET, 777); // OK because new record.
		ses.commit(); // Updates BUDGET only.

		ses.begin();

		Department d900a = ses.findOrCreate(Department.DEPARTMENT, "900");
		if (d900a.getDouble(d900a.BUDGET) != 777)
			throw new SException.Test("Bad Budget " + d900a);

		ses.commit();
		ses.begin();

		// / Use an explicit list of fields
		Department d200a = ses.findOrCreate(Department.DEPARTMENT, new SFieldScalar[]{Department.BUDGET}, SFOR_UPDATE, "200");
		TestUte.assertTrue(!d200a.isValid(d200a.NAME));
		TestUte.assertTrue(d200a.getDouble(d200a.BUDGET) == 20000);

		ses.commit();

	}

	static void selectTest() {
		SSessionJdbc ses = SSessionJdbc.getThreadLocalSession();
		ses.begin();

		List<Department> res = ses.query(new SQuery<Department>(Department.DEPARTMENT).select(SDESCRIPTIVE).queryMode(SREAD_ONLY).gt(
				Department.BUDGET, new Integer(10000)).descending(
				Department.NAME));
		//res.next();
		Department dept = (Department) res.get(0);
		dept.getString(dept.NAME); // OK
		TestUte.assertTrue(!dept.isValid(dept.BUDGET));
		try {
			dept.setString(dept.NAME, "XXX"); // ReadOnly;
			throw new SException.Test("ReadONLY not trapped");
		} catch (SException.Error se) {
			SLog.getSessionlessLogger().message("Set Failure " + dept + " "
					+ dept.getString(dept.NAME) + " " + se);
		}

		ses.commit();
	}

	/** Test partial queries of references. */
	static void referenceTest() {
		SSessionJdbc ses = SSessionJdbc.getThreadLocalSession();
		ses.begin();
		{
			Employee e100a = ses.findOrCreate(Employee.EMPLOYEE, 
                new SFieldScalar[]{Employee.DEPT_ID}, SQueryMode.SFOR_UPDATE, "100");

			Department d100a = (Department) e100a.findReference(e100a.DEPARTMENT);
			TestUte.assertEqual("DOne00a", d100a.getString(d100a.NAME));

    		TestUte.assertTrue(!e100a.isValid(e100a.SALARY));
			Department d200a = ses.findOrCreate(Department.DEPARTMENT, "200");
			e100a.setReference(e100a.DEPARTMENT, d200a);
		}

		ses.commit();
		ses.begin();

		{
			Employee e100b = ses.findOrCreate(Employee.EMPLOYEE,new SFieldScalar[]{Employee.DEPT_ID}, SREAD_ONLY, "100");

			Department d200b = (Department) e100b
					.findReference(e100b.DEPARTMENT);
			TestUte.assertTrue("D200".equals(d200b.getString(d200b.NAME)));

			try {
				Department d300a = ses.findOrCreate(Department.DEPARTMENT, "300");
				e100b.setReference(e100b.DEPARTMENT, d300a);
				throw new SException.Test("Read Only Dept not trapped");
			} catch (SException.Error sde) {
				SLog.getSessionlessLogger().message("Set Failure " + sde);
			}
		}

		ses.commit();
		ses.begin();

		{
			Employee e100r = ses.findOrCreate(Employee.EMPLOYEE, "100");
			Department dept = (Department) e100r.findReference(e100r.DEPARTMENT, SDESCRIPTIVE);
			TestUte.assertTrue(dept.isValid(dept.NAME));
			TestUte.assertTrue("D200".equals(dept.getString(dept.NAME)));
			TestUte.assertTrue(dept.isValid(dept.MAX_SALARY)); // Set to default value and newly created, so field is valid
			TestUte.assertTrue(!dept.isValid(dept.BUDGET));
		}

		ses.commit();

	}

	/** Make sure referenced records are updated first.
	 *  That is, we create a new dept. This one must be flushed before employee
	 *  can have a foreign key to it. So update order matters. 
	 */
	static void updateOrderTest() {
		SSessionJdbc ses = SSessionJdbc.getThreadLocalSession();
		ses.begin();

		// create employee first
		Employee e300c = ses.findOrCreate(Employee.EMPLOYEE, "300");
		e300c.setDouble(Employee.SALARY, 0);
		// Then create dept.
		Department d800a = ses.findOrCreate(Department.DEPARTMENT, "800");
		d800a.assertNewRow();
		// then try to make reference form emp to dept ==> bad insert order !
		try {
			e300c.setReference(Employee.DEPARTMENT, d800a);
			throw new SException.Test("Bad FKey update order not trapped");
		} catch (SException.Error re) {
			SLog.getSessionlessLogger().message("RSet Failure " + re);
		}

		Department d700a = ses.findOrCreate(Department.DEPARTMENT, "700");
		d700a.assertNewRow();
		//d700a.setDirty(true); // Check no errors with just key set.

		ses.commit();
		ses.begin();

		Department d700b = ses.findOrCreate(Department.DEPARTMENT, "700");
		d700b.assertNotNewRow();
		//d700b.setDirty(true); // Trap empty UPDATE ... SET WHERE...

		ses.commit();
	}

	/** Test SCOLUMN_QUERY */
	static void columnQueryTest() {
		SSessionJdbc ses = SSessionJdbc.getThreadLocalSession();
		ses.begin();

		Employee e100 = ses.findOrCreate(Employee.EMPLOYEE, SALL, "100");
//		String dname = e100.getString(e100.DEPT_NAME);
//		TestUte.assertTrue("D200".equals(dname));

//		try {
//			e100.setString(e100.DEPT_NAME, "Changed");
//			throw new SException.Test(
//					"Attempt to set read only Dept_Name not trapped");
//		} catch (SException.Error se) {
//			SLog.slog.message("Set Failure " + se);
//		}

		ses.commit();
		ses.begin();

		List<Employee> res = ses.query(new SQuery<Employee>(Employee.EMPLOYEE).select(SALL).eq(
				Employee.EMPEE_ID, "100"));
		//res.next(1);
		Employee e100b = res.get(0);
//		String dname2 = e100b.getString(e100.DEPT_NAME);
//		TestUte.assertTrue("D200".equals(dname2));
//		res.get(1);

		ses.commit();
	}
}
