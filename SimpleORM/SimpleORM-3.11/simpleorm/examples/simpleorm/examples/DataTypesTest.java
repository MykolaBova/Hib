package simpleorm.examples;

import java.math.BigDecimal;

import simpleorm.dataset.SFieldBigDecimal;
import simpleorm.dataset.SFieldBooleanChar;
import simpleorm.dataset.SFieldBytes;
import simpleorm.dataset.SFieldDate;
import simpleorm.dataset.SFieldInteger;
import simpleorm.dataset.SFieldLong;
import simpleorm.dataset.SFieldString;
import simpleorm.dataset.SFieldTime;
import simpleorm.dataset.SFieldTimestamp;
import simpleorm.dataset.SRecordInstance;
import simpleorm.dataset.SRecordMeta;
import simpleorm.dataset.SFieldFlags;
import simpleorm.sessionjdbc.SSessionJdbc;
import simpleorm.sessionjdbc.SDataLoader;
import simpleorm.utils.SException;
import simpleorm.utils.SLog;

/**
 * This test class uses all data types including Dates and Times. What makes it
 * hard is the inconsistent mapping by different database vendors to the SQL-92
 * data types.
 */
public class DataTypesTest {

	public static class DataTypes extends SRecordInstance {

//		public DataTypes(boolean attached){
//			super(attached);
//		}
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public static final SRecordMeta<DataTypes> meta 
            = new SRecordMeta<DataTypes>(DataTypes.class,   "XX_DATA_TYPES");

		public static final SFieldString DATA_ID 
            = new SFieldString(meta, "DATA_ID", 20, SFieldFlags.SPRIMARY_KEY)
                .overrideSqlDataType("CHAR (20)");

		// CHAR types typically have COBOL style trailing spaces.

		public static final SFieldString NAME = new SFieldString(meta, "NAME", 20);

		public static final SFieldString EMPTYSTR = new SFieldString(meta, "EMPTYSTR", 20);

		public static final SFieldInteger ANINT = new SFieldInteger(meta, "ANINT");

		public static final SFieldLong ALONG = new SFieldLong(meta, "ALONG");

		public static final SFieldTimestamp TSTAMP = new SFieldTimestamp(meta, "TSTAMP");

		public static final SFieldDate ADATE = new SFieldDate(meta, "ADATE");

		public static final SFieldTime ATIME = new SFieldTime(meta, "ATIME");

		public static final SFieldTimestamp TSTAMP_OBJ = new SFieldTimestamp(meta, "TSTAMP_OBJ");

		// for Create Table

		public static final SFieldBigDecimal ADECIMAL = new SFieldBigDecimal(meta, "ADECIMAL", 22, 4);

		// ie. 123456789012345678.3333, more precission than double.

		public static final SFieldBytes ABYTES = new SFieldBytes(meta, "ABYTES", 100);

		public static final SFieldBooleanChar YNBOOL = new SFieldBooleanChar(meta, "YNBOOL", "Y", "N");

		public SRecordMeta getMeta() {
			return meta;
		}; // specializes abstract method
	}

	public static void main(String[] argv) throws Exception {
		SSessionJdbc ses = TestUte.initializeTest(DataTypesTest.class); // Look at this code
		try {
			testInit();
			dataTest();
		} finally {
			ses.close();
		}
	}

	/** Prepare for tests, Delete old data. */
	static void testInit() throws Exception {
		SSessionJdbc ses = SSessionJdbc.getThreadLocalSession();
		ses.begin();
		// Delete any old data from a previous run.
		try {
			ses.rawUpdateDB("DROP TABLE XX_DATA_TYPES");
		} catch (SException.Jdbc ex) {
			// fro : need to rollback here, or the excpetion blocks the
			// transaction (Postgresql at least)
			ses.rollback();
			ses.begin();
		}
		; // Tables may not exist.
		ses.rawUpdateDB(ses.getDriver().createTableSQL(DataTypes.meta));
		ses.commit();
	}

	static void dataTest() throws Exception {
		SSessionJdbc ses = SSessionJdbc.getThreadLocalSession();
		ses.begin();

		SDataLoader dataDL = new SDataLoader(ses, DataTypes.meta);

		long now = System.currentTimeMillis();
		dataDL.insertRecords(new Object[][] { { "100", "Trailing  ", "",
				"2000000000", "123456789012345678",
				new java.sql.Timestamp(now), new java.util.Date(now),
				new java.sql.Time(now), new java.sql.Timestamp(now),
				"123456789012345678.3333", new byte[] { 1, 3, 5, 7 },
				Boolean.TRUE } });

		ses.commit();
		ses.begin();

		DataTypes dt = ses.findOrCreate(DataTypes.meta, "100                 "); 
        // Trailing spaces REQUIRED for Oracle as CHAR, not VARCHAR
        TestUte.assertTrue(!dt.isNewRow());

		java.sql.Timestamp nowTS = dt.getTimestamp(DataTypes.TSTAMP);
		// The following is necessary because nowTS.getTime() is rounded
		// to 1 second.
		long now2 = nowTS.getTime() / 1000 * 1000 
            // Sometimes (HSql) getTime is in milliseconds, not seconds.
				+ nowTS.getNanos() / 1000000;

		dt.setDate(dt.ADATE, new java.util.Date()); // ie. not java.sql.Date()

		SLog.getSessionlessLogger().message("Retrieved Name '" + dt.getObject(DataTypes.NAME)
				+ "' TStamp " + dt + nowTS + "(" + nowTS.getTime() + ", "
				+ nowTS.getNanos() + "<-" + now + ") "
				+ new java.util.Date(now) + " " + now + " "
				+ new java.util.Date(now2) + " " + now2 + ", Date "
				+ dt.getDate(DataTypes.ADATE) + ", Time "
				+ dt.getTime(DataTypes.ATIME) + ", Object "
				//+ dt.getObject(DataTypes.TSTAMP_OBJ).getClass().getName()
				+ ", BDec " + dt.getBigDecimal(DataTypes.ADECIMAL) + ", byte "
				+ dt.getBytes(DataTypes.ABYTES)[2] + ", YNBool "
				+ dt.getBoolean(DataTypes.YNBOOL));

		TestUte.assertTrue(dt.getObject(DataTypes.NAME).toString().equals("Trailing  "));
		TestUte.assertTrue(dt.getString(DataTypes.NAME).equals("Trailing")); // trimed
		// TestUte.assertTrue(dt.getString(DataTypes.EMPTYSTR).equals(""));
		TestUte.assertTrue(dt.isNull(DataTypes.EMPTYSTR) 
            // Oracle returnsnull!, '' === NULL!
				|| dt.getString(DataTypes.EMPTYSTR).equals(""));

		TestUte.assertEqual("" + (now2 + 50) / 100, "" + (now + 50) / 100);
		// ## Postgresql seems to loose the last few digits precission!
		// HSQL works exactly. Other dbs unknown (JDBC/DB bug).
		TestUte.assertTrue(dt.getInt(DataTypes.ANINT) == 2000000000);
		TestUte.assertTrue(dt.getLong(DataTypes.ALONG) == 123456789012345678L);
		BigDecimal decim = dt.getBigDecimal(DataTypes.ADECIMAL); 
		TestUte.assertTrue(dt.getBigDecimal(DataTypes.ADECIMAL).equals(
				new java.math.BigDecimal("123456789012345678.3333"))); // Precisely
		TestUte.assertEqual(dt.getBytes(DataTypes.ABYTES)[2] + "", "5");

		TestUte.assertTrue(dt.getBoolean(DataTypes.YNBOOL));
		ses.commit();
	}
}
