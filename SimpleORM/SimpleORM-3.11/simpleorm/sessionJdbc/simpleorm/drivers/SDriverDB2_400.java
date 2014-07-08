package simpleorm.drivers;


/**
 * Contains DB2/400 (iSeries DB2/Implementation tested on OS400 v5.1)
 */
public class SDriverDB2_400 extends SDriverDB2 {
	protected String driverName() {
		return "AS/400 Toolbox for Java JDBC Driver";
	}

}
