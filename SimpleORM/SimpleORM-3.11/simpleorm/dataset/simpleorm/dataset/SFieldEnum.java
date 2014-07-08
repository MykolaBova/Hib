package simpleorm.dataset;

import java.sql.ResultSet;

import simpleorm.dataset.validation.SValidatorEnum;
import simpleorm.utils.SException;

/**
 * Represents Enum field meta data.  
 * Note that internal storage is now Enum, not String, which is more consistent if less flexible eg. if database has bad data.
 * So getObject() returns Enum.
 */

public class SFieldEnum<T extends Enum<T>> extends SFieldScalar {

	static final long serialVersionUID = 3L;
	
	private Class<T> enumClass = null;
	
	/**
	 * <code>enumType</code> is the Enum type backing this field
	 */
	public SFieldEnum(SRecordMeta<?> meta, String columnName, Class<T> enumType, SFieldFlags... pvals) {
		super(meta, columnName, pvals);
		int maxSize = 0;
		enumClass = enumType;
		//enumInstance = exampleValue;
		for (T val : enumClass.getEnumConstants()) {
			maxSize = maxSize < val.name().length() ? val.name().length() : maxSize;
		}
		setMaxSize(maxSize);
   		addValidator(new SValidatorEnum<T>(this));
	}
	
	public Class<T> getEnumClass() {
		return enumClass;
	}

	public Object queryFieldValue(ResultSet rs, int sqlIndex) throws Exception {
		String val = rs.getString(sqlIndex);
        if (val == null) return null;
        return convertToDataSetFieldType(val);
	}

	/**
	 * Internal representation of SFieldEnum is Enum.
	 */
	protected Object convertToDataSetFieldType(Object raw) {
		if (raw == null) return null;
		else if (raw instanceof String) return Enum.valueOf(enumClass, (String)raw);
		else if (enumClass.isInstance(raw)) return raw;
		throw new SException.Data("Cannot convert " + raw + " to Enum.");
	}
    
	@Override public String defaultSqlDataType() {
		return "VARCHAR(" + this.getMaxSize()+")";
	}

	/**
	 * Nota : foreign key is allowed if the enum values can be stored
	 * in the database... We don't force checking the values (ie use same enum type)
	 */
	public boolean isFKeyCompatible(SFieldScalar field) {

		if (field instanceof SFieldEnum) {
			SFieldEnum<?> strField = (SFieldEnum<?>) field;
			if (strField.getMaxSize() != this.getMaxSize())
				return false;
			return true;
		}
			
		if (field instanceof SFieldString) {
			SFieldString strField = (SFieldString) field;
			if (strField.getMaxSize() != this.getMaxSize())
				return false;
			return true;
		}
		
		return false;
	}
	
	@Override
	public Object writeFieldValue(Object value) {
		if (value==null) return null;
		return ((Enum<T>)value).name();
	}

	@Override public int javaSqlType() {
		return java.sql.Types.VARCHAR;
	}
	
	@Override
	public int compareField(SRecordInstance inst, SRecordInstance other) {
		T v1 = inst.getEnum(this);
		T v2 = other.getEnum(this);
		if (other.isNull(this) && inst.isNull(this)) {
			return 0;
		}
		if (inst.isNull(this)) {
			return 1; // null is greater than v1
		}
		if (other.isNull(this)) {
			return -1;
		}
		return v1.compareTo(v2);
	}
}
