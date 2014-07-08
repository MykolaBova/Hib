package simpleorm.dataset;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import simpleorm.utils.SException;
import simpleorm.utils.SUte;

/**
 * Defines the meta data for a {@link SRecordInstance} such as the table name.
 * Details about each field are stored in {@link SFieldMeta} refered to from
 * this object.
 * <p>
 * Thus Instance Variables of this class only describe the definition of a
 * Record, not instances or connections.
 * <p>
 * 
 * This class also contains routines to create new {@link SRecordInstance}es
 * such as {@link #findOrCreate} and {@link #select}. (This packaging makes the
 * calls shorter than using a static method on SRecordInstance.)
 * <p>
 * 
 * The meta data is NOT serialized, any more than Class definitions are.  See SRecordInstance.getMeta() for details.
 */

public class SRecordMeta<T extends SRecordInstance> // implements Serializable  // See SRecordInstance.getMeta()
{

	/*
	 * instances_ is a hash map of SRecordMetas. key is userClassName, and value
	 * is SRecordMeta. Static map of all SRecordMeta instances. used for
	 * de-serialization
	 */
	private static HashMap<String, SRecordMeta<?>> instances_ = new HashMap<String, SRecordMeta<?>>();
	
   	/** Name of underlying java class. Used for de-serialization */
	private String userClassName;

    /** Underlying table name, as specified in constructor.*/
	private String tableName = null;

	
	private LinkedHashMap<String, SFieldMeta> fieldMap = new LinkedHashMap<String, SFieldMeta>(); // used to work as transient?!

    // Following are a cache
	private transient SFieldScalar[] keyScalarFields = null;
	private transient SFieldScalar[] descriptiveScalarFields = null;
	private transient SFieldScalar[] queriedScalarFields = null;
	private transient SFieldScalar[] allScalarFields = null;
		
	public boolean quoteName = false;

	private int nextFieldIndex = 0; 


	/** The underlying java class for this object. */
	private transient Class<T> userClass;
	//private transient Constructor<T> recordConstructor;
    
    Map userProperties = new LinkedHashMap(3);
	
	/**
	 * Create a new table/record definition.
	 * <p>
	 * @param userClass an instance of the class this RecordMeta holds the metadata for (ie T )
	 * @param tableName The name of the SQL table that will be associated with this record.
	 */
	public SRecordMeta(Class<T> userClass, String tableName) {
		
		this.userClassName = userClass.getName();
		instances_.put(userClassName, this);
		this.tableName = tableName;
		this.userClass = userClass;
//		try {
//			this.recordConstructor = getUserClass().getConstructor(SDataSet.class);
//		}
//		catch (Exception e) {
//			throw new SException.Error("Unable to get a constructor for this record type : "+this.toString());
//		}
	}
	
	 T newRecordInstance() {
		try {
			return this.userClass.newInstance();
		}
		catch (Exception e) {
			throw new SException.Error("Unable to get new record instance from "+this, e);
		}
	}

//	/**
//	 * SRecordMeta is like a singleton, in that only one instance of SRecordMeta
//	 * must exist in the VM for a specific table. This is a special method used
//	 * during de-serialization to determine if the object de-serialized should
//	 * be substituted. This method is implemented to return the SRecordMeta
//	 * object for the appropriate user Class that already exists.<p>
//     * 
//     * (This method is reflected into and magically called by serialization.)
//	 */
//	protected Object readResolve() throws ObjectStreamException {
//		
//		try {
//			Class.forName(userClassName); // This forces class to load and, thus, its SRecordMeta to be instantiated
//
//			Object substituted = instances_.get(userClassName);
//			if (substituted == null) {
//				throw new NullPointerException();
//			}
//			return substituted;
//		} catch (Exception e) {
//			throw new SException.InternalError(
//					"Error de-serializing SRecordMeta for " + userClassName);
//		}
//	}

    SFieldMeta addField(SFieldMeta field) {
		
		// Check field not already added or duplicate field name
		if (getFieldNames().contains(field.getFieldName()))
			throw new SException.Error("Duplicate field name. Maybe are you trying to add "+field+" twice");
		
		// Reset cached field arrays
		keyScalarFields = null;
		queriedScalarFields = null;
		descriptiveScalarFields = null;
        allScalarFields = null;
		
		// Update field sets
		this.fieldMap.put(field.getFieldName(), field);
		field.index = nextFieldIndex;
		nextFieldIndex++;
		return field;
	}

    /**
	 * @return the an unmodifiable view of the field metas (both Scalar
	 * and References)
	 */
	public List<SFieldMeta> getFieldMetas() {
		List<SFieldMeta> fields = new ArrayList<SFieldMeta>(fieldMap.values());
		return Collections.unmodifiableList(fields);
	}

    /**
	 * Returns SFieldMetas that are updated to select given SelectMode.
	 */
	public SFieldScalar[] fieldsForMode(SSelectMode selectMode) {		
		switch (selectMode) {
		case SDESCRIPTIVE:
			return getDescriptiveScalarFields();
		case SALL:
			return getAllScalarFields();
		case SNONE:
			return new SFieldScalar[0];
		default:
			return getQueriedScalarFields();
		}
	}
    private void makeFieldArrays() {
        if (keyScalarFields == null) {
            ArrayList<SFieldMeta> keyFieldsList = new ArrayList<SFieldMeta>();
            ArrayList<SFieldMeta> descriptiveFieldsList = new ArrayList<SFieldMeta>();
            ArrayList<SFieldMeta> allScalarFieldsList = new ArrayList<SFieldMeta>();
            ArrayList<SFieldMeta> queriedScalarFieldsList = new ArrayList<SFieldMeta>();

            for (SFieldMeta field : fieldMap.values()) {
                if (field instanceof SFieldScalar) {
                    if (((SFieldScalar)field).isPrimary()) {
                        keyFieldsList.add(field);
                        descriptiveFieldsList.add(field);
                        allScalarFieldsList.add(field);
                        queriedScalarFieldsList.add(field);
                    } else {
                        allScalarFieldsList.add(field);
                        if (field.isDescriptive()) {
                            descriptiveFieldsList.add(field);
                        }
                        if (!field.isUnqueried()) {
                            queriedScalarFieldsList.add(field);
                        }
                    }
                }
                keyScalarFields = keyFieldsList.toArray(new SFieldScalar[keyFieldsList.size()]);
                descriptiveScalarFields = descriptiveFieldsList.toArray(new SFieldScalar[descriptiveFieldsList.size()]);
                queriedScalarFields = queriedScalarFieldsList.toArray(new SFieldScalar[queriedScalarFieldsList.size()]);
                allScalarFields = allScalarFieldsList.toArray(new SFieldScalar[allScalarFieldsList.size()]);
            }
        }
    }
	/**
	 * Get the scalar primary key fields meta for this record meta
	 * @return a readonly list of the primary key Fields meta of the record meta
	 */
	public SFieldScalar[] getPrimaryKeys() {
        makeFieldArrays();
		return keyScalarFields;
	}
	
	public SFieldScalar[] getQueriedScalarFields() {
        makeFieldArrays();
		return queriedScalarFields;
	}
	
	public SFieldScalar[] getAllScalarFields() { 
        makeFieldArrays();
        return allScalarFields;
	}
	
	public SFieldScalar[] getDescriptiveScalarFields() {
        makeFieldArrays();
		return descriptiveScalarFields;
	}   
	    
    public @Override String toString() {
		return "[SRecM " + SUte.cleanClass(userClass) + "]";
	}
    /** Displays all columns */
    public String toLongerString() {
        StringBuffer res = new StringBuffer(this + ":-\n");
		for (SFieldMeta fmeta : getFieldMetas()) {
			res.append("    " + fmeta.toLongerString()	+ "\n");
		}
		return res.toString();
	}

    ///////////////////////// Empty Accessors go here ////////////////////
    
    	// Record instances factory
	public Class<T> getUserClass() {
		return userClass;
	}

    	/**
	 * All the field names. use getField to look up the actual field.
	 */
	public Set<String> getFieldNames() {
		return fieldMap.keySet();
	}

    	/** Returns the field by its name, or null if not found. */
	public SFieldMeta getField(String fieldName) {
		return fieldMap.get(fieldName);
	}

    public String getTableName(){return tableName;}

        /** Causes the generated sql to quote ("") the table name.
     * Makes it case sensitive, allows reserved words and odd characters.
     * Beware that Postgresql (only) defaults to lower case, not upper case.
     */
	public SRecordMeta setQuoted(boolean quote) {
		this.quoteName = quote;
		return this;
	}

     public SRecordMeta putUserProperty(Object key, Object value) {
         userProperties.put(key, value);
         return this;
     }
     public <T extends Object> T getUserProperty(Object key) {
         return (T)userProperties.get(key);
     }
     public Map getUserProperties() {
         return userProperties;
     }

}
