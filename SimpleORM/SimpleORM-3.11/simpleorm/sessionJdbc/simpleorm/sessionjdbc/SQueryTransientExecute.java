package simpleorm.sessionjdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import simpleorm.dataset.SQuery;
import simpleorm.dataset.SQueryMode;
import simpleorm.dataset.SQueryResult;
import simpleorm.dataset.SQueryTransient;
import simpleorm.dataset.SRecordInstance;
import simpleorm.dataset.SRecordTransient;
import simpleorm.dataset.SQueryTransient.AggregateField;
import simpleorm.dataset.SQueryTransient.OrderBy;
import simpleorm.dataset.SQueryTransient.AggregateField.AggregateFunction;
import simpleorm.utils.SException;

/**
 * Internal class to creates the SQL that corresponds to an SQueryTransient.
 */

class SQueryTransientExecute<RI extends SRecordInstance> extends SQueryExecute<RI> {
	
	SQueryTransient aggregateQuery;

	@SuppressWarnings("unchecked")
	SQueryTransientExecute(SSessionJdbc session, SQueryTransient aggQuery) {
		super(session, (SQuery<RI>) aggQuery.getUnderlyingQuery());
		this.aggregateQuery = aggQuery;
	}

	@Override
	SQueryResult<RI> executeQuery() {
		throw new SException.InternalError("executeQuery called on an SQueryTransientExecute, probably instead of executeAggregateQuery");
	}
	
	SQueryResult<SRecordTransient> executeAggregateQuery() {
		
		session.statistics.incrementNrQueryDatabase();
		ResultSet rs = null;
		try {
			// Parse the Query into sql
			this.sqlQuery = buildSqlQuery();

			// get a resultSet from the session
			rs = session.executeQuery(query.getOffset(), sqlQuery, query.getQueryParameters());
			
			// create the resulting list of maps
			SQueryResult<SRecordTransient> result = new SQueryResult<SRecordTransient>(sqlQuery);
			for (int ax = 0; ax < query.getLimit() && session.rsNext(rs); ax++) {
				SRecordTransient row = createFromResultSet(rs);
				result.add(row);
			}
			return result;
		} catch (SQLException e) {
			throw new SException.Jdbc(e);
		} finally {
			SSessionJdbc.closeResultSetAndStatement(rs);
        }
	}
	
	String buildSqlQuery() {
		return session.getDriver().selectSQL(selectClause(),
											 getDriver().fromSQL(query.getTables().get(0)),
											 joinClause(), whereClause(),
											 groupByClause(),
											 orderByClause(),
											 query.getQueryMode() == SQueryMode.SFOR_UPDATE,
											 query.getLimit(), query.getOffset()
											 );
	}
	
	private SRecordTransient createFromResultSet(ResultSet rs) throws SQLException {
		
		SRecordTransient res = new SRecordTransient();
		int rx = 0;
		for (AggregateField fld : aggregateQuery.getFields()) {
			res.put(fld.getFieldAlias(), rs.getObject(rx + 1));
			rx++;
        }
		
		return res;
	}
	
	@Override
	protected String selectClause() {
		// Select list
		StringBuffer selectBuf = new StringBuffer();
		boolean first = true;
		
		for (AggregateField fld : aggregateQuery.getFields()) {
			if ( ! first) {
				selectBuf.append(", ");
			}
			appendAggField(selectBuf, fld);
			// as alias
			selectBuf.append(" AS ").append(fld.getFieldAlias());
			first = false;
		}
		return selectBuf.toString();
	}

	protected String groupByClause() {
		List<AggregateField> fields = aggregateQuery.getFields();
		StringBuffer res = new StringBuffer(30);
		// cannot use aggFieldAlias in group by as Oracle (for example) doesn't allow it :-(
		for (AggregateField fld : fields) {
			if (fld.getFunction().equals(AggregateFunction.GROUP)) {
				if (res.length() > 0)
					res.append(", ");
				// raw select
				if (fld.getRawSelect() != null) {
					res.append(fld.getRawSelect());
				}
				// normal case
				else {
					getDriver().appendField(fld.getTable(), fld.getField(), res);
				}
			}
		}
		return res.length() > 0 ? res.toString() : null;
	}
	
	@Override
	protected String orderByClause() {
		StringBuffer res = new StringBuffer(30);
		OrderBy ob = aggregateQuery.getOrderBy();
		while (ob != null) {
			if (res.length() > 0)
				res.append(", ");
			appendAggField(res, ob.getAggField());
			if (!ob.isAscending())
				res.append(" DESC");
			ob = ob.getNext();
		}
		return res.length() > 0 ? res.toString() : null;
	}
	
	private void appendAggField(StringBuffer sb, AggregateField fld) {
		// "function(" eg. "AVG(" if it is anything but GROUP
		if ( ! fld.getFunction().equals(AggregateFunction.GROUP) && ! fld.getFunction().equals(AggregateFunction.COUNT_DISTINCT) ) {
			sb.append(" ").append(fld.getFunction().toString()).append("(");   // ## delegate function.toString() to SDriver ?
		}
		if (fld.getFunction().equals(AggregateFunction.COUNT_DISTINCT)) {
			sb.append(" ").append("COUNT ( DISTINCT ");   // ## delegate function.toString() to SDriver ?
		}
		// field
		if (fld.getField() != null) {
			// columnName or table.columnName if several tables
			if (query.getTables().size() == 1 && query.getRawJoin() == null) {
				getDriver().appendColumnName(fld.getField(), sb);
			}
			else {
				getDriver().appendField(fld.getTable(), fld.getField(), sb);
			}
		}
		else if (fld.getFunction().equals(AggregateFunction.COUNT)){ // count(*)
			sb.append("*");
		}
		else { // rawSelect
			sb.append(" ").append(fld.getRawSelect());
		}
		// ")"
		if ( ! fld.getFunction().equals(AggregateFunction.GROUP)) {
			sb.append(")");
		}
	}

	@Override SDriver getDriver() {
		return session.getDriver();
	}

	@Override public String toString() {
		return "[SPreparedStatement " + sqlQuery + "]";
	}
}