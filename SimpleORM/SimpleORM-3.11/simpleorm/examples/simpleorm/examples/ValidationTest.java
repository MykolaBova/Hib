package simpleorm.examples;

import java.io.Serializable;

import simpleorm.dataset.SFieldDouble;
import simpleorm.dataset.SFieldMeta;
import simpleorm.dataset.SFieldReference;
import simpleorm.dataset.SFieldScalar;
import simpleorm.dataset.SFieldString;
import simpleorm.dataset.SRecordInstance;
import simpleorm.dataset.SRecordMeta;
import simpleorm.dataset.SFieldFlags;
import simpleorm.dataset.validation.SValidatorEnumeratedValues;
import simpleorm.dataset.validation.SValidatorI;
import simpleorm.sessionjdbc.SSessionJdbc;
import simpleorm.sessionjdbc.SDataLoader;
import simpleorm.utils.SException;
import simpleorm.utils.SLog;

/**
 * Tests validation, OnFieldValidate etc. (Basic validation test is also in
 * ADemo/Employee.)
 */

public class ValidationTest  {

	static public class Validated extends SRecordInstance {

//		public Validated(boolean attached){
//			super(attached);
//		}

		public static final SRecordMeta<Validated> meta = new SRecordMeta(Validated.class, "XX_VALIDATED");

		public static final SFieldString VALID_ID 
            = new SFieldString(meta,	"VALID_ID", 20, SFieldFlags.SPRIMARY_KEY)
            .addValidator(new SValidatorI() {
			    public void onValidate(SFieldMeta field, SRecordInstance instance) throws SException.Validation {
						((Validated)instance).validId = instance.getObject(field);
					}
				});

		public static final SFieldString NAME 
            =  new SFieldString(meta, "VNAME", 40, SFieldFlags.SDESCRIPTIVE)
            .addValidator(new SValidatorI() {
			    public void onValidate(SFieldMeta field, SRecordInstance instance) throws SException.Validation {
						((Validated)instance).name = instance.getObject(field);
				}
			});

		public static final SFieldString PHONE_NR = new SFieldString(meta,	"PHONE_NR", 20);

		public static final SFieldDouble SALARY =  new SFieldDouble(meta, "SALARY")
            .addValidator(new SValidatorI(){
                public void onValidate(SFieldMeta field, SRecordInstance instance) throws SException.Validation {
					double sal = instance.getObject(field) == null ? 0 : ((Number) instance.getObject(field)).doubleValue();
					if (sal < 0)
						throw new SException.Validation("Salary {0} < 0.", new Double(sal));
					}
				}
			);

		public static final SFieldString THIS_THAT = new SFieldString(meta, "THIS_THAT", 20)
            .addValidator(new SValidatorEnumeratedValues("THIS", "THAT"));
        
        public static final SFieldString DEPT_ID =   new SFieldString(meta, "DEPT_ID", 10);
	
    	static final SFieldReference<Department> DEPARTMENT 
          = new SFieldReference(meta, Department.DEPARTMENT, "DEPT");

		@Override public SRecordMeta getMeta() {
			return meta;
		};

		public Object salary, validId, name; // Only used for tests.

        
        double moreSalary;
       

        /** Called when the record is queried from database. */
        @Override public void onQueryRecord() {
            moreSalary = getDouble(SALARY) * 2;
        }
        
        /** Called each time a field value is set. */
        @Override public void onValidateField(SFieldMeta field, Object newValue) {
            if (field == SALARY)
                moreSalary = getDouble(SALARY) * 3;
        }
        
		/**
		 * Called when the record is flushed, and so both Departent and Salary
		 * will have been set.
		 */
		@Override public void onValidateRecord() {
			SLog.getSessionlessLogger().fields("Validated.record " + this);
			if (isValid(Validated.SALARY)) {
				double sal = getDouble(SALARY);
				Department dept = findReference(Validated.DEPARTMENT);
			    double max = dept.getDouble(Department.MAX_SALARY);
				if (sal > max)
					throw new SException.Validation(
							"Salary " + sal + " is greater than the Departments Maximum " + max, sal, max);
			}
		}
	} // Validated

	public static void main(String[] argv) throws Exception {
		TestUte.initializeTest(ValidationTest.class); // Look at this code.
		try {
			testInit();
			validationTest();
		} finally {
			SSessionJdbc.getThreadLocalSession().close();
		}
	}

	/** Prepare for tests, Delete old data. */
	static void testInit() throws Exception {
		System.out.println("################ Init #################");
		SSessionJdbc ses = SSessionJdbc.getThreadLocalSession();

        TestUte.createDeptEmp(ses);
		ses.begin();
		ses.rawUpdateDB(ses.getDriver().createTableSQL(Validated.meta));
		ses.commit();
	}

	static void validationTest() { // Foreign Keys
		System.out.println("################ validationTest #################");
		SSessionJdbc ses = SSessionJdbc.getThreadLocalSession();
		ses.begin();

		// / Create a validated
		SDataLoader vldDL = new SDataLoader(ses, Validated.meta);
		Validated[] recs = (Validated[]) vldDL.insertRecords(new Object[][] {
				{ "100", "One00", "123 456 7890", 10000,  "THIS", "100" },
				{ "200", "Too00", "123 456 7890", 20000, "THAT", "100" } });
        
		ses.commit();
		ses.begin();

		// / Create empty v300
		Validated v300 = (Validated) ses.create(Validated.meta, "300"); // new
		assertTrue("300".equals(v300.validId));
        
        ////// Field validators
        try {
            v300.setString(v300.PHONE_NR, "123456789.123456789.1");
            throw new SException.Test("Too long phone nr not detected");                
        } catch (SException.Validation vpe) {}
        v300.setString(v300.THIS_THAT, "THAT");
        try {
            v300.setString(v300.THIS_THAT, "OOPS    ");;
            throw new SException.Test("Too long phone nr not detected");                
        } catch (SException.Validation vtte) {}

        
        ses.flush();

		// / Non-creation of empty v400.
		Validated v400 = ses.findOrCreate(Validated.meta, "400"); // new
		assertTrue("400".equals(v400.validId));
		ses.flush();

		// / Attempt to pay e200 -ve is traped immediately.
		Validated e200 = ses.findOrCreate(Validated.meta, "200");
        TestUte.assertEqual(e200.moreSalary, e200.getDouble(e200.SALARY) * 2); // set by onQueryRecord
		try {
			e200.setDouble(Validated.SALARY, -1);
			throw new SException.Test("Negative Salary not detected.");
		} catch (SException.Validation ve) {
			SLog.getSessionlessLogger().message("Salary Negative message: " + ve.getMessage());
            TestUte.assertEqual(ve.getFieldValue(), -1);
		}

		// / Attempt to pay e200 too much is detected at Commit/Flush time.
		e200.setDouble(Validated.SALARY, 200001);
        TestUte.assertEqual(e200.moreSalary, e200.getDouble(e200.SALARY) * 3); // set by onValidateField
		try {
			ses.commit();
			throw new SException.Test("Big Salary not detected.");
		} catch (SException.Validation ve) {
			SLog.getSessionlessLogger().message("Big Salary message: " + ve.getRecordInstance() + ve.getMessage());
            TestUte.assertEqual(ve.getRecordInstance(), e200); // saved by SimpleOrm
			ses.rollback();
		}

	}

	static void assertTrue(boolean cond) {
		TestUte.assertTrue(cond);
	}
}
