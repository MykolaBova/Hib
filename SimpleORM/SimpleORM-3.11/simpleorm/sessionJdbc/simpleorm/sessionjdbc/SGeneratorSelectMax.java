package simpleorm.sessionjdbc;

import simpleorm.dataset.SFieldScalar;
import simpleorm.dataset.SRecordMeta;

/**
 * Basic Select Max style generator.
 */
class SGeneratorSelectMax extends SGenerator {

	SGeneratorSelectMax(SFieldScalar  field, String name) {
		super(field, name);
	}

	public long generateKey(SSessionJdbc session,SRecordMeta meta, SFieldScalar keyField) {
		long key = session.getDriver().generateKeySelectMax(meta, keyField);
		return key;
	}
}