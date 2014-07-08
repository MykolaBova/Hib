package org.orman.sql;

/**
 * Includes templates for SQL statements of various query types.
 * <b>This is compatible with only the SQL standards.</b> To handle different
 * grammars of different database management systems, set a 
 * <code>SQLGrammarProvider</code> to this class at runtime.
 * 
 * <p>Caution: Any query type added/removed from here should be also updated on
 * {@link SQLGrammarProvider} implementations of various DBMSes. 
 * </p>
 * 
 * @author ahmet alp balkan
 * @see SQLGrammarProvider
 */
public enum QueryType {
	USE_DATABASE("USE DATABASE {DATABASE}"), // highly depends on DBMS
	
	CREATE_TABLE("CREATE TABLE {TABLE_LIST} ({COLUMN_OR_CONSTRAINT_DESCRIPTION_LIST})"),
	CREATE_TABLE_IF_NOT_EXSISTS("CREATE TABLE IF NOT EXISTS {TABLE_LIST} ({COLUMN_OR_CONSTRAINT_DESCRIPTION_LIST})"),
	DROP_TABLE("DROP TABLE {TABLE_LIST}"),
	DROP_TABLE_IF_EXISTS("DROP TABLE IF EXISTS {TABLE_LIST}"),
	
	SELECT("SELECT {SELECT_COLUMN_LIST} FROM {TABLE_LIST} {JOIN}{WHERE}{GROUP_BY}{HAVING}{ORDER_BY}{LIMIT}"),
	SELECT_DISTINCT("SELECT DISTINCT {SELECT_COLUMN_LIST} FROM {TABLE_LIST} {JOIN}{WHERE}{GROUP_BY}{HAVING}{ORDER_BY}{LIMIT}"),
	
	INSERT("INSERT INTO {TABLE_LIST} ({COLUMN_LIST}) VALUES ({VALUE_LIST})"),
	UPDATE("UPDATE {TABLE_LIST} SET {COLUMN_VALUE_LIST} {WHERE}"),
	DELETE("DELETE FROM {TABLE_LIST} {WHERE}"),
	
	// Actually INDEX queries are not in SQL standards. (-: 
	CREATE_INDEX("CREATE INDEX {INDEX_NAME} ON {TABLE_LIST} ({SELECT_COLUMN_LIST}) {TABLE_CONSTRAINT}"), // highly depends on DBMS
	CREATE_UNIQUE_INDEX("CREATE UNIQUE INDEX {INDEX_NAME} ON {TABLE_LIST} ({SELECT_COLUMN_LIST}) {TABLE_CONSTRAINT}"), // highly depends on DBMS
	CREATE_INDEX_IF_NOT_EXISTS("CREATE INDEX IF NOT EXISTS {INDEX_NAME} ON {TABLE_LIST} ({SELECT_COLUMN_LIST})  {TABLE_CONSTRAINT}"), // highly depends on DBMS
	CREATE_UNIQUE_INDEX_IF_NOT_EXISTS("CREATE UNIQUE INDEX IF NOT EXISTS {INDEX_NAME} ON {TABLE_LIST} ({SELECT_COLUMN_LIST}) {TABLE_CONSTRAINT}"), // highly depends on DBMS
	DROP_INDEX("DROP INDEX {INDEX_NAME}"), // highly depends on DBMS
	DROP_INDEX_IF_EXISTS("DROP INDEX IF EXISTS {INDEX_NAME} "), // highly depends on DBMS

	BEGIN_TRANSACTION("BEGIN"), // highly depends on DBMS
	COMMIT_TRANSACTION("COMMIT"), // highly depends on DBMS
	ROLLBACK_TRANSACTION("ROLLBACK"); // highly depends on DBMS
	
	private String template;
	private static SQLGrammarProvider provider;

	private QueryType(String tpl) {
		this.template = tpl;
	}

	public String getTemplate() {
		if (provider != null)
			return provider.getTemplate(this);
		else
			return this.template;
	}

	public static void setProvider(SQLGrammarProvider p) {
		provider = p;
	}
}