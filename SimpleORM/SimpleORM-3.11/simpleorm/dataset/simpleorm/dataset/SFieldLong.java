package simpleorm.dataset;

import java.sql.ResultSet;
import java.sql.Timestamp;

import simpleorm.utils.SException;

/**
 * Represents Long field meta data. Default SQL type is NUMERIC(18,0), which is
 * roughly sql-92.
 */

public class SFieldLong extends SFieldScalar {
	static final long serialVersionUID = 3L;

	public SFieldLong(SRecordMeta meta, String columnName,
			SFieldFlags... pvals) {
		super(meta, columnName, pvals);
	}

	public Object queryFieldValue(ResultSet rs, int sqlIndex) throws Exception {
		long res = rs.getLong(sqlIndex);
		if (rs.wasNull()) // ie. last operation!
			return null;
		else
			return new Long(res);
	}

	protected Object convertToDataSetFieldType(Object raw) throws Exception {
		if (raw instanceof Long)
			return raw;
		if (raw == null)
			return null;
		if (raw instanceof Number)
			return new Long(((Number) raw).longValue());
		if (raw instanceof String) {
			return Long.parseLong((String) raw);
		}
		throw new SException.Data("Cannot convert " + raw + " to Long.");
	}

	/**
	 * Specializes SFieldMeta. This is basically SQL 2, and fairly database
	 * independent, we hope. Note that "LONG" for Oracle means a text field that
	 * can contain over 2K characters!
	 */
	@Override
	public String defaultSqlDataType() {
		return "NUMERIC(18,0)";
	}

	public boolean isFKeyCompatible(SFieldScalar field) {

		if (!(field instanceof SFieldLong))
			return false;
		return true;
	}

	@Override
	public
	int javaSqlType() {
		return java.sql.Types.NUMERIC;
	}
	
	@Override
	public int compareField(SRecordInstance inst, SRecordInstance other) {
		Long v1 = inst.getLong(this); // return 0 for null...
		Long v2 = other.getLong(this); // return 0 for null...
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
