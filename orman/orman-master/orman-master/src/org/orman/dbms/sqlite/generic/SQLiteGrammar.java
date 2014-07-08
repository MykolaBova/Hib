package org.orman.dbms.sqlite.generic;

import java.util.EnumMap;
import java.util.Map;

import org.orman.sql.IndexType;
import org.orman.sql.QueryType;
import org.orman.sql.SQLGrammarProvider;
import org.orman.sql.TableConstraintType;
import org.orman.sql.exception.QueryTypeNotImplementedException;

/**
 * MySQL grammar for SQL language. Update accordingly {@link SQLGrammarProvider}
 *
 * @see SQLGrammarProvider
 * 
 * @author Ahmet Alp Balkan <ahmetalpbalkan at gmail.com>
 */
@SuppressWarnings("serial")
public class SQLiteGrammar implements SQLGrammarProvider {
	
	private Map<QueryType, String> queryGrammar = new EnumMap<QueryType, String>(QueryType.class){{
		// put statements into map;
		put(QueryType.USE_DATABASE, "USE DATABASE {DATABASE}"); 
		
		put(QueryType.CREATE_TABLE, "CREATE TABLE {TABLE_LIST} ({COLUMN_OR_CONSTRAINT_DESCRIPTION_LIST})");
		put(QueryType.CREATE_TABLE_IF_NOT_EXSISTS, "CREATE TABLE IF NOT EXISTS {TABLE_LIST} ({COLUMN_OR_CONSTRAINT_DESCRIPTION_LIST})");
		put(QueryType.DROP_TABLE, "DROP TABLE {TABLE_LIST}");
		put(QueryType.DROP_TABLE_IF_EXISTS, "DROP TABLE IF EXISTS {TABLE_LIST}");
		
		put(QueryType.SELECT, "SELECT {SELECT_COLUMN_LIST} FROM {TABLE_LIST} {JOIN}{WHERE}{GROUP_BY}{HAVING}{ORDER_BY}{LIMIT}");
		put(QueryType.SELECT_DISTINCT, "SELECT DISTINCT {SELECT_COLUMN_LIST} FROM {TABLE_LIST} {JOIN}{WHERE}{GROUP_BY}{HAVING}{ORDER_BY}{LIMIT}");
		
		put(QueryType.INSERT, "INSERT INTO {TABLE_LIST} ({COLUMN_LIST}) VALUES ({VALUE_LIST})");
		put(QueryType.UPDATE, "UPDATE {TABLE_LIST} SET {COLUMN_VALUE_LIST} {WHERE}");
		put(QueryType.DELETE, "DELETE FROM {TABLE_LIST} {WHERE}");
		
		put(QueryType.CREATE_INDEX, "CREATE INDEX {INDEX_NAME} ON {TABLE_LIST} ({SELECT_COLUMN_LIST})"); // highly depends on DBMS
		put(QueryType.CREATE_UNIQUE_INDEX, "CREATE UNIQUE INDEX {INDEX_NAME} ON {TABLE_LIST} ({SELECT_COLUMN_LIST})"); // highly depends on DBMS
		put(QueryType.CREATE_INDEX_IF_NOT_EXISTS, "CREATE INDEX IF NOT EXISTS {INDEX_NAME} ON {TABLE_LIST} ({SELECT_COLUMN_LIST})"); // highly depends on DBMS
		put(QueryType.CREATE_UNIQUE_INDEX_IF_NOT_EXISTS, "CREATE UNIQUE INDEX IF NOT EXISTS {INDEX_NAME} ON {TABLE_LIST} ({SELECT_COLUMN_LIST})"); // highly depends on DBMS
		put(QueryType.DROP_INDEX, "DROP INDEX {INDEX_NAME}"); 
		put(QueryType.DROP_INDEX_IF_EXISTS, "DROP INDEX IF EXISTS {INDEX_NAME} "); 

		put(QueryType.BEGIN_TRANSACTION, "BEGIN TRANSACTION"); 
		put(QueryType.COMMIT_TRANSACTION, "COMMIT"); 
		put(QueryType.ROLLBACK_TRANSACTION, "ROLLBACK"); 
	}};
	
	
	private Map<TableConstraintType, String> constraintGrammar = new EnumMap<TableConstraintType, String>(TableConstraintType.class){{
		put(TableConstraintType.FOREIGN_KEY, "FOREIGN KEY (%s) REFERENCES %s (%s)");
		put(TableConstraintType.UNIQUE, "UNIQUE (%s)");
		put(TableConstraintType.PRIMARY_KEY ,"PRIMARY KEY (%s)");
		put(TableConstraintType.AUTO_INCREMENT, "PRIMARY KEY AUTOINCREMENT");
		put(TableConstraintType.USING, "USING (%s)");
		put(TableConstraintType.NOT_NULL, "NOT NULL");
	}};
	
	
	private Map<IndexType, String> indexTypeGrammar = new EnumMap<IndexType, String>(IndexType.class){{
		put(IndexType.BTREE, "BTREE");
		put(IndexType.HASH, "HASH");
	}};
	
	private static final String[] reservedKeywords = {
		"ABORT","ACTION","ADD","AFTER",
		"ALTER","ANALYZE","AND","AS",
		"ATTACH","AUTOINCREMENT","BEFORE","BEGIN",
		"BY","CASCADE","CASE","CAST",
		"COLLATE","COLUMN","COMMIT","CONFLICT",
		"CREATE","CROSS","CURRENT_DATE","CURRENT_TIME",
		"DATABASE","DEFAULT","DEFERRABLE","DEFERRED",
		"DESC","DETACH","DISTINCT","DROP",
		"ELSE","END","ESCAPE","EXCEPT",
		"EXISTS","EXPLAIN","FAIL","FOR",
		"FROM","FULL","GLOB","GROUP",
		"IF","IGNORE","IMMEDIATE","IN",
		"INDEXED","INITIALLY","INNER","INSERT",
		"INTERSECT","INTO","IS","ISNULL",
		"KEY","LEFT","LIKE","LIMIT",
		"NATURAL","NO","NOT","NOTNULL",
		"OF","OFFSET","ON","OR",
		"OUTER","PLAN","PRAGMA","PRIMARY",
		"RAISE","REFERENCES","REGEXP","REINDEX",
		"RENAME","REPLACE","RESTRICT","RIGHT",
		"ROW","SAVEPOINT","SELECT","SET",
		"TEMP","TEMPORARY","THEN","TO",
		"TRIGGER","UNION","UNIQUE","UPDATE",
		"VACUUM","VALUES","VIEW","VIRTUAL", "WHERE"
	};

	
	@Override
	public String getTemplate(QueryType type) {
		String tpl = queryGrammar.get(type);
		if (tpl == null)
			throw new QueryTypeNotImplementedException(type.toString());
		return tpl;
	}


	@Override
	public String getConstraint(TableConstraintType type) {
		String tpl = constraintGrammar.get(type);
		if (tpl == null)
			throw new QueryTypeNotImplementedException(type.toString());
		return tpl;
	}


	@Override
	public String getIndexType(IndexType indexType) {
		String tpl = indexTypeGrammar.get(indexType);
		if (tpl == null)
			throw new QueryTypeNotImplementedException(indexType.toString());
		return tpl;
	}

	@Override
	public String[] getReservedKeywords() {
		return reservedKeywords;
	}
}
