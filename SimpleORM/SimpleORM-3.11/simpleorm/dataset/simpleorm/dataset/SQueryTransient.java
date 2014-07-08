package simpleorm.dataset;

import java.util.ArrayList;
import java.util.List;

import simpleorm.dataset.SQueryTransient.AggregateField.AggregateFunction;
import simpleorm.utils.SException;

/*
 * Author : Franck Routier <alci> franck.routier@axege.com
 */

/**
 * An aggregate Query inteface that provides structured queries for aggragates.
 * SAggregate query is based on a SQuery for the from, join and where clauses.
 * It will then allow adding aggregate functions, group by and order by.
 * <p>
 * For example:-
 * <xmp>  
 * SQuery<Employee> query = new SQuery(Employee.EMPLOYEE)
 *  .eq(Employee.DEPARTMENT, d100q);
 * SQueryTransient<Employee> aggQuery = new SQueryTransient(query)
 *  .sum(Employee.SALARY).as("sumOfSalaries")
 *  .groupBy(Employee.DEPT_ID).as("dept");
 *  
 * List<Map<String, SFieldAggregate>> salaries = session.query(aggQuery); 
 * </xmp>
 *
 * Limit and offset of the given SQuery will be used if available.
 * 
 * <p>
 * 
 * The normal expansion of the query into SQL is performed by SQueryTransientExecute in the SSessionJdbc
 * package.
 * <p>
 * 
 */

// TODO handle having statements

public class SQueryTransient {
	
	public static final String COUNT_ALL="COUNT_ALL";
	
	private SQuery<?> query;
	private List<AggregateField> aggFields = new ArrayList<AggregateField>();
	private OrderBy orderBy;
	
	/** Create a new SQueryTransient, based on qry.
	 * qry must have _no_ orderBy clause, which would be meaningless in the aggregate, otherwise you'll
	 * get an SExecption.Error.
	 * Also notice that the select lists for the different tables in qry are meaningless and will be ignored.
	 * @param qry
	 */
	public SQueryTransient(SQuery<?> qry) {
		if (qry.getOrderBy() != null)
			throw new SException.Error("Error, query has an order by clause. When using aggregate query, orderby must be applied on SQueryTransient, not on underlying SQuery.");
		if (qry.getRawSql() != null)
			throw new SException.Error("Error, query has a rawSql clause. If you want rawSql, use rawQueryMaps instead.");
		this.query = qry;
	}
	
	/**
	 * Add a group by statement, and _adds_ the field to the select list.
	 * Column in the resulting select will be aliased as GROUP_fieldName
	 * @param tableAlias
	 * @param fld
	 * @return
	 */
	public SQueryTransient groupBy(String tableAlias, SFieldScalar fld) {
		addField(tableAlias, fld, AggregateFunction.GROUP);
		return this;
	}
	
	public SQueryTransient groupBy(String rawSelect) {
		addRawField(rawSelect, AggregateFunction.GROUP);
		return this;
	}
		
	/**
	 * Add a count(*) aggregate to the query.
	 * Default alias for this aggregate will be "COUNT_all"
	 * @return
	 */
	public SQueryTransient count() {
		addField(null, null, AggregateFunction.COUNT);
		return this;
	}
	/**
	 * Add a count(tableAlias.fieldName) aggregate to the query.
	 * Default alias for this aggregate will be COUNT_fieldName
	 * @return
	 */
	public SQueryTransient count(String tableAlias, SFieldScalar fld) {
		addField(tableAlias, fld, AggregateFunction.COUNT);
		return this;
	}
	
	/**
	 * Add a count(rawSelect) aggregate to the query
	 * Default alias for this aggregate will be COUNT_fieldName
	 * @return
	 */
	public SQueryTransient count(String rawSelect) {
		addRawField(rawSelect, AggregateFunction.COUNT);
		return this;
	}
	
	/**
	 * Add a count(distinct tableAlias.fieldName) aggregate to the query.
	 * Default alias for this aggregate will be COUNT_fieldName
	 * @return
	 */
	public SQueryTransient countDistinct(String tableAlias, SFieldScalar fld) {
		addField(tableAlias, fld, AggregateFunction.COUNT_DISTINCT);
		return this;
	}
	
	/**
	 * Add a countDistinct(rawSelect) aggregate to the query
	 * Default alias for this aggregate will be COUNT_fieldName
	 * @return
	 */
	public SQueryTransient countDistinct(String rawSelect) {
		addRawField(rawSelect, AggregateFunction.COUNT_DISTINCT);
		return this;
	}
	
	/**
	 * Add a max aggregate function to the select list.
	 * Column in the resulting select will be aliased as MAX_fieldName
	 * @param tableAlias
	 * @param fld
	 * @return
	 */
	public SQueryTransient max(String tableAlias, SFieldScalar fld) {
		addField(tableAlias, fld, AggregateFunction.MAX);
		return this;
	}
	
	/**
	 * Add a max aggregate function to the select list.
	 * Column in the resulting select will be aliased as MAX_fieldName
	 * @param rawSelect eg max(case when ... then when ... then end)
	 * @return
	 */
	public SQueryTransient max(String rawSelect) {
		addRawField(rawSelect, AggregateFunction.MAX);
		return this;
	}
		
	/**
	 * Add a mim aggregate function to the select list.
	 * Column in the resulting select will be aliased as MIN_fieldName
	 * @param tableAlias
	 * @param fld
	 * @return
	 */
	public SQueryTransient min(String tableAlias, SFieldScalar fld) {
		addField(tableAlias, fld, AggregateFunction.MIN);
		return this;
	}
	
	/**
	 * Add a min aggregate function to the select list.
	 * @param rawSelect eg min(case when ... then when ... then end)
	 * @return
	 */
	public SQueryTransient min(String rawSelect) {
		addRawField(rawSelect, AggregateFunction.MIN);
		return this;
	}
	
	/**
	 * Add an avg aggregate function to the select list.
	 * Column in the resulting select will be aliased as AVG_fieldName
	 * @param tableAlias
	 * @param fld
	 * @return
	 */
	public SQueryTransient avg(String tableAlias, SFieldScalar fld) {
		addField(tableAlias, fld, AggregateFunction.AVG);
		return this;
	}
	
	/**
	 * Add a avg aggregate function to the select list.
	 * @param rawSelect eg avg(case when ... then when ... then end)
	 * @return
	 */
	public SQueryTransient avg(String rawSelect) {
		addRawField(rawSelect, AggregateFunction.AVG);
		return this;
	}

	
	/**
	 * Add a sum aggregate function to the select list.
	 * Column in the resulting select will be aliased as SUM_fieldName
	 * @param tableAlias
	 * @param fld
	 * @return
	 */
	public SQueryTransient sum(String tableAlias, SFieldScalar fld) {
		addField(tableAlias, fld, AggregateFunction.SUM);
		return this;
	}
	
	/**
	 * Add a max aggregate function to the select list.
	 * @param rawSelect eg sum(case when ... then when ... then end)
	 * @return
	 */
	public SQueryTransient sum(String rawSelect) {
		addRawField(rawSelect, AggregateFunction.SUM);
		return this;
	}

	/**
	 * Choose your own alias for the aggregate field that has just been added.
	 * @param fieldAlias
	 * @return
	 */
	public SQueryTransient as(String fieldAlias) {
		if (aggFields == null || aggFields.size() < 1)
			throw new SException.Error("as() can only be called after adding an aggregate field");
		AggregateField aggFld = this.aggFields.get(aggFields.size()-1);
		aggFld.alias = fieldAlias;
		return this;
	}
	
	/**
	 * Add an ASCENDING order by statement on the aggFieldAlias aggregate function.
	 * eg. ascending("GROUP_myFieldName")
	 * or ascending("myAggAlias")
	 * @param aggFieldAlias
	 * @return this
	 */
	public SQueryTransient ascending(String aggFieldAlias) {
		return appendNewOrderBy(aggFieldAlias, true);
	}
	/**
	 * Add a DESCENDING order by statement on the aggFieldAlias aggregate function.
	 * eg. descending("GROUP_myFieldName")
	 * or descending("myAggAlias")
	 * @param aggFieldAlias
	 * @return this
	 */
	public SQueryTransient descending(String aggFieldAlias) {
		return appendNewOrderBy(aggFieldAlias, false);
	}
	/** Private method to add order by*/
	private SQueryTransient appendNewOrderBy(String aggFieldAlias, boolean ascending) {
		AggregateField aggFld = findField(aggFieldAlias);
		if (aggFld == null)
			throw new SException.Error("Aggregate field "+aggFieldAlias+" is not in query");
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
        ob.aggField = aggFld;
        ob.ascending = ascending;
        return this;
    }
	/** Return the orderBy for this SQueryTransient */
	public OrderBy getOrderBy() {
		return orderBy;
	}
	/** Return the SQuery that backs this SQueryTransient */
	public SQuery<?> getUnderlyingQuery() {
		return this.query;
	}
	/** Return the AggreagateFields in this query*/
	public List<AggregateField> getFields() {
		return aggFields;
	}
	
	private AggregateField findField(String aggFieldAlias) {
		for (AggregateField fld : this.aggFields) {
			if (fld.alias.equals(aggFieldAlias))
				return fld;
		}
		return null;
	}
	
	private AggregateField addField(String tableAlias, SFieldScalar fld, AggregateFunction func) {

		AggregateField aggFld = new AggregateField();
		SQueryTable<?> tbl = null;
		switch(func) {
		case COUNT:
			if (tableAlias == null && fld == null) {
				aggFld.alias = COUNT_ALL;
			}
			break;
		default:
			tbl = query.getTableForAlias(tableAlias);
			if (tbl == null)
				throw new SException.Error("Table "+tableAlias+" is not in the query");
			if (fld != null && fld.getRecordMeta() != tbl.getRecordMeta())
				throw new SException.Error("Field "+fld+" does not belong to table "+tbl);
			aggFld.alias = func+"_"+fld.getFieldName();
		}
		aggFld.field = fld;
		aggFld.table = tbl;
		aggFld.function = func;
		aggFields.add(aggFld);
		return aggFld;
	}
	
	private AggregateField addRawField(String rawSelect, AggregateFunction func) {

		AggregateField aggFld = new AggregateField();
		aggFld.alias = func+"_"+rawSelect.hashCode(); // user should really use as() :-)
		aggFld.rawSelect = rawSelect;
		aggFld.function = func;
		aggFields.add(aggFld);
		return aggFld;
	}

	/**An aggregateField in the query, for internal use in Simpleorm
	 * Not to be confused with SFieldAggregate which is returned in maps
	 * as a result of executing a SQueryTransient.
	 */
    static public class AggregateField {
    	
    	public enum AggregateFunction {MAX, MIN, SUM, AVG, COUNT, COUNT_DISTINCT, GROUP};
    	
    	// field based
        SFieldScalar field;
        SQueryTable<?> table;
        // or rawSelect
        String rawSelect;
        // everytime
        AggregateFunction function;
        String alias;
        
        public String getRawSelect() {
        	return rawSelect;
        }
        
        public SFieldScalar getField() {
            return field;
        }
        public SQueryTable<?> getTable() {
            return table;
        }
        public AggregateFunction getFunction() {
            return function;
        }
        public String getFieldAlias() {
        	return alias;
        }
        @Override
        public String toString() {
        	return function+"("+alias+")";
        }
    }
    /** An order by on an aggregate field */
    static public class OrderBy {
        AggregateField aggField;
        boolean ascending;
        OrderBy next;
        
        public boolean isAscending() {
            return ascending;
        }
        public AggregateField getAggField() {
            return aggField;
        }
        public AggregateFunction getFunction() {
            return aggField.getFunction();
        }
        public SQueryTable<?> getTable() {
            return aggField.getTable();
        }
        public String getRawSelect() {
            return aggField.getRawSelect();
        }
        public OrderBy getNext() {
            return next;
        }        
    }
}
