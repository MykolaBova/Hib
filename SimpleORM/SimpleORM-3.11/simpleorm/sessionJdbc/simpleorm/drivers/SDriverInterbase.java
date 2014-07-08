package simpleorm.drivers;

import simpleorm.dataset.SFieldScalar;
import simpleorm.sessionjdbc.SDriver;

/**
 * Borland's db.
 * 
 * @author Richard Schmidt
 */
public class SDriverInterbase extends SDriver {
	protected String driverName() {
		return "InterClient";
	}

	protected void addNull(StringBuffer sql, SFieldScalar fld) {

		// SLog.slog.log( "addNull" + sql.toString() + " " +
		// fld.toLongerString());
		if (fld.isPrimary() || fld.isMandatory())
			sql.append(" NOT NULL");
		// not needed for interbAW
		// else
		// sql.append(" NULL");

	}

	// ToDO Need to fix TimeStamp
	// Change how table is generated.
	// How to generate a timestamp.
	protected String wholeColumnSQL(SFieldScalar fld) {

		String sql = super.wholeColumnSQL(fld);
		// SLog.slog.log( sql);
		// SLog.slog.log( fld.toLongerString());

		return sql;
	}
}
