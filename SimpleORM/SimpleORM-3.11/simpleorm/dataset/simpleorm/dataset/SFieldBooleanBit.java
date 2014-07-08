/*
 * Created on Jan 18, 2005
 * Author dhristodorescu
 * Borderfree
 */
package simpleorm.dataset;

import java.sql.ResultSet;
import simpleorm.utils.SException;


/**
 * @author dhristodorescu Borderfree
 */
public class SFieldBooleanBit extends SFieldBoolean {
	static final long serialVersionUID = 20083L;

	public SFieldBooleanBit(SRecordMeta meta, String columnName,
			SFieldFlags... pvals) {
		super(meta, columnName, pvals);
	}

	/**
	 * Converts from database representation to internal representation
	 */
	public Object queryFieldValue(ResultSet rs, int sqlIndex) throws Exception {
		boolean res = rs.getBoolean(sqlIndex);
        if (rs.wasNull()) return null;
        return res;
	}

   	protected Object convertToDataSetFieldType(Object raw) 
	{
        if (raw == null) return null;
		if (Boolean.TRUE.equals(raw)) return Boolean.TRUE; // Autoboxing if necessary
		if (Boolean.FALSE.equals(raw)) return Boolean.FALSE; // Autoboxing if necessary
		if (raw instanceof String) {
			String s = (String) raw;
			if ("TRUE".equalsIgnoreCase(s)) // Boolean.TRUE.toString()
				return Boolean.TRUE;
			else if ("FALSE".equalsIgnoreCase(s))
				return Boolean.FALSE;
		}
		throw new SException.Data("Cannot Convert '" + raw	+ "' to Boolean.");

	}

	/** Specializes SFieldMeta. */
	@Override
	public String defaultSqlDataType() {
		return "BIT"; // For MS SQL.
	}

	public boolean isFKeyCompatible(SFieldScalar field) {

		boolean result = true;
		if (!(field instanceof SFieldBooleanBit))
			result = false;
		return result;
	}

	@Override public	int javaSqlType() {
		return java.sql.Types.BIT;
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
