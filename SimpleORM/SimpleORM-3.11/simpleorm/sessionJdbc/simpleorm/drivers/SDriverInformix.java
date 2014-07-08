package simpleorm.drivers;

import simpleorm.sessionjdbc.SDriver;



/**
 * This contains Informix specific code.
 */

public class SDriverInformix extends SDriver {

	protected String driverName() {
		return "Informix JDBC Driver for Informix Dynamic Server";
	}

}
