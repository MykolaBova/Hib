package simpleorm.examples;

import simpleorm.dataset.SDataSet;
import simpleorm.dataset.SFieldDouble;
import simpleorm.dataset.SFieldInteger;
import simpleorm.dataset.SFieldString;
import simpleorm.dataset.SRecordInstance;
import simpleorm.dataset.SRecordMeta;
import simpleorm.dataset.SFieldFlags;
import simpleorm.dataset.SFieldLong;
import simpleorm.sessionjdbc.SGenerator;
import simpleorm.sessionjdbc.SSessionJdbc;
import simpleorm.sessionjdbc.SDataLoader;
import simpleorm.utils.SException;
import simpleorm.utils.SLog;

import static simpleorm.dataset.SGeneratorMode.*;

/**
 * Demonstrated Generated Primary Keys 
 * (ie. their value is set automatically to max+1 or similar.)
 */
public class GeneratedKeyTest {

	/** This test class defines the Invoice table which has a generated InvoiceNr. */
	public static class Invoice extends SRecordInstance {

//		public Invoice(boolean attached){
//			super(attached);
//		}
		public static final SRecordMeta<Invoice> meta = new SRecordMeta(Invoice.class,	"XX_INVOICE");

		public static final SFieldLong INVOICE_NR = (SFieldLong) new SFieldLong(
            meta, "INVOICE_NR", SFieldFlags.SPRIMARY_KEY)
             .setGeneratorMode(SSELECT_MAX,  "invoice_seq"); // sequence tested too.

		// Add SSEQUENCE_NAME.pval(Boolean.FALSE) to suppress Postgres SEQUENCES

		public static final SFieldString NAME = new SFieldString(meta, "INAME",	40, SFieldFlags.SDESCRIPTIVE);

		public static final SFieldDouble VALUE = new SFieldDouble(meta, "IVALUE");

		public SRecordMeta<Invoice> getMeta() {
			return meta;
		};

		boolean validated; // just for unit test.

		public void onValidateRecord() {
			SLog.getSessionlessLogger().fields("Validated.record " + this);
			validated = true;
		}
	}

	public static void main(String[] argv) throws Exception {
		SSessionJdbc ses = TestUte.initializeTest(GeneratedKeyTest.class); // Look at this code.
		try {
			// / first using the default Select Max method
			genTest();

			// / Now using Sequences
			if (ses.getDriver().supportsKeySequences()) {
				Invoice.INVOICE_NR.setGeneratorMode(SSEQUENCE, "invoice_seq");
				ses.begin();
				ses.commit();

				genTest();
			}
            
            if (ses.getDriver().supportsInsertKeyGeneration()) {
				Invoice.INVOICE_NR.setGeneratorMode(SINSERT);
				ses.begin();                   
				ses.commit();

				genTest(); // Recreates table                
            }

		} finally {
			SSessionJdbc.getThreadLocalSession().close();
		}
	}

	/** Basic examples/tests not involving foreign keys. */
	static void genTest() throws Exception {

        SLog.getSessionlessLogger().message("\n################# genTest " + Invoice.INVOICE_NR.getGeneratorMode() + " ##################");
		// / (re)create tables
		SSessionJdbc ses = SSessionJdbc.getThreadLocalSession();
		ses.begin();
		TestUte.dropTableNoError(ses, "XX_INVOICE");
		ses.rawUpdateDB(ses.getDriver().createTableSQL(Invoice.meta));
		ses.commit();
       
		SGenerator.setNewGenerator(Invoice.INVOICE_NR);
        SGenerator gen = Invoice.INVOICE_NR.getGenerator();

		// (SGeneratorMeta)Invoice.INVOICE_NR.getProperty(SGENERATED_KEY);
        String cddl=gen.createDDL(ses);
        if (cddl != null) {
            ses.begin();
            try {
            ses.rawUpdateDB(gen.dropDDL(ses));
            } catch (Exception Ex) {
            	ses.commit();
            	ses.begin();
            }
            ses.rawUpdateDB(cddl);
            ses.commit();
        }
		ses.begin();

		// / Create First
		Invoice inv1 = ses.createWithGeneratedKey(Invoice.meta);
		inv1.validated = false;
		ses.flush();
		TestUte.assertTrue(inv1.validated);
		inv1.setString(inv1.NAME, "First");
		inv1.setDouble(inv1.VALUE, 123);
		long key1 = inv1.getLong(inv1.INVOICE_NR);
		inv1.validated = false;
		ses.flush();
		TestUte.assertTrue(inv1.validated);

		Invoice inv1a = ses.findOrCreate(Invoice.meta, new Long(key1));
		TestUte.assertEqual(inv1 + "", inv1a + "");

		// / Create some Records using the SDataLoader
		SDataLoader<Invoice> invoiceDL = new SDataLoader<Invoice>(ses, Invoice.meta);
		invoiceDL.insertRecords(new Object[][] { { "Second", "234" },
				{ "Third", "345" } });
		ses.commit();

		// Check Result
		ses.begin();
		Invoice inv3a = ses.findOrCreate(Invoice.meta, new Long(key1 + 2));
		TestUte.assertEqual(inv3a.getString(inv3a.NAME), "Third");

		Object sum = ses
				.rawQuerySingle("SELECT SUM(IVALUE) FROM XX_INVOICE",true);
		if (((Number) sum).intValue() != (123 + 234 + 345))
			throw new SException.Test("Bad Value sum " + sum);
        
		ses.commit();

        ///////// Detached generation ////////////
        
        SDataSet ds = new SDataSet();
//        System.err.println("\n\n========= cwnk ===========\n");
        Invoice invd1 = ds.createWithNullKey(Invoice.meta);
        invd1.setString(invd1.NAME, "Remote Gened");
        
        ses.begin(ds);
        ses.flush();
        ses.getLogger().message("Gened Detached " + invd1.allFields());
        TestUte.assertEqual(invd1, ses.mustFind(Invoice.meta, invd1.getLong(invd1.INVOICE_NR)));
        ses.commit();
	}
}
