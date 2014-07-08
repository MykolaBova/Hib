package simpleorm.dataset;

import java.sql.ResultSet;
import java.sql.Timestamp;

import simpleorm.utils.SException;

/** 
 * Represents Timestamp field meta data.
 * See getSqlType for issues.
 */

public class SFieldTimestamp extends SFieldScalar {

	static final long serialVersionUID = 3L;

	public SFieldTimestamp(SRecordMeta meta, String columnName,
			SFieldFlags... pvals) {
		super(meta, columnName, pvals);
	}

	public Object queryFieldValue(ResultSet rs, int sqlIndex) throws Exception {
		java.sql.Timestamp res = rs.getTimestamp(sqlIndex);
		if (rs.wasNull()) // ie. last operation!
			return null;
		else
			return res;
	}

	protected Object convertToDataSetFieldType(Object raw) throws Exception {
		if (raw instanceof java.sql.Timestamp)
			return (java.sql.Timestamp) raw;
		if (raw == null)
			return null; // Follows JDBC
		if (raw instanceof String)
			return java.sql.Timestamp.valueOf((String) raw);
		if (raw instanceof java.util.Date)
			return new java.sql.Timestamp(((java.util.Date) raw).getTime());

		throw new SException.Data("Cannot convert " + raw + " to Timestamp.");
	}

    /**
     *Time & date is a mess in SQL.<p>
     * 
     * ISO-92:  seems to only support TIMESTAMP.  Maybe WITH TIME ZONE.
     * HSQL:  Seems to support both TIMESTAMP and DATETIME.  No WITH TIME ZONE.
     * MSSQL: TIMESTAMP is not a time, but a transaction id like thing.  DATETIME is a date time.
     * POSTGRESQL: Appears to support both TIMESTAMP and DATETIME.  Suports? WITH TIME ZONE.
     * MySQL:  DATETIME and TIMESTAMP are different data types with different precission.
     * 
     * TIMESTAMP is ISO, seems to be lowest common denominator.
     */
    @Override public String defaultSqlDataType(){
		return "TIMESTAMP";
	}

	public boolean isFKeyCompatible(SFieldScalar field) {

		if (!(field instanceof SFieldTimestamp))
			return false;
		return true;
	}

	@Override
	public
	int javaSqlType() {
		return java.sql.Types.TIMESTAMP;
	}
	
	@Override
	public int compareField(SRecordInstance inst, SRecordInstance other) {
		Timestamp v1 = inst.getTimestamp(this);
		Timestamp v2 = other.getTimestamp(this);
		if (v2 == null && v1 == null) {
			return 0;
		}
		if (v1 == null) {
			return 1; // null is greater than v1
		}
		if (v2 == null) {
			return -1;
		}
		return v1.compareTo(v2);
	}

}
