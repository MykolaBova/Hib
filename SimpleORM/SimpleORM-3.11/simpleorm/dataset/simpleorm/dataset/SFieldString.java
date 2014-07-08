package simpleorm.dataset;

import java.sql.ResultSet;
import simpleorm.dataset.validation.SValidatorMaxLength;

/** Represents String field meta data. */

public class SFieldString extends SFieldScalar {

	static final long serialVersionUID = 3L;

	/**
	 * <code>maxSize</code> is the maximum size in bytes (not characters) of
	 * the column. This fairly meaningless number is required for all database
	 * DDL except PostgreSQL, for which it is ignored.
	 */
	public SFieldString(SRecordMeta<?> meta, String columnName, int maxSize,
			SFieldFlags... pvals) {
		super(meta, columnName, pvals);
		setMaxSize(maxSize);
        addValidator(new SValidatorMaxLength());
	}

	@Override public Object queryFieldValue(ResultSet rs, int sqlIndex) throws Exception {
		return rs.getString(sqlIndex);
	}

	@Override protected Object convertToDataSetFieldType(Object raw) {
		return raw == null ? null : raw.toString();
	}

	@Override public String defaultSqlDataType() {
        // use .overrideSqlDataType, see data types.
//	if (getFlags().contains(SFieldFlags.SAS_CHAR)) {
//			return "CHAR(" + getMaxSize() + ")";
//		}
		return "VARCHAR(" + getMaxSize() + ")";
	}
	
	@Override public boolean isFKeyCompatible(SFieldScalar field) {

		if (!(field instanceof SFieldString))
			return false;
		SFieldString strField = (SFieldString) field;
		if (strField.getMaxSize() != this.getMaxSize())
			return false;
		return true;
	}

	@Override public int javaSqlType() {
		return java.sql.Types.VARCHAR;
	}

	@Override
	public int compareField(SRecordInstance inst, SRecordInstance other) {
		String s1 = inst.getString(this);
		String s2 = other.getString(this);
		if (s2 == null && s1 == null) {
			return 0;
		}
		if (s1 == null) {
			return 1; // null is greater than s1
		}
		if (s2 == null) {
			return -1;
		}
		return s1.compareTo(s2);
	}
}
