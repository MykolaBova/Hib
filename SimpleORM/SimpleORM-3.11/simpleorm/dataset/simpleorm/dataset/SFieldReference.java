package simpleorm.dataset;

import java.sql.ResultSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import simpleorm.utils.SException;
import simpleorm.utils.SUte;

/*
 * Copyright (c) 2002 Southern Cross Software Queensland (SCSQ).  All rights
 * reserved.  See COPYRIGHT.txt included in this distribution.
 */

/**
 * Represents a foreign key reference from one SRecordMeta to another.<p>
 * 
 * The following example shows two foreign keys, one from Employee to Department, and 
 * a recursive one from an Employee to their Manager.<p>
 *
 * <xmp>
	public static final SFieldString DEPT_ID =   new SFieldString(EMPLOYEE, "DEPT_ID", 10);
	
	static final SFieldReference<Department> DEPARTMENT 
        = new SFieldReference(EMPLOYEE, Department.DEPARTMENT, "DEPT");

	public static final SFieldString MANAGER_EMPEE_ID 
        = new SFieldString(EMPLOYEE, "MANAGER_EMPEE_ID", 20);

	static final SFieldReference<Employee> MANAGER // Recursive Reference
	    = new SFieldReference(EMPLOYEE, Employee.EMPLOYEE, "MANAGER", MANAGER_EMPEE_ID);
 * </xmp><p>
 * 
 * The DEPT_ID field represents the scalar column in the table that contains the department id
 * string.  
 * The DEPARTMENT field above contains a reference to the actual Departement object in 
 * memory (which will be retrieved from disk if needed).
 * (This allows DEPT_ID to be seen without retrieving the DEPARTMENT record at all.)<p>
 * 
 * Between them they create the DDL along the following lines:-
 * <xmp>CONSTRAINT XX_EMPLOYEE_6_DEPT
 * FOREIGN KEY (DEPT_ID)
 * REFERENCES XX_DEPARTMENT (DEPT_ID)</xmp>
 *
 * The Foreign key fields are normally assumed to have the same name as
 * the referenced primary key fields, which is generally a sound practice.
 * But this can be overriden where needed for cases such as the recursive
 * MANAGER reference.  Mutli column keys are also supported.<p>
 * 
 * (It is strongly recommended that tables be given key names such as 
 * "EMPLOYEE_ID" rather than just "ID".  This enables natrual joins to 
 * be performed in SQL, and facilitates many end user reporting tools.)<p>
 * 
 * "Overlapping" foreign keys are supported. Ie. Given SRecordMeta T(_K_, A, B, C) one
 * can define reference T.R which uses T.A and T.B to reference table U, and 
 * also define reference T.S which uses T.A and T.C to reference table V.  
 * So SRecordReference R and S overlap by both using scalar field A.<p>
 * 
 * Any attempt to change the value of a referencing scalar field (such as DEPT_ID above) 
 * automatically nulls the corresponding reference field (DEPARTMENT).  This includes updates
 * via Overlapping foreign keys.<p>
 * 
 * (Earlier versions of SimpleOrm (before 3.0) could automatically create the referenced 
 * scalar fields given the reference.  Ie. it automatically mapped from the conceptual to relational model.
 * This was cool, but it was considered to be too complex and has been removed.)<p>
 */

public class SFieldReference<RI extends SRecordInstance> extends SFieldMeta {

	static final long serialVersionUID = 3L;

	/**
     * Maps each scalar foreign key field to the corresponding primary key.
     * (There is a different map for each SFieldReference because each foreign key may be used as part of
     * several different "overlapping" references.)<p>
     * 
     * Guaranteed to be sorted in Primary key order regardless how created.<p>
     * 
     * (The same foreign key can only reference one primary key field for a given reference.
     * So we do not support the double use of X in<br>
     * 
     * <code>CONSTRAINT BAD FOREIGN KEY (X, X, Y) REFERENCES ODD (A, B, C)</code>
	 */
	Map<SFieldScalar, SFieldScalar> foreignKeyToPKey = new LinkedHashMap<SFieldScalar, SFieldScalar>();

	/**
	 * The ultmatiley referenced record whose keys correspond to this
	 * reference's foreign keys. Ie. the end of the chain.
	 */
	SRecordMeta<RI> referencedRecordMeta = null;


	/**
	 * Creates a foreign key field named fieldName that uses foreignKeys
     * to reference referencedRecord.referencedKeys.  
     * The foreign and referenced scalar fields are explicitly enumerated, 
     * so no naming convention is assumed.<p>
     * 
	 * @author franck.routier@axege.com
	 */
	public SFieldReference(
        SRecordMeta<?> meta, SRecordMeta<RI> referencedRecord,	String fieldName, 
        SFieldScalar[] foreignKeys,	SFieldScalar[] referencedPrimaryKeys) {

		super(meta, fieldName);
		setup(meta, referencedRecord, fieldName, foreignKeys, referencedPrimaryKeys);
	}

	/**
	 * Short cut for tables with a single foreign key column.
     */
	public SFieldReference(
        SRecordMeta<?> meta, SRecordMeta<RI> referencedRecord,	String fieldName, SFieldScalar... foreignKeys) {
        this(meta, referencedRecord, fieldName, foreignKeys, referencedRecord.getPrimaryKeys());
	}
    
    
    /**
     * Shortcut that creates a foreign key field named fieldName that assumes 
     * that the names of the scalar foreign keys are the 
     * same as the referenced private keys.
     */
	public SFieldReference(SRecordMeta<?> meta, SRecordMeta<RI> referencedRecord, String fieldName) {
		this(meta, referencedRecord, fieldName, 
            mappedForeignKeys(meta, referencedRecord),	referencedRecord.getPrimaryKeys());
	}
	
	/**
	 * Really setup the field. Must be called by the other constructors
	 * 
	 * @param meta
	 * @param referenced
	 * @param fieldName
	 * @param foreignKeys
	 * @param referencedKeys
	 * @param pvals
	 */
	private void setup(SRecordMeta<?> meta, SRecordMeta<RI> referenced,
			String fieldName, SFieldScalar[] foreignKeys,
			SFieldScalar[] referencedPrimaryKeys) {
        if (getFlags().contains(SFieldFlags.SPRIMARY_KEY)) throw new SException.Error(
            "References are not primary Keys, just the underlying scalar fields " + this);

		// Set the referenced record
		this.referencedRecordMeta = referenced;

		// Check that there is as many referenced keys as there are pkeys in
		// referenced record.
		// (we will check later, one by one, if they are the primary keys)
		if ((referencedPrimaryKeys.length != referenced.getPrimaryKeys().length)) {
			throw new SException.Error("Wrong number of foreign/primary keys for " + fieldName);
		}
		// check that the join has the same number of fields on both sides...
		if (foreignKeys.length != referencedPrimaryKeys.length) {
			throw new SException.Error(
					"Number of foreign keys and referenced keys don't match :\n"
							+ foreignKeys.length + " foreign Keys.\n" + "but "
							+ referencedPrimaryKeys.length + " referenced keys !\n");
		}

		// Run through both referenced keys and foreign keys.
        // We sort them into primary key order if they are not already.
        // (Avoids potential bugs.  Mixed order also breaks at least HSQLDB).
		for (int pkx = 0; pkx < referenced.getPrimaryKeys().length; pkx++) {
        
            int parx = -1;
            for (int xx=0; xx<referenced.getPrimaryKeys().length; xx++) 
                if (referencedPrimaryKeys[xx] == referenced.getPrimaryKeys()[pkx])
                    parx=xx;
                
			SFieldScalar refedKey = referencedPrimaryKeys[parx];
			SFieldScalar fkey = foreignKeys[parx];
            
			// check that only the pk is referenced
			if (!refedKey.isPrimary()) {
				throw new SException.Error("Referenced key " + refedKey
						+ " is not part of the primary key of " + referenced);
			}

			
			// Check that fkey and corresponding pkey have same type, length,
			if (!refedKey.isFKeyCompatible(fkey)) {
				throw new SException.Error("Foreign key " + fkey
						+ " not same type or length as key " + refedKey);
			}

            // Eg.  this=Employee.Department, fkey=Employee.DepartementId, refedKey=Departement.DepartmentId
			fkey.addReference(this);
            
            if ( foreignKeyToPKey.get(fkey) != null )
                throw new SException.Error("Cannot use FKey " + fkey + " twice in same reference " + this);
			foreignKeyToPKey.put(fkey, refedKey);
		}
	}

    /*
	 * Just maps foreign keys based on "same name" assumption. Fails if no corresponding fkey
	 */
	static private SFieldScalar[] mappedForeignKeys(SRecordMeta<?> recordMeta,
			SRecordMeta<?> refedMeta) {

		SFieldScalar[] mappedFKeys = new SFieldScalar[refedMeta.getPrimaryKeys().length];
		
		int i = 0;
		for (SFieldScalar pk : refedMeta.getPrimaryKeys()) {
			// check if a field of the same name exists in referenced record
			if (recordMeta.getFieldNames().contains(pk.getFieldName())) {
				mappedFKeys[i] = (SFieldScalar) recordMeta.getField(pk.getFieldName());
			} else { //don't try to be clever, just throw exception
				throw new SException.Error("No corresponding foreign key for this refed pkey "+pk);
			}
		}
		return mappedFKeys;
	}

	// //////////////////////////////////////////////////////////////////////////

	public String createColumnSQL() {
		throw new SException.Error("No column can be created for a reference");
	}

	/**
	 * Specializes SFieldMeta.getRawFieldValue, which is called by
	 * getFieldValue, thence SRecoredInstance.getObject...
	 * <p>
	 * 
	 */
	@Override Object getRawFieldValue(SRecordInstance rinst, SQueryMode queryMode, SSelectMode selectMode) {
		
		SRecordInstance result = null;
		// First, try to get it from the field instance, in case it was already retrieved
		if (rinst.isValid(this))
			result = (SRecordInstance) rinst.getRawArrayValue(this);
		// If result is not null but is NOT valid in any way, set it to null
		if (result != null
				&& (!result.isNotDestroyed() || (rinst.isAttached() && !result.isAttached()))) {
			result = null;
		}
		// Result is null, try to find or create it
		if (result == null) {            
            Object[] keyValues = new Object[referencedRecordMeta.getPrimaryKeys().length];
            int kv = 0;
            for (SFieldScalar fkey : getForeignKeyMetas()) { // Guarenteed to be in order of pkey
                Object fkValue = rinst.getObject(fkey); 
                //if (fkValue == null)  return null; // if any scalar key is null then reference is null.
                
                if (fkValue == null) {
                    rinst.setRawArrayValue(this, null); // For next time.
                    return null; // if any scalar key is null then reference is null.
                }
                
                keyValues[kv] = fkValue ;
                kv++;
            }

       		if (result == null && queryMode.equals(SQueryMode.SREFERENCE_NO_QUERY))
    			return Boolean.FALSE; // would return null if a scalar field was null.

            SDataSet ds = rinst.getDataSet();
            if (ds == null) 
               throw new SException.Data(
                   "Attempt to find a referenced record which has no DataSet (and thus no SSession) " + rinst + this);
                result = ds.find(referencedRecordMeta, keyValues);
                if (result == null) {
                    SSessionI ses = ds.getSession();
                    if (ses == null) {
                        throw new SException.Data(
                            "Attempt to find a referenced record from the database but not in a session " + rinst + this);
                    }
                    result = ses.find(referencedRecordMeta, referencedRecordMeta.fieldsForMode(selectMode), queryMode, keyValues);
                    if (result == null) throw new SException.Data("Inconsitent reference. Could not find the reference in database, so the foreign key os probably broken.");
                }
               rinst.setRawArrayValue(this, result); // For next time.
		}
		return result;
	}

	/**
	 * Sets the instance reference to value. Then recursively copies all of the
	 * foreign keys. <code>this</code> must be a top level foreign key. (value
	 * could be null.)
	 */

	@Override void setRawFieldValue(SRecordInstance instance, Object value) {

		SRecordInstance instValue = (SRecordInstance) value;

		if (instValue != null && ! instValue.isNotDestroyed() )
			throw new SException.Error("Cannot set " + instance + "." + this + " to Destroyed " + instValue);
        
        if (instance.getDataSet() == null)
            throw new SException.Error("Cannot set ref to instance without DataSet " + instance);
        
		// / Check for bad update orders (would trigger foreign keys constraints violations in database)
		if (instValue != null) {
			if (instValue.isNewRow() && instance.isDirty() && instValue.dirtyRecordsIndex > instance.dirtyRecordsIndex) {
					throw new SException.Error(
							"Attempt to set " + instance + "." + this + " to new row " + instValue
									+ " with incorrect update order will produce foreign key violations. (do a SSession.flush first.)");
			}
			if (instValue.isDeleted())
				throw new SException.Error("Attempt to set " + instance + "." + this + " to deleted row " + instValue);
		}


		// / Set foreign keys (all scalar)
		for (SFieldScalar fkey : getForeignKeyMetas()) {
			SFieldScalar refedPk = this.getPrimaryKeyForForegnKey(fkey);
			Object pkValue = instValue == null ? null : instValue.getRawArrayValue(refedPk);

			// Issue is that if we deliberately set the value to null (Basic
			// Tests) there is no looked up value, get directRefed == null.
			// But in IdentFKeys we only retrieve the Payslip, not the
			// Employee rec, and so Empee ref is null although Empee Nr is
			// not.
            
            // Do not null overlapping references here -- that happens in SFieldScalar so
            // that it also catches direct updates to the scalar fields.
//System.err.println("Settign fkey " + fkey + pkValue + instance.isDirty() + " was " + instance.getRawArrayValue(fkey));
			instance.setObject(fkey, pkValue);
//System.err.println("Have Sett fkey " + fkey + value + instance.isDirty());
            
		}
        
        // Set the actual record on instance.
        // Do last to stop overlapping foreign keys from just clearing it.
		instance.setRawArrayValue(this,  instValue);
}
    
    /** Called from SFieldScalar if a foreign key value is changed.
     * Not sure if this is right.
     */
    void clearOverlappedForeignKey(SRecordInstance instance) {
        instance.setRawArrayValue(this, null);
    }

	public String toString() {
		// / Make pretty referenceName
		return "[FR "
				+ (getRecordMeta() != null ? SUte
						.cleanClass(getRecordMeta().getUserClass()) : "NULL")
				+ "."
				+ "_"
				+ (referencedRecordMeta != null ? SUte
						.cleanClass(referencedRecordMeta.getUserClass()) : "NULL")
				+ "]";
	}

    public @Override String toLongerString() {
		StringBuffer res = new StringBuffer("[FRL " + this 
                + (isForeignKey() ? " FKey" : "")
				+ " RefedRec " + referencedRecordMeta 
                + " FKeyFlds ");
		res.append(SUte.arrayToString(getForeignKeyMetas()));
		res.append("]");
		return res.toString();
	}

	public Set<SFieldScalar> getForeignKeyMetas() {
		return foreignKeyToPKey.keySet();
	}
    /** The Primary key in the other table refrenced by this foreign key 
     * for this reference (might be overlapping). */
	public SFieldScalar getPrimaryKeyForForegnKey(SFieldScalar fkey){return foreignKeyToPKey.get(fkey);}

	@Override public Object queryFieldValue(ResultSet rs, int sqlIndex) {
		throw new SException.InternalError("Attempt to query " + this);
	}

	@Override protected Object convertToDataSetFieldType(Object raw) throws Exception {
		Class<?> refed = referencedRecordMeta.getUserClass();
		if (raw != null && !refed.isInstance(raw))
			throw new SException.Error("Object " + raw + " not a "
					+ refed.getName());
		return raw;
	}

	/** Specializes SFieldMeta. */
	String defaultDataType() {
		throw new SException.InternalError("SFieldReference has no datatype.");
	}


	/**
     * The RecordMeta that this reference returns SRecordInstances of.
	 * (Replaces badly named getReferencedRecord)
	 */
	public SRecordMeta<RI> getReferencedRecordMeta() {
		return referencedRecordMeta;
	}
    
    
}
