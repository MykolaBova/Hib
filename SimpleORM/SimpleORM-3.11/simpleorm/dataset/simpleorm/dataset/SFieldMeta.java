package simpleorm.dataset;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;

import java.util.Map;
import simpleorm.dataset.validation.SValidatorI;
import simpleorm.utils.SException;
import simpleorm.utils.SUte;

/**
 * Each instance defines the meta data for a field in an {@link SRecordMeta}.
 * Subclasses are used for specific data types, with {@link SFieldObject} being
 * the most generic. Like JDBC, type conversions are made automatically.
 * <p>
 * 
 * Internally, the types are stored accurately. Ie. the
 * <code>SRecordInstance.getObject</code> objects are the exact types as
 * declared (String, Integer, Employee etc.) However, generous automatic
 * conversions are performed both when accessing these from the application and
 * when getting and setting columns in the database.
 * <p>
 * 
 */

public abstract class SFieldMeta // implements Serializable { // See SRecordInstance.getMeta()
{


	/** The record that this field belongs to. */
	private SRecordMeta<?> sRecordMeta;

    final String fieldName;	
    	
	int index = -1;

    private int hashCode = 0;
	
	private EnumSet<SFieldFlags> flags = EnumSet.noneOf(SFieldFlags.class);
		
    public boolean quoteName = false;
        
	/** Can only be read, not set. Mainly SCOLUMN_NAME */
	private boolean readOnly = false;

	/** A list of SFieldValidator */
	private List<SValidatorI> validators = new ArrayList<SValidatorI>();
	
	/**
	 * The last key value generated for this <code>SGENERATED_KEY</code> field
	 * in this JVM. Used by the default driver as a hack to minimize collisions
	 * with the SELECT MAX method. Synchronize all access.
	 */
	private transient long lastGeneratedKeyValue = 0;
    
    Map userProperties = new LinkedHashMap(3);

	protected EnumSet<SFieldFlags> getFlags() {
		return flags;
	}   
    
	/**
	 * Creates a new field for <code>sRecord</code> corresponding to
	 * <code>columnName</code>. <code>pvalues</code> are an arbitrary list
	 * of properties that can be associated with this field.
	 * <code>fieldName</code> is only used for the fieldMap and defaults to
	 * the <code>columnName</code> or prefix for references.
	 */
	SFieldMeta(SRecordMeta<?> sRecord, String fieldName, SFieldFlags... flags) {
		
		this.sRecordMeta = sRecord;
		this.fieldName = fieldName;
		if (flags.length > 0) {
			this.flags = EnumSet.copyOf(Arrays.asList(flags));
		}
		sRecordMeta.addField(this); // Will throw an exception on duplicate field name
	}
    

	public boolean isUnqueried() {
		return flags.contains(SFieldFlags.SUNQUERIED);
	}
	
	public boolean isDescriptive() {
		return flags.contains(SFieldFlags.SDESCRIPTIVE);
	}
	
     /** Causes the generated sql to quote ("") the column name.
     * Makes it case sensitive, allows reserved words and odd characters.
     * Beware that Postgresql (only) defaults to lower case, not upper case.
     */
	public <T extends SFieldMeta> T setQuoted(boolean quoted) {
		this.quoteName = quoted;
		return (T) this;
	}

	abstract Object getRawFieldValue(SRecordInstance finst, SQueryMode queryMode, SSelectMode selectMode);
        
    /**
	 * Specialized for References. 
	 */
    abstract void setRawFieldValue(SRecordInstance instance, Object value);

	/**
	 * Issues a JDBC get*() on the result set for the field and converts the
	 * database type to the appropriate internal type, eg, Double for a double
	 * field. The first column has sqlIndex==1.
	 */
	public abstract Object queryFieldValue(ResultSet rs, int sqlIndex) throws Exception;

	/**
	 * Converts the parameter from the raw type parameter to the correct internal Object
	 * type that is stored in the data set. Returns the object if no conversion necessary. Used
	 * by <code>SRecordInstance.setObject</code> etc., Not getObject.
	 */
	abstract protected  Object convertToDataSetFieldType(Object raw) throws Exception;

	/**
	 * Places a value in a prepared statement in the database representation
	 * used during SRecordInstance.flush. Can convert between internal values
	 * and database values, eg. TRUE to "Y".
	 * 
	 * (This does NOT need to handle NULL values (those are handled seperately
	 * by SRecordInstance))
	 */
	public void writeFieldValue(PreparedStatement ps, int sqlIndex, Object value) {
		try {
			ps.setObject(sqlIndex, writeFieldValue(value));
		} catch (Exception ex) {
			throw new SException.Jdbc(ex);
		}
	}

	/**
	 * Converts a single value from internal representation to database
	 * representation. Used primarily by the writeFieldValue above, but also
	 * used for converting optimistic lock values in SRecordInstance.flush.
	 * <p>
	 * 
	 * Overidden by SFieldBoolean. (not by SFieldString).
	 * 
	 * NOTE: This does NOT need to handle NULL values (those are handled
	 * seperately by SRecordInstance)
	 */
	public Object writeFieldValue(Object value) {
		return value;
	}

	/** Lists the record and column name only. Useful in traces. */
	public String toString() {
		return "[F " + SUte.cleanClass(sRecordMeta.getUserClass()) + "."
				+ getFieldName() + "]";
	}

	/** Lists all the details of the field. */
	public abstract String toLongerString();

	protected boolean isForeignKey() {
		return false;
	}

	/**
     * This is used to fudge generation of key values for in databases that do not properly
	 * suport them. It is rough and fails if there are multiple JVMs, or the
	 * user switches schemas in Oracle etc.
	 */
	public synchronized long nextGeneratedValue(long minimum) {
		if (lastGeneratedKeyValue < minimum)
			lastGeneratedKeyValue =minimum;
		else
			lastGeneratedKeyValue += 1;
		return lastGeneratedKeyValue;
	}

//	/**
//	 * Reflected into during de-serialization to return any pre-existing SFieldMeta object
//	 */
//	protected Object readResolve() throws ObjectStreamException {
//		Object substituted = sRecordMeta.getField(this.getFieldName());//getFieldMetas().get(getFieldIndex());
//		return substituted;
//	}

	public String getFieldName() {
		return fieldName;
	}

	@Override
	public boolean equals(Object that) {
		if (this == that) {
			return true;
		}
		if (!(that instanceof SFieldMeta))
			return false;
        SFieldMeta thatf = (SFieldMeta)that;
        if (!getFieldName().equals(thatf.getFieldName())) return false;
        if (getRecordMeta() != thatf.getRecordMeta()) return false;
        return true;
	}
    
	@Override
	public int hashCode() {
		if ( hashCode == 0) {
			hashCode = getFieldName().hashCode();
            hashCode += getRecordMeta().getTableName().hashCode() << 1;
            if (hashCode == 0) hashCode = 1;
		}
		return this.hashCode;
	}

	/**
	 * @return the sRecordMeta
	 */
	public SRecordMeta getRecordMeta() {
		return sRecordMeta;
	}

	public boolean isMandatory() {
		return false;
	}
	
	/**
	 * @param readOnly
	 *            the readOnly to set
	 */
	void setReadOnly(boolean isReadOnly) {
		this.readOnly = isReadOnly;
	}

	/**
	 * @return the readOnly
	 */
	boolean isReadOnly() {
		return readOnly;
	}

	
	/**
	 * Add a custom FieldValidator. Validator corresponding to
	 * data constaints (type, length, not null, ...) are added automatically.
	 */
	 public <T extends SFieldMeta> T addValidator(SValidatorI val) {
		 validators.add(val);
		 return (T) this;
	 }
	 
	 /**
	 * Add a FieldFlag as an afterthought. Allows conditional
	 * flags in a constructor.
	 */
	 public <T extends SFieldMeta> T addFlag(SFieldFlags flag) {
		 flags.add(flag);
		 return (T) this;
	 }
	 
	 /**
	 * Get the list of validators that are bound to this field.
	 * Can be useful for UI to know allowed values, etc. 
	 */
	 public List<SValidatorI> getValidators() {
		 return validators;
	 }
	 
     /** Called when individual field changed, or when record updated.
      * (Default behaviour is to call all registered validators.)
      * @see SRecordInstance#onValidateField
      */
	 protected void doValidate(SRecordInstance instance) {	 
		 for (SValidatorI val : validators) {             
             try {
    			 val.onValidate(this, instance);
             } catch (SException.Validation ve){
                 ve.setFieldMeta(this);
                 ve.setInstance(instance);
                 ve.setValidator((Class<SValidatorI>)val.getClass());
                 ve.setFieldValue(instance.getObject(this));
                 throw ve;
             }                 
		 }
	 }
     
     public <T extends SFieldMeta> T putUserProperty(Object key, Object value) {
         userProperties.put(key, value);
         return (T)this;
     }
     public <T extends Object> T getUserProperty(Object key) {
         return (T)userProperties.get(key);
     }
     public Map getUserProperties() {
         return userProperties;
     }
   
     ///////////// Empty Accessors

}
