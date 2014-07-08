package simpleorm.quickstart;


/**
 * Interface to convert the database table or column name to your names.<br>
 * SimpleORMGenerate must be created with an object that instanciates this interface.<p>
 * Typically you should simple algrothem to return a nice name (like DefaultFormatter), 
 * but the code could be complex as you feel like (like SimpleORMNiceNameFormatter).
 * 
 *
 * @author <a href="mailto:richard.schmidt@inform6.com">Richard Schmidt</a>
 */
public interface INiceNameFormatter {
	
	/**return a nice name for the database table name.*/
    String niceNameForTable(String table);

	/**return a nice name for the column of the table passed as parameters.*/
    String niceNameForColumn(String table, String column);

	/**return a nice name for the foreign key that links localTable to ForeignTable.*/
    String niceNameForForeignKey(String localTable, String foreignTable);
}
