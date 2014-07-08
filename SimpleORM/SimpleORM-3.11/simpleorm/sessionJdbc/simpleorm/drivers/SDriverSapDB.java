package simpleorm.drivers;

import simpleorm.sessionjdbc.SDriver;



/**
 * Alix Jermyn
 * 
 * Sorry Anthony, the Driver class is com.sap.dbtech.jdbc.DriverSapDB
 * 
 * And db urls look like jdbc:sapdb://<Host_Machine>/>databasename>
 * 
 * To add a further twist, SAPDB can be put into 4 distinct Sql modes: INTERNAL,
 * ANSI, DB2, or ORACLE
 * 
 * Internal is the default, which seems to be the most commonly used. If you can
 * update Sdriver appropriately, and give me a starting sub class of Sdriver for
 * sapdb, I can do some testing with it when working on the Key Generator. I
 * haven't used simpleorm with sapdb yet, but my impression of Sapdb after one
 * month's use is that is pretty ANSI 92 compliant, and the generic driver would
 * work for most if not all standard sql commands.
 * 
 * 
 */
public class SDriverSapDB extends SDriver {

	protected String driverName() {
		return ""; // ### Don't know much about SapDB yet.
	}

}
