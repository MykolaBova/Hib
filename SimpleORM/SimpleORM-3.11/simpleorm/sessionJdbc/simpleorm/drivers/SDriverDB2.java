package simpleorm.drivers;

import simpleorm.dataset.SFieldScalar;
import simpleorm.sessionjdbc.SDriver;

/**
 * Contains DB2/400 (iSeries DB2/Implementation tested on OS400 v5.1)
 * 
 * CHAR/VARCHAR max 255 else LONG VARCHAR. CHARS are auto trimed on retrieval. ''
 * is not null (unlike ORACLE).
 * 
 * @author Eric Merritt
 */
public class SDriverDB2 extends SDriver {

	protected String driverName() {
		return "IBM DB2 JDBC Universal Driver Architecture"; // new type 4
		// return "IBM DB2 JDBC 2.0 Type 2"; // old type 2
	}

	public int maxIdentNameLength() {
		return 18;
	} // Yes, only 18 chars!

	/** DB2 does not allow just NULL. */
	protected void addNull(StringBuffer sql, SFieldScalar fld) {
		if (fld.isPrimary() || fld.isMandatory())
			sql.append(" NOT NULL");
	}

}
