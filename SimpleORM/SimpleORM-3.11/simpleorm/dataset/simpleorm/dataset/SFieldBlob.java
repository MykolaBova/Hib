package simpleorm.dataset;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.sql.ResultSet;


/**
 * Represents a BLOB, Binary large object. See SFieldBytes for short binary
 * strings. Supports returning the entire blob as a byte array, but that is not
 * normal practice. ### Untested.
 */

public class SFieldBlob extends SFieldScalar {
	static final long serialVersionUID = 3L;

	public SFieldBlob(SRecordMeta meta, String columnName, int maxSize,
			SFieldFlags... pvals) {
		super(meta, columnName, pvals);
		setMaxSize(maxSize); 
	}

	public Object queryFieldValue(ResultSet rs, int sqlIndex) throws Exception {
		// From Marcius. just getBytes should actually work though, but maybe
		// not in Oracle
		// Object res = rs.getBytes(sqlIndex);
		// return res;

		// Get the binary content from the result set as a stream
		InputStream is = rs.getBinaryStream(sqlIndex);
		// Create a byte array output stream so we can convert the content to a
		// byte[]
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		// A small buffer to reduce the number of reads
		byte[] buf = new byte[1024];
		// Read bytes from the input stream and place them in the baos
		int bytesRead = is.read(buf);
		while (bytesRead > 0) {
			baos.write(buf, 0, bytesRead);
			bytesRead = is.read(buf);
		}
		return baos.toByteArray();
	}

	protected Object convertToDataSetFieldType(Object raw) {
		return raw;
	}

	@Override
	public String defaultSqlDataType() {
		return "BLOB";
	}
	
	public boolean isFKeyCompatible(SFieldScalar field) {

		if (!(field instanceof SFieldBlob))
			return false;
		if (field.getMaxSize() != this.getMaxSize())
			return false;
		return true;
	}

	@Override
	public
	int javaSqlType() {
		return java.sql.Types.BLOB;
	}
	
	@Override
	public int compareField(SRecordInstance inst, SRecordInstance other) {
		throw new UnsupportedOperationException("Cannot compare on Blob fields");
	}

}
