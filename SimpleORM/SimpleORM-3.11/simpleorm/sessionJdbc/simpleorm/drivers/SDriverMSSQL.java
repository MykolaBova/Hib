package simpleorm.drivers;

import simpleorm.dataset.SFieldLong;
import simpleorm.dataset.SFieldScalar;
import simpleorm.sessionjdbc.SDriver;

/**
 * This contains MS SQL Server specific code.
 * 
 * CHAR, VARCHAR max 8,000. TEXT 2^31. NVARCHAR/NCHAR UTF16, max size 4,000.
 */

public class SDriverMSSQL extends SDriver {

	protected String driverName() {
		return "SQLServer";
	}

	/*
	 * paging selectSQL(...) { 0 = SQL string 1 = Page int -> LastUnwantedRecord
	 * 2 = RecsPerPage int 3 = unique column 4 = Sort Select TOP @RecsPerPage *
	 * FROM (@SQL) T WHERE T.@ID NOT IN (select TOP ((@RecsPerPage*(@Page-1))
	 * @ID FROM (@SQL) T9 ORDER BY @Sort) order by @Sort
	 * 
	 * Params : String pageid, int pageindex, int recsperpage ) / // for now we
	 * can only page if there is only one primary key SFieldMeta sfmKey = null;
	 * int iPKeyCount = 0; for (int i = 0; i < from.keySFieldMetas.size(); i++) {
	 * sfmKey = (SFieldMeta)from.keySFieldMetas.get(i); if
	 * (sfmKey.sFieldReference == null) iPKeyCount++; } if (iPKeyCount != 1)
	 * throw new SException.Error("Cannot page a query that has multiple primary
	 * keys (for now) " + this);
	 * 
	 * String strSQL = "SELECT TOP {2,number,integer} * FROM ({0}) T WHERE T.{3}
	 * NOT IN " + "(select TOP {1,number,integer} {3} FROM ({0}) T9 {4}) {4}";
	 * Object[] args = { ret.ToString(), sps.getOffset(); sps.getLimit();
	 * 
	 * new Long(recsperpage * (pageindex - 1)), new Long(recsperpage),
	 * sfmKey.getString(sfmKey.SCOLUMN_NAME), " ORDER BY " + (orderBy != null ?
	 * orderBy : sfmKey.getString(sfmKey.SCOLUMN_NAME)) }; return
	 * java.text.MessageFormat.format(strSQL,args); / * paging
	 */

	/** Apparantly need DATETIME instead of TIMESTAMP */
	@Override protected String columnTypeSQL(SFieldScalar field, String defalt) {
		// TODO review types
		if (defalt.equals("TIMESTAMP"))
			return "DATETIME";
		else if (defalt.equals("DATE"))
			return "DATETIME";
		else if (defalt.equals("TIME") )
			return "DATETIME";
		else if (field instanceof SFieldLong)
			return "BIGINT";
		else if (defalt.equals("BYTES") )
			return "IMAGE"; // Ie. just a byte array.
		else
			return defalt;
	}

	protected String forUpdateSQL(boolean forUpdate) {
		return "";
	}

	/**
	 * It is unclear how MS SQL 2005 does snapshot locking -- very complex, 
     * needs experementation.
     * http://msdn.microsoft.com/en-us/library/ms345124.aspx<p>
     * 
     * Need for XLOCK is unclear.
	 * However, Jorge says that it is unnecessary and makes SimpleORM fail, but
	 * it works for me.
	 * 
	 * @see SDriver#forUpdateSQL for discussion.
	 * 
	 */
	protected @Override String postFromSQL(boolean forUpdate) {
//		if (unrepeatableRead)
//			return " WITH (NOLOCK)";
//		else 
         if (forUpdate && xLockEnabled)
			return " WITH (XLOCK)";
		return "";
	}

	boolean xLockEnabled = false;

	/**
	 * Enables WITH (XLOCK) for updatable queries. Should reduce deadlocks but
	 * apparantly can cause bugs. (Not supported in SQL 7? Something about
	 * HOLDLOCK?)
	 */
	public void setXLockEnabled(boolean xlock) {
		xLockEnabled = xlock;
	}

	public boolean getXLockEnabled() {
		return xLockEnabled;
	}

	public String alterTableAddColumnSQL(SFieldScalar field) {
		StringBuffer sql = new StringBuffer();

		sql.append("\nALTER TABLE ");
        appendTableName(field.getRecordMeta(), sql);
		sql.append(" ADD ");
		sql.append(wholeColumnSQL(field));
		sql.append(clauseSeparator("    "));

		return sql.toString();
	}

	// ### We need a key generator here!
}
