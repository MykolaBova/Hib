package simpleorm.examples;

import simpleorm.dataset.SFieldDouble;
import simpleorm.dataset.SFieldReference;
import simpleorm.dataset.SFieldString;
import simpleorm.dataset.SRecordInstance;
import simpleorm.dataset.SRecordMeta;
import simpleorm.dataset.SFieldFlags;

/**
 * This test class defines the Project table. It has an Identifying
 * relataionship with Department, ie. Department forms part of its key.
 */
public class Project extends SRecordInstance {

//	public Project(boolean attached){
//		super(attached);
//	}
	public static final SRecordMeta meta = new SRecordMeta(Project.class,
			"XX_PROJECT");

	static final SFieldReference DEPARTMENT = new SFieldReference(meta,
			Department.DEPARTMENT, (String) null);

	public static final SFieldString PROJECT_ID = new SFieldString(meta,
			"PROJECT_ID", 10, SFieldFlags.SPRIMARY_KEY);

	public static final SFieldString NAME = new SFieldString(meta, "NAME", 40,
			SFieldFlags.SDESCRIPTIVE);

	public static final SFieldDouble BUDGET = new SFieldDouble(meta, "BUDGET");

	public SRecordMeta getMeta() {
		return meta;
	}; // specializes abstract method
}
