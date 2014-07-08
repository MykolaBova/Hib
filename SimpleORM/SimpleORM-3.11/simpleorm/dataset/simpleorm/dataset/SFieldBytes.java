package simpleorm.dataset;

import java.sql.ResultSet;
import simpleorm.dataset.validation.SValidatorMaxLength;


/**
 * Represents Bytes field meta data. Ie. Just a byte array. Note that thisis
 * just for relatively short strings, see Blob.
 */

public class SFieldBytes extends SFieldScalar {
	static final long serialVersionUID = 3L;

	public SFieldBytes(SRecordMeta meta, String columnName, int maxSize,
			SFieldFlags... pvals) {
		super(meta, columnName, pvals);
		setMaxSize(maxSize);
        addValidator(new SValidatorMaxLength());
	}


	public Object queryFieldValue(ResultSet rs, int sqlIndex) throws Exception {
		Object res = rs.getBytes(sqlIndex);
		return res;
	}

	protected Object convertToDataSetFieldType(Object raw) {
		return raw;
	}

	@Override
	public String defaultSqlDataType() {
		return "BYTES";
	}
	
	public boolean isFKeyCompatible(SFieldScalar field) {

		if (!(field instanceof SFieldBytes))
			return false;
		if (field.getMaxSize() != this.getMaxSize())
			return false;
		return true;
	}


	@Override
	public
	int javaSqlType() {
		// TODO what is BYTES data type supposed to be. Is Bytes a standard sql type ?
		return java.sql.Types.VARCHAR;
	}
	
	@Override
	public int compareField(SRecordInstance inst, SRecordInstance other) {
		String v1 = inst.getString(this);
		String v2 = other.getString(this);
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
