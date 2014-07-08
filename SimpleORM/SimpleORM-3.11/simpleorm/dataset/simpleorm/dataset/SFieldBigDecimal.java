package simpleorm.dataset;

import java.math.BigDecimal;
import java.sql.ResultSet;

import simpleorm.utils.SException;

/**
 * Represents BigDecimal field meta data. Default SQL type is
 * NUMERIC(precission, scale), which is roughly sql-92.
 * <p>
 * 
 * What is the best way to represent money ammounts exactly? Scaled longs are no
 * good for direct end user queries on the database. Doubles tend to loose
 * precission due to Java truncating instead of rounding -- things will not
 * balance. And BigDecimals are inefficient and a real pain to use. Your choice.
 * 
 */
public class SFieldBigDecimal extends SFieldScalar {
	static final long serialVersionUID = 3L;

	private int precision = 0;
	private int scale = 0;
	/**
	 * Note that precission and scale parameters only affect how the tables are
	 * created. The scale that is actually returned is up to JDBC. And then you
	 * are responsible for dealing with rounding issues.
	 */
	public SFieldBigDecimal(SRecordMeta meta, String columnName,
			int precission, int scale, SFieldFlags... pvals) {
		super(meta, columnName, pvals);
		this.precision = precission;
		this.scale = scale;
	}

	public int getPrecision() {
		return precision;
	}
	public int getScale() {
		return scale;
	}

	public Object queryFieldValue(ResultSet rs, int sqlIndex) throws Exception {
		/**
		 * CHANGED to add second parameter
		 */
		BigDecimal res = rs.getBigDecimal(sqlIndex);
		if (rs.wasNull()) // ie. last operation!
			return null;
		else
			return res;
	}

	protected Object convertToDataSetFieldType(Object raw) throws Exception {
		if (raw instanceof BigDecimal)
			return (BigDecimal) raw;
		if (raw == null)
			return null;
		if (raw instanceof Number)
			return new BigDecimal(((Number) raw).doubleValue());
		// ## This will loose precission for longs, but Java does not
		// provide an easy way to convert longs to BigDecimals! There is
		// certainly no Number.bigDecimalValue.
		if (raw instanceof String) {
			BigDecimal val = new BigDecimal((String) raw);
			return val;
		}
		throw new SException.Data("Cannot convert " + raw + " to BigDecimal.");
	}

	/**
	 * This is basically SQL 2, and fairly database
	 * independent, we hope. Note that "BIGDECIMAL" for Oracle means a text
	 * field that can contain over 2K characters!
	 */
	@Override
	public String defaultSqlDataType() {
		return "NUMERIC("
				+ getPrecision()
				+ "," + getScale()
				+ ")";
	}
	
	public boolean isFKeyCompatible(SFieldScalar field) {

		if (!(field instanceof SFieldBigDecimal))
			return false;
		SFieldBigDecimal bdField = (SFieldBigDecimal) field;
		if (bdField.getPrecision() != this.getPrecision())
			return false;
		if (bdField.getScale() != this.getScale())
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
		BigDecimal v1 = inst.getBigDecimal(this);
		BigDecimal v2 = other.getBigDecimal(this);
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
