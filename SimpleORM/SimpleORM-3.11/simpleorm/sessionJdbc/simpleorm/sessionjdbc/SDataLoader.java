package simpleorm.sessionjdbc;

import simpleorm.utils.*;
import java.util.ArrayList;

import java.util.List;
import simpleorm.dataset.SFieldScalar;
import simpleorm.dataset.SQueryMode;
import simpleorm.dataset.SRecordInstance;
import simpleorm.dataset.SRecordMeta;

/**
 * This class provides simple bulk data loading facilities which are
 * particularly handy for developing test data. It is used as follows:-
 * 
 * <xmp> SSession.rawJDBC("DELETE FROM XX_EMPLOYEE");
 * 
 * SDataLoader<Employee> empDL = new SDataLoader(Employee.meta);
 * 
 * Employee e1 = empDL.insert("100", "One00", "123 4567", "50000"});
 * 
 * empDL.insert(new String[][]{ 
 *   {"200", "Two00", "123 4567", "50000"}, 
 *   {"200", "Two00", "123 4567", "50000"}, 
 *   {"300", "Three00", "123 4567", "50000"}});
 * </xmp>
 */

public class SDataLoader<T extends SRecordInstance> {//implements SSimpleORMProperties {

    SSessionJdbc session;
    
	SRecordMeta<T> sRecordMeta = null;

	SFieldScalar[] sFieldMetas = null;

	SFieldScalar[] keyMetas = null;

	/**
	 * A data loader for record <code>sRecordMeta</code> will load
	 * <code>fields</code>. The primary key fields are always implicitly
	 * included at the beginning and need not be repeated here. The default for
	 * <code>fields</code> is all fields except those flagged
	 * <code>SQY_UNQUERIED</code>.
	 */
	public SDataLoader(SSessionJdbc session, SRecordMeta<T> meta, SFieldScalar[] fields) {
        this.session = session;
		this.sRecordMeta = meta;
        List<SFieldScalar> flds = new ArrayList();
        List<SFieldScalar> keys = new ArrayList();
        for (SFieldScalar fld: fields) {
            if (fld.getGeneratorMode() == null) {
                flds.add(fld);
                if (fld.isPrimary())
                    keys.add(fld);
            }
        }
        sFieldMetas = flds.toArray(new SFieldScalar[0]);
        keyMetas = keys.toArray(new SFieldScalar[0]);
	}

	public SDataLoader(SSessionJdbc session, SRecordMeta<T> meta) {
		// TODO what about generated non primary keys ?
		this(session, meta, meta.getQueriedScalarFields());
	}

      
	/**
	 * Inserts (or updates) one record in the databases, and returns it. The
	 * first element(s) in <code>record</code> contain the primary key
	 * field(s), the rest contain the public
	 */        
     public SRecordInstance insertRecord(Object... record) {
		// / Primary Key
		if (record == null || record.length != sFieldMetas.length) {
			throw new SException.Error(sFieldMetas.length
					+ " columns required in parameter array.");
		}

		// / Set Keys and Query
		T rec = null;
		int nrKeys = keyMetas.length;
		if (nrKeys > 0) {
			// Object[] keys = new Object[nrKeys];
			// for (int kx=0; kx<nrKeys; kx++)
			// keys[kx] = record[kx];

//			for (int i = 0; i < keyMetas.length; i++) {
//				SFieldMeta fk = keyMetas[i];
//				if (fk instanceof SFieldScalar) {
//					nrScalarKeys++;
//				} else {
//					nrScalarKeys += ((SFieldReference) fk)
//							.getForeignKeys().size();
//				}
//			}
			Object[] keys = new Object[nrKeys];
			int index = 0;
			for (int kx = 0; kx < nrKeys; kx++) {
//				if (keyMetas[kx] instanceof SFieldScalar) {
					keys[index] = record[kx];
					index++;
			}
//				} else {
//					SRecordInstance recInst = (SRecordInstance) record[kx];
//					Iterator it = recInst.getMeta().getPrimaryKeys()
//							.iterator();
//					while (it.hasNext()) {
//						SFieldMeta pkf = (SFieldMeta) it.next();
//						keys[index] = recInst.getObject(pkf);
//						index++;
//					}
//				}
//			}
            //System.err.println("IRKeys " + SUte.arrayToString(keys));
			rec = session.findOrCreate(sRecordMeta, sFieldMetas, SQueryMode.SFOR_UPDATE, keys);
		} else {
			rec = session.createWithGeneratedKey(sRecordMeta);
		}

		// / Set column Values
		int cx = 0;
		for (SFieldScalar fld : sFieldMetas) {
			if ( ! fld.isPrimary()) {
				rec.setObject(fld, record[cx]);
			}
			cx++;
		}
		return rec;
	}

	/**
	 * Conveniently inserts multiple records in one go. Each inner array is
	 * simply passed to <code>insert(String[])</code>.
	 */
	public SRecordInstance[] insertRecords(Object[][] records) {
		SRecordInstance[] res = (SRecordInstance[]) java.lang.reflect.Array
				.newInstance(sRecordMeta.getUserClass(), records.length);
		for (int x = 0; x < records.length; x++) {
			res[x] = insertRecord(records[x]);
		}
		return res;
	}

}
