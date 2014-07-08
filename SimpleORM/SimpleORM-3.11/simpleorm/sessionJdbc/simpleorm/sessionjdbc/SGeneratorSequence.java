package simpleorm.sessionjdbc;

import simpleorm.dataset.SFieldScalar;
import simpleorm.dataset.SRecordMeta;

/**
 * Generator using seperate SEQUENCE objects for databases that support them.
 * Common method for Oracle.
 */
class SGeneratorSequence extends SGenerator {

	SGeneratorSequence(SFieldScalar field, String name) {
		super(field, name);
	}

	public long generateKey(SSessionJdbc session, SRecordMeta<?> meta, SFieldScalar keyField) {
		long key = session.getDriver().generateKeySequence(meta, keyField);
		return key;
	}

	private String sequenceName() {
		String name = (String)keyField.getGeneratorParameter()[0];
		return name;
	}

	@Override public String createDDL(SSessionJdbc session) {
		return session.getDriver().createSequenceDDL(getName());
	}

	@Override public String dropDDL(SSessionJdbc session) {
		return session.getDriver().dropSequenceDDL(sequenceName());
	}
}