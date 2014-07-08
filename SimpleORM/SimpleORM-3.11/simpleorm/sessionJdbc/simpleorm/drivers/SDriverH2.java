/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package simpleorm.drivers;

/**
 * This contains H2 specific code. 
 * Cree par Harry Karadimas a 9 sept. 08
 */

public class SDriverH2 extends SDriverHSQLH2 {

	protected String driverName() {
		return "H2 JDBC Driver";
	}

}

