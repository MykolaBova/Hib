package simpleorm.drivers;

import simpleorm.dataset.SFieldScalar;
import simpleorm.dataset.SRecordMeta;
import simpleorm.sessionjdbc.SDriver;
import simpleorm.sessionjdbc.SGenerator;

/**
 * This contains PostgreSQL specific code.
 * 
 * VARCHAR without size means unlimited. No artificial upper size? TEXT has the
 * same behaviour as VARCHAR, apparantly.
 * 
 */

public class SDriverPostgres extends SDriver {

	protected String driverName() {
		return "PostgreSQL Native Driver";
	}

	public int maxIdentNameLength() {
		return 60;
	} // I think that this is correct.

	/** Never need to specify byteSize for varchar */
	@Override protected String columnTypeSQL(SFieldScalar field, String defalt) {
		if (defalt.startsWith("VARCHAR") )
			return "VARCHAR"; // No max size necessary or useful for Postgres
		else if (defalt.equals("BYTES"))
			return "BYTEA"; // Ie. just a byte array.
		else
			return defalt;
	}

	/** Specializes SDriver.generateKeySequence using the Postgres SEQUENCEs. */
	protected long generateKeySequence(SRecordMeta<?> rec, SFieldScalar keyFld) {
   		Object sequenceName = ((SGenerator)keyFld.getGenerator()).getName();

		String qry = "SELECT nextval('" + (String) sequenceName + "')";
		Object next = getSession().rawQuerySingle(qry, false);

		if (next == null)
			return 0;
		if (next instanceof Number)
			return ((Number) next).longValue();
		else
			return Long.parseLong(next.toString());
	}

	public boolean supportsKeySequences() {
		return true;
	}

	protected String createSequenceDDL(String name) {
		return "CREATE SEQUENCE " + name;
	}

	protected String dropSequenceDDL(String name) {
		return "DROP SEQUENCE " + name;
	}

	protected OffsetStrategy getOffsetStrategy() {
		return OffsetStrategy.QUERY;
	}
	protected String limitSQL(long offset, long limit) {
		return " LIMIT "+ limit + " OFFSET "+offset;
	}
}
