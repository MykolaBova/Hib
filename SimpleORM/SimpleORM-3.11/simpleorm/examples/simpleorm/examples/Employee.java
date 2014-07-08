package simpleorm.examples;

import simpleorm.dataset.SFieldDouble;
import simpleorm.dataset.SFieldEnum;
import simpleorm.dataset.SFieldInteger;
import simpleorm.dataset.SFieldReference;
import simpleorm.dataset.SFieldString;
import simpleorm.dataset.SRecordInstance;
import simpleorm.dataset.SRecordMeta;
import simpleorm.dataset.SFieldFlags;
import simpleorm.dataset.validation.SValidatorGreaterEqual;
import simpleorm.utils.SException;

import static simpleorm.dataset.SFieldFlags.*;

/** This test class defines the Employee table */
public class Employee extends SRecordInstance {

//	public Employee(boolean attached){
//		super(attached);
//	}
	public static final SRecordMeta<Employee> EMPLOYEE 
        = new SRecordMeta<Employee>(Employee.class,	"XX_EMPLOYEE");

	// ie. SRecord objects describe SRecordInstances

	public static final SFieldString EMPEE_ID 
        = new SFieldString(EMPLOYEE, "Empee*Id", 20, SPRIMARY_KEY).setQuoted(true);
        // Quoted means that the DDL will wrap the column name in ""s (or []s for MSSQL).

	public static final SFieldString NAME 
        = new SFieldString(EMPLOYEE, "ENAME", 40, SMANDATORY, SDESCRIPTIVE);

	public static final SFieldString PHONE_NR 
        = new SFieldString(EMPLOYEE,	"PHONE_NR", 20)
        .putUserProperty("DISPLAY_LABEL", "Telephone Number");
       

	public static final SFieldDouble SALARY = new SFieldDouble(EMPLOYEE, "SALARY")
        .addValidator(new SValidatorGreaterEqual(0));

	public static final SFieldInteger NR_DEPENDENTS = new SFieldInteger(EMPLOYEE, "NR_DEPENDENTS");

	public static final SFieldString DEPT_ID =   new SFieldString(EMPLOYEE, "DEPT_ID", 10);
	
	static final SFieldReference<Department> DEPARTMENT 
        = new SFieldReference(EMPLOYEE, Department.DEPARTMENT, "DEPT");

	public static final SFieldString MANAGER_EMPEE_ID 
        = new SFieldString(EMPLOYEE, "MANAGER_EMPEE_ID", 20);

	static final SFieldReference<Employee> MANAGER // Recursive Reference
	    = new SFieldReference(EMPLOYEE, Employee.EMPLOYEE, "MANAGER", MANAGER_EMPEE_ID);

    public enum EType{PERMANENT, CASUAL, CONTRACT};
    static final SFieldEnum<EType> ETYPE = new SFieldEnum(EMPLOYEE, "ETYPE", EType.class);

	static final SFieldString RESUME // Curriculam Vitae
     	= new SFieldString(EMPLOYEE, "RESUME", 200, SUNQUERIED, // Not normally retrieved.
        SNOT_OPTIMISTIC_LOCKED)
        .overrideSqlDataType("VARCHAR ( 200)");	 // Could be LONG VARCHAR for Oracle, say.

	public @Override() SRecordMeta<Employee> getMeta() {
		return EMPLOYEE;
	}; 

	/**
	 * Called when the record is flushed, and so both Departent and Salary will
	 * have been set.
	 */
	public void onValidateRecord() {
		if (isValid(SALARY)) {
			double sal = getDouble(SALARY);
			Department dept = (Department) findReference(DEPARTMENT);
			if (dept != null && dept.isValid(Department.MAX_SALARY)) {
                //System.err.println("-- VR " + dept.allFields());
				double max = dept.getDouble(Department.MAX_SALARY);
				if (sal > max)
					throw new SException.Validation(
							"Salary "+ sal + " is greater than the Departments Maximum " + max, sal, max);
			}
		}
	}
    
     // Completely optional get/set methods.
     public String getPhoneNumber() {
   return getString(PHONE_NR);
 }
     public Employee setPhoneNumber(String value) {
   setString(PHONE_NR, value);  return this;}
}
