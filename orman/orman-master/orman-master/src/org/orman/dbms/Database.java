package org.orman.dbms;

import org.orman.sql.SQLGrammarProvider;

/**
 * Generic database interface that databases
 * should implement.
 *  
 * @author ahmet alp balkan <ahmetalpbalkan@gmail.com>
 */
public interface Database {
	public DataTypeMapper getTypeMapper();
	public SQLGrammarProvider getSQLGrammar();
	public QueryExecutionContainer getExecuter();
	public DatabaseSchemaInspector getSchemaInspector();
	public void closeConnection();
}
