package simpleorm.drivers;


import simpleorm.dataset.SFieldScalar;
import simpleorm.dataset.SRecordMeta;
import simpleorm.sessionjdbc.SDriver;
import simpleorm.sessionjdbc.SGenerator;

/**
 * This contains Oracle specific code.
 * 
 * In oracle VARCHAR2 '' == null. Ie. min string length is 2. VARCHAR ==
 * VARCHAR2? but not recommended? VARCHARs max size 2000 or 4000 Oracle 8. CHARS
 * max size 2000 all versions. LONG for larger strings.
 * 
 * Dates DATE, TIMESTAMP [WITH TIMEZONE].
 * 
 * It is reported that CHARs need to be trimed for comparison?
 */

public class SDriverOracle extends SDriver {

	protected String driverName() {
		return "Oracle JDBC driver";
	}

	public int maxIdentNameLength() {
		return 30;
	} // Correct for Oracle.

	/** Specializes SDriver.generateKeySequence using Oracle SEQUENCEs. */
	protected long generateKeySequence(SRecordMeta<?> rec, SFieldScalar keyFld) {
		Object sequenceName = ((SGenerator)keyFld.getGenerator()).getName();

		String qry = "SELECT " + (String) sequenceName + ".NEXTVAL FROM DUAL";
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

	/**
	 * 
	 */
	@Override protected String columnTypeSQL(SFieldScalar field, String defalt) {
		if (defalt.equals("BYTES"))
			return "RAW (" + field.getMaxSize() + ")" ; // Ie. just a byte array. Use SFieldBlob for BLOB.
			// if (dataType.equals("INTEGER"))
			// return "numeric(9,0)";
			// else if ( dataType.equals("TIMESTAMP") )
			// return "DATE"; // Is this correct ##
		else if (defalt.startsWith("TIME"))
			return "TIMESTAMP";
		else if (defalt.startsWith("VARCHAR") )
			return "VARCHAR2(" + field.getMaxSize() + ")";
		else
			return defalt;
	}
	
	protected OffsetStrategy getOffsetStrategy() {
		return OffsetStrategy.JDBC;
	}
    
//    // Paging Proposal by "Dennis Xi" <xitx@hotmail.com>
//    In SDriver.java,
// 
// protected String selectSQL(
//      SFieldMeta[] select, SRecordMeta from, String where,
//      String orderBy, boolean forUpdate, SRecordMeta[] joinTables, SQuery[] loadTables,
//      boolean distinct, long pageindex, long recsperpage) {
//      // By default, paging is not supported.
//      return selectSQL( select, from, where, orderBy, forUpdate, joinTables, loadTables, distinct );
//  }
// 
// 
//In SDriverOracle.java, add
// 
// protected String selectSQL(
//        SFieldMeta[] select, SRecordMeta from, String where,
//        String orderBy, boolean forUpdate, SRecordMeta[] joinTables, SQuery[] loadTables,
//        boolean distinct, long pageindex, long recsperpage )
//    {
//        // No support FOR UPDATE, because it makes nosense
//        StringBuffer ret = new StringBuffer ( 1000 );
//        ret.append( "SELECT * " );
//        ret.append( " FROM ( SELECT a.*, rownum r FROM ( ");
//        ret.append( selectSQL( select, from, where, orderBy, false, joinTables, loadTables, distinct ) );
//        ret.append( " ) a ");
//        ret.append( "WHERE rownum <= " + ( pageindex * recsperpage ) + " ) ");
//        ret.append( " WHERE r > " + ( ( pageindex - 1) * recsperpage ));
// 
//        return ret.toString();
//    }
 

}
