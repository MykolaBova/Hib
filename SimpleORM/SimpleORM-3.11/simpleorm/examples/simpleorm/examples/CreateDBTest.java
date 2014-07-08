package simpleorm.examples;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;

import simpleorm.sessionjdbc.SDriver;
import simpleorm.sessionjdbc.SSessionJdbc;

/**
 * Creates the createdb.sql script (Not necessary to run tests). This test
 * creates temp/createdb.sql which contains DROP and CREATE TABLE statments that
 * can recreate the tables in the given database. This should be used if
 * SimpleORM is to be made the single source of truth for database definitions.
 * <p>
 * 
 * Note that this is not actually required to run the other tests as they each
 * explicitly create any tables that they need using <code>executeUpdate</code>.
 */
public class CreateDBTest {
	// ### Provide an E-R diagram of the schema.

	public static void main(String[] argv) throws Exception {
		System.setErr(System.out); // Tidy up any stack traces.
		// SLog.level = 20; //Uncomment to reduce trace output.
		TestUte.initializeTest(CreateDBTest.class);
		createTables();
		SSessionJdbc.getThreadLocalSession().close();
	}

	/**
	 * Creates the createdb.sql script file for the database. This is referenced
	 * as part of the build process.
	 * <p>
	 * 
	 * Take a look at the generated file to see how the foreign key propagation
	 * works.
	 * <p>
	 */
	static void createTables() throws Exception {

		File file = File.createTempFile("createdb-",".sql");
		FileOutputStream fis = new FileOutputStream(file);
		PrintStream out = new PrintStream(fis);
		out.println("-- Warning Generated file\n");

		out.println("DROP TABLE XX_PSLIP_DETAIL;");
		out.println("DROP TABLE XX_PAY_SLIP;");
		out.println("DROP TABLE XX_PAY_PERIOD;");
		out.println("DROP TABLE XX_EMPLOYEE;");
		out.println("DROP TABLE XX_DEPARTMENT;\n");

        SDriver sd = SSessionJdbc.getThreadLocalSession().getDriver();
		out.println(sd.createTableSQL(Department.DEPARTMENT) + ";");
		out.println(sd.createTableSQL(Employee.EMPLOYEE) + ";");
		out.println(sd.createTableSQL(Payroll.Period.meta) + ";");
		out.println(sd.createTableSQL(Payroll.PaySlip.meta) + ";");
		out.println(sd.createTableSQL(Payroll.PaySlipDetail.meta) + ";");

		out.close();
	}

}
