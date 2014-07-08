package simpleorm.dataset;

import java.sql.ResultSet;
import java.util.Date;

import simpleorm.utils.SException;

/** Represents Date field meta data. */

public class SFieldDate extends SFieldScalar {
	static final long serialVersionUID = 3L;

	public SFieldDate(SRecordMeta meta, String columnName,
			SFieldFlags... pvals) {
		super(meta, columnName, pvals);
	}

	public Object queryFieldValue(ResultSet rs, int sqlIndex) throws Exception {
		java.sql.Date res = rs.getDate(sqlIndex);
		if (rs.wasNull()) // ie. last operation!
			return null;
		else
			return res;
	}

	protected Object convertToDataSetFieldType(Object raw) throws Exception {
		if (raw instanceof java.sql.Date)
			return (java.sql.Date) raw;
		if (raw == null)
			return null; // Follows JDBC
		if (raw instanceof String)
			return java.sql.Date.valueOf((String) raw);
		if (raw instanceof java.util.Date)
			return new java.sql.Date(((java.util.Date) raw).getTime());

		throw new SException.Data("Cannot convert " + raw + " to Date.");
	}

	/** Specializes SFieldMeta. */
	@Override
	public String defaultSqlDataType() {
		return "DATE";
	}
	
	public boolean isFKeyCompatible(SFieldScalar field) {

		if (!(field instanceof SFieldDate))
			return false;
		return true;
	}

	@Override
	public
	int javaSqlType() {
		return java.sql.Types.DATE;
	}
	
	@Override
	public int compareField(SRecordInstance inst, SRecordInstance other) {
		Date v1 = inst.getDate(this);
		Date v2 = other.getDate(this);
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
