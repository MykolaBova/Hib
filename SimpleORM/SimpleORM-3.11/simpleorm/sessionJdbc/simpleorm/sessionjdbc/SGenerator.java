package simpleorm.sessionjdbc;

import simpleorm.dataset.*;
import simpleorm.sessionjdbc.SSessionJdbc;
import simpleorm.utils.SException;

/**
 * Generate keys using rows in a separate sequence table. This should be done in
 * a separate transaction to avoid locking problems.<p>
 * 
 * One instance per SFieldMeta, ie. not per session or per driver.
 */

abstract public class SGenerator {
    SFieldScalar keyField;
	SRecordMeta<?> record;
	String generatorName = null;

	SGenerator(SFieldScalar field, String name) {
        this.keyField = field;
		this.record = field.getRecordMeta();
		this.generatorName = name;
	}
	
    public static  void setNewGenerator(SFieldScalar fld) {
        SGenerator gen = fld.getGenerator();
        if (gen == null) {
            if (fld.getGeneratorMode() == null)
                throw new SException.Error("No generator mode specified for " + fld);
            switch (fld.getGeneratorMode()) {
            case SSELECT_MAX: 
                gen = new SGeneratorSelectMax(fld, (String)fld.getGeneratorParameter()[0]); break;
            case SSEQUENCE: 
                gen = new SGeneratorSequence(fld, (String)fld.getGeneratorParameter()[0]); break;
            case SINSERT: 
                gen = new SGeneratorInsertIdentity(fld); break;
            }        
        fld.setGenerator(gen);
        }
    }

	public String getName() {
		return generatorName;
	}

	/**
	 * Create a new record with a generated key.
     * Overridden for SINSERT
	 */
	SRecordInstance createWithGeneratedKey(
        SSessionJdbc session, SRecordMeta<?> meta) {

		if (meta != record)
			throw new SException.Error("Inconsistent record metas " + record + " !=" + meta);
		// / Generate the key.
		long gened = generateKey(session, meta,keyField);

		// / Create the new record.
		SRecordInstance newRec = session.create(meta, new Long(gened));

		if (session.getLogger().enableQueries())
			session.getLogger().queries("createWithGeneratedKey: " + newRec);

		return newRec;
	}

	/**
	 * Update instance with a newly generated key before an INSERT. 
     * For example, when reattaching a record. 
     * Not used for SINSERT.
	 */
	void preUpdateWithGeneratedKey(SSessionJdbc session, SRecordInstance instance) {

		SRecordMeta<?> meta = instance.getMeta();
		if (meta != record)
            throw new SException.Error("Inconsistent record metas " + record + " !=" + meta);

		// / Generate the key.
		long gened = generateKey(session, meta,keyField);

		instance.setLong(keyField, gened);

		if (session.getLogger().enableQueries())
			session.getLogger().queries("updateWithGeneratedKey: " + instance);
	}
    
    /** For SINSERT */
    void postUpdateWithGeneratedKey(SSessionJdbc session, SRecordInstance instance) {}

    /** Not used for SINSERT */
	public long generateKey(SSessionJdbc session, SRecordMeta<?> meta, SFieldScalar keyField){
        throw new SException.Error("generateKey not implemented for " + keyField);}

	/**
	 * returns DDL required to support number generation, Eg. "CREATE SEQUENCE
	 * FOO..." Returns a string rather than just doing it so that the caller can
	 * create a DDL file if they want to.
	 */
	public String createDDL(SSessionJdbc session) {
		return null;
	}

	public String dropDDL(SSessionJdbc session) {
		return null;
	}
	
	SRecordMeta<?> getRecordMeta() {
		return record;
	}
    
    public String toString(){return "[Generator " + keyField + ": " + keyField.getGeneratorMode() + "]";
    }
}
