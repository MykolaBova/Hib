package simpleorm.dataset;

import java.sql.ResultSet;

import simpleorm.utils.SException;

/** Represents Double field meta data. */

public class SFieldDouble extends SFieldScalar {
	static final long serialVersionUID = 3L;

	public SFieldDouble(SRecordMeta meta, String columnName,
			SFieldFlags... pvals) {
		super(meta, columnName, pvals);
	}

	/**
	 * Specidalizes abstract method to actually query a column from a result
	 * set.
	 */
	public Object queryFieldValue(ResultSet rs, int sqlIndex) throws Exception {
		double res = rs.getDouble(sqlIndex);
		if (rs.wasNull()) // ie. last operation!
			return null;
		else
			return new Double(res);
	}

	protected Object convertToDataSetFieldType(Object raw) throws Exception {
		if (raw instanceof Double)
			return raw;
		if (raw == null)
			return null; // Follows JDBC
		if (raw instanceof Number)
			return new Double(((Number) raw).intValue());
		if (raw instanceof String) {
			return Double.parseDouble((String) raw);
		}
		throw new SException.Data("Cannot convert " + raw + " to Double.");
	}

	@Override
	public String defaultSqlDataType() {
		return "DOUBLE PRECISION";
	}
	
	public boolean isFKeyCompatible(SFieldScalar field) {

		if (!(field instanceof SFieldDouble))
			return false;
		return true;
	}

	@Override
	public
	int javaSqlType() {
		return java.sql.Types.DOUBLE;
	}
	
	@Override
	public int compareField(SRecordInstance inst, SRecordInstance other) {
		Double v1 = inst.getDouble(this);
		Double v2 = other.getDouble(this);
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
