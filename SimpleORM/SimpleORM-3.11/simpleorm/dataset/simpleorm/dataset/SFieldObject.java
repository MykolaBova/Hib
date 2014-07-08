package simpleorm.dataset;

import java.sql.ResultSet;


/**
 * Represents columns that are not objects known to SimpleORM. No conversions
 * are done, so this provides a direct gateway to
 * <code>java.sql.RecordSet.getObject</code>. Useful for database specific
 * data types such as PostgreSQL's GIS types, and also for the new SQL array and
 * object types.
 * <p> ## Should add the JDBC object mapping property, but normally the default
 * map is OK.
 */

public class SFieldObject extends SFieldScalar {
	static final long serialVersionUID = 3L;

	public SFieldObject(SRecordMeta meta, String columnName,
			SFieldFlags... pvals) {
		super(meta, columnName, pvals);
	}


	/**
	 * Specidalizes abstract method to actually query a column from a result
	 * set.
	 */
	public Object queryFieldValue(ResultSet rs, int sqlIndex) throws Exception {
		Object res = rs.getObject(sqlIndex);
		if (rs.wasNull()) // ie. last operation!
			return null;
		else
			return res;
	}

	protected Object convertToDataSetFieldType(Object raw) throws Exception {
		return raw; // No Conversions.
	}

	@Override
	public String defaultSqlDataType() {
		return "OBJECT";
	}
	
	public boolean isFKeyCompatible(SFieldScalar field) {

		if (!(field instanceof SFieldObject))
			return false;
		return true;
	}


	@Override
	public
	int javaSqlType() {
		return java.sql.Types.JAVA_OBJECT;
	}
	
	@Override
	public int compareField(SRecordInstance inst, SRecordInstance other) {
		throw new UnsupportedOperationException("Cannot compare on raw Object fields");
	}

}
