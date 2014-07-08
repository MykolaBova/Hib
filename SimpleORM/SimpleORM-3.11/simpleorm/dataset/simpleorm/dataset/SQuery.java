package simpleorm.dataset;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import simpleorm.dataset.SQueryTable.JoinType;
import simpleorm.utils.SException;

/*
 * Copyright (c) 2002 Southern Cross Software Queensland (SCSQ).  All rights
 * reserved.  See COPYRIGHT.txt included in this distribution.
 */

/**
 * The main Query inteface that provides structured queries.
 * <p>
 * 
 * For example:-
 * <xmp> 
 * Department d100q = session.findOrCreate(Department.DEPARTMENT, "100"); 
 * SQuery<Employee> query = new SQuery(Employee.EMPLOYEE)
 *  .eq(Employee.DEPARTMENT, d100q) // and
 *  .like(Employee.NAME, "%One%")
 *  .descending(Employee.SALARY);
 * List<Employee> emps = session.query(query); 
 * </xmp>
 * 
 * Alias for tables can be given. Then they must be composed of [A-Za-z0-9_-]
 * 
 * This API is a little unusual in that there is only one SQuery instance
 * created, and each of the methods update its state and then return
 * <code>this</code>.
 * <p>
 * 
 * The normal expansion of the query into SQL is performed by SQueryExecute in the SSessionJdbc
 * package.  The raw* methods can be used to poke any string into the SQL Where or Order
 * By clauses in the order they appear.
 * <p>
 * 
 */

public class SQuery<T extends SRecordInstance> {
	
	// The list of all tables composing the query. Table at index 0 is always the main query table.
	private ArrayList<SQueryTable<?>> tables = new ArrayList<SQueryTable<?>>();
    private Where where;
    private OrderBy orderBy;
	private ArrayList<Object> queryParameters = new ArrayList<Object>();
	private SQueryMode queryMode = SQueryMode.SBASIC;
	private long limit = Integer.MAX_VALUE;
	private long offset = 0;

    // Used to tell which fields in the resultset will be used in which SQueryTable 
    private int currentFieldIndex = 1;
    // Complete raw SQL. Just poked in.  Note also rawClause().
    private String rawSql;
    // Raw join sql string to add special purpose join clause for filtering (like clause on > or <). See rawJoin() 
    private String rawJoin = null;
    
	/** A new query object that can be .executed to produce an SResultSet. */
	@SuppressWarnings("unchecked")
	public SQuery(SRecordMeta<T> record) {
		addTable(new SQueryTable(record.getTableName(), record, this.currentFieldIndex));     
    }
	
	/**
	 * Change queryMode from default (SQueryMode.SBASIC)
	 * to the given mode
	 * @see simpleorm.dataset.SQueryMode
	 * @param mode
	 * @return the query object itself (this)
	 */
	public SQuery<T> queryMode(SQueryMode mode) {
		this.queryMode = mode;
		return this;
	}
	/**
	 * Set an alias for the table that was last added
	 * @param alias
	 * @return the query object itself (this)
	 */	
	public SQuery<T> as(String alias) {
		SQueryTable<?> currentTable = tables.get(tables.size()-1);
		if (getTableForAlias(alias) != null &&  ! getTableForAlias(alias).equals(currentTable))
			throw new SException.Error("The alias "+alias+" is already used for table "+ getTableForAlias(alias));
		currentTable.setAlias(alias);
		return this;
	}
	
	/**
	 * Set a selectMode for the table that was last added.
	 * If a selectList was first given, it will be overridden by the one
	 * corresponding to the selectMode.
	 * @param selectMode
	 * @return the query object itself (this)
	 */	
	public SQuery<T> select(SSelectMode selectMode) {	
		int prev = tables.get(tables.size()-1).setSelectMode(selectMode);
		this.currentFieldIndex = currentFieldIndex - prev + tables.get(tables.size()-1).getSelectList().length; 
		return this;
	}
	/**
	 * Set the selectList for the table that was last added.
	 * If a selectList was generated by a selectMode, it will be overridden by
	 * this one
	 * @param selectList
	 * @return the query object itself (this)
	 */	
	public SQuery<T> select(SFieldScalar[] selectList) {
		int prev = tables.get(tables.size()-1).setSelectList(selectList);
		this.currentFieldIndex = currentFieldIndex - prev + tables.get(tables.size()-1).getSelectList().length; 
		return this;
	}

	@SuppressWarnings("unchecked")
	public SRecordMeta<T> getRecordMeta() {
		return (SRecordMeta<T>) tables.get(0).getRecordMeta();
	}
	
	/**
	 * Get the list of all tables composing the query, that is main table
	 * and joined tables
	 * @return tables
	 */
	public List<SQueryTable<?>> getTables() {
		return tables;
	}

	/**
	 * Set limit
	 */
	public SQuery<T> setLimit(long lim) {
		limit = lim;
		return this;
	}
	public long getLimit() {
		return this.limit;
	}
	/**
	 * Set offset
	 */
	public SQuery<T> setOffset(long off) {
		offset = off;
		return this;
	}
	public long getOffset() {
		return this.offset;
	}
	
	/**
	 * Add a raw join to the join statement. This kind of raw join can only serve
	 * the purpose of doing some relational algebra, and can as well be done with
	 * a subselect (matter of taste ?). This kind of raw join won't impact which
	 * record instance will be created or not.
	 * 
	 * Notice spaces before and after statement are added systematically.
	 * 
	 * Deprecated, use rawInnerJoin instead
	 * @see rawInnerJoin
	 */
	@Deprecated
	public SQuery<T> rawJoin(String sqlJoin, Object... parameters) {

		if (this.rawJoin == null) {
			this.rawJoin = sqlJoin;
		}
		else {
			this.rawJoin = this.rawJoin+" "+sqlJoin;
		}
        for (Object par:parameters) rawParameter(par);
		return this;
	}
	/**
	 * Adds a raw join to the join statement and plug an arbitrary table into
	 * the query. This will allow to make a join on a table that has no known
	 * relation to the current query (no SReferenceField) but the user want to
	 * arbitrary link based on fields values. The joinedTable is added to the query.
	 * @param fromAlias
	 * @param joinedTable
	 * @param rawOnClause the on clause, without the "ON" keyword,
	 * eg "field1 = field2 AND field3 = field4"
	 * @return
	 */
	public SQuery<T> rawInnerJoin(String fromAlias, SRecordMeta<?> joinedTable, String rawOnClause) {
		return rawGenericJoin(fromAlias, joinedTable, rawOnClause, JoinType.INNER);
	}
	public SQuery<T> rawLeftJoin(String fromAlias, SRecordMeta<?> joinedTable, String rawOnClause) {
		return rawGenericJoin(fromAlias, joinedTable, rawOnClause, JoinType.LEFT);
	}
	@SuppressWarnings("unchecked")
	private SQuery<T> rawGenericJoin(String fromAlias, SRecordMeta<?> joinedTable, String rawOnClause, JoinType type) {
		SQueryTable<?> toTable = new SQueryTable(joinedTable.getTableName(), joinedTable, this.currentFieldIndex);
		addTable(toTable);
		toTable.setRawJoin(type, rawOnClause);
		return this;
	}
	/**
	 * Add predicate to the where class, eg. "BUDGET > ?". Not normally called
	 * directly.
	 */
	public SQuery<T> rawPredicate(String predicate, Object... parameters) {
        
        Where nw = new Where();
        nw.allRaw = predicate;
        addWhere(nw);
        
        for (Object par:parameters) rawParameter(par);
		return this;
	}
	/**
	 * Add a clause to the OrderBy statement, eg. "NAME DESC". Commas are added
	 * automatically.
	 */
	public SQuery<T> rawOrderBy(String raw) {
        appendNewOrderBy().raw = raw;
		return this;
	}
	
    /** Just specifies the entire SQL statement, including the Select. */
    public SQuery<T> rawSql(String rawSelectSql, Object... parameters) {
        this.rawSql = rawSelectSql;        
        for (Object par:parameters) rawParameter(par);
        return this;
    }
    
    /**
	 * Generates <code>field clause</code>, ie the clause string is poked
	 * literally into the query. Eg.
	 * <p>
	 * <code>fieldQuery(Employee.Name, "= 'Fred'")</code>
	 * <p>
	 * 
	 * Use fieldRelopParameter instead for parameters determined at run time as
	 * blindly concatenating strings is dangerous.
	 */
	public SQuery<T> fieldQuery(SFieldScalar field, String clause) {
		return fieldQuery(field.getRecordMeta().getTableName(), field, clause);
	}
		
	public SQuery<T> fieldQuery(String alias, SFieldScalar field, String clause) {

       Where nw = new Where();
       nw.leftField = field;
       nw.leftTable = getTableForAlias(alias);
       if (nw.leftTable == null) // test if alias exists, to avoid a nasty NPE when building the sql later
    	   throw new SException.Error("Table "+alias+" is not present in query");
       nw.operator = "";
       nw.rightRaw = clause;
       addWhere(nw);

       return this;
	}

	/**
	 * Join to another table, based on the specified reference whose one of the side points to one of
	 * the tables in query.
     * This peforms an eager lookup on the referenced tables, so avoiding the N+1 problem.
	 * <p>
	 * 
     * Joins work from a table already in the query to tables directly liked by the reference,
     * or form the table of the reference to a table in the query that is linked by the reference.
     * 
     * It is possible to join a table to itself (eg. Employee.Manager) using aliases.<p>
     * 
     * Normally the looked up records are also retrieved, although it is possible to
     * specify selectMode = SNONE if the join is only for the WHERE clause.<p>
     * 
     * Inner joins and left outer joins are possible.<p>
     * 
     * Note that this is only useful if the number of joined in records is substantial. 
     * For example, if there are 1,000 employees in 10 departments, then it is probably
     * faster (and certainly simpler) to retrieve the departments lazily rather than
     * retrieve the fields for the 10 departments 1000 times and throw them away each time.<p>
     * 
     * Note also that Subselects can be used to reference fields in the where clause without
     * introduing outer join issues.<p>
     * 
     * Note using a one to many join results in a list where the same record will be repeated
     * multiple times (but still, it will be the same object, and it exists only once in the DataSet)
	 */
	
	
	/** InnerJoin */
	public SQuery<T> innerJoin(SFieldReference<?> reference) {
		return generalJoin(JoinType.INNER, reference);
	}
	public SQuery<T> innerJoin(String fromAlias, SFieldReference<?> reference) {
		return generalJoin(JoinType.INNER, fromAlias, reference);
	}
	
	/** leftJoin */
	public SQuery<T> leftJoin(SFieldReference<?> reference) {
		return generalJoin(JoinType.LEFT, reference);
	}
	public SQuery<T> leftJoin(String fromAlias, SFieldReference<?> reference) {
		return generalJoin(JoinType.LEFT, fromAlias, reference);
	}
	
	/** General join type */
	public SQuery<T> generalJoin(JoinType type, SFieldReference<?> reference) {
		if (isRefFieldInQuery(reference)) {
			return generalJoin(type, reference.getRecordMeta().getTableName(), reference);
		}
		else {
			return generalJoin(type, reference.getReferencedRecordMeta().getTableName(), reference);
		}
	}
	public SQuery<T> generalJoin(JoinType type, String fromAlias, SFieldReference<?> reference) {
		addJoin(type, getTableForAlias(fromAlias), reference);
		return this;
	}


	/**
	 * Normally just adds <code>fieldRelopParameter(field, "=",
	 value)</code>.
	 * <p>
	 * 
	 * If field is a reference it expands the foreign keys, and
	 * value must be an instance of the same record type.
	 * <p>
	 * 
	 * value must not be null, you need the special case IS NULL test. (It would
	 * be possible to optimize this here, but what about field == field where
	 * one of them is null -- that would be inconsistent.)
	 * <p>
	 */
	public SQuery<T> eq(SFieldMeta field, Object value) {
		return eq(field.getRecordMeta().getTableName(), field, value);
	}
	public SQuery<T> eq(String alias, SFieldMeta field, Object value) {
		return eqNeAux(alias, field, value, true);
	}

	public SQuery<T> eq(SFieldScalar field1, SFieldScalar field2) {
		return eq(field1.getRecordMeta().getTableName(), field1,
				  field2.getRecordMeta().getTableName(), field2);
	}
	public SQuery<T> eq(String alias1, SFieldScalar field1, String alias2, SFieldScalar field2) {
		return fieldRelopParameter(alias1, field1, "=", alias2, field2);
	}


	/**
	 * True if boolean field == writeFieldValue(value). Ie. value is converted
	 * from bool to "Y"/"N" etc.
	 */
	public SQuery<T> equivalent(SFieldBoolean field, boolean value) {
		return eq(field, field.writeFieldValue(value ? Boolean.TRUE
				: Boolean.FALSE));
	}

	/**
	 * shortcut for equivalent(field, true);
	 */
	public SQuery<T> isTrue(SFieldBoolean field) {
		return equivalent(field, true);
	}

	public SQuery<T> isFalse(SFieldBoolean field) {
		return equivalent(field, false);
	}

	/**
	 * Just adds <code>fieldRelopParameter(field, "<>", value)</code>.
	 * <p>
	 * 
	 * Note that there are few methods <code>ne(SfieldMeta, int)</code> etc.
	 * This is because 5 relops * 5 data types would require 25 methods! Java
	 * 1.5 boxing will (finally) make this unnecessary anyway.
	 */
	public SQuery<T> ne(SFieldMeta field, Object value) {
		return ne(field.getRecordMeta().getTableName(), field, value);
	}
	public SQuery<T> ne(String alias, SFieldMeta field, Object value) {
		return eqNeAux(alias, field, value, false);
	}

	public SQuery<T> ne(SFieldScalar field1, SFieldScalar field2) {
		return ne(field1.getRecordMeta().getTableName(), field1,
				  field2.getRecordMeta().getTableName(), field2);
	}
	public SQuery<T> ne(String alias1, SFieldScalar field1, String alias2, SFieldScalar field2) {
		return fieldRelopParameter(alias1, field1, "<>", alias2, field2);
	}

//####    I would also like eq/ne(field, null) to be handled automatically by SQuery.  
//    (convert to IS NULL)  Need to be careful about Oracle where x = "" must also be converted.  
//    So the conversion needs to happen in SQuery.Sql.

	/** Just adds <code>fieldQuery(field, "IS NULL")</code> */
	public SQuery<T> isNull(SFieldMeta field) {
		return isNull(field.getRecordMeta().getTableName(), field);
	}
	public SQuery<T> isNull(String alias, SFieldMeta field) {
		return nullAux(alias, field, true);
	}

	/** Just adds <code>fieldQuery(field, "IS NULL")</code> */
	public SQuery<T> isNotNull(SFieldMeta field) {
		return isNotNull(field.getRecordMeta().getTableName(), field);
	}
	public SQuery<T> isNotNull(String alias, SFieldMeta field) {
		return nullAux(alias, field, false);
	}

	/** Just adds <code>fieldRelopParameter(field, "&gt;", value)</code>
	 *  Java5 autoboxing will take care of alias + raw types
	 */
	public SQuery<T> gt(String alias, SFieldScalar field, Object value) {
		return fieldRelopParameter(alias, field, ">", value);
	}
	
	public SQuery<T> gt(SFieldScalar field, Object value) {
		return gt(field.getRecordMeta().getTableName(), field, value);
	}



	public SQuery<T> gt(SFieldScalar field1, SFieldScalar field2) {
		return gt(field1.getRecordMeta().getTableName(), field1,
				  field2.getRecordMeta().getTableName(), field2);
	}
	public SQuery<T> gt(String alias1, SFieldScalar field1, String alias2, SFieldScalar field2) {
		return fieldRelopParameter(alias1, field1, ">", alias2, field2);
	}

	/** Just adds <code>fieldRelopParameter(field, "&lt;", value)</code>
	 *  Java5 autoboxing will take care of alias + raw types
	 */
	public SQuery<T> lt(String alias, SFieldScalar field, Object value) {
		return fieldRelopParameter(alias, field, "<", value);
	}
	
	public SQuery<T> lt(SFieldScalar field, Object value) {
		return lt(field.getRecordMeta().getTableName(), field, value);
	}


	public SQuery<T> lt(String alias1, SFieldScalar field1, String alias2, SFieldScalar field2) {
		return fieldRelopParameter(alias1, field1, "<", alias2, field2);
	}
	public SQuery<T> lt(SFieldScalar field1, SFieldScalar field2) {
		return lt(field1.getRecordMeta().getTableName(), field1, 
				  field2.getRecordMeta().getTableName(), field2);
	}

	/** Just adds <code>fieldRelopParameter(field, "&lt;=", value)</code>
	 *  Java5 autoboxing will take care of alias + raw types
	 */
	public SQuery<T> le(String alias, SFieldScalar field, Object value) {
		return fieldRelopParameter(alias, field, "<=", value);
	}
	public SQuery<T> le(SFieldScalar field, Object value) {
		return le(field.getRecordMeta().getTableName(), field, value);
	}

	public SQuery<T> le(SFieldScalar field1, SFieldScalar field2) {
		return le(field1.getRecordMeta().getTableName(), field1,
				  field2.getRecordMeta().getTableName(), field2);
	}
	public SQuery<T> le(String alias1, SFieldScalar field1, String alias2, SFieldScalar field2) {
		return fieldRelopParameter(alias1, field1, "<=", alias2, field2);
	}

	/** Just adds <code>fieldRelopParameter(field, "&gt;=", value)</code> */
	public SQuery<T> ge(SFieldScalar field, Object value) {
		return ge(field.getRecordMeta().getTableName(), field, value);
	}
	public SQuery<T> ge(String alias, SFieldScalar field, Object value) {
		if (value instanceof SFieldScalar) {
			SFieldScalar f2 = (SFieldScalar) value;
			return fieldRelopParameter(alias, field, ">=", f2.getRecordMeta().getTableName(), f2);
		}
		return fieldRelopParameter(alias, field, ">=", value);
	}

	public SQuery<T> ge(SFieldScalar field1, SFieldScalar field2) {
		return fieldRelopParameter(field1.getRecordMeta().getTableName(), field1, ">=",
				                   field2.getRecordMeta().getTableName(), field2);
	}
	
	public SQuery<T> ge(String alias1, SFieldScalar field1, String alias2, SFieldScalar field2) {
		return fieldRelopParameter(alias1, field1, ">=",
				                   alias2, field2);
	}

	/** Use or clause to simulate the in clause */
	public SQuery<T> in(SFieldScalar field, Object... values) {
		return in(field.getRecordMeta().getTableName(), field, values);
	}
	public SQuery<T> in(String alias, SFieldScalar field, Object... values) {
        StringBuffer buf = new StringBuffer(100);
        
		buf.append(" IN (");
		for (int ii = 0; ii < values.length; ii++) {
            if (ii>0) buf.append(", ");
            buf.append("?");
    		rawParameter(values[ii]);        
		}
		buf.append(")");
        
        fieldQuery(alias, field, buf.toString());
		return this;
	}


	/** Just adds <code>fieldRelopParameter(field, "like", value)</code> */
	public SQuery<T> like(SFieldScalar field, String value) {
		return like(field.getRecordMeta().getTableName(), field, value);
	}
	public SQuery<T> like(String alias, SFieldScalar field, String value) {
		return fieldRelopParameter(alias, field, "like", value);
	}

	/** <code>rawOrderBy(field.columnName)</code> */
	public SQuery<T> ascending(SFieldMeta field) {
		return ascending(field.getRecordMeta().getTableName(), field);
	}
	/** <code>rawOrderBy(field.columnName)</code> */
	public SQuery<T> ascending(String alias, SFieldMeta field) {
		return rawOrderBy(alias, field, true);
	}

	/** <code>rawOrderBy(field.columnName) DESC</code> */
	public SQuery<T> descending(SFieldMeta field) {
		return descending(field.getRecordMeta().getTableName(), field);
	}
	/** <code>rawOrderBy(field.columnName) DESC</code> */
	public SQuery<T> descending(String alias, SFieldMeta field) {
		return rawOrderBy(alias, field, false);
	}


    /** See SSession.queryToString for a fuller print out. */
	public String toString() {
		return "[SQuery " + getRecordMeta() + "]";
	}
    
    /////////////////////////////// empty properties //////////////////
    public ArrayList<Object> getQueryParameters() {
		return queryParameters;
	}

    public OrderBy getOrderBy() {
        return orderBy;
    }

    public Where getWhere() {
        return where;
    }
        
	public SQueryMode getQueryMode() {
		return queryMode;
	}

    public String getRawSql() {
        return rawSql;
    }
    
    public String getRawJoin() {
        return rawJoin;
    }

    //
    // Private methods
    //
    
	/**
	 * Throws exception if field is not from the table for given alias
	 */
	private void verifyFieldOkToUse(String alias, SFieldMeta field) {
		SQueryTable<?> tbl = getTableForAlias(alias);
		if ( tbl == null) {
			throw new SException.Error("Table "+alias+" is not part of the query.");
		}
		if ( tbl.getRecordMeta() != field.getRecordMeta()) {
			throw new SException.Error("Field " + field + " is not from record " + tbl);
		}
	}
	
	// package visibility to allow use in SQueryTransient
	SQueryTable<?> getTableForAlias(String alias) {
		for (SQueryTable<?> tbl : tables) {
			if (alias.equals(tbl.getAlias()))
				return tbl;
		}
		return null;
	}
	
	/**
	 * Tell if a table has been joined with this alias.
	 */
	public boolean hasTable(String alias) {
		return getTableForAlias(alias) != null ? true : false;
	}
	
	private OrderBy appendNewOrderBy() {
        OrderBy ob = new OrderBy();
        if (this.orderBy == null) {
            this.orderBy = ob;
        } else {
            OrderBy current = this.orderBy;
            while (current.next != null) {
                current = current.next;
            }
            current.next = ob;
        }
        return ob;
    }
	
    private void addWhere(Where newWhere) {
        Where ow = where;
        if (ow == null) {
            where = newWhere;
        } else {
            where = new Where();
            where.left = ow;
            where.operator = "AND";
            where.right = newWhere;
        }
    }
    @SuppressWarnings("unchecked")
	private void addJoin(JoinType type, SQueryTable<?> from, SFieldReference<?> reference) {
    	
    	// find new record, can be on both sides of the SFieldReference, allow join on both sides.
		SRecordMeta<?> newRec = reference.getReferencedRecordMeta();
		SRecordMeta<?> fromRec = reference.getRecordMeta();
		if (newRec == from.getRecordMeta()) {  // the join is the other way around
			newRec = reference.getRecordMeta();
			fromRec = reference.getReferencedRecordMeta();
		}
		// Test at least one side points to from Table
		if (newRec != from.getRecordMeta()
				&& fromRec != from.getRecordMeta()) {  
			 throw new SException.Error("Reference must be in record " + reference + " " + from.getRecordMeta());
		}
		SQueryTable<?> toTable = new SQueryTable(newRec.getTableName(), newRec, this.currentFieldIndex);
		addTable(toTable);
		toTable.setJoin(type, from, reference);
    }


	/**
	 * Add a parameter value to the internal array. These will be
	 * <code>setObject</code> later after the prepared statement is created
	 * but before it is executed.  Not normally used.
	 */
	private SQuery<T> rawParameter(Object parameter) {
		queryParameters.add(parameter);
		return this;
	}

	/**
	 * Generates <code>field relop ?</code> and then subsequently sets the
	 * parameter value. Eg.
	 * <p>
	 * 
	 * <code>fieldRelopParameter(Employee.Name, "=", myName)</code>
	 * <p>
	 */
	private SQuery<T> fieldRelopParameter(
        String alias, SFieldScalar field, String relop,	Object value) {
		// If object passed in is really an SFieldMeta, use the method that is
		// appropriate
		if (value instanceof SFieldScalar) {
			SFieldScalar fld = (SFieldScalar) value;
			return this.fieldRelopParameter(alias, field, relop, fld.getRecordMeta().getTableName(), fld);
		}
			

       fieldRelopParameterInner(alias, field, relop, "?");
       
       rawParameter(value);

		return this;
	}

	/**
	 * Generates <code>field1 relop field2</code>
	 * 
	 * E.g.
	 * <code>fieldRelopParameter(Order.QuantityRequired, "=", Order.QuantityReceived)</code>
	 * <p>
	 * Mainly useful for Joins.
	 */
	private SQuery<T> fieldRelopParameter(
         String alias1, SFieldScalar field1, String relop, String alias2, SFieldScalar field2) {
       fieldRelopParameterInner(alias1, field1, relop, alias2, field2);
       return this;
	}

    private Where fieldRelopParameterInner(String alias1, SFieldScalar field1, String relop, String alias2, SFieldScalar field2) {
        Where nw = new Where();
        nw.leftField = field1;
        nw.leftTable = getTableForAlias(alias1);
        nw.operator = relop;
        nw.rightField = field2;
        nw.rightTable = getTableForAlias(alias2);
        addWhere(nw);
        return nw;
    }
    
    private Where fieldRelopParameterInner(String alias1, SFieldScalar field1, String relop, String rawRight) {
        Where nw = new Where();
        nw.leftField = field1;
        nw.leftTable = getTableForAlias(alias1);
        nw.operator = relop;
        nw.rightRaw = rawRight;
        addWhere(nw);
        return nw;
    }

	
		
	/**
	 * returns true if the recordMeta of the SFieldReference is already in query with its
	 * default alias (ie tableName).
	 * Returns false if the referenced record meta is already in query with its default alias.
	 * 
	 * Throws an exception if neither is true. 
	 */
	private boolean isRefFieldInQuery(SFieldReference<?> ref) {
		SQueryTable<?> existingTable = getTableForAlias(ref.getRecordMeta().getTableName());
		if (existingTable == null)
			existingTable = getTableForAlias(ref.getReferencedRecordMeta().getTableName());
		if (existingTable == null)
			throw new SException.Error("Reference is not linked to any table in the query");			
		if (existingTable.getRecordMeta() == ref.getRecordMeta()) {
			return true;  // the reference belongs to a table already in query
		}
		else {
			return false; // the reference points to a table already in query
		}
	}

	/**
	 * Compares ref with value, recurively. operator is normally "=", but can be
	 * "NOT NULL" etc.
	 */
	private void opReference(String alias, SFieldReference<?> ref, SRecordInstance value,
							 String operator, boolean disjoin, boolean diadic) {

        int count=0;
		for (SFieldScalar fkey : ref.getForeignKeyMetas()) {
			SFieldScalar refed = ref.getPrimaryKeyForForegnKey(fkey);
			if (disjoin && count>0) 
                throw new SException.Error("Ref<>Ref not supported for multiple keys yet");
			if (diadic) {
				Object val = value.getObject(refed);
				fieldRelopParameter(alias, fkey, operator, val);
			} else
				fieldQuery(fkey, operator);
		}
        count++;
	}

	private SQuery<T> eqNeAux(String alias, SFieldMeta field, Object value, boolean isEq) {
		if (value == null)
			throw new SException.Error("Use isNull to test for nulls " + field);
		String op = isEq ? "=" : "<>";
		if (!(field instanceof SFieldReference)) {
			return fieldRelopParameter(alias, (SFieldScalar)field, op, value);
		} else {
			if (!(value instanceof SRecordInstance))
				throw new SException.Error("value " + value
						+ " must be an SRecordInstance for reference " + field
						+ " building " + this);
			SFieldReference<?> refFld = (SFieldReference<?>) field;
			SRecordMeta<?> refedRec = refFld.getReferencedRecordMeta();
			if (((SRecordInstance) value).getMeta() != refedRec)
				throw new SException.Error("Value " + value + " must be a "
						+ refedRec + " building " + this);
			opReference(alias, (SFieldReference<?>) field, (SRecordInstance) value, op,
					!isEq, true);
			return this;
		}
	}

	private SQuery<T> nullAux(String alias, SFieldMeta field, boolean isNull) {
		String op = isNull ? "IS NULL" : "IS NOT NULL";
		if (!(field instanceof SFieldReference)) {
			SFieldScalar sclFld = (SFieldScalar) field;
			return fieldQuery(alias, sclFld, op);
		} else {
			// SFieldReference refFld = (SFieldReference)field;
			opReference(alias, (SFieldReference<?>) field, null, op, isNull, false);
			return this;
		}
	}
    
	/**
	 * Adds the table to tables list, and increment the currentFieldIndex by the number
	 * of fields this table will select
	 * @param newTable
	 * @throws SException.Error if the alias is already used in the query
	 */
    private void addTable(SQueryTable<?> newTable) {
    	if (getTableForAlias(newTable.getAlias()) != null) {
    		throw new SException.Error("Cannot add duplicate alias "+newTable.getAlias()+" in query.");
    	}
    	tables.add(newTable);
    	this.currentFieldIndex = this.currentFieldIndex + newTable.getSelectList().length;
    }
    
    
    private SQuery<T> rawOrderBy(String alias, SFieldMeta field, boolean ascending) {
		verifyFieldOkToUse(alias, field);
		if (field instanceof SFieldReference) {
			Set<SFieldScalar> fields = ((SFieldReference<?>) field).getForeignKeyMetas();
			for (SFieldScalar fld : fields) {
				rawOrderBy(alias, fld, ascending);
			}
		} else {
            OrderBy ob = appendNewOrderBy();
            ob.field = field;
            ob.table = getTableForAlias(alias);
            ob.ascending = ascending;
        }
		return this;
	}
    
    //
    // static nested classes
    //
    
    static public class Where {
        Where left;

        private SFieldScalar leftField;
        private SQueryTable<?> leftTable;
        private String operator;
        private Where right;
        private String rightRaw;
        private SFieldScalar rightField;
        private SQueryTable<?> rightTable;
        private String allRaw;
        
        public Where getLeft() {
            return left;
        }
        public SFieldScalar getLeftField() {
            return leftField;
        }
        public SQueryTable<?> getLeftTable() {
            return leftTable;
        }
        public String getOperator() {
            return operator;
        }
        public Where getRight() {
            return right;
        }
        public SFieldScalar getRightField() {
            return rightField;
        }
        public SQueryTable<?> getRightTable() {
            return rightTable;
        }
        public String getRightRaw() {
            return rightRaw;
        }
        public String getAllRaw() {
            return allRaw;
        }               
    }
    
    static public class OrderBy {
        SFieldMeta field;
        SQueryTable<?> table;
        String raw;
        boolean ascending;
        OrderBy next;
        public boolean isAscending() {
            return ascending;
        }
        public SFieldMeta getField() {
            return field;
        }
        public SQueryTable<?> getTable() {
            return table;
        }
        public String getRaw() {
            return raw;
        }
        public OrderBy getNext() {
            return next;
        }        
    }
}
