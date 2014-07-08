package simpleorm.dataset;

import java.sql.ResultSet;
import java.sql.Time;

import simpleorm.utils.SException;

/** Represents Time field meta data. */

public class SFieldTime extends SFieldScalar {

	static final long serialVersionUID = 3L;

	public SFieldTime(SRecordMeta meta, String columnName,
			SFieldFlags... pvals) {
		super(meta, columnName, pvals);
	}

	public Object queryFieldValue(ResultSet rs, int sqlIndex) throws Exception {
		java.sql.Time res = rs.getTime(sqlIndex);
		if (rs.wasNull()) // ie. last operation!
			return null;
		else
			return res;
	}

	protected Object convertToDataSetFieldType(Object raw) throws Exception {
		if (raw instanceof java.sql.Time)
			return (java.sql.Time) raw;
		if (raw == null)
			return null; // Follows JDBC
		if (raw instanceof String)
			return java.sql.Time.valueOf((String) raw);
		if (raw instanceof java.util.Date)
			return new java.sql.Time(((java.util.Date) raw).getTime());
		throw new SException.Data("Cannot convert " + raw + " to Time.");
	}

	@Override
	public String defaultSqlDataType() {
		return "TIME";
	}
	
	public boolean isFKeyCompatible(SFieldScalar field) {

		if (!(field instanceof SFieldTime))
			return false;
		return true;
	}

	@Override
	public
	int javaSqlType() {
		return java.sql.Types.TIME;
	}
	
	@Override
	public int compareField(SRecordInstance inst, SRecordInstance other) {
		Time t1 = inst.getTime(this);
		Time t2 = other.getTime(this);
		if (t2 == null && t1 == null) {
			return 0;
		}
		if (t1 == null) {
			return 1; // null is greater than t1
		}
		if (t2 == null) {
			return -1;
		}
		return t1.compareTo(t2);
	}

}
