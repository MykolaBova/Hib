package simpleorm.dataset;

import java.sql.ResultSet;

import simpleorm.utils.SException;

/** Represents Integer field meta data. */

public class SFieldInteger extends SFieldScalar {
	static final long serialVersionUID = 3L;

	public SFieldInteger(SRecordMeta meta, String columnName,
			SFieldFlags... pvals) {
		super(meta, columnName, pvals);
	}

	public Object queryFieldValue(ResultSet rs, int sqlIndex) throws Exception {
		int res = rs.getInt(sqlIndex);
		if (rs.wasNull()) // ie. last operation!
			return null;
		else
			return new Integer(res);
	}

	protected Object convertToDataSetFieldType(Object raw) throws Exception {
		if (raw instanceof Integer)
			return raw;
		if (raw == null)
			return null;
		if (raw instanceof Number)
			return new Integer(((Number) raw).intValue());
		if (raw instanceof String) {
			return new Integer(Integer.parseInt((String)raw));
		}
		throw new SException.Data("Cannot convert " + raw + " to Integer.");
	}

	@Override
	public String defaultSqlDataType() {
		return "INTEGER";
	}	

	public boolean isFKeyCompatible(SFieldScalar field) {

		if (!(field instanceof SFieldInteger))
			return false;
		return true;
	}

	@Override	public	int javaSqlType() {
		return java.sql.Types.INTEGER;
	}

	@Override
	public int compareField(SRecordInstance inst, SRecordInstance other) {
		Integer v1 = inst.getInt(this); // return 0 for null...
		Integer v2 = other.getInt(this); // return 0 for null...
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
