package org.orman.sql;

/**
 * Class used to provide different SQL grammars for query statements. Implement
 * this for each DBMS and then bind to {@link QueryType} at runtime.
 * 
 * @see QueryType
 * @author ahmet alp balkan <ahmetalpbalkan@gmail.com>
 * 
 */
public interface SQLGrammarProvider {
	/**
	 * Returns query template of given type of implementor DBMS including clause
	 * fields binded at runtime (surrounded by {}) without trailing semicolons.
	 * 
	 * @param type
	 * @return
	 */
	public String getTemplate(QueryType type);

	/**
	 * Returns constraint clause template of given table constraint type
	 * including some fields with (%s), that should be formatted at runtime.
	 * 
	 * @param tableConstraintType
	 * @return
	 */
	public String getConstraint(TableConstraintType tableConstraintType);

	/**
	 * Returns keyword of an {@link IndexType} using grammar provider.
	 * 
	 * @param tableConstraintType
	 * @return
	 */
	public String getIndexType(IndexType indexType);
	
	/**
	 * Returns reserved keyword array. All of the words assumed to
	 * be uppercase and trimmed. Word groups are allowed.
	 * 
	 * @return not-null array of reserved keywords in DBMS. 
	 */
	public String[] getReservedKeywords();
}
