package simpleorm.examples;

import java.util.List;

import simpleorm.dataset.SQuery;
import simpleorm.examples.Payroll.PaySlip;
import simpleorm.sessionjdbc.SDriver;
import simpleorm.sessionjdbc.SSessionJdbc;
import simpleorm.sessionjdbc.SDataLoader;
import simpleorm.utils.SException;
import simpleorm.utils.SLog;
import simpleorm.utils.SUte;

/**
 * Tests and demonstrates Identifying Foreign Keys.  <p>
 * 
 * Somewhat obsolete, see ReferenceTest.
 */
public class IdentFKeysTest {
	// ### Provide an E-R diagram of the schema.

	public static void main(String[] argv) throws Exception {
		System.setErr(System.out); // Tidy up any stack traces.
		// SLog.level = 20; //Uncomment to reduce trace output.
		TestUte.initializeTest(IdentFKeysTest.class);
		try {
			testInit();
			payrollTest();
			payrollQuery();
			payrollUpdate();
			uglyPaySlipDetail();

		} finally {
			SSessionJdbc.getThreadLocalSession().close();
		}
	}

	/** Prepare for tests, Delete old data. */
	static void testInit() throws Exception {
		System.out.println("################ testInit #################");
		SSessionJdbc ses = SSessionJdbc.getThreadLocalSession();
		ses.begin();

		// / Delete any old data from a previous run.
		TestUte.dropAllTables(ses);

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
		ses.rawUpdateDB(sd.createTableSQL(Department.DEPARTMENT));
		ses.rawUpdateDB(sd.createTableSQL(Employee.EMPLOYEE));
		ses.rawUpdateDB(sd.createTableSQL(Payroll.Period.meta));
		ses.rawUpdateDB(sd.createTableSQL(Payroll.PaySlip.meta));
		ses.rawUpdateDB(sd.createTableSQL(Payroll.PaySlipDetail.meta));
		ses.rawUpdateDB(sd.createTableSQL(Payroll.UglyPaySlipDetail.meta));

		SLog.getSessionlessLogger().setLevel(level);

		ses.commit();

		// / Create some Departments and Employees.
		ses.begin();

		SDataLoader deptDL = new SDataLoader(ses, Department.DEPARTMENT);
		deptDL.insertRecords(new Object[][] {
				{ "100", "One00", "Count Pennies", "10000", "200000" },
				{ "200", "Two00", "Be Happy", "20000", "150000" },
				{ "300", "Three00", "Enjoy Life", "30000", "300000" } });

		SDataLoader empDL = new SDataLoader(ses, Employee.EMPLOYEE);
		Department d100 = ses.findOrCreate(Department.DEPARTMENT, "100");
		Department d200 = ses.findOrCreate(Department.DEPARTMENT, "200");
		// / Create an Employee
		Employee e100 = (Employee) empDL.insertRecord(new Object[] { "100",
				"One00", "123 456 7890", "10000", "3", null, null });
		Employee[] emps1 = (Employee[]) empDL
				.insertRecords(new Object[][] {
						{ "200", "Two00", "123 456 7890", "20000", "0", "200",
								"100" },
						{ "300", "Three00", "123 456 7890", "30000", "1", null,
								"100" } });
		ses.commit();

	}

	/**
	 * Demonstrates more advanced schema involving multi column multi level
	 * identifying foreign keys.
	 */
	static void payrollTest() {
		System.out.println("################ Payroll Test #################");
		SSessionJdbc ses = SSessionJdbc.getThreadLocalSession();
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

		// / Change a Detail.
		details[0].setInt(details[0].VALUE, 567);
		ses.flush();

		// / Check sums to ensure that the database is updated correctly.
		Object slipSum1 = ses
				.rawQuerySingle("SELECT SUM(\"VALUE\") FROM \"XX_PSLIP_DTL\" "
						+ " WHERE \"INCONSIST_EMP_NR\" = '100' ",true);
		if (((Number) slipSum1).intValue() != 567 + 234 + 456)
			throw new SException.Test("Bad Payslip Emp sum " + slipSum1);

		Object slipSum2 = ses
				.rawQuerySingle("SELECT SUM(\"VALUE\") FROM \"XX_PSLIP_DTL\" "
						+ " WHERE \"YEAR\" = 2001 ",true);
		if (((Number) slipSum2).intValue() != 567 + 234 + 345)
			throw new SException.Test("Bad Payslip Year sum " + slipSum2);

		ses.commit();
		ses.begin();

		// / Explicitly retrieve a value from PaySlipDetail
		Payroll.Period p2002_2 = ses.findOrCreate(Payroll.Period.meta, "2002", "2");
		Employee e100a = ses.findOrCreate(Employee.EMPLOYEE, "100");
		Payroll.PaySlip pe100_2002_2 = ses.findOrCreate(Payroll.PaySlip.meta, e100a, p2002_2);
		Payroll.PaySlipDetail pe100_2002_2_13 = ses.findOrCreate(Payroll.PaySlipDetail.meta, pe100_2002_2, "13");
		int val = pe100_2002_2_13.getInt(pe100_2002_2_13.VALUE);
		if (val != 456)
			throw new SException.Test("Bad PaySlipDetail value " + val);

		ses.commit();

	}

	/** Test basic queries on PayslipDetails */
	static void payrollQuery() {
		SSessionJdbc ses = SSessionJdbc.getThreadLocalSession();
		ses.begin();

//		SResultSet rs0 = Payroll.PaySlip.meta.select(
//				"\"COMMENTS\" = '2001, prd1, emp100'", null).execute();
//		Payroll.PaySlip ps0 = (Payroll.PaySlip) rs0.getOnlyRecord();
		
		Payroll.PaySlip ps0 = ses.query(new SQuery<PaySlip>(PaySlip.meta).eq(PaySlip.COMMENTS, "2001, prd1, emp100")).oneOrNone();

		List<Payroll.PaySlipDetail> psdq = ses.query(new SQuery<Payroll.PaySlipDetail>(Payroll.PaySlipDetail.meta).eq(
				Payroll.PaySlipDetail.PAY_SLIP, ps0));
		double total = 0;
		//while (psdq.next()) {
		for (Payroll.PaySlipDetail psd : psdq) {
//			Payroll.PaySlipDetail psd = (Payroll.PaySlipDetail) psdq
//					.getRecord();
			double val = psd.getDouble(psd.VALUE);
			total += val;
			SLog.getSessionlessLogger().debug("PSDtl " + psd + " = " + val + " " + total);
		}
		TestUte.assertTrue(total == 567 + 234);

		ses.commit();
	}

	/** More testing on updating Payrolls */
	static void payrollUpdate() {
		SSessionJdbc ses = SSessionJdbc.getThreadLocalSession();
		ses.begin();
//		SResultSet rs0 = Payroll.PaySlip.meta.select(
//				"\"COMMENTS\" = 'Deleted e200 2'", null).execute();
//		Payroll.PaySlip ps2 = (Payroll.PaySlip) rs0.getOnlyRecord();

		PaySlip ps2 = ses.query(new SQuery<PaySlip>(PaySlip.meta).eq(PaySlip.COMMENTS, "Deleted e200 2")).oneOrNone();
		Employee e200a = (Employee) ps2.findReference(ps2.EMPLOYEE);
		TestUte.assertTrue("Two00".equals(e200a.getString(e200a.NAME)));

		ps2.deleteRecord();

		ses.commit();
		ses.begin();

		Employee e200b = ses.findOrCreate(Employee.EMPLOYEE, "200");
		Payroll.Period prd1 = ses.findOrCreate(Payroll.Period.meta, "2002", "1");
		Payroll.PaySlip ps4 = ses.findOrCreate(Payroll.PaySlip.meta, e200b, prd1);
		ps4.assertNotNewRow();
		TestUte.assertTrue("ps4 Deleted e200 1".equals(ps4
				.getString(ps4.COMMENTS)));

		Payroll.PaySlipDetail psd4 = ses.findOrCreate(Payroll.PaySlipDetail.meta, ps4, "22");
		psd4.assertNotNewRow();

		psd4.setDouble(psd4.VALUE, 123);

		ses.flush();

		psd4.deleteRecord();

		ps4.deleteRecord();

		ses.commit();

	}

	static void uglyPaySlipDetail() {
		SSessionJdbc ses = SSessionJdbc.getThreadLocalSession();
		ses.begin();

		// / Create UglyPaySlipDetails
//		SResultSet rs0 = Payroll.PaySlip.meta.select(
//				"\"COMMENTS\" = '2001, prd1, emp100'", null).execute();
//		Payroll.PaySlip ps2 = (Payroll.PaySlip) rs0.getOnlyRecord();

		PaySlip ps2 = ses.query(new SQuery<PaySlip>(PaySlip.meta).eq(PaySlip.COMMENTS, "2001, prd1, emp100")).oneOrNone();
		
		SDataLoader detailDL = new SDataLoader(ses, Payroll.UglyPaySlipDetail.meta);
		Payroll.UglyPaySlipDetail[] details = (Payroll.UglyPaySlipDetail[]) detailDL
				.insertRecords(new Object[][] { { ps2, new Integer(99),
						"666" } });

		ses.flush();

		// / Change a Detail.
		details[0].setInt(details[0].VALUE, 567);
		ses.commit();
		ses.begin();

		// / Read it back and check
//		Payroll.PaySlip ps2a = (Payroll.PaySlip) Payroll.PaySlip.meta.select(
//				"\"COMMENTS\" = '2001, prd1, emp100'", null).execute()
//				.getOnlyRecord();
		
		PaySlip ps2a = ses.query(new SQuery<PaySlip>(PaySlip.meta).eq(PaySlip.COMMENTS, "2001, prd1, emp100")).oneOrNone();

		Payroll.UglyPaySlipDetail ud99 = ses.findOrCreate(Payroll.UglyPaySlipDetail.meta,
				ps2a, new Integer(99));
		int val = ud99.getInt(ud99.VALUE);
		if (val != 567)
			throw new SException.Test("Bad ugly value " + val);
		ses.commit();

	}

}
