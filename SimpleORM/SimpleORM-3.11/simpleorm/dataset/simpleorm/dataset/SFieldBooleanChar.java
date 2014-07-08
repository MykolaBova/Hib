package simpleorm.dataset;

import java.sql.ResultSet;

import simpleorm.utils.SException;

/**
 * Booleans which are represented by a string. Constructor determine whether
 * "T", "Y", "Yes" etc.
 */

public class SFieldBooleanChar extends SFieldBoolean {

	private static final long serialVersionUID = 20083L;

	/**
	 * Represents Boolean field meta data as trueStr and falseStr chars.
	 * 
	 * @author Martin Snyder.
	 */
	private String TRUE_VALUE;

	private String FALSE_VALUE;

	public SFieldBooleanChar(SRecordMeta meta, String columnName, String trueStr, String falseStr,
			SFieldFlags... pvals) {
		super(meta, columnName, pvals);
		setMaxSize((trueStr.length() > falseStr.length()) ? trueStr.length() : falseStr.length());
		TRUE_VALUE = trueStr;
		FALSE_VALUE = falseStr;
	}

	/**
	 * Converts from database representation to internal representation
	 */
	public Object queryFieldValue(ResultSet rs, int sqlIndex) throws Exception {
		String res = rs.getString(sqlIndex);
        if (rs.wasNull()) return null;
		if (TRUE_VALUE.equals(res))
			return Boolean.TRUE;
		else
			// Default to false if value is in fact not expected
			return Boolean.FALSE;
	}

	@Override public Object writeFieldValue(Object value) {
		if (value==null) return null;
        if (((Boolean) value).booleanValue())
            return TRUE_VALUE;
	    else
		  	return FALSE_VALUE;
	}


	@Override
	public String defaultSqlDataType() {
        int size=TRUE_VALUE.length() > FALSE_VALUE.length() ? TRUE_VALUE.length() : FALSE_VALUE.length();
		return "VARCHAR(" + size + ")";
	}
	
	public int javaSqlType() {
		return java.sql.Types.VARCHAR;
	}

	@Override
	boolean isFKeyCompatible(SFieldScalar field) {
		if (!(field instanceof SFieldBooleanChar)) {
			return false;
		}
		return true;
	}
	
	@Override
	protected Boolean convertToDataSetFieldType(Object raw) {
		if (raw == null)
			return null;
		if (Boolean.TRUE.equals(raw)) return Boolean.TRUE; // Autoboxing if necessary
		if (Boolean.FALSE.equals(raw)) return Boolean.FALSE; // Autoboxing if necessary
		if (raw instanceof String) {
			String s = (String) raw;
			if (TRUE_VALUE.equalsIgnoreCase(s))
				return Boolean.TRUE;
			else if (FALSE_VALUE.equalsIgnoreCase(s))
				return Boolean.FALSE;
            else if ("TRUE".equalsIgnoreCase(s)) // Boolean.TRUE.toString()
				return Boolean.TRUE;
			else if ("FALSE".equalsIgnoreCase(s))
				return Boolean.FALSE;
		}
		throw new SException.Data("Cannot Convert '" + raw	+ "' to Boolean.");
	}
	
	@Override
	public int compareField(SRecordInstance inst, SRecordInstance other) {
		Boolean v1 = inst.getBoolean(this);
		Boolean v2 = other.getBoolean(this);
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
