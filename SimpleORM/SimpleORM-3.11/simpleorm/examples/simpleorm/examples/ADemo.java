package simpleorm.examples;

import static simpleorm.dataset.SQueryMode.SFOR_UPDATE;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.List;

import simpleorm.dataset.SFieldString;
import simpleorm.dataset.SQuery;
import simpleorm.dataset.SQueryResult;
import simpleorm.sessionjdbc.SDataLoader;
import simpleorm.sessionjdbc.SSessionJdbc;
import simpleorm.utils.SException;
import simpleorm.utils.SLog;
import simpleorm.utils.SUte;

/**
 * <b>START HERE</b> -- this class provides basic examples of the main
 * SimpleORM features. It demonstrates simple record manipulations including
 * non-identifying foreign keys.
 * <p>
 */

public class ADemo {

	public static void main(String[] argv) throws Exception {
		TestUte.initializeTest(ADemo.class); // Look at this code.

		try {
            printTables();
			testInit();
			deptTest();
			empTest();
            employeePlugIn();
            printTables();
		} finally {
			SSessionJdbc.getThreadLocalSession().close();
		}
	}   

	/** Prepare for tests, Delete old data. */
	static void testInit() throws Exception {
		SLog.getSessionlessLogger().message("################ Init #################");
        SSessionJdbc ses = SSessionJdbc.getThreadLocalSession();
		ses.begin();

		// / Delete any old data from a previous run.
		TestUte.dropAllTables(ses);

		// / Create the tables.
        ses.rawUpdateDB(ses.getDriver().createTableSQL(Department.DEPARTMENT));
		ses.rawUpdateDB(ses.getDriver().createTableSQL(Employee.EMPLOYEE));

		ses.commit();
	}

	/** Basic examples/tests not involving foreign keys. */
	static void deptTest() throws Exception {
		SLog.getSessionlessLogger().message("################ deptTest #################");
        SSessionJdbc ses = SSessionJdbc.getThreadLocalSession();
		ses.getLogger().enableDebug();
		ses.begin();

		// / Create some Departments using the SDataLoader
		SDataLoader deptDL = new SDataLoader(ses, Department.DEPARTMENT);

		deptDL.insertRecords(new Object[][] {
				{ "100", "One00", "Count Pennies", 10000, 200000 },
				{ "200", "Two00", "Be Happy", 20000, 300000 },
				{ "300", "Three00", "Enjoy Life", 30000, 150000 } });

		// / Flush the records to the database and commit the transaction.
		ses.commit();
		ses.begin();

		// / Retrieve a Department and check that we have the right one.
		String key = "100";
		Department department = ses.mustFind(Department.DEPARTMENT, key);
        System.err.println("??? " + department.getString(Department.NAME));
		TestUte.assertEqual("One00", department.getString(Department.NAME));

		// / Query the same Department again. This does not query the database.
		Department department2 = ses.find(Department.DEPARTMENT, "100");
		assertTrue(department == department2);

		// / Create a new, empty department and set some column values.
		Department newDept = ses.create(Department.DEPARTMENT, "900");
		newDept.setString(Department.NAME, "New900");
		newDept.setDouble(Department.BUDGET, 90000);

		ses.getDataSet().dumpDataSet(); // debug only
		ses.flush(); // To the database, but do not commit yet.
		ses.getDataSet().dumpDataSet();

		// / At this point there should be three departments with > $10,000.
		selectDepartments(20000 + 30000 + 90000);

		// / Rollback new 900 Department. Flush() does not mean commit.
		ses.rollback();
		ses.begin();

        // / Delete Department 300.
		// / findOrCreate will (temporarily) create record 300 if it did not exist.
		Department delDept = ses.findOrCreate(Department.DEPARTMENT, "300");
		delDept.deleteRecord();

        Department opt100 = ses.mustFind(Department.DEPARTMENT, SFOR_UPDATE, "100");
        opt100.setDouble(Department.BUDGET, opt100.getDouble(Department.BUDGET) + 1000);
        // We still have the original value available.
        TestUte.assertEqual(10000.0, opt100.getInitialValue(Department.BUDGET));        
        
		ses.commit();
		ses.begin();

		selectDepartments(11000 + 20000);

		ses.commit();
	}

	/**
	 * Query all the departments with Budget > 10000 and check that the total
	 * budget == total.
	 */
	static void selectDepartments(int total) {
        SSessionJdbc ses = SSessionJdbc.getThreadLocalSession();
		int qbudget = 10000;

		// / Prepare and execute the query. The two queries are identical,
		// but one uses raw SQL and the other uses the query builder.
		SQueryResult<Department> res  = ses.query(
            new SQuery(Department.DEPARTMENT)
                .gt(Department.BUDGET, qbudget)
                .descending(Department.NAME)); 

		// / loop through the results, adding up the budgets.
		double totBudget = 0;
		for (Department dept : res) {
			double budget = dept.getDouble(Department.BUDGET);
			totBudget += budget;
		}

		// / Check that the total is what we expect.
		assertTrue(totBudget == total);
	}

	/**
	 * Examples non identifying foreign key relationship from Employee to
	 * Department, and from Employee to Employee.Manager.
	 */
	static void empTest() { // Foreign Keys
		SLog.getSessionlessLogger().message("################ empTest #################");
        SSessionJdbc ses = SSessionJdbc.getThreadLocalSession();

		ses.begin();

		// / Show the definition of all the fields in the Employee record. Note that the
		// / DEPT_ID is included even though it is not explicitly part of the
		// / Employee record's definition.
		System.err.println("Employee Fields " + SUte.allFieldsString(Employee.EMPLOYEE));

		// / Create an Employee
		SDataLoader empDL = new SDataLoader(ses, Employee.EMPLOYEE);
		Employee e100 = (Employee) empDL.insertRecord(
            "100", "One00", "123 456 7890", 10000, 3, null, null, Employee.EType.PERMANENT);

		// / Explicitily set the Department
		Department d100 = ses.findOrCreate(Department.DEPARTMENT, SFOR_UPDATE, "100");
		e100.setReference(Employee.DEPARTMENT, d100);
        
		// allFields() shows the value of all the fields, as opposed to the concise toString() that just shows keys.
		System.err.println("e100#" + e100.allFields());

        // / Create more Employees setting the Department using the DataLoader
		ses.findOrCreate(Department.DEPARTMENT, "200");
		empDL.insertRecords(new Object[][] { // Twice should not cause grief
			{ "200", "Two00", "123 456 7890", 20000, 0, "200" /*dept id*/, "100"/*mgr emp id*/, Employee.EType.CONTRACT},
			{ "300", "Three00", "123 456 7890", 30000, 1, null,	"100", "CASUAL" },
			{ "400", "Four00", "123 456 7890", 40000, 1, "100",	"100", "CASUAL" }, 
			{ "500", "Four+One00", "123 456 7890", 50000, 1, "100",	"200", "CASUAL" }, 
        });
		ses.flush();
		System.err.println("----------- emp 200 and 300 flushed ------------");
		Employee e300 = ses.query(new SQuery<Employee>(Employee.EMPLOYEE)
            .eq(Employee.NAME, "Three00")).oneOrNone();
		assertTrue("123 456 7890".equals(e300.getString(Employee.PHONE_NR)));
		assertTrue("123 456 7890".equals(e300.getPhoneNumber()));

		ses.commit();
		ses.begin();

		// / Check e100 record NOT valid after a commit().  This is safe
		// / (locks released so need to requery).
		try {
			e100.getString(Employee.NAME); // Error as e100 destroyed by commit.
			throw new SException.Test("e100 not destroyed.");
		} catch (SException.Error er) {
			ses.getLogger().message("Destroyed Record Message: " + er);
		}
		
		// / Retrieve e100's Department.
		Employee e100a = ses.findOrCreate(Employee.EMPLOYEE, "100");
		
		Department d100a = e100a.findReference(Employee.DEPARTMENT);
		assertTrue(d100a.getString(Department.NAME).equals("One00"));
		                
        ////////// User Property        
        String  label = Employee.PHONE_NR.getUserProperty("DISPLAY_LABEL");
        ses.getLogger().message("Employee " + label + ": " + e100a.getString(e100a.PHONE_NR) );            
        TestUte.assertEqual("Telephone Number", label);
                      
        //////// Enum Test
        Employee.EType etype = e100a.getEnum(e100a.ETYPE);
        TestUte.assertEqual(Employee.EType.PERMANENT, etype);
        
        try {
            e100a.setString(e100a.ETYPE, "FOO");
            throw new SException.Test("Bad Enum not detected");
        } catch (SException.Data ene) {
            TestUte.assertEqual(ene.getInstance(), e100a);
        }

        
        ///////  Validatation Tests 
		// / Attempt to pay e200 -ve is traped immediately.
		Employee e200 = ses.findOrCreate(Employee.EMPLOYEE, "200");
		try {
			e200.setDouble(e200.SALARY, -1);
			throw new SException.Test("Negative Salary not detected.");
		} catch (SException.Validation ve) {
			ses.getLogger().message("Salary Negative message: " + ve.getMessage());
		}

		// / Attempt to pay e200 too much is detected at Commit/Flush time.
		e200.setDouble(e200.SALARY, 500000);
		try {
			ses.commit();
			throw new SException.Test("Big Salary not detected.");
		} catch (SException.Validation ve) {
			ses.getLogger().message("Big Salary message: " + ve.getRecordInstance()
					+ ve.getMessage());
			ses.rollback();
		}
        
        ///////// Query ///////////
        ses.begin();
   		Department d100q = ses.findOrCreate(Department.DEPARTMENT, "100");          
        SQuery<Employee> query = new SQuery(Employee.EMPLOYEE)
            .eq(Employee.DEPARTMENT, d100q) // and
            .like(Employee.NAME, "%One%")
            .descending(Employee.SALARY);
        List<Employee> emps = ses.query(query);
        
        TestUte.assertEqual("100", emps.get(1).getString(Employee.EMPEE_ID));
        
        ses.commit();       
	}

    static boolean doPlugin = true;
    static void employeePlugIn() {
        
        // SimpleORM's generalized structures enable separate modules
        // to add extra fields to a given record type.
        
        if (doPlugin) { // Suppressed when doing all tests
            final SFieldString PUBLIC_KEY = new SFieldString(Employee.EMPLOYEE, "PUBLIC_KEY", 200);

            SSessionJdbc ses = SSessionJdbc.getThreadLocalSession();
            TestUte.createDeptEmpDDL(ses);

            ses.begin();
            Employee fred = ses.create(Employee.EMPLOYEE, "100");
            fred.setString(fred.NAME, "Fred");
            fred.setString(PUBLIC_KEY, "ae6d82f9a7e9c...");
            ses.commit();
        }        
    }
    
    static void printTables() throws Exception {
        System.err.println("======== PRINTING TABLES ========");
        SSessionJdbc ses = SSessionJdbc.getThreadLocalSession();
		ses.begin();        
         DatabaseMetaData dbmd = ses.getJdbcConnection().getMetaData();
         ResultSet tableData  = dbmd.getTables(null, null, "%", new String[]{ "TABLE", "VIEW" });
         while (tableData.next()) {
            System.err.println("=== TABLE " + tableData.getString("TABLE_NAME")) ;
         }        
         ses.commit();
    }
	static void assertTrue(boolean cond) {
		TestUte.assertTrue(cond);
	}
}
