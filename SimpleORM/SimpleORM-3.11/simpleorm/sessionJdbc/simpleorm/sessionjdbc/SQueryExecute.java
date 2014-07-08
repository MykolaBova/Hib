package simpleorm.sessionjdbc;

import java.sql.ResultSet;
import java.util.List;

import simpleorm.dataset.SFieldScalar;
import simpleorm.dataset.SQuery;
import simpleorm.dataset.SQueryMode;
import simpleorm.dataset.SQueryResult;
import simpleorm.dataset.SQueryTable;
import simpleorm.dataset.SRecordInstance;
import simpleorm.utils.SException;
import simpleorm.utils.SLog;

/**
 * Internal class to creates the SQL that corresponds to an SQuery.
 */
class SQueryExecute<RI extends SRecordInstance> {
	
	SSessionJdbc session = null;
	SQuery<RI> query = null;
	String sqlQuery = null;

	SQueryExecute(SSessionJdbc session, SQuery<RI> query) {
		this.session = session;
		this.query = query;
	}

	SQueryResult<RI> executeQuery() {
		session.statistics.incrementNrQueryDatabase();
		ResultSet rs = null;
		try {
			// Parse the Query into sql
			if (query.getRawSql() != null)
				this.sqlQuery = query.getRawSql();
			else
				this.sqlQuery = session.getDriver().selectSQL(
						selectClause(), fromClause(), joinClause(), whereClause(),
						null, // group by
						orderByClause(),
						query.getQueryMode() == SQueryMode.SFOR_UPDATE,
						query.getLimit(), query.getOffset());

			// get a resultSet from the session
			rs = session.executeQuery(query.getOffset(), this.sqlQuery, query.getQueryParameters());
			
			// create record instances out of the resultset
			SQueryResult<RI> al = new SQueryResult<RI>(this.sqlQuery);
			for (int ax = 0; ax < query.getLimit() && session.rsNext(rs); ax++) {
				RI ri = findOrCreateFromResultSet(rs);
				ri.doQueryRecord();
				al.add(ri);
			}
			return al;
		} finally {
			SSessionJdbc.closeResultSetAndStatement(rs);
        }
	}

	@SuppressWarnings({ "deprecation", "unchecked" })
	private RI findOrCreateFromResultSet(ResultSet rs) {
		
		SRecordInstance result = null;
		for (SQueryTable<?> tbl : query.getTables()) {
			if (tbl.getSelectList().length > 0) {
				// Create New Instances. Too early to know if already in transCache.
				SRecordInstance inst = session.getDataSet().newInstanceNotInDataSet(tbl.getRecordMeta());
				
				session.sessionHelper.retrieveRecord(inst, tbl.getSelectList(), tbl.getFirstFieldIndex(), rs,
						                             query.getQueryMode() == SQueryMode.SREAD_ONLY, false);

				// Check to see if it is already in Transaction Cache
				SLog logger = session.getLogger();
				if ( ! isNullRecord(inst)) { // don't put null records (eg left join) in dataset
					SRecordInstance cache = session.getDataSet().findUsingPrototype(inst);
					if (cache == null
							|| session.sessionHelper.needsRequery(cache, query
									.getQueryMode() == SQueryMode.SREAD_ONLY, tbl.getSelectList())) {
						// ## What happens here if the user holds a reference to the previously cached record, and we do requery ?
						// ## Potential inconsistence ?
						session.getDataSet().pokeIntoDataSet(inst);
						if (logger.enableQueries())
							logger.queries("getRecord: " + inst
									+ " (from database)");
					} else {
						inst = cache;
						if (cache.getDataSet() != session.getDataSet())
							throw new SException.Error("Inconsistent Connections "
									+ cache + cache.getDataSet()
									+ session.getDataSet());
						if (logger.enableQueries())
							logger.queries("getRecord: " + cache + " (from cache)");
					}
				}
				if (SQueryTable.JoinType.MAIN_TABLE.equals(tbl.getType()))
					result = inst;
			}
		}
		// DataSets can now find referenced records even if detached.
		return (RI) result;
	}

	String getSQL() {
		return sqlQuery;
	}

	// check if the record has at least one not null pk (then it must be not null
	// altogether)
	private boolean isNullRecord(SRecordInstance inst) {
		for (SFieldScalar pk : inst.getMeta().getPrimaryKeys()) {
			if ( ! inst.isNull(pk))
				return false;
		}
		return true;
	}

	protected String joinClause() {
		StringBuffer sb = new StringBuffer();

		if (query.getRawJoin() != null)
			sb.append(query.getRawJoin());
		for (SQueryTable<?> table : query.getTables()) {
			if ( ! SQueryTable.JoinType.MAIN_TABLE.equals(table.getType()))
				sb.append(getDriver().joinSQL(table)).append(
						getDriver().clauseSeparator("    "));
		}
		return sb.toString();
	}

	protected String whereClause() {
		SQuery.Where where = query.getWhere();
		StringBuffer res = new StringBuffer(50);
		doWhere(where, res);
		return res.length() > 0 ? res.toString() : null;
	}

	protected void doWhere(SQuery.Where where, StringBuffer res) {
		if (where == null)
			;
		else if (where.getAllRaw() != null) {
			//res.append(" (");
			res.append(where.getAllRaw());
			//res.append(") ");
		} else {
			if (where.getLeft() != null) {
				//res.append("(");
				doWhere(where.getLeft(), res);
				//res.append(")");
			} else {
				getDriver().appendField(where.getLeftTable(), where.getLeftField(), res);
			}
			res.append(" ");
			res.append(where.getOperator());
			res.append(" ");
			if (where.getRight() != null) {
				//res.append("(");
				doWhere(where.getRight(), res);
				//res.append(")");
			} else {
				if (where.getRightRaw() != null) {
					res.append(" ");
					res.append(where.getRightRaw());
					res.append(" ");
				} else
					getDriver().appendField(where.getRightTable(), where.getRightField(), res);
			}
		}
	}

	protected String selectClause() {
		// Select list
		StringBuffer selectBuf = new StringBuffer();
		boolean first = true;
		List<SQueryTable<?>> tables = query.getTables();
		if (tables.size() == 1 && query.getRawJoin() == null) {
			// if we have only one table and no rawJoin, don't prefix column names (to get more readable sql)
			for (SFieldScalar sfld : tables.get(0).getSelectList()) {
				if ( ! first) {
					selectBuf.append(", ");
				}
				getDriver().appendColumnName(sfld, selectBuf);
				first = false;
			}
		}
		else {
			// prefix column names with table names or aliases
			for (SQueryTable<?> rel : tables) {
				for (SFieldScalar sfld : rel.getSelectList()) {
					if ( ! first) {
						selectBuf.append(", ");
					}
					getDriver().appendField(rel, sfld, selectBuf);
					first = false;
				}
			}
		}
		return selectBuf.toString();
	}
	
	protected String fromClause() {
		return getDriver().fromSQL(query.getTables().get(0));
	}
	
	protected String orderByClause() {
		SQuery.OrderBy ob = query.getOrderBy();
		StringBuffer res = new StringBuffer(30);
		while (ob != null) {
			if (res.length() > 0)
				res.append(", ");
			if (ob.getRaw() != null)
				res.append(ob.getRaw());
			else {
				SFieldScalar sclField = (SFieldScalar) ob.getField();
				getDriver().appendField(ob.getTable(), sclField, res);
				if (!ob.isAscending())
					res.append(" DESC");
			}
			ob = ob.getNext();
		}
		return res.length() > 0 ? res.toString() : null;
	}

	/** For Debug etc. only */
	protected String queryToString(SQuery<?> query) {
		String res = "[SQuerySql " + query.getRecordMeta() + " WHERE "
				+ whereClause() + " ORDER " + orderByClause() + "]";
		return res;
	}


	SDriver getDriver() {
		return session.getDriver();
	}

	public SQuery<?> getQuery() {
		return query;
	}

	public String toString() {
		return "[SPreparedStatement " + sqlQuery + "]";
	}
}