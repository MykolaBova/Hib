package simpleorm.examples;

import simpleorm.dataset.SFieldDouble;
import simpleorm.dataset.SFieldString;
import simpleorm.dataset.SRecordInstance;
import simpleorm.dataset.SRecordMeta;
import simpleorm.dataset.SFieldFlags;
import static simpleorm.dataset.SFieldFlags.*;


/** This test class defines the Department table */
public class Department extends SRecordInstance {

//	public Department(boolean attached){
//		super(attached);
//	}
	private static final long serialVersionUID = 1L;

	public static final SRecordMeta<Department> DEPARTMENT 
        = new SRecordMeta(Department.class, "XX_DEPARTMENT");

	// ie. SRecord objects describe SRecordInstances

	public static final SFieldString DEPT_ID 
        = new SFieldString(DEPARTMENT, "DEPT_ID", 10, SPRIMARY_KEY);

	public static final SFieldString NAME 
        = new SFieldString(DEPARTMENT, "DNAME", 40, SDESCRIPTIVE);

	public static final SFieldString MISSION 
        = new SFieldString(DEPARTMENT, "MISSION", 40);

	public static final SFieldDouble BUDGET = new SFieldDouble(DEPARTMENT, "BUDGET");

	public static final SFieldDouble MAX_SALARY 
        = new SFieldDouble(DEPARTMENT, "MAX_SALARY").setInitialValue(new Double(8999));

	public @Override() SRecordMeta<Department> getMeta() {
		return DEPARTMENT;
	};
}
