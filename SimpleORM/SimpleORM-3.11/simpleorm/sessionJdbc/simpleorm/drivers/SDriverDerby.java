package simpleorm.drivers;

import simpleorm.dataset.SFieldScalar;
import simpleorm.sessionjdbc.SDriver;

/**
 * Contains Derby/Cloudscape 10.0 implementation
 * 
 * $Revision: 1.0 $ $Date: Apr 12 2004 14:33:46 $
 * 
 * @author Denis Rodrigues Cassiano deniscassiano@gmail.com
 */
public class SDriverDerby extends SDriver {

	// protected String driverName() {
	// return "IBM DB2 JDBC Universal Driver Architecture"; // new type 4
	// //return "IBM DB2 JDBC 2.0 Type 2"; // old type 2
	// }

	protected String driverName() {
		return "Derby Database Engine Driver";
	}

	/**
	 * Derby supports different locking levels, but no FOR UPDATE with ORDER BY.
	 * So supportsLocking returns false for now ...
	 */
	public boolean supportsLocking() {
		return false;
	} // ### not right.

	public int maxIdentNameLength() {
		return 18;
	} // Yes, only 18 chars!

	/** DB2 does not allow just NULL. */
	protected void addNull(StringBuffer sql, SFieldScalar fld) {
		if (fld.isPrimary() || fld.isMandatory())
			sql.append(" NOT NULL");
	}

	// /* (non-Javadoc)
	// * @see simpleorm.core.SDriver#quoteColumn(java.lang.String)
	// */
	// public String quoteColumn(String ident) {
	//		
	// return ident;
	// }
	// /* (non-Javadoc)
	// * @see simpleorm.core.SDriver#quoteConstraint(java.lang.String)
	// */
	// public String quoteConstraint(String ident) {
	// return ident;
	// }
	// /* (non-Javadoc)
	// * @see simpleorm.core.SDriver#quoteIdentifier(java.lang.String)
	// */
	// public String quoteIdentifier(String ident) {
	// return ident;
	// }
	// /* (non-Javadoc)
	// * @see simpleorm.core.SDriver#quoteTable(java.lang.String)
	// */
	// public String quoteTable(String ident) {
	// return ident;
	// }
}
