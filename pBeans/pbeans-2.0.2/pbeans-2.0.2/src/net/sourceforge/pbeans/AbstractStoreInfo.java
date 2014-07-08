package net.sourceforge.pbeans;

import java.beans.*;
import java.sql.*;
import java.util.*;
import java.io.*;
import java.text.*;
import net.sourceforge.pbeans.data.*;
import net.sourceforge.pbeans.util.*;

/**
 * Abstract implementation of {@link StoreInfo}.
 * @deprecated Use {@link net.sourceforge.pbeans.annotations.PersistentClass} annotations instead.
 */
public abstract class AbstractStoreInfo implements StoreInfo {
	private final Class clazz;
	private final Map propertyDescriptors = new HashMap();
	private final PropertyDescriptor[] propertyDescriptorsArray;
	private String tableName;

	/**
	 * Constucts an instance of AbstractStoreInfo.
	 * @param clazz A class assignable to Persistent.
	 */
	protected AbstractStoreInfo(Class clazz) {
		if (!Persistent.class.isAssignableFrom (clazz)) {
			throw new IllegalArgumentException ("Class " + clazz.getName() + " is expected to be annotated with @PersistentClass (or implement " + Persistent.class.getName() + ").");
		}
		this.clazz = clazz;
		try {
			BeanInfo binfo = Introspector.getBeanInfo (clazz);
			PropertyDescriptor[] pds = binfo.getPropertyDescriptors();
			this.propertyDescriptorsArray = pds;
			for (int i = 0; i < pds.length; i++) {
				this.propertyDescriptors.put (pds[i].getName(), pds[i]);
			}
		} catch (IntrospectionException ie) {
			throw new IllegalArgumentException ("Unable to instrospect " + clazz);
		}
	}
	
	public Class getBeanClass() {
		return this.clazz;
	}

	public String getIdField() {
		return "JP__OBJECTID";
	}
	
	public PropertyDescriptor[] getPropertyDescriptors() {
		return this.propertyDescriptorsArray;
	}

	public boolean isDeleteFields() {
		return false;
	}
	
	public PropertyDescriptor getPropertyDescriptorByNormalFieldName(Store store, String normalFieldName) {
		try {
			BeanInfo binfo = Introspector.getBeanInfo (clazz);
			PropertyDescriptor[] pds = binfo.getPropertyDescriptors();
			for (int i = 0; i < pds.length; i++) {
				FieldDescriptor fd = this.getFieldDescriptor(store, pds[i]);
				if(fd != null && store.normalizeName(fd.getName()).equals(normalFieldName)) {
					return pds[i];
				}
			}
			return null;
		} catch (IntrospectionException ie) {
			throw new IllegalArgumentException ("Unable to instrospect " + clazz);
		}
	}

	public boolean isAutoIncrementRequested() {
		return false;
	}

	public boolean isUserManaged() {
		return false;
	}

	/**
	 * Gets the requested table name. Note that this implementation
	 * of getTableName() caches the name for subsequent calls, regardless
	 * of the maxLength value.
	 * @param maxLength The maximum table name length
	 */
	public String getTableName(int maxLength) {
		synchronized(this) {
			String tableName = this.tableName;
			if(tableName != null) {
				return tableName;
			}
			String prefix = "T";
			String className = clazz.getName();
			String tName = className.replace('.','_').replace('$','_');
			int totalLength = prefix.length() + tName.length();
			if (totalLength <= maxLength) {
				tableName = prefix + tName;
			}
			else {
				// Note: Don't change what the fixedHash is obtained from.
				int hash = Math.abs(Hash.fixedHash (className));
				String hashStr = String.valueOf(hash);
				int canUseLength = maxLength - prefix.length() - hashStr.length();
				tableName =
					prefix + 
					hashStr + 
					tName.substring(
							tName.length() - canUseLength, 
							tName.length());

			}
			this.tableName = tableName;
			return tableName;
		}
	}

	public PropertyDescriptor getPropertyDescriptor(String propertyName) {
		return (PropertyDescriptor) this.propertyDescriptors.get(propertyName);
	}

	/**
	 * Creates an instance of the class passed to the constructor
	 * of this AbstractStoreInfo instance. The class is expected
	 * to have a default public constructor.
	 */
	public Object create (Store store) throws StoreException {
		try {
			return this.clazz.newInstance();
		} catch (InstantiationException ie) {
			throw new StoreException ("Unable to instantiate class " + this.clazz.getName() + ". It should have a default public constructor, or it should have a _StoreInfo suffixed class which implements StoreInfo.");
		} catch (IllegalAccessException iae) {
			throw new StoreException (iae);
		}
	}

	/**
	 * Gets a field descriptor for a property if its <i>compile-time</i> type
	 * is a primitive type, a boxed primitive type, String, java.util.Date, java.sql.Date,
	 * java.sql.Timestamp, java.sql.Time, byte[], or <code>Persistent</code>.
	 * For any other types, this method returns <code>null</null>.
	 */
	public FieldDescriptor getFieldDescriptor (Store store, PropertyDescriptor pd) {
		if (pd.getReadMethod() == null || pd.getWriteMethod() == null) {
			return null;
		}
		Class propType = pd.getPropertyType();
		String fieldName = store.getShortColumnName(pd.getName(), "F");
		if (String.class == propType) {
			return new FieldDescriptor (fieldName, Types.VARCHAR);
		}
		else if (PersistentID.class == propType) {
			return new FieldDescriptor (fieldName, Types.BIGINT);
		}
		else if (Persistent.class.isAssignableFrom (propType)) {
			return new FieldDescriptor (fieldName, Types.BIGINT);
		}
		else if (int.class == (propType)) {
			return new FieldDescriptor (fieldName, Types.INTEGER);
		}
		else if (Integer.class == (propType)) {
			return new FieldDescriptor (fieldName, Types.INTEGER);
		}
		else if (boolean.class == (propType)) {
			return new FieldDescriptor (fieldName, Types.BOOLEAN);
		}
		else if (Boolean.class == (propType)) {
			return new FieldDescriptor (fieldName, Types.BOOLEAN);
		}
		else if (long.class == (propType)) {
			return new FieldDescriptor (fieldName, Types.BIGINT);
		}
		else if (Long.class == (propType)) {
			return new FieldDescriptor (fieldName, Types.BIGINT);
		}
		else if (double.class == (propType)) {
			return new FieldDescriptor (fieldName, Types.DOUBLE);
		}
		else if (Double.class == (propType)) {
			return new FieldDescriptor (fieldName, Types.DOUBLE);
		}
		else if (float.class == (propType)) {
			return new FieldDescriptor (fieldName, Types.FLOAT);
		}
		else if (Float.class == (propType)) {
			return new FieldDescriptor (fieldName, Types.FLOAT);
		}
		else if (short.class == (propType)) {
			return new FieldDescriptor (fieldName, Types.SMALLINT);
		}
		else if (Short.class == (propType)) {
			return new FieldDescriptor (fieldName, Types.SMALLINT);
		}
		else if (char.class == (propType)) {
			return new FieldDescriptor (fieldName, Types.CHAR);
		}
		else if (Character.class == (propType)) {
			return new FieldDescriptor (fieldName, Types.CHAR);
		}
		else if (byte.class == (propType)) {
			return new FieldDescriptor (fieldName, Types.SMALLINT);
		}
		else if (Byte.class == (propType)) {
			return new FieldDescriptor (fieldName, Types.SMALLINT);
		}
		else if (java.sql.Timestamp.class.isAssignableFrom (propType)) {
			return new FieldDescriptor (fieldName, Types.TIMESTAMP);
		}
		else if (java.sql.Date.class.isAssignableFrom (propType)) {
			return new FieldDescriptor (fieldName, Types.DATE);
		}
		else if (java.sql.Time.class.isAssignableFrom (propType)) {
			return new FieldDescriptor (fieldName, Types.TIME);
		}
		else if (java.util.Date.class == propType) {
			return new FieldDescriptor (fieldName, Types.BIGINT);
		}
		else if (byte[].class == (propType)) {
			return new FieldDescriptor (fieldName, Types.VARBINARY);
		}
		else if(GlobalPersistentID.class == propType) {
			return new FieldDescriptor(fieldName, Types.VARCHAR);
		}
		else {
			// Ignoring... don't know how to persist this
			return null;
		}
	}

	public Object marshallValue (PropertyDescriptor pd, Object value) throws StoreException {
		Class propertyType = pd.getPropertyType();
		if (value instanceof String) {
			if (String.class == propertyType) {
				return value;
			}
			else if (java.util.Date.class == propertyType) {
				try {
					java.util.Date date = new SimpleDateFormat().parse((String) value);
					return new Long(date.getTime());
				} catch (ParseException pe) {
					throw new StoreException(pe);
				}
			}
			else {
				return value;
			}
		}
		else if (value == null) {
			return null;
		}
		else if (PersistentID.class == propertyType) {
			return ((PersistentID) value).longValue();
		}
		else if (java.util.Date.class == propertyType) {
			return new Long(((java.util.Date) value).getTime());
		}
    	else if (GlobalPersistentID.class == propertyType) {
    		return ((GlobalPersistentID) value).toString();
    	}
		else {
			return value;
		}
	}

	public Object unmarshallValue (PropertyDescriptor pd, Object fieldValue) throws StoreException {
		Class propertyType = pd.getPropertyType();
		if (fieldValue instanceof String && String.class == propertyType) {
			return fieldValue;
		}
		else if (fieldValue instanceof Integer && (int.class == propertyType || Integer.class == propertyType)) {
			return fieldValue;
		}
		else if (PersistentID.class == propertyType) {
			return new PersistentID (((Long) fieldValue).longValue());
		}
		else if (fieldValue == null) {
			return null;
		}
		else if ((char.class == propertyType || java.lang.Character.class == propertyType) && fieldValue instanceof String) {
			return new Character(((String) fieldValue).charAt(0));
		}
		else if (java.util.Date.class == propertyType) {
			return new java.util.Date(((Long) fieldValue).longValue());
		}
		else if ((float.class == propertyType || java.lang.Float.class == propertyType) && fieldValue instanceof Double) {
			return new Float((float) ((Double) fieldValue).doubleValue());
		}
		else if ((short.class == propertyType || java.lang.Short.class == propertyType) && fieldValue instanceof Integer) {
			return new Short((short) ((Integer) fieldValue).intValue());
		}
		else if ((byte.class == propertyType || java.lang.Byte.class == propertyType) && fieldValue instanceof Integer) {
			return new Byte((byte) ((Integer) fieldValue).intValue());
		}
		else if (byte[].class == propertyType && fieldValue instanceof String) {
			try {
				return ((String) fieldValue).getBytes("8859_1");
			} catch (java.io.UnsupportedEncodingException uee) {
				throw new StoreException ("Encoding US-ASCII is unsupported!?", uee);
			}
		}
		else if (fieldValue instanceof byte[] && (Character.class == propertyType || char.class == propertyType)) {
			try {
				String s = new String((byte[]) fieldValue, "UTF-8");
				return new Character(s.charAt(0));
			} catch (UnsupportedEncodingException ue) {
				throw new IllegalArgumentException (ue.getMessage());
			}
		}
    	else if(GlobalPersistentID.class == propertyType) {
    		return GlobalPersistentID.valueOf((String) fieldValue);
    	}
		else {
			return fieldValue;
		}
	}

	/**
	 * Returns non-unique Index instances for all JavaBean properties
	 * that are persisted
	 * except those of type BLOB.
	 */
	public Index[] getIndexes (Store store) throws StoreException {
		try {
			BeanInfo binfo = Introspector.getBeanInfo(this.clazz);
			PropertyDescriptor[] ppDescriptors = binfo.getPropertyDescriptors();
			Collection indexes = new LinkedList();
			for (int i = 0; i < ppDescriptors.length; i++) {
				FieldDescriptor fd = this.getFieldDescriptor(store, ppDescriptors[i]);
				if (fd != null) {
					int type = fd.getSqlType();
					if (type != Types.BLOB && type != Types.BINARY && type != Types.VARBINARY) {
						indexes.add (new Index (false, fd.getName()));
					}
				}
			}
			return (Index[]) indexes.toArray (new Index[0]);        
		} catch (IntrospectionException ie) {
			throw new StoreException (ie);
		}
	}

	public ClassLoader getClassLoader() {
		return this.clazz.getClassLoader();
	}
}
