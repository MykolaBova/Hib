package simpleorm.dataset;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import simpleorm.utils.SException;
import simpleorm.utils.SLog;
import simpleorm.utils.SUte;
import simpleorm.utils.SException.Error;

/**
 * Each SRecordInstance represents an individual record in memory that either
 * correspond to a row in the database or a new record that has yet to be
 * inserted.
 * <p>
 * 
 * RecordInstances are normally created by either {@link SSessionJdbc#findOrCreate
 * findOrCreate} or {@link SSessionJdbc#Query}. The former selects a specific
 * record by primary key, while the latter retrieves all records that satify a query.  
 * Instances may also be directly created by {@Link SDataSet#findOrCreate} without reference
 * to a session.
 * <p>
 * 
 * The main methods are <code>get*</code> and <code>set*</code> which get
 * and set field values of the record. {@link #deleteRecord} deletes a row.
 * <p>
 * 
 * No two RecordInstances can have the same primary key field values within the
 * same connection. Attempts to retrieve the same row twice will simple return a
 * pointer to an existing SRecordInstance. There is no relationship between
 * RecordInstances in different connections -- all locking is done by the
 * underlying database.
 * <p>
 */

public abstract class SRecordInstance extends SRecordGeneric implements Serializable {

  	static final long serialVersionUID = 20084L;

 /**
	 * This must be defined in every user record's definition to access the
	 * SRecord which provides the meta data for this instance. It is normally
	 * defined as:-
	 * <p>
	 * 
	 * <pre>
     * 	public static final SRecordMeta<Employee> EMPLOYEE 
            = new SRecordMeta<Employee>(Employee.class,	"XX_EMPLOYEE");
     * ...
	 * SRecord getMeta() {
	 * 	return EMPLOYEE;
	 * };
	 * </pre>
	 * 
	 * <p>
	 * 
	 * Note that when instances are serialized, the meta data is not also serialized.
     * They are expected to be available in the JVM.
     * This is similar to the way Classes are not serialized when ordinary POJOs are serialized.
     * That is why SRecrodMeta is not serializable.<p>
     * 
     * Storing the <code>EMPLOYEE</code> variable in a static ensures that they will
     * not be serialized and that the normal Java class matching will be used to locate the
     * correct super class and thus SRecordMeta upon deserialization. <p>
     * 
     * However, if some other mechanism is used then it is up to the use to ensure that
     * the RecordMeta is not serialized, eg. by declaring it transient, and then to find them again. 
     * (That would be an unusual, advanced usage of SimpleORM.)<P>
  * 
  * TODO: Should ideally check meta is correct one, eg. by comparying a hash of field names.<p>
	 */
	public abstract SRecordMeta<?> getMeta();

	/**
	 * The one connection to which this Instance is glued. Instances may never
	 * be shared across connections.
	 */
	
	SDataSet sDataSet = null;
	  
    /** 
     * The value for each field.  
     * Stored in the type they are declared in.  So SFieldIntegers are use JDBC ResultSet.getInt(),
     * and setString (say) converts to an Integer.
     * 
     * There is no actual SimpleOrm object that corresponds to an individual field instance value,
     * a dubious optimization.
     */
	Object[] fieldValues = null;
    /** Value of fieldValue when record retrieved, used to implement optimistic locking. */
	Object[] fieldOptimisticValues = null;
	
	/**
	 * True if queried read only. 
     * Was important if FOR UPDATE not used, but now always optimistically locked anyway.
     * So now just a convenience, to be able to prevent updates to a record.
	 */
	boolean readOnly = false;	

	/**
	 * -1: not in sConnection.updateList either because 1. not dirty or 2. not
	 * attached.
	 */
	int dirtyRecordsIndex = -1; // (Transient means set to 0, not -1!)
	
	/** Need to delete at commit. */
	boolean deleted = false;
	
	/**
	 * Set after <code>findOrInsert()</code> that does not find the row
	 * already in the database.
	 */
	boolean newRow = false;

	/**
	 * @see #wasInCache()
	 */
	boolean wasInCache = false;

    /**
//     * Used for the savepoint feature
//     */	
//	private boolean spNewRow;
//	    /**
//     * Used for the savepoint feature
//     */	
//	private boolean spDeleted;
//	/**
//     * Used for the savepoint feature
//     */	
//	private Object[] spFieldValues;
//	/**
//     * Used for the savepoint feature
//     */	
//	private Object[] spFieldOptimisticValues;
//	/**
//     * Used for the savepoint feature
//     */	
//	private int spDirtyRecordIndex;
//	/**
//     * Used for the savepoint feature
//     */	
//	private SDataSet spDataSet;
		    
    @SuppressWarnings("serial")
	static class InvalidValue implements Serializable {} // Always test with instanceof, not ==
                                                         // Notice in hashCode we use ==...
    static final InvalidValue INVALID_VALUE = new InvalidValue();
    
	
	/**
	 * Protected default constructor. Necessary to allow definition
	 * of record with no explicit constructor.
	 * If overriding is REALLY necessary, don't forget to call super();
	 */
	protected SRecordInstance() {
		createFields(getMeta().getFieldMetas());
	}

    /**
	 * Creates field instances when creating the record
	 *
	 */
	private void createFields(List<SFieldMeta> fieldList) {
		int nbFlds = getMeta().getFieldMetas().size();
		fieldValues = new Object[nbFlds];
		fieldOptimisticValues = new Object[nbFlds];
		
        Arrays.fill(fieldValues, INVALID_VALUE);
	}
	
//	void savepoint() {
//		spDataSet = sDataSet;
//		spNewRow = newRow;
//		spDeleted = deleted;
//		spDirtyRecordIndex = dirtyRecordsIndex;
//		spFieldValues = Arrays.copyOf(fieldValues, fieldValues.length);
//		spFieldOptimisticValues = Arrays.copyOf(fieldOptimisticValues, fieldOptimisticValues.length);
//	}
//	
//	void restoreSavepoint() {
//		if (hasSavepoint()) {
//			sDataSet = spDataSet;
//			newRow = spNewRow;
//			deleted = spDeleted;
//			dirtyRecordsIndex = spDirtyRecordIndex;
//			fieldValues = spFieldValues;
//			fieldOptimisticValues = spFieldOptimisticValues;
//			clearSavepoint();
//		}
//		else {
//    		throw new SException.Error("No savepoint for this record. Inconsistent call.");
//		}
//	}
//	
//	boolean hasSavepoint() {
//		return (spFieldValues != null);
//	}
//
//	void clearSavepoint() {
//		spDataSet = null;
//		//spNewRow = null; Not an object
//		spDirtyRecordIndex = -2;
//		spFieldValues = null;
//		spFieldOptimisticValues = null;
//	}
	
	/**
	 * Determines whehter two SRecordInstances are for the same SRecordMeta and
	 * have the same primary key fields.
	 * <p>
	 * 
	 * This is purely for the purpose of putting <code>SRecordInstances</code>
	 * in the <code>SSession.transactionCache</code>. This is done as
	 * <code>tc.put(SRecordInstance, SRecordInstance)</code>, ie the instance
	 * is used for both the key and the body. This avoids having to create a
	 * separate primary key object for each record instance.
	 * <p>
	 * 
	 * It only foreign keys, not referenced SRecordInstances which may be null
	 * if the parent record has not been retrieved.
	 * 
	 */
	@Override public boolean equals(Object key2) {
		if (this == key2) // even if key is null
			return true;
		if (!(key2 instanceof SRecordInstance))
			return false;

		SRecordInstance pkey2 = (SRecordInstance) key2;
		SRecordMeta<?> meta = getMeta();
		if (meta != pkey2.getMeta())
			return false;
		
//		 * Two records not belonging to the same SDataSet ar not equal, even if their fields are
//		 * all equal... Should also be reflected in hashCode
//		if ( ! pkey2.getDataSet().equals(getDataSet()))
//			return false;
		
		for (SFieldScalar fmeta : getMeta().getPrimaryKeys()) {
			Object k1 = this.getRawArrayValue(fmeta);
			Object k2 = pkey2.getRawArrayValue(fmeta);
            if (k1 == null || k2 == null) // If DataSet.createWinNullKey.
                return false; // even if both null.
			if (!k1.equals(k2))
				return false; // Can never be null
		}
		return true;
    }

	/** See <code>equals()</code>. */
	@Override public int hashCode() {
		// Note that SFieldMeta.rawSetFieldValue can change the hash code.
		// No real point in caching, only calculated once per object anyway when put in.
		int code = getMeta().getTableName().hashCode();
		for (SFieldScalar fmeta : getMeta().getPrimaryKeys()) {
			Object k1 = this.getRawArrayValue(fmeta);
			if (k1 == INVALID_VALUE) 
                throw new SException.InternalError("hashCode before primary key set");
			if (k1!=null) // createWithNullKey
                code = k1.hashCode() + code << 1;
            else
                code = System.identityHashCode(this); // Just to avoid collisions.
		}
		return code; // code % (1<<16 -1) only produces marginal improvement (10K --> 7K).
	}

    static private int hasMapsHash(int h) { // Copied from HashMap for testing purposes only.  It does a BAD job!
        // This function ensures that hashCodes that differ only by
        // constant multiples at each bit position have a bounded
        // number of collisions (approximately 8 at default load factor).
        h ^= (h >>> 20) ^ (h >>> 12);
        h = h ^ (h >>> 7) ^ (h >>> 4);
        return h & (1<<16 -1);
    }
    
	/**
	 * Returns the field's value as a Java Object in the type stored in the dataset. <P>
     * 
     * Methods such as
	 * <code>getString()</code> dispach to this method but are generally more
	 * convenient because the cast the result to the correct type. This method
	 * in turn just dispaches to <code>field.getFieldValue()</code>, ie. it
	 * is the declaredtype of the SField that determines how the value is
	 * retrieved.<P>
     * 
     * Also lazily fetches referenced records if field is a reference.  But findReference normally used instead. 
	 * Don't rely on null result, as it could mean field is null, or it has
	 * not been queried or set. Use isNull(field) instead. <p>
     * 
     * Enums are now stored as enums.<p>
     * 
	 */
	public Object getObject(SFieldMeta field) {
        checkFieldIsAccessible(field);
		return field.getRawFieldValue(this, SQueryMode.SFOR_UPDATE, SSelectMode.SNORMAL);
	}

    private void checkFieldIsAccessible(SFieldMeta field) throws Error {
        if (!isNotDestroyed()) {
            throw new SException.Error("Cannot access destroyed record " + this);
        }
        if (isDeleted()) {
            throw new SException.Error("Attempt to access Deleted record " + this);
        }
        if (field.getRecordMeta() != this.getMeta()) {
            throw new SException.Error("Field " + this + " is not in " + this.getMeta());
        }
    }


	/**
	 * Generic routine to set a fields value. Just dispaches to
	 * <code>field.setFieldValue(convertToField(value))</code>
	 * <p>
	 */
	public void setObject(SFieldMeta field, Object value) {
        setObject(field, value, true);
    }
	
    /** Enables value to be set with field validation suppressed, rarely used. */
    public void setObject(SFieldMeta field, Object value, boolean fieldValidate) {
		checkFieldIsAccessible(field);
		// throw exception if not updatable
		if (isReadOnly()) 
            throw new SException.Error("Record is ReadOnly " + this);

		// / Convert rawValue if necessary.
		Object convValue = null;
		try {
			convValue = field.convertToDataSetFieldType(value);
		} catch (Exception ex) {
			throw new SException.Data("Converting " + value + " for " + this + "." + field, ex)
                     .setFieldMeta(field).setInstance(this).setParams(value);
        }

		// / Set the field value if different.
		Object oldValue = null;
		if (isValid(field))
			oldValue = getRawArrayValue(field);
		if (        !isValid(field)
				|| (convValue == null && oldValue != null)
				|| (convValue != null && 
                       (oldValue == null || !convValue.equals(oldValue)))) {
            
            if (field instanceof SFieldScalar && ((SFieldScalar) field).isPrimary()) {
                if (isValid(field) && getRawArrayValue(field) != null) // else createWithNullKey
                        throw new SException.Error("Cannot change primary key field " 
                        + this + "." + field + " from " + getRawArrayValue(field));
            }
            
			if (field instanceof SFieldScalar) 
                // References only set record dirty if one of the underlying scalars is updated.
                // Merely retrieving a referenced record does not make the record dirty.
				// Do it before innetSetObject so that saavepoint saves the previous value
                this.setDirty(true);
			
  			innerSetObject(field, convValue);
            
			// / Now do any user validations. Do them now so validators can access new field value.
			try {
				if (fieldValidate) this.doValidateField(field, convValue); // oldValue not passed as not available for Record validation.
			}
			catch (SException.Validation e) {
				// if validation fails, reset field to its initial value
				innerSetObject(field, oldValue);
				throw e;
			}
		}
	}
    
	private void innerSetObject(SFieldMeta field, Object value) {
        if (getLogger().enableFields())
            getLogger().fields("Set " + this + "." + field + " = " + value);
        if (isReadOnly()) 
            throw new SException.Error("Attempt to set read only field " + this + "." + field + " to " + value);
        if (field instanceof SFieldScalar && ((SFieldScalar) field).isPrimary()) {
            if (sDataSet!=null && sDataSet.records.remove(this) != this) // we are changing the key.
              throw new SException.InternalError("Instance not in dataset " + this);
            // (If there are multiple keys this will just be done multiple times, redundant but fast and safe.)
            field.setRawFieldValue(this, value);
            if (sDataSet!=null) sDataSet.records.put(this, this); // we are changing the key.
        } else
            field.setRawFieldValue(this, value);
	}


    /** Was the field SQL NULL in the database?  
     * (eg. getInt would return 0 for this case.)
     * Does not need to do query for reference fields, just checks if any refing scalars are null.
     */
	public boolean isNull(SFieldMeta field) {
        checkFieldIsAccessible(field);
        return field.getRawFieldValue(this, SQueryMode.SREFERENCE_NO_QUERY, SSelectMode.SNORMAL) == null;
	}
		
	@Override
	public boolean isNull(String fieldName) {
		return isNull(getMeta().getField(fieldName));
	}

	@Override
	public void setNull(String fldName) {
		setNull(getMeta().getField(fldName));
	}
	
	public void setNull(SFieldMeta field) {
		setObject(field, null); // Reference scalars are nulled if ref set.
	}

	public void setNull(SFieldMeta field, boolean fieldValidate) {
		setObject(field, null, fieldValidate); // Reference scalars are nulled if ref set.
	}

	/**
	 * True if field is the empty value, ie. null or "" or spaces.
	 * @see #SMANDATORY
	 */
	public boolean isEmpty(SFieldMeta field) {
        Object obj = getObject(field);
        if (obj == null) return true;
        if (obj instanceof String) {
            String str = (String)obj;
            for (int sx=0; sx < str.length(); sx++)
                if (str.charAt(sx) != ' ') return false;
            return true;
        }
        return false;
	}

	/**
	 * Sets field to be empty, ie. currently <code>setObject(,
	 null)</code>.
	 * But other options will be added later.
	 * 
	 * @see #SMANDATORY
	 */
	public void setEmpty(SFieldMeta field) {
		setObject(field, null);
	}

	/**
	 * Gets the value of field cast to a String, trimed of trailing spaces. This
	 * is equivalent to <code>getObject().toString().trimTrailingSpaces()</code>.
	 * So the field type itself need not be <code>SFieldString</code>, but
	 * just something that can be cast to a String. Trailing spaces can be a
	 * problem with some databases and fileds declared <code>CHAR</code>.
	 * <p>
	 * 
	 * Note that if you do not want trailing spaces trimmed, then just call
	 * getObject().toString() manually. (For CHAR fields, most dbs/jdbc drivers
	 * seem to trim, but this is highly inconsistent.)
	 * <p> ## Trimming is an issue with CHAR style fields that pad with spaces.
	 * Currently we always read from database into the fields without trimming.
	 * The idea being to let the SimpleORM user get at the raw query result
	 * using getObject, whatever that raw result is.
	 * <p>
	 * 
	 * Most DBs seem to trim for us. But some may not, and some may require the
	 * trailing spaces on queries. Certainly trailing spaces in the objects will
	 * upset the record cache, and there is some dubious code in
	 * SRecordFinder.retrieveRecord() to handle this.
	 * <p>
	 * 
	 * I think that for CHAR fields we need to always trim at database read, and
	 * pad where needed. This should also be dispatched to DB handler. I think
	 * that Oracle gives grief. (Note that trim means trailing spaces, leading
	 * should be left alone.)
	 * <p>
	 * 
	 * I have not done this because it would require testing on many datbases.
	 * <p>
	 */
	public String getString(SFieldMeta field) {
		return convertToString(getObject(field));
	}

    /** Sets the value of the field from a string, casting if necessary.
     * So this can be used to set an Integer field, say.
     */
	public void setString(SFieldMeta field, String value) {
		setObject(field, value);
	}

	/**
	 * Casts getObject() to int iff a Number, see getString(). Returns 0 if
	 * null, following JDBC.
	 */
	public int getInt(SFieldMeta field) {
		Object val = this.getObject(field);
		try {
			return convertToInt(val);
		} catch (Exception ex) {
			throw new SException.Data("Could not convert " + field + " to int " + val, ex)
												.setFieldMeta(field).setRecordInstance(this).setParams(val);
		}
	}

	public void setInt(SFieldMeta field, int value) {
		setObject(field, new Integer(value));
	}

	/**
	 * Casts getObject() to long iff a Number, see getString(). Returns 0 if
	 * null, following JDBC. Note that longs may not be accurately supported by
	 * the database -- see SFieldLong.
	 */
	public long getLong(SFieldMeta field) {
		Object val = this.getObject(field);
		try {
			return convertToLong(val);
		} catch (Exception ex) {
			throw new SException.Data("Could not convert " + field + " to long " + val, ex)
												.setFieldMeta(field).setRecordInstance(this).setParams(val);
		}
	}

	public void setLong(SFieldMeta field, long value) {
		setObject(field, new Long(value));
	}

	/**
	 * Casts getObject() to double iff a Number, see getString(). Returns 0 if
	 * null, following JDBC.
	 */
	public double getDouble(SFieldMeta field) {
		Object val = getObject(field);
		try {
			return convertToDouble(val);
		} catch (Exception ex) {
            throw new SException.Data("Could not convert " + field + " to double " + val, ex)
            								    .setFieldMeta(field).setRecordInstance(this).setParams(val);
		}
	}

	public void setDouble(SFieldMeta field, double value) {
		setObject(field, new Double(value));
	}

	/**
	 * Returns false if null.
	 */
	public boolean getBoolean(SFieldMeta field) {
		return getBoolean(field.getFieldName());
	}
    
    
	public void setBoolean(SFieldMeta field, boolean value) {
		setObject(field, value ? Boolean.TRUE : Boolean.FALSE);
	}

	/** etc. for Timestamp. */
	public java.sql.Timestamp getTimestamp(SFieldMeta field) {
		Object val = getObject(field);
		try {
			return convertToTimestamp(val);
		}
		catch (ClassCastException e) {
			throw new SException.Data(val + " cannot be converted to TimeStamp.", e)
		    									.setFieldMeta(field).setRecordInstance(this).setParams(val);
		}
	}

	/**
	 * Note that value should normally be a java.sql.Timestamp (a subclass of
	 * java.util.Date). However, if it is a java.util.Date instead, then it will
	 * replaced by a new java.sql.Timestamp object before being set. This is
	 * convenient for people using java.util.Date as their main date type.
	 */
	public void setTimestamp(SFieldMeta field, java.util.Date value) {
		setObject(field, value);
	}

	/** etc. for Date. 
     ### Should convert strings to Date, ISO etc.  Likewise other types.
     (Painful to do in Java!)
     */
	public java.sql.Date getDate(SFieldMeta field) {
		Object raw = getObject(field);
		try {
			return convertToDate(raw);
		}
		catch (ClassCastException e) {
			throw new SException.Data(raw + " cannot be converted to Date.", e)
												.setFieldMeta(field).setRecordInstance(this).setParams(raw);
		}
	}

	/** See {@link #setTimestamp} for discussion of Date parameter. */
	public void setDate(SFieldMeta field, java.util.Date value) {
		setObject(field, value);
	}

	/** etc. for Time. */
	public java.sql.Time getTime(SFieldMeta field) {
		Object raw = getObject(field);
		try {
			return convertToTime(raw);
		}
		catch (ClassCastException e) {
				throw new SException.Data(raw + " cannot be converted to Time.", e)
													.setFieldMeta(field).setRecordInstance(this).setParams(raw);
		}
	}

	/** See {@link #setTimestamp} for discussion of Date parameter. */
	public void setTime(SFieldMeta field, java.util.Date value) {
		setObject(field, value);
	}

	/** etc. for BigDecimal. */
	public BigDecimal getBigDecimal(SFieldMeta field) {
		Object value = this.getObject(field);
		try {
			return convertToBigDecimal(value);
		}
		catch (NumberFormatException e) {
			throw new SException.Data(value + " cannot be converted to BigDecimal.")
												.setFieldMeta(field).setRecordInstance(this).setParams(value);
		}
	}
	
	public void setBigDecimal(SFieldMeta field, BigDecimal value) {
		setObject(field, value);
	}

	/**
	 *  getEnum has no meaning for SRecordGeneric. Keep it here.
	 * @param <T>
	 * @param field
	 * @return
	 */
    @SuppressWarnings("unchecked")
	public <T extends Enum<T>> T getEnum(SFieldEnum<T> field) {
        return (T)getObject(field);
    }
     public <T extends Enum<T>> void setEnum(SFieldEnum<T> field, Enum<T> value) {
        setObject(field, value);
    }

	/** Casts getObject() to byte[]. */
	public byte[] getBytes(SFieldMeta field) {
		return getBytes(field.getFieldName());
	}

	public void setBytes(SFieldMeta field, byte[] value) {
		setObject(field, value);
	}


	/**
	 * Gets a record referenced by <code>this.field</code>. 
     * Does a lazy lookup on the SDataSet.getSession()  if necessary, and if attached to a session.<p>
     * 
     * If the field is null but all the primary key fields are non
	 * null then does a <code>dataset.find()</code> and if necessary a <code>SSessionI.find()</code>
     * to provide a referenced record. <p>
     * 
     * If the dataset is not attached to a session, will return Boolean.FALSE, as iwe have no way to know
     * if the reference exists in the database or not (well, it should really).
     * 
     * If the dataset is attached to a session, the foreign keys are not null, but the reference cannot be found,
     * this method throw a SException.Data, as the foreign is broken...
     * 
     * @See #getReferenceNoQuery to just get the reference without accessing either dataset or database.
	 */
	@SuppressWarnings("unchecked")
	public <T extends SRecordInstance> T findReference(SFieldReference<T> field, SQueryMode queryMode, SSelectMode selectMode) {
        checkFieldIsAccessible(field);
		return (T) field.getRawFieldValue(this, queryMode, selectMode);
	}
	
	public <T extends SRecordInstance> T findReference(SFieldReference<T> field, SSelectMode selectMode) {
		return findReference(field, SQueryMode.SFOR_UPDATE, selectMode);
	}
	
	public <T extends SRecordInstance> T findReference(SFieldReference<T> field, SQueryMode queryMode) {
		return findReference(field, queryMode, SSelectMode.SNORMAL);
	}
	
    public <T extends SRecordInstance> T findReference(SFieldReference<T> field) {
		return findReference(field, SQueryMode.SFOR_UPDATE, SSelectMode.SNORMAL);
	}
        
	public void setReference(SFieldReference<?> field, SRecordInstance value) {
		setObject(field, value);
	}

	/**
	 * Get a reference, but do not query either the dataset or the database. 
     * Returns null if a corresponding scalar field is null.
     * Returns Boolean.FALSE if it is not null, but has not been queried from the database.
     */
	public Object getReferenceNoQuery(SFieldReference<?> field) {
        return field.getRawFieldValue(this, SQueryMode.SREFERENCE_NO_QUERY, null);
	}

	/**
	 * Sets this record to be dirty so that it will be updated in the database.
	 * Normally done implicitly by setting a specific column. 
	 * 
	 * NOP - But may
	 * occasionally be useful after a findOrInsert() to add a record that
	 * contains nothing appart from its primary key. ===> isNewRow is here for that
     * @See SDataSet#makeFlushedDirty which also makes dirty.
	 */
	public void setDirty(boolean val) {
        if (val == isDirty()) return;
		if (val) {
			if (readOnly)
				throw new SException.Error("Record retrieved read only " + this);
//			dirty = true;
			if (sDataSet != null) { // && updateListIndex == -1) {  // Instances not attached to any Data Set.
//				if (sDataSet.hasSavepoint())
//					this.savepoint();
				sDataSet.putInDirtyList(this);
			}
			
		} else {
			if (sDataSet != null && dirtyRecordsIndex != -1) {
				sDataSet.removeFromDirtyList(dirtyRecordsIndex, this);
                // Sets null.  Need to do this so that update order OK for findReference etc.
			}
//			dirty = false;
			dirtyRecordsIndex = -1;
		}
	}

	/**
	 * True iff this record is dirty but not yet flushed to the database. May be
	 * both dirty and unattached.
	 * <p>
	 * 
	 * A record is not dirty if a record has been flushed to the database but
	 * the transaction not committed.
	 * <p> ## Should add <tt>wasEverDirty</tt> method for both record and
	 * fields for validation tests.
	 */
	public boolean isDirty() {
		return dirtyRecordsIndex >= 0;
            //dirty;
	}

	/** Tests whether just field is dirty. */
	public boolean isDirty(SFieldMeta field) {
        return isValid(field) && fieldOptimisticValues[field.index] != fieldValues[field.index]; // eq or equal?
        // Should ideally have test that simply retrieved references are not dirty.
	}

	/**
	 * Was in the cache before the most recent findOrCreate. (Will always been
	 * in the cache after a findOrCreate.) Used to prevent two create()s for the
	 * same key. Also for unit tests.
	 */
	public boolean wasInCache() {
		return wasInCache;
	}

	/**
	 * Sets a flag to delete this record when the transaction is commited. The
	 * record is not removed from the transaction cache. Any future
	 * <code>findOrCreate</code> will thus return the deleted record but its
	 * fields may not be referenced. (<code>isDeleted</code> can be used to
	 * determine that the record has been deleted.)
	 * <p>
	 * 
	 * The record is only deleted from the database when the transaction is
	 * committed or is flushed. Thus a transaction that nulls out references to
	 * a parent record and then deletes the parent record will not cause a
	 * referential integrity violation because the update order is preserved so
	 * that the updates will (normally) be performed before the deletes.
	 * <p>
	 * 
	 * Note that for Optimistic locks only those fields that were retrieved are
	 * checked for locks.
	 */
	public void deleteRecord() {
		if (! isNotDestroyed() || deleted)
			throw new SException.Error("Cannot delete destroyed record " + this);
		setDirty(true);
		deleted = true;
	}

	public boolean isDeleted() {
		return deleted;
	}

	/**
	 * Often called after {SRecordMeta#findOrCreate} to determine whether a row
	 * was retrieved from the database (not a new row) or whether this record
	 * represents a new row that will be inserted when it is flushed.
	 */
	public boolean isNewRow() {
		return newRow;
	}
	public void setNewRow(boolean val) {
		newRow = val;
	}

	/**
	 * Throws an excpetion if !{@link #isNewRow}. Handy, use often in your
	 * code to trap nasty errors.
	 */
	public void assertNewRow() {
		if (!isNewRow())
			throw new SException.Error("Not a new row " + this);
	}

	/** @see #assertNewRow */
	public void assertNotNewRow() {
		if (isNewRow())
			throw new SException.Error("Is a new row " + this);
	}

	/**
	 * True if the record has valid data, ie. it has not been destroyed. (This
	 */
	// TODO how should this be implemented ?
	public boolean isNotDestroyed() {
		return fieldValues != null;
	}

	/**
	 * True if the field has been queried as part of the current transaction and
	 * so a get is valid. Use this to guard validations if partial column
	 * queries are performed. See isDirty.
	 */
	public boolean isValid(SFieldMeta field) {
        return ! (fieldValues[field.index] instanceof InvalidValue);
	}

	/**
	 * Destroys this instance so that it can no longer be used. Also nulls out
	 * variables so to reduce risk of memory leaks. Note that it does not remove
	 * the record from the transaction cache and update list -- it cannot be
	 * called on its own.
	 */
	void destroy() {
		dirtyRecordsIndex = -2;
        fieldValues = null;
        fieldOptimisticValues=null;
		sDataSet = null;
//		clearSavepoint();
	}


	/**
	 * True if this instance is attached to the current begun transaction.
	 * Exception if is attached but not to the current transaction or the
	 * current transaction has not begun.
     * #### not right.
	 */
	public boolean isAttached() {
		return sDataSet!=null && sDataSet.isAttached();
	}


	/**
	 * toString just shows the Key field(s). It is meant to be consise, often
	 * used as part of longer messages.
	 */
	@Override public String toString() {
		return toStringDefault();
	}

	/**
	 * Default behavior of toString(). This was split out from the toString()
	 * method to avoid infinite recursion if a subclass of SRecordInstance
	 * overrode toString() to use any of the get...() methods.
	 */
	String toStringDefault() {
		StringBuffer ret = new StringBuffer("[" + SUte.cleanClass(getClass())
				+ " ");
		if ( ! isNotDestroyed())
			ret.append("[Destroyed SRecordInstance]");
		else {
			boolean first = true;
			for (SFieldScalar fld : getMeta().getPrimaryKeys()) {
				if (!first)
					ret.append(", ");
				first = false;
				if (isValid(fld))
					ret.append(getRawArrayValue(fld));
			}
		}
		if (newRow)
			ret.append(" NewRecord");
		if (deleted)
			ret.append(" Deleted");
		if (isDirty())
			ret.append(" Dirty"+dirtyRecordsIndex);
		ret.append("]");
		return ret.toString();
	}

	/** For debugging like toString(), but shows all the fields. */
	public String allFields() {
		StringBuffer ret = new StringBuffer("[" + SUte.cleanClass(getClass())
				+ " ");
		if ( ! isNotDestroyed())
			ret.append("[Destroyed SRecordInstance]");
		else {
			int px = 0;
			for (SFieldMeta fmeta : getMeta().getFieldMetas()) {
				if (px++ > 0)
					ret.append("| ");
				if (isValid(fmeta))
					ret.append(getRawArrayValue(fmeta));
				else ret.append("UNQUERIED");
			}
		}
		if (newRow)
			ret.append(" NewRecord");
		if (deleted)
			ret.append(" Deleted");
		if (isDirty())
			ret.append(" Dirty");
		ret.append("]");
		return ret.toString();
	}

	
	/**
	 * Exception thrown due to broken optimistic locks. This one may be worth
	 * trapping by the application so gets its own class.
	 */
	public static class BrokenOptimisticLockException extends SException {
		static final long serialVersionUID = 20083L;

		SRecordInstance instance;

		public BrokenOptimisticLockException(SRecordInstance instance) {
			super("Broken Optimistic Lock " + instance, null);
			this.instance = instance;
		}

		/** The record that had the broken optimistic lock. */
		@Override public SRecordInstance getRecordInstance() {
			return instance;
		}
	}
	
	void setPrimaryKeys(Object[] pkeys) {
		// / If keys is a single key then make it an singleton array.
		int passedKeysIndex = 0;
		for (SFieldScalar keyf : getMeta().getPrimaryKeys()) {
			if (pkeys.length < passedKeysIndex + 1)
				throw new SException.Data("Too few key params "	+ SUte.arrayToString(pkeys));

			// Object rawValue = npkeys != null ? npkeys[kx] : pkey;
			Object rawValue = pkeys[passedKeysIndex];
			if (rawValue == null)
				throw new SException.Data("Null Primary key " + keyf + " "+ passedKeysIndex);

			// / Convert rawValue if necessary.
			Object convValue = null;
			try {
				convValue = keyf.convertToDataSetFieldType(rawValue);
			}
			catch (Exception ex) {
				throw new SException.Data("Converting " + rawValue+ " for " + this + "." + keyf, ex)
                    .setFieldMeta(keyf).setInstance(this).setParams(rawValue);
			}

            if (getLogger().enableFields())
                getLogger().fields("Set " + this + "." + keyf + " = " + convValue);

			// For references this will also copy the foreign key values.
            keyf.setRawFieldValue(this, convValue);

            passedKeysIndex++;
		}
		if (passedKeysIndex != pkeys.length)
			throw new SException.Error("Too many key params "
					+ (passedKeysIndex + 1) + " < " + SUte.arrayToString(pkeys)+ ".length");
	}
    
    /** For createWithNullKey */
	void nullPrimaryKeys() {
		// / If keys is a single key then make it an singleton array.
		for (SFieldScalar keyf : getMeta().getPrimaryKeys()) {
            if (getRawArrayValue(keyf) != INVALID_VALUE)
                throw new SException.InternalError("Nulling non new key");
			setRawArrayValue(keyf, null);
		}
	}
	
	/**
	 * Will only happen on creation of new records. Sets the initial values on field that
	 * have one, if the fields have not been set to null yet (by a createWithNullKeys).
	 * Does not make the record itself dirty, but create already does.
	 * Does make the fields that have initial values dirty.
	 */
	void setInitialValues() {
		for (SFieldScalar fld : getMeta().getAllScalarFields()) {
			// We don't make the record dirty, but create will do it for us.
			// But setting the raw value to initialValue will make the field dirty... (see isDirty(field)).
			// If the field has laready been set, possibly to null by a createWithNullKeys, don't set it to initial value.
			if (fld.getInitialValue() != null && getRawArrayValue(fld) == INVALID_VALUE) {
				this.setRawArrayValue(fld, fld.getInitialValue());
			}
		}
	}
	

	void doValidateField(SFieldMeta field, Object newValue) {
		field.doValidate(this);
        try {
            onValidateField(field, newValue);
        } catch (SException.Validation ve) {
            ve.setFieldMeta(field);
            ve.setRecordInstance(this);
            ve.setFieldValue(newValue);
        }
	}
    
    /**
     * Called after individual field validators each time a field's value is set,
	 * (now) including keys.  Not called as values are retrieved from database.<p>
     * 
	 * Throw an SException.Validation if not OK. The value is not assigned, and
	 * the transaction can continue.  The exception will be augmented with 
	 * <p>
	 * 
	 * This is called after the value has been converted to its proper type, eg.
	 * from a String to a Double and the field value has been updated. 
	 * <p>
     * If the validation fails, the field is automatically set back to its oldValue.<p>
     * 
	 * This is called for key values as well. This is only for newly created
	 * records but is during the findOrCreate -- ie it is called even if the
	 * record is never made dirty and thus saved.<p>
     *  
	 *@See #onQueryRecord
	 */
    protected void onValidateField(SFieldMeta field, Object newValue){}


	public void doValidateRecord() {
		// Don't validate is deleted
		if (isDeleted()) return;
		// else
		List<SFieldMeta> fields = getMeta().getFieldMetas();
		for (SFieldMeta field : fields) {
       		if (isValid(field))
  			  field.doValidate(this);
		}
        try {
            onValidateRecord();
        } catch (SException.Validation vre) {
            vre.setInstance(this);
            throw vre;
        }
	}

    /** Override this to validate records before flush. 
	 * Throw an SValidationException if not OK.<p>
	 * 
	 * This is called just before a record would be flushed. Only dirty records
	 * are validated. If the validation fails then the record is not flushed.
	 * (It may also be called directly by the application to validate the record
	 * before it is flushed.)
	 * <p>
	 * 
	 * Use this routine when a record may be in a temporarily invalid state, but
	 * which must be corrected before flushing. This is common when there is a
	 * more complex relationship between different fields that cannot be
	 * validated until all the fields have been assigned values.<p>
	 */
   	protected void onValidateRecord() {}

    /** Override this to get field values just after a record is actually retrieved, by findOrCreate or query. */
    protected void onQueryRecord() {}    
    public void doQueryRecord() {onQueryRecord();}
    
	public void validatePrimaryKeys() {
		Object value = null;
		for (SFieldScalar keyf : getMeta().getPrimaryKeys()) {
			//if ( ! keyf.isForeignKey()) { // TODO why this test ?
				value = getObject(keyf);
				doValidateField(keyf, value);
			//}
		}
	}
		
	// Filed instance methods
	
	/**
	 * Raw setter for field value. Internal to Simpleorm.
     * Assigns directly into the array of values.  No validation or conversion.
	 * @param fmeta the field to set
	 * @param value the new value of the field, as is, can be null
	 */
	public void setRawArrayValue(SFieldMeta fmeta, Object value) {
		fieldValues[fmeta.index] = value;
	}
	/**
	 * Raw accessor for field value. Internal to Simpleorm.
     * Gets directly from the array of values.  No findReference.  No Valid test.
	 * @param fmeta the field to get
	 * @param value the raw value of the field, as is, can be null
	 */
	public Object getRawArrayValue(SFieldMeta fmeta) {
		return fieldValues[fmeta.index];
	}
    	
    /** The value that was read from (or flushed to) the database.
     * Used for optimistic locking, and also just to tell how fields change.
     */
    public Object getInitialValue(SFieldMeta fieldMeta) {
		return fieldOptimisticValues[fieldMeta.index];
	}
   	
    /**
     * Defines the current value of the field to be the initial value, which is 
     * then used for optimistic locks etc.
     * Called just after a record is read or updated.
	 * <p>
	 */
	public void defineInitialValue(SFieldMeta field) {
    	// Already done, maybe findOrCreating the same record twice
		fieldOptimisticValues[field.index] = fieldValues[field.index];
	}

    public SLog getLogger() {
        if (getDataSet() != null)
           return getDataSet().getLogger();
        else
           return SLog.getSessionlessLogger();
    }

    //////////// ACCESSORS ////////////
    public boolean isReadOnly() {
		return readOnly;
	}
	public void setReadOnly(boolean val){
		readOnly = val;
	}
	public SDataSet getDataSet() {
		return sDataSet;
	}
	
	// Internal state - package visible only, used by SDataSet
	//
	
	void setDataSet(SDataSet ds) {
		sDataSet = ds;
		//sDataSet.putInCache(this);
	}

    void setWasInCache(boolean val) {
		wasInCache = val;
	}

   
    // *******************************************************************************************
    //
    // Implement Map
    //
    // Beware, the Map view of a SRecordInstance is not very efficient, and you should consider
    // using "native" accesors when possible. 
    //
    // Implementing Map in SRecordInstance is not as easy as it would seem, since
    // the semantics are quite different: SRecordInstance has a predefined number of fields,
    // which can be already set or not. In a Map, either the key has a value, and then it exists, or it
    // has no value and it doesn't exist.
    // So the choice was made to consider unquieried/unset fields not  existing in the Map
    // implementation, especially regarding the size(), containsKey(), isEmpty and values() methods.
    // Please note that clear() is an UnsupportedOperation (has pk cannot be cleared)
    // Please note that remove() is an UnsupportedOperation
    //
    // Also notice that values() return read-only data, although the Map API assumes
    // that they are directly backed by the Map and that changes on one side should be reflected
    // on the other side, which is at least uneasy.
    //
    @Override public void clear() {
		throw new UnsupportedOperationException();
	}
    
    /** Has isValid field key.  key must be String. */
	@Override public boolean containsKey(Object key) {
		if (! (key instanceof String))
			throw new SException.Error("Not a String " + key);
		SFieldMeta fld = getMeta().getField((String)key);
		if (fld != null)
			return isValid(fld);
		else
			return false;
	}

	@Override public boolean containsValue(Object value) {
		return Arrays.asList(fieldValues).contains(value);
	}


		@Override public Set<Map.Entry<String, Object>> entrySet() {
			return new AbstractSet<Map.Entry<String, Object>>() {
				@Override public Iterator<Map.Entry<String, Object>> iterator() {
					Set<Map.Entry<String, Object>> entries = new HashSet<Map.Entry<String, Object>>(fieldValues.length);
					for (String fldName : getMeta().getFieldNames()) {
						SFieldMeta fld = getMeta().getField(fldName);
						if (isValid(fld))
							entries.add(new SREntry(fld));
					}
					return entries.iterator();
				}
				@Override public int size() {
					return SRecordInstance.this.size();
				}
				@Override public boolean contains(Object o) {
					return SRecordInstance.this.containsValue(o);
				}
				@Override public void clear() {
					SRecordInstance.this.clear();
				}
			};			
		}
		// Inner class to implement entrySet()...
		class SREntry implements Map.Entry<String, Object> {
			private SFieldMeta fld;
			public SREntry(SFieldMeta fld) {
				this.fld = fld;
			}
			@Override public Object setValue(Object val) {
				return SRecordInstance.this.put(fld.getFieldName(), val);
			}
			@Override public Object getValue() {
				return SRecordInstance.this.get(fld);
			}
			@Override public String getKey() {
				return fld.getFieldName();
			}
			@Override public String toString() {
				return "{SREntry " + SRecordInstance.this + fld + "}"; 
			}
		}
		
	/*
	 * Key must be a String  
	 * Looks up the field, returns null if no such field (ie. not fail fast like getObject).
	 * @see java.util.Map#get(java.lang.Object)
	 */
	@Override public Object get(Object key) {
		if (! (key instanceof String))
			throw new SException.Error("Not a String " + key);
		SFieldMeta fld = getMeta().getField((String)key);
		if (fld != null)
			return getObject(fld);
		else
			return null;
	}

	/*
	 * isEmpty will only return true if the record has no field set, which can only happen when
	 * using createWithNullKeys and no field has been set...
	 * Even then, pk will be set soon.
	 * (non-Javadoc)
	 * @see java.util.Map#isEmpty()
	 */
	@Override public boolean isEmpty() {
		return size() == 0;
	}

	@Override public AbstractSet<String> keySet() {
		return new AbstractSet<String>() {
			@Override public Iterator<String> iterator() {
				Set<String> keys = new HashSet<String>(fieldValues.length);
				for (String fldName : getMeta().getFieldNames()) {
					if (isValid(getMeta().getField(fldName))) {
						keys.add(fldName);
					}
				}
				return keys.iterator();
			}
			@Override public int size() {
				return SRecordInstance.this.size();
			}
			@Override public boolean contains(Object o) {
				return SRecordInstance.this.containsKey(o);
			}
			@Override public void clear() {
				SRecordInstance.this.clear();
			}
		};			
	}


	@Override public Object put(String key, Object value) {
		// Only accept values for existing fields
		// nb: maybe later we could allow to store transient values this way, but not now
		if ( ! getMeta().getFieldNames().contains(key)) 
				throw new SException.Error("No field named " + key);
		SFieldMeta fld = getMeta().getField(key);
		Object previousValue = getObject(fld);
		setObject(fld, value);
		return previousValue;
	}

	@Override public void putAll(Map<? extends String, ? extends Object> map) {
		for (Map.Entry<? extends String, ? extends Object> entry : map.entrySet()) {
			put(entry.getKey(), entry.getValue());
		}
	}

	/*
	 * Use setNull instead.
	 * (non-Javadoc)
	 * @see java.util.Map#remove(java.lang.Object)
	 */
	@Override public Object remove(Object key) {
		throw new UnsupportedOperationException();
	}

	/*
	 * Really return the number of fields that are isValid (ie. set or queried).
	 * Mayb be less than the total number of fields in SRecordMeta.
	 * @see java.util.Map#size()
	 */
	@Override public int size() {
		int size=0;
		for (String fldName : getMeta().getFieldNames())
			if (isValid(getMeta().getField(fldName))) size++;
		return size;
	}

	/*
	 * InvalidValues (unquieried or unset fields) are considered as not existing in the Map
	 * (non-Javadoc)
	 * @see java.util.Map#values()
	 */
	@Override public Collection<Object> values() {
		return new AbstractCollection<Object>() {
	        @Override public Iterator iterator() {
	    		ArrayList<Object> vals = new ArrayList<Object>(Arrays.asList(fieldValues));
	    		Iterator<Object> it = vals.iterator();
	    		while (it.hasNext()) {
	    			Object o = it.next();
	    			if (o instanceof InvalidValue)
	    				it.remove();
	    		}
	    		return vals.iterator();
	        }
	        @Override public int size() {
	            return SRecordInstance.this.size();
	        }
	        @Override public boolean contains(Object o) {
	            return SRecordInstance.this.containsValue(o);
	        }
	        @Override public void clear() {
	        	SRecordInstance.this.clear();
	        }
		};
	}
}