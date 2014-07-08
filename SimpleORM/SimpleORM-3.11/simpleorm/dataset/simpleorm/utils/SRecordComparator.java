package simpleorm.utils;

import java.io.Serializable;
import java.util.Comparator;

import simpleorm.dataset.SFieldReference;
import simpleorm.dataset.SFieldScalar;
import simpleorm.dataset.SRecordInstance;
import simpleorm.dataset.SRecordMeta;

/**
 * Comparator based on the value of each field.
 * 
 * <em>Beware, this compartor is not strictly consistent with equals<em>, as two records with null keys
 * are said to be NOT equal, while comparing the same records on this null key will return 0.
 * @author Franck Routier aka alci
 *
 * @param <T>
 */
public class SRecordComparator<T extends SRecordInstance> implements Comparator<T>, Serializable {

	private static final long serialVersionUID = 1L;
	private SFieldScalar[] fields;
	private int reverseFactor = 1;
	
	public SRecordComparator(SFieldScalar... flds) {
		this(false, flds);
	}
	public SRecordComparator(boolean reverse, SFieldScalar... flds) {
		this.fields = flds;
		if (reverse) reverseFactor = -1;
	}
	/** Creates a comprator based on primary keys */
	public SRecordComparator(SRecordMeta<T> meta) {
		this(false, meta.getPrimaryKeys());
	}
	public SRecordComparator(boolean reverse, SRecordMeta<T> meta) {
		this(reverse, meta.getPrimaryKeys());
	}
	public SRecordComparator(SFieldReference<?> fld) {
		this(false, fld);
	}
	public SRecordComparator(boolean reverse, SFieldReference<?> fld) {
		this.fields = fld.getForeignKeyMetas().toArray(new SFieldScalar[0]);
		if (reverse) reverseFactor = -1;
	}
	
	@Override
	public int compare(T inst, T other) {
		// if other is null, consider it greater than inst
		if (other == null)
			return -1;
		for (SFieldScalar fld : fields) {
			int res = fld.compareField(inst, other);
			if (res != 0)
				return res*reverseFactor;
		}
		return 0;
	}

}
