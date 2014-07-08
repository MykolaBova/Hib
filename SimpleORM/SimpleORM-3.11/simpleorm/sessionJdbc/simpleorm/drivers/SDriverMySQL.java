package simpleorm.drivers;

import simpleorm.dataset.SFieldReference;
import simpleorm.dataset.SRecordMeta;
import simpleorm.sessionjdbc.SDriver;

/**
 * This contains MySQL specific code.
 * <p>
 * 
 * 
 * CHAR/VARCHAR max size 255(!) Thence TEXT.
 * 
 * 
 * ### pierrea@email.com suggests:- (Needs testing)
 *  - SDriverMysql should have its own quoteIdentifier using ` instead of " (the
 * character next to the 1 button above the tab button on a std US keyboard).
 * The double quotation fails for the tablenames.
 * <p>
 *  - SDriverMysql should be updated with a new driverName: MySQL-AB JDBC
 * Driver. This is the driver name for all mysql jdbc drivers as posted on Mysql
 * developer section. It might be worthwhile to change the driverName function
 * to return an array of possible names instead of a single name, either that or
 * subclass SDriverMysql to SDriverMMMysql and then include both.
 * 
 */

public class SDriverMySQL extends SDriver {

	protected String driverName() {
		return "MySQL-AB JDBC Driver";
	}

	// "Mark Matthews' MySQL Driver";}

  	protected @Override void appendQuotedIdentifier(String ident, StringBuffer buf) {
        appendQuotedIdentifier(ident, buf, '`');
    }

	/** Any other text to be added after the final ")" */
	protected String postTablePostParenSQL(SRecordMeta meta) {
		return "\n    TYPE = InnoDB;";
	}

	/**
	 * ### This assuems their "innoDB" mode. Not true for traditional MySQL.
	 * Needs work.
	 */
	public boolean supportsLocking() {
		return true;
	}

	// ### Need to add generateKey() like Postgres driver.

	/**
	 * Returns <code>INDEX (KCOL)
	 * Apparantly MySQL needs the index to be created as part of the Create Table statement or it generates
	 * errors.  I don't think any other DBs support INDEX clauses. 
	 @author Joseph Greenwood
	 */
	protected String indexKeySQL(SRecordMeta meta) {
		return mapForeignKeys(meta, false);
	}

	protected void makeForeignKeyIndexSQL(SRecordMeta meta, int fx,
			SFieldReference fldRef, StringBuffer sbFkey, StringBuffer sbRefed,
			StringBuffer fkey) {
		fkey.append(",\n   INDEX ");
		fkey.append(" (");
		fkey.append(sbFkey.toString());
		fkey.append(")");
	}

}
