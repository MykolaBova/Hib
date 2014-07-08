package simpleorm.quickstart;


/**
 * This is the standard name conversion used by SimpleORMGenerator. 
 * Performs a sort camel case conversion.<p>
 *
 * Examples<br>
 * Table My_NEW_table" converts to "MyNEWTable".<br>
 * Field My_NEW_field" converts to "fldMyNEWField"<br>
 * ForeignKey "My_NEW_F_Key" converts to refMyNewFKey"<br>
 * 
 * @author <a href="mailto:richard.schmidt@inform6.com">Richard Schmidt</a>
 */
public class DefaultFormatter implements INiceNameFormatter {
    /**
     * To create a reasonable class name from the table name,
     * capitalize first character and convert a_b or a-b to aB
     */
    protected String niceName(String _name, boolean capFirst) {
        String name = _name.toLowerCase();

        StringBuffer newName = new StringBuffer(name.length());
        boolean capNextChar = capFirst;
        char nextChar;

        for (int i = 0; i < name.length(); i++) {
            nextChar = name.charAt(i);

            if (i == 0) {
                nextChar = Character.toLowerCase(nextChar);
            }

            if ((nextChar == '-') || (nextChar == '_')) {
                capNextChar = true;

                continue;
            }

            // skip junk characters
            if (!Character.isJavaIdentifierPart(nextChar)) {
                continue;
            }

            if (capNextChar) {
                nextChar = Character.toUpperCase(nextChar);
            }

            newName.append(nextChar);
            capNextChar = false;
        }

        // check to see if a java reserved word. If so, add _ at end
        name = newName.toString() + " ";

        if ("abstract byte byvalue cast catch case const implements extends native return super volatile synchronized this throw throws try import instanceof default switch do while class true false if continue break private public protected return char boolean float int double short long void static for exception else finally final finalize new package ".indexOf(
                    name) >= 0) {
            newName.append('_');
        }

        return newName.toString();
    }

    /**
     * Return the table name camel cased.
     * @see simpleorm.quickstart.INiceName#niceNameForTable(Table)
     */
    public String niceNameForTable(String table) {
        return niceName(table, true);
    }

    /**
     * Return the field name camel case with "fld" as a prefix.
     * @see simpleorm.quickstart.INiceName#niceNameForColumn(Column)
     */
    public String niceNameForColumn(String table, String column) {
        return "fld" + niceName(column, true);
    }
    
    /**
     * Return the foreign key table camel cased with "ref" as a prefix.
     * @see simpleorm.quickstart.INiceName#niceNameForColumn(Column)
     */
    public String niceNameForForeignKey(String localTable, String foreignTable) {
        return "ref" + niceName(foreignTable, true);
    }
}
