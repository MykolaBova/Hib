package simpleorm.drivers;

import simpleorm.dataset.SFieldScalar;
import simpleorm.sessionjdbc.SDriver;

/**
 * This contains Sybase Adaptive Server specific code.
 */

public class SDriverSybase extends SDriver {

	protected String driverName() {
		return "jConnect (TM) for JDBC (TM)";
	}

	/**
	 * Sybase only understands DATETIME (and SMALLDATETIME) and timestamp (must
	 * be lc!)
	 */
	@Override protected String columnTypeSQL(SFieldScalar field, String defalt) {
		if (defalt.equals("TIMESTAMP"))
			return "timestamp";
		if (defalt.equals("DATE"))
			return "datetime";
		if (defalt.equals("TIME") )
			return "datetime";
		else
			return defalt;
	}

	protected String forUpdateSQL(boolean forUpdate) {
		return "";
	}

	/**
	 * MSSQL and Sybase do not support FOR UPDATE. But the docs say to use WITH
	 * (XLOCK). But Jorge says that it is unnecessary and makes SimpleORM fail.
	 * Very odd.
	 */
	protected String postFromSQL(boolean forUpdate) {
		// if (forUpdate) return " WITH (XLOCK)";
		return "";
	}
}
