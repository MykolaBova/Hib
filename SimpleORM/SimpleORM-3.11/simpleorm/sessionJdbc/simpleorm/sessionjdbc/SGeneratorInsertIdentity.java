package simpleorm.sessionjdbc;

import simpleorm.dataset.SDataSet;
import simpleorm.dataset.SFieldScalar;
import simpleorm.dataset.SRecordInstance;
import simpleorm.dataset.SRecordMeta;
import simpleorm.utils.SException;

/**
 * Generator using Identity columns which have values added by the database when
 * rows are inserted. Only supported by some dbs, notably MSSQL.
 */
public class SGeneratorInsertIdentity extends SGenerator {

	public SGeneratorInsertIdentity(SFieldScalar field) {
		super(field, "InsertIdentity");
	}

    
    	SRecordInstance createWithGeneratedKey(SSessionJdbc session, SRecordMeta<?> meta) {

		if (meta != record)
			throw new SException.Error("Inconsistent record metas " + record + " !=" + meta);

        SDataSet ds = session.getDataSet();
        
		// / Create the new record.
		SRecordInstance newRec = ds.createWithNullKey(meta);

		return newRec;
	}
        
        
	/**
	 * Update instance with a newly generated key before an INSERT. 
     * For example, when reattaching a record. 
     * Not used for SINSERT.
	 */
	void preUpdateWithGeneratedKey(SSessionJdbc session, SRecordInstance instance) {}

    
    /** For SINSERT */
    void postUpdateWithGeneratedKey(SSessionJdbc session, SRecordInstance instance) {
        SRecordMeta<?> meta = instance.getMeta();
		if (meta != record)
            throw new SException.Error("Inconsistent record metas " + record + " !=" + meta);

		long key = session.getDriver().retrieveInsertedKey(meta, keyField);
               
		instance.setLong(keyField, key);

		if (session.getLogger().enableQueries())
			session.getLogger().queries("updateWithGeneratedKey: " + instance);
	}
}