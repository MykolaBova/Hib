package simpleorm.dataset;

import simpleorm.utils.SException;

/**
 * Handles a table that is part of the query. This class
 * will hold the table alias, the selected fields, and the record
 * type. If the table is added as a join (as opposed to the main query table)
 * it will also handle the join type and the table it is joined to.
 * Used to generate the select and join parts of a query.
 * 
 * @author franck routier aka alci
 */
public class SQueryTable<RI extends SRecordInstance> {
	
	public enum JoinType {INNER, LEFT, RIGHT, FULL, MAIN_TABLE, RAW_TABLE};
	
	String tableAlias;
	private SRecordMeta<RI> rmeta;
	private SFieldScalar[] selectList;
	// indicates the place where the first field of this alias will be in the resultSet
	private int firstFieldIndex;
	
	private SQueryTable<?> fromTable = null;
	private SFieldReference<?> reference = null;
	private JoinType joinType = JoinType.MAIN_TABLE;
	private String rawOnClause = null;
	
	public SQueryTable(String alias, SRecordMeta<RI> meta, int firstIndex) {
		this(alias, meta, null, firstIndex);
	}
	
	public SQueryTable(String alias, SRecordMeta<RI> meta, SFieldScalar[] list, int firstIndex) {
		this.rmeta = meta;
		this.firstFieldIndex = firstIndex;
		this.setAlias(alias);
		if (list != null) {
			this.setSelectList(list);
		}
		else {
			this.setSelectMode(SSelectMode.SNORMAL);
		}
	}
	
	SQueryTable<RI> setJoin(SQueryTable.JoinType type, SQueryTable<?> from, SFieldReference<?> ref) {
		this.joinType = type;
		this.fromTable = from;
		this.reference = ref;
		return this;
	}
	
	SQueryTable<RI> setRawJoin(JoinType type, String rawOnClause) {
		this.joinType = type;
		this.rawOnClause = rawOnClause;
		return this;
	}
	
	public SQueryTable<RI> setAlias(String alias) {
		//checkAlias(alias);
		this.tableAlias = alias;
		return this;
	}
	
	protected int setSelectMode(SSelectMode mode) {
		int previousFieldsNr = selectList == null ? 0 : selectList.length;
		this.selectList = this.rmeta.fieldsForMode(mode);
		return previousFieldsNr;
	}
	protected int setSelectList(SFieldScalar[] list) {
		int previousFieldsNr = selectList == null ? 0 : selectList.length;
		// this case is handled low level in SDriver...
//		List<SFieldScalar> slist = Arrays.asList(list);
//		List<SFieldScalar> pkeys = Arrays.asList(this.rmeta.getPrimaryKeys());
//		if ( ! slist.containsAll(pkeys))
//			throw new SException.Error("Part of the primary key is missing. When given an explicit select list, you must include the primary key");
		this.selectList = list;
		return previousFieldsNr;
	}
	
	public SRecordMeta<RI> getRecordMeta() {
		return rmeta;
	}
	public SFieldScalar[] getSelectList() {
		return selectList;
	}
	public String getAlias() {
		return tableAlias;
	}
	public int getFirstFieldIndex() {
		return this.firstFieldIndex;
	}
	public SQueryTable.JoinType getType() {
    	return joinType;
    }
	public String getRawOnClause() {
		return this.rawOnClause;
	}
	public SQueryTable<?> getFromTable() {
    	return fromTable;
    }
	public SFieldReference<?> getFieldReference() {
    	return reference;
    }
	public String toString() {
		return rmeta.toString() + " AS "+tableAlias;
	}
	
	void checkAlias(String alias) {
    	String regexp = "[A-Za-z0-9_-]+";
    	if( ! alias.matches(regexp))
    		throw new SException.Error("User given alias "+alias+" must match "+ regexp + " regexp (only non special sql characters)");
    }
}