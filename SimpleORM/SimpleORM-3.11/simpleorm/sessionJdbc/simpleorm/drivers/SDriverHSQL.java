package simpleorm.drivers;

import simpleorm.sessionjdbc.SDriver;

/**
 * This contains HSQL specific code. Note that this toy database does not
 * support locking, so is not safe in multi user mode even with optimistic
 * locking. See {@link SDriver#supportsLocking}.
 */

public class SDriverHSQL extends SDriverHSQLH2 {

	protected String driverName() {
		return "HSQL Database Engine Driver";
	}

}
